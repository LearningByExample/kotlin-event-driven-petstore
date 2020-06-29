package org.learning.by.example.petstore.command.test

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
internal class CustomKafkaContainerTest {
    companion object {
        const val TEST_TOPIC = "test-topic"

        @Container
        private val KAFKA_CONTAINER = CustomKafkaContainer()
    }

    @Test
    fun `we could send and read messages from a topic`() {
        assertThat(KAFKA_CONTAINER.isRunning).isTrue()

        assertThat(KAFKA_CONTAINER.createTopic(TEST_TOPIC)).isTrue()

        assertThat(KAFKA_CONTAINER.sendMessage(TEST_TOPIC, "one")).isTrue()
        assertThat(KAFKA_CONTAINER.sendMessage(TEST_TOPIC, "two")).isTrue()

        assertThat(KAFKA_CONTAINER.getMessage(TEST_TOPIC)).isEqualTo("one")
        assertThat(KAFKA_CONTAINER.getMessage(TEST_TOPIC)).isEqualTo("two")
    }

    @Test
    fun `we could create a topic n times`() {
        repeat(3) {
            val message = it.toString()
            assertThat(KAFKA_CONTAINER.createTopic(TEST_TOPIC)).isTrue()
            assertThat(KAFKA_CONTAINER.sendMessage(TEST_TOPIC, message)).isTrue()
            assertThat(KAFKA_CONTAINER.getMessage(TEST_TOPIC)).isEqualTo(message)
        }
    }
}
