package org.learning.by.example.petstore.command.consumer

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.learning.by.example.petstore.command.consumer.CommandsConsumerConfig.Constants.CONSUMER_VALIDATE_PROPERTY
import org.learning.by.example.petstore.command.test.CustomKafkaContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.test.StepVerifier
import java.time.Duration
import java.time.Instant
import java.util.UUID

@SpringBootTest
@Testcontainers
@ActiveProfiles("consumer")
internal class CommandsConsumerImplTest(
    @Autowired val commandsConsumerImpl: CommandsConsumerImpl,
    @Autowired val commandsConsumerConfig: CommandsConsumerConfig
) {
    companion object {
        private val FIRST_COMMAND =
            """
            {
              "id": "4cb5294b-1034-4bc4-9b3d-899adb782d89",
              "timestamp": "2020-06-28T08:53:35.168283Z",
              "commandName": "example command 1",
              "payload": {
                "attribute1": "value1",
                "attribute2": 123
              }
            }
            """.trimIndent().replace("\n", "")

        private val SECOND_COMMAND =
            """
            {
              "id": "4cb5294b-1034-4bc4-9b3d-542adb232a21",
              "timestamp": "2020-06-28T11:22:13.456732Z",
              "commandName": "example command 2",
              "payload": {
                "attribute1": false,
                "attribute2": 125.5
              }
            }
            """.trimIndent().replace("\n", "")

        private const val INVALID_COMMAND = "invalid"
        private const val EMPTY_COMMAND = ""

        @Container
        private val KAFKA_CONTAINER = CustomKafkaContainer()

        @JvmStatic
        @DynamicPropertySource
        private fun testProperties(registry: DynamicPropertyRegistry) {
            registry.add(CONSUMER_VALIDATE_PROPERTY, KAFKA_CONTAINER::getBootstrapServers)
        }
    }

    @BeforeEach
    fun setup() {
        assertThat(KAFKA_CONTAINER.createTopic(commandsConsumerConfig.topic)).isTrue()
    }

    @Test
    fun `we should receive commands`() {
        assertThat(KAFKA_CONTAINER.sendMessage(commandsConsumerConfig.topic, FIRST_COMMAND)).isTrue()
        assertThat(KAFKA_CONTAINER.sendMessage(commandsConsumerConfig.topic, SECOND_COMMAND)).isTrue()

        StepVerifier.create(commandsConsumerImpl.receiveCommands())
            .expectSubscription()
            .thenRequest(Long.MAX_VALUE)
            .consumeNextWith {
                assertThat(it.commandName).isEqualTo("example command 1")
                assertThat(it.id).isEqualTo(UUID.fromString("4cb5294b-1034-4bc4-9b3d-899adb782d89"))
                assertThat(it.timestamp).isEqualTo(Instant.parse("2020-06-28T08:53:35.168283Z"))
                assertThat(it.get<String>("attribute1")).isEqualTo("value1")
                assertThat(it.get<Int>("attribute2")).isEqualTo(123)
            }
            .consumeNextWith {
                assertThat(it.commandName).isEqualTo("example command 2")
                assertThat(it.id).isEqualTo(UUID.fromString("4cb5294b-1034-4bc4-9b3d-542adb232a21"))
                assertThat(it.timestamp).isEqualTo(Instant.parse("2020-06-28T11:22:13.456732Z"))
                assertThat(it.get<Boolean>("attribute1")).isEqualTo(false)
                assertThat(it.get<Double>("attribute2")).isEqualTo(125.5)
            }
            .expectNextCount(0L)
            .thenCancel()
            .verify(Duration.ofSeconds(5L))
    }

    @Test
    fun `we should error on invalid command`() {
        assertThat(KAFKA_CONTAINER.sendMessage(commandsConsumerConfig.topic, INVALID_COMMAND)).isTrue()

        StepVerifier.create(commandsConsumerImpl.receiveCommands())
            .expectSubscription()
            .thenRequest(Long.MAX_VALUE)
            .expectErrorMatches {
                it is ErrorDeserializingObject
            }
            .verify(Duration.ofSeconds(5L))
    }

    @Test
    fun `we should error on empty command`() {
        assertThat(KAFKA_CONTAINER.sendMessage(commandsConsumerConfig.topic, EMPTY_COMMAND)).isTrue()

        StepVerifier.create(commandsConsumerImpl.receiveCommands())
            .expectSubscription()
            .thenRequest(Long.MAX_VALUE)
            .expectErrorMatches {
                it is ErrorDeserializingObject
            }
            .verify(Duration.ofSeconds(5L))
    }
}
