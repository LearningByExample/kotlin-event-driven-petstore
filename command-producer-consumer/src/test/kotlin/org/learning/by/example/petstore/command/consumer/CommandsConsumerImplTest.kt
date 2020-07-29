package org.learning.by.example.petstore.command.consumer

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.learning.by.example.petstore.command.Command
import org.learning.by.example.petstore.command.consumer.CommandsConsumerConfig.Constants.CONSUMER_CONFIG_BOOSTRAP_SERVER
import org.learning.by.example.petstore.command.test.CustomKafkaContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit

@SpringBootTest
@Testcontainers
@ActiveProfiles("consumer")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@DirtiesContext
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
            registry.add(CONSUMER_CONFIG_BOOSTRAP_SERVER, KAFKA_CONTAINER::getBootstrapServers)
        }
    }

    @BeforeEach
    fun setup() {
        if (KAFKA_CONTAINER.isRunning) assertThat(KAFKA_CONTAINER.createTopic(commandsConsumerConfig.topic)).isTrue()
    }

    @Test
    @Order(1)
    fun `we should receive commands`() {
        assertThat(KAFKA_CONTAINER.sendMessage(commandsConsumerConfig.topic, FIRST_COMMAND)).isTrue()
        assertThat(KAFKA_CONTAINER.sendMessage(commandsConsumerConfig.topic, SECOND_COMMAND)).isTrue()

        val commands = arrayListOf<Command>()

        val disposable = commandsConsumerImpl.receiveCommands {
            commands.add(it)
            Mono.empty()
        }.subscribe()

        await().atMost(30, TimeUnit.SECONDS).until {
            commands.size == 2
        }

        disposable.dispose()

        assertThat(commands).hasSize(2)

        with(commands[0]) {
            assertThat(commandName).isEqualTo("example command 1")
            assertThat(id).isEqualTo(UUID.fromString("4cb5294b-1034-4bc4-9b3d-899adb782d89"))
            assertThat(timestamp).isEqualTo(Instant.parse("2020-06-28T08:53:35.168283Z"))
            assertThat(get<String>("attribute1")).isEqualTo("value1")
            assertThat(get<Int>("attribute2")).isEqualTo(123)
        }

        with(commands[1]) {
            assertThat(commandName).isEqualTo("example command 2")
            assertThat(id).isEqualTo(UUID.fromString("4cb5294b-1034-4bc4-9b3d-542adb232a21"))
            assertThat(timestamp).isEqualTo(Instant.parse("2020-06-28T11:22:13.456732Z"))
            assertThat(get<Boolean>("attribute1")).isEqualTo(false)
            assertThat(get<Double>("attribute2")).isEqualTo(125.5)
        }
    }

    @Test
    @Order(1)
    fun `we should error on invalid command`() {
        assertThat(KAFKA_CONTAINER.sendMessage(commandsConsumerConfig.topic, INVALID_COMMAND)).isTrue()

        StepVerifier.create(commandsConsumerImpl.receiveCommands { Mono.empty() })
            .expectSubscription()
            .thenRequest(Long.MAX_VALUE)
            .expectErrorMatches {
                it is ErrorDeserializingObject
            }
            .verify(Duration.ofSeconds(5L))
    }

    @Test
    @Order(1)
    fun `we should error on empty command`() {
        assertThat(KAFKA_CONTAINER.sendMessage(commandsConsumerConfig.topic, EMPTY_COMMAND)).isTrue()

        StepVerifier.create(commandsConsumerImpl.receiveCommands { Mono.empty() })
            .expectSubscription()
            .thenRequest(Long.MAX_VALUE)
            .expectErrorMatches {
                it is ErrorDeserializingObject
            }
            .verify(Duration.ofSeconds(5L))
    }

    @Order(1)
    @Test
    fun `we can get check if we can connect to Kafka`() {
        StepVerifier.create(commandsConsumerImpl.isKafkaAvailable)
            .expectSubscription()
            .expectNext(true)
            .verifyComplete()
    }

    @Order(2)
    @Test
    fun `we can get check if not alive`() {
        if (KAFKA_CONTAINER.isRunning) KAFKA_CONTAINER.stop()

        StepVerifier.create(commandsConsumerImpl.isKafkaAvailable)
            .expectSubscription()
            .expectErrorMatches {
                it is ConnectingToKafkaException
            }
            .verify()
    }

    @Order(2)
    @Test
    fun `we should error on failure to connect`() {
        if (KAFKA_CONTAINER.isRunning) KAFKA_CONTAINER.stop()

        StepVerifier.create(commandsConsumerImpl.receiveCommands { Mono.empty() })
            .expectSubscription()
            .thenRequest(Long.MAX_VALUE)
            .expectErrorMatches {
                it is ConnectingToKafkaException
            }
            .verify(Duration.ofSeconds(5))
    }
}
