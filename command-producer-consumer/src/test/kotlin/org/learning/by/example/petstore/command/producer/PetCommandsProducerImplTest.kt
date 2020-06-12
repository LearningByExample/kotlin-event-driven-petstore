package org.learning.by.example.petstore.command.producer

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
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

@SpringBootTest
@Testcontainers
internal class PetCommandsProducerImplTest(@Autowired val petCommandsImpl: PetCommandsProducerImpl,
                                           @Autowired val petCommandsProducerConfig: PetCommandsProducerConfig) {
    companion object {
        private const val CLIENT_ID = "pet_commands_consumer"
        private const val GROUP_ID = "pet_commands_consumers"
        private const val OFFSET_EARLIEST = "earliest"
        private const val VALID_UUID = "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"
        private const val BOOTSTRAP_SERVERS_PROPERTY = "${PetCommandsProducerConfig.CONFIG_PREFIX}.bootstrap-server"

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
        var id = ""
        StepVerifier
            .create(petCommandsImpl.sendCommand("fluffy"))
            .expectSubscription()
            .thenRequest(Long.MAX_VALUE)
            .consumeNextWith {
                id = it
                Assertions.assertThat(id).matches(VALID_UUID)
            }
            .verifyComplete()

        StepVerifier.create(getStrings())
            .expectSubscription()
            .thenRequest(Long.MAX_VALUE)
            .expectNext(id)
            .expectNextCount(0L)
            .thenCancel()
            .verify()
    }

    private fun getStrings() = getKafkaReceiver().receive().flatMap {
        val receiverOffset = it.receiverOffset()
        receiverOffset.acknowledge()
        it.value().toMono()
    }

    private fun getKafkaReceiver() = KafkaReceiver.create(ReceiverOptions.create<String, String>(hashMapOf<String, Any>(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to petCommandsProducerConfig.bootstrapServer,
        ConsumerConfig.CLIENT_ID_CONFIG to CLIENT_ID,
        ConsumerConfig.GROUP_ID_CONFIG to GROUP_ID,
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to OFFSET_EARLIEST
    )).subscription(setOf(petCommandsProducerConfig.topic)))
}
