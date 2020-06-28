package org.learning.by.example.petstore.command.consumer

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.learning.by.example.petstore.command.test.CustomKafkaContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
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
internal class CommandsConsumerImplTest(
    @Autowired val commandsConsumerImpl: CommandsConsumerImpl,
    @Autowired val commandsConsumerConfig: CommandsConsumerConfig
) {
    companion object {
        private const val BOOTSTRAP_SERVERS_PROPERTY = "${CommandsConsumerConfig.CONFIG_PREFIX}.bootstrap-server"
        private val ONE_COMMAND =
            """
            {
              "id": "4cb5294b-1034-4bc4-9b3d-899adb782d89",
              "timestamp": "2020-06-28T04:53:35.168283Z",
              "commandName": "example command",
              "payload": {
                "attribute1": "value1",
                "attribute2": 123,
                "attribute3": false,
                "attribute4": 125.5
              }
            }
            """.trimIndent().replace("\n", "")

        @Container
        private val KAFKA_CONTAINER = CustomKafkaContainer()

        @JvmStatic
        @DynamicPropertySource
        private fun testProperties(registry: DynamicPropertyRegistry) {
            registry.add(BOOTSTRAP_SERVERS_PROPERTY, KAFKA_CONTAINER::getBootstrapServers)
        }
    }

    @Test
    fun `we should receive commands`() {
        assertThat(KAFKA_CONTAINER.createTopic(commandsConsumerConfig.topic)).isTrue()

        assertThat(KAFKA_CONTAINER.sendMessage(commandsConsumerConfig.topic, ONE_COMMAND)).isTrue()

        StepVerifier.create(commandsConsumerImpl.receiveCommands())
            .expectSubscription()
            .thenRequest(Long.MAX_VALUE)
            .consumeNextWith {
                assertThat(it.commandName).isEqualTo("example command")
                assertThat(it.id).isEqualTo(UUID.fromString("4cb5294b-1034-4bc4-9b3d-899adb782d89"))
                assertThat(it.timestamp).isEqualTo(Instant.parse("2020-06-28T04:53:35.168283Z"))
                assertThat(it.get<String>("attribute1")).isEqualTo("value1")
                assertThat(it.get<Int>("attribute2")).isEqualTo(123)
                assertThat(it.get<Boolean>("attribute3")).isEqualTo(false)
                assertThat(it.get<Double>("attribute4")).isEqualTo(125.5)
            }
            .expectNextCount(0L)
            .thenCancel()
            .verify(Duration.ofSeconds(5L))
    }
}
