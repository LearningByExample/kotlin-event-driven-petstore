package org.learning.by.example.petstore.command.consumer

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.test.StepVerifier
import java.time.Duration

@SpringBootTest
@Testcontainers
internal class CommandsConsumerImplTest(@Autowired val commandsConsumerImpl: CommandsConsumerImpl) {
    companion object {
        private const val SCRIPT_PATH = "scripts"
        private const val SCRIPT_MESSAGES_PATH = "messages"
        private const val SCRIPT_SEND_MESSAGE = "send_messages.sh"
        private const val CONTAINER_PATH = "/usr/helpers"
        private const val CONTAINER_MESSAGE_COMMAND = "$CONTAINER_PATH/$SCRIPT_SEND_MESSAGE"
        private const val CONTAINER_MESSAGES_PATH = "$CONTAINER_PATH/$SCRIPT_MESSAGES_PATH"

        private const val TWO_MESSAGES_FILE = "two_messages.txt"
        private const val CONTAINER_MESSAGE_TWO_MESSAGE = "$CONTAINER_MESSAGES_PATH/$TWO_MESSAGES_FILE"

        private val CHMOD_SCRIPT_CMD = arrayOf("chmod", "+xX", CONTAINER_MESSAGE_COMMAND)
        private val CREATE_TWO_MESSAGES_CMD = arrayOf(CONTAINER_MESSAGE_COMMAND, CONTAINER_MESSAGE_TWO_MESSAGE)

        @Container
        private val KAFKA_CONTAINER = KafkaContainer()
            .withClasspathResourceMapping(SCRIPT_PATH, CONTAINER_PATH, BindMode.READ_ONLY)

        @JvmStatic
        @BeforeAll
        fun setup() {
            assertThat(KAFKA_CONTAINER.execInContainer(*CHMOD_SCRIPT_CMD).exitCode).isEqualTo(0)
        }

        @JvmStatic
        @DynamicPropertySource
        private fun testProperties(registry: DynamicPropertyRegistry) {
            registry.add("service.commands.consumer.bootstrap-server", KAFKA_CONTAINER::getBootstrapServers)
        }
    }

    @Test
    fun `we should receive commands`() {
        assertThat(KAFKA_CONTAINER.execInContainer(*CREATE_TWO_MESSAGES_CMD).exitCode).isEqualTo(0)

        StepVerifier.create(commandsConsumerImpl.receiveCommands())
            .expectSubscription()
            .thenRequest(Long.MAX_VALUE)
            .expectNext("one")
            .expectNext("two")
            .expectNextCount(0L)
            .thenCancel()
            .verify(Duration.ofSeconds(5L))
    }
}
