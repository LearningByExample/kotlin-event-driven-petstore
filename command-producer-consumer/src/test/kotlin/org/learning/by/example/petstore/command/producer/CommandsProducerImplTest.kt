package org.learning.by.example.petstore.command.producer

import com.jayway.jsonpath.JsonPath
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.learning.by.example.petstore.command.Command
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.kotlin.core.publisher.toMono
import reactor.test.StepVerifier
import java.time.Instant

@SpringBootTest
@Testcontainers
internal class CommandsProducerImplTest(
    @Autowired val commandsProducerImpl: CommandsProducerImpl,
    @Autowired val commandsProducerConfig: CommandsProducerConfig
) {
    companion object {
        private const val CLIENT_ID = "pet_commands_consumer"
        private const val GROUP_ID = "pet_commands_consumers"
        private const val OFFSET_EARLIEST = "earliest"
        private const val BOOTSTRAP_SERVERS_PROPERTY = "${CommandsProducerConfig.CONFIG_PREFIX}.bootstrap-server"

        @Container
        private val KAFKA_CONTAINER = KafkaContainer()

        @JvmStatic
        @DynamicPropertySource
        private fun testProperties(registry: DynamicPropertyRegistry) {
            registry.add(BOOTSTRAP_SERVERS_PROPERTY, KAFKA_CONTAINER::getBootstrapServers)
        }
    }

    @Test
    fun `we should send commands`() {
        val commandToSend = Command("example command") {
            +("attribute1" to "value1")
            +("attribute2" to 123)
            +("attribute3" to false)
            +("attribute4" to 125.5)
        }

        StepVerifier.create(commandsProducerImpl.sendCommand(commandToSend))
            .expectSubscription()
            .thenRequest(Long.MAX_VALUE)
            .consumeNextWith {
                Assertions.assertThat(it).isEqualTo(commandToSend.id)
            }
            .verifyComplete()

        StepVerifier.create(getStrings())
            .expectSubscription()
            .thenRequest(Long.MAX_VALUE)
            .consumeNextWith {
                assertThat(JsonPath.read<String>(it, "\$.id")).isEqualTo(commandToSend.id.toString())
                assertThat(Instant.parse(JsonPath.read(it, "\$.timestamp")))
                    .isEqualTo(commandToSend.timestamp)
                assertThat(JsonPath.read<String>(it, "\$.commandName")).isEqualTo("example command")
                assertThat(JsonPath.read<String>(it, "\$.payload.attribute1")).isEqualTo("value1")
                assertThat(JsonPath.read<Int>(it, "\$.payload.attribute2")).isEqualTo(123)
                assertThat(JsonPath.read<Boolean>(it, "\$.payload.attribute3")).isEqualTo(false)
                assertThat(JsonPath.read<Double>(it, "\$.payload.attribute4")).isEqualTo(125.5)
            }
            .expectNextCount(0L)
            .thenCancel()
            .verify()
    }

    private fun getStrings() = getKafkaReceiver().receive().flatMap {
        val receiverOffset = it.receiverOffset()
        receiverOffset.acknowledge()
        it.value().toMono()
    }

    private fun getKafkaReceiver() = KafkaReceiver.create(
        ReceiverOptions.create<String, String>(
            hashMapOf<String, Any>(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to commandsProducerConfig.bootstrapServer,
                ConsumerConfig.CLIENT_ID_CONFIG to CLIENT_ID,
                ConsumerConfig.GROUP_ID_CONFIG to GROUP_ID,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to OFFSET_EARLIEST
            )
        ).subscription(setOf(commandsProducerConfig.topic))
    )
}
