package org.learning.by.example.petstore.command.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.learning.by.example.petstore.command.dsl.command
import org.learning.by.example.petstore.command.test.CustomKafkaContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.test.StepVerifier
import java.time.Duration

@SpringBootTest
@Testcontainers
internal class CommandsConsumerImplTest(
    @Autowired val commandsConsumerImpl: CommandsConsumerImpl,
    @Autowired val commandsConsumerConfig: CommandsConsumerConfig
) {
    companion object {
        private const val BOOTSTRAP_SERVERS_PROPERTY = "${CommandsConsumerConfig.CONFIG_PREFIX}.bootstrap-server"

        @Container
        private val KAFKA_CONTAINER = CustomKafkaContainer()

        @JvmStatic
        @DynamicPropertySource
        private fun testProperties(registry: DynamicPropertyRegistry) {
            registry.add(BOOTSTRAP_SERVERS_PROPERTY, KAFKA_CONTAINER::getBootstrapServers)
        }
    }

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `we should receive commands`() {
        assertThat(KAFKA_CONTAINER.createTopic(commandsConsumerConfig.topic)).isTrue()

        val commandOne = command("example command 1") {
            "attribute1" value "value1"
            "attribute2" value 123
            "attribute3" value false
            "attribute4" value 125.5
        }
        val commandTwo = command("example command 2") {
            "attribute1" value "value1"
            "attribute2" value 123
            "attribute3" value false
            "attribute4" value 125.5
        }

        assertThat(
            KAFKA_CONTAINER.sendMessage(
                commandsConsumerConfig.topic,
                objectMapper.writeValueAsString(commandOne)
            )
        ).isTrue()
        assertThat(
            KAFKA_CONTAINER.sendMessage(
                commandsConsumerConfig.topic,
                objectMapper.writeValueAsString(commandTwo)
            )
        ).isTrue()

        StepVerifier.create(commandsConsumerImpl.receiveCommands())
            .expectSubscription()
            .thenRequest(Long.MAX_VALUE)
            .expectNext(commandOne)
            .expectNext(commandTwo)
            .expectNextCount(0L)
            .thenCancel()
            .verify(Duration.ofSeconds(5L))
    }
}
