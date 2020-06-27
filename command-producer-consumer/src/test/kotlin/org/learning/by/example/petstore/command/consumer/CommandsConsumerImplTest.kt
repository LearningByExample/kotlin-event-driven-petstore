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

    @Test
    fun `we should receive commands`() {
        assertThat(KAFKA_CONTAINER.createTopic(commandsConsumerConfig.topic)).isTrue()
        assertThat(KAFKA_CONTAINER.sendMessage(commandsConsumerConfig.topic, "one")).isTrue()
        assertThat(KAFKA_CONTAINER.sendMessage(commandsConsumerConfig.topic, "two")).isTrue()

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
