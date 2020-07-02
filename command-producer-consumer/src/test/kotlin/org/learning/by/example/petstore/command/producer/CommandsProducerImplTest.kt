package org.learning.by.example.petstore.command.producer

import com.jayway.jsonpath.JsonPath
import org.apache.kafka.common.errors.TimeoutException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.learning.by.example.petstore.command.dsl.command
import org.learning.by.example.petstore.command.producer.CommandsProducerConfig.Constants.PRODUCER_CONFIG_BOOSTRAP_SERVER
import org.learning.by.example.petstore.command.test.CustomKafkaContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.test.StepVerifier
import java.time.Instant

@SpringBootTest
@Testcontainers
@ActiveProfiles("producer")
@TestMethodOrder(OrderAnnotation::class)
internal class CommandsProducerImplTest(
    @Autowired val commandsProducerImpl: CommandsProducerImpl,
    @Autowired val commandsProducerConfig: CommandsProducerConfig
) {
    companion object {
        @Container
        private val KAFKA_CONTAINER = CustomKafkaContainer()

        @JvmStatic
        @DynamicPropertySource
        private fun testProperties(registry: DynamicPropertyRegistry) {
            registry.add(PRODUCER_CONFIG_BOOSTRAP_SERVER, KAFKA_CONTAINER::getBootstrapServers)
        }
    }

    @Test
    @Order(1)
    fun `we should send commands`() {
        assertThat(KAFKA_CONTAINER.createTopic(commandsProducerConfig.topic)).isTrue()
        val commandToSend = command("example command") {
            "attribute1" value "value1"
            "attribute2" value 123
            "attribute3" value false
            "attribute4" value 125.5
        }

        StepVerifier.create(commandsProducerImpl.sendCommand(commandToSend))
            .expectSubscription()
            .thenRequest(Long.MAX_VALUE)
            .consumeNextWith {
                assertThat(it).isEqualTo(commandToSend.id)
            }
            .verifyComplete()

        val message = KAFKA_CONTAINER.getMessage(commandsProducerConfig.topic)

        assertThat(JsonPath.read<String>(message, "\$.id")).isEqualTo(commandToSend.id.toString())
        assertThat(Instant.parse(JsonPath.read(message, "\$.timestamp")))
            .isEqualTo(commandToSend.timestamp)
        assertThat(JsonPath.read<String>(message, "\$.commandName")).isEqualTo("example command")
        assertThat(JsonPath.read<String>(message, "\$.payload.attribute1"))
            .isEqualTo(commandToSend.get("attribute1"))
        assertThat(JsonPath.read<Int>(message, "\$.payload.attribute2"))
            .isEqualTo(commandToSend.get("attribute2"))
        assertThat(JsonPath.read<Boolean>(message, "\$.payload.attribute3"))
            .isEqualTo(commandToSend.get("attribute3"))
        assertThat(JsonPath.read<Double>(message, "\$.payload.attribute4"))
            .isEqualTo(commandToSend.get("attribute4"))
    }

    @Test
    @Order(2)
    fun `we should handle timeouts`() {
        KAFKA_CONTAINER.stop()

        StepVerifier.create(commandsProducerImpl.sendCommand(command("example command") {}))
            .expectSubscription()
            .thenRequest(Long.MAX_VALUE)
            .expectErrorMatches {
                it is SendCommandException && it.cause is TimeoutException
            }
            .verify()
    }
}
