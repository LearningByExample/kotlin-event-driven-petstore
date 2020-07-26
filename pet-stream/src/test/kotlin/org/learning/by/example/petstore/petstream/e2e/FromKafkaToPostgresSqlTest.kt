package org.learning.by.example.petstore.petstream.e2e

import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.learning.by.example.petstore.command.consumer.CommandsConsumerConfig
import org.learning.by.example.petstore.command.dsl.command
import org.learning.by.example.petstore.command.producer.CommandsProducer
import org.learning.by.example.petstore.command.producer.CommandsProducerConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.kotlin.core.publisher.toMono
import java.time.Instant
import java.util.concurrent.TimeUnit

@SpringBootTest
@Testcontainers
class FromKafkaToPostgresSqlTest(
    @Autowired val producer: CommandsProducer,
    @Autowired val databaseClient: DatabaseClient
) {
    companion object {
        @Container
        private val KAFKA_CONTAINER = KafkaContainer().apply {
            withReuse(false)
        }

        @Container
        val postgresSQLContainer: PostgreSQLContainer<Nothing> = PostgreSQLContainer<Nothing>().apply {
            withDatabaseName("pets")
            withUsername("petuser")
            withPassword("petpwd")
            withReuse(false)
        }

        @JvmStatic
        @DynamicPropertySource
        private fun testProperties(registry: DynamicPropertyRegistry) {
            registry.add(CommandsConsumerConfig.CONSUMER_CONFIG_BOOSTRAP_SERVER, KAFKA_CONTAINER::getBootstrapServers)
            registry.add(CommandsProducerConfig.PRODUCER_CONFIG_BOOSTRAP_SERVER, KAFKA_CONTAINER::getBootstrapServers)

            with(postgresSQLContainer) {
                registry.add("spring.r2dbc.url") {
                    "r2dbc:postgresql://$host:$firstMappedPort/$databaseName"
                }
                registry.add("spring.r2dbc.username", ::getUsername)
                registry.add("spring.r2dbc.password", ::getPassword)
                registry.add("db.initialize") { "true" }
            }
        }
    }

    fun numberOfPets() = databaseClient.execute("SELECT count(id) as total FROM pets").fetch().one().map {
        it.getValue("total")
    }.onErrorReturn(0.toMono()).block()

    @Test
    fun `we should save 10 pets when sending 10 pet create commands`() {
        for (i in 1..10) {
            val cmd = command("pet_create") {
                "name" value "name $i"
                "category" value "category $i"
                "breed" value "breed $i"
                "vaccines" values listOf("vaccine $i 1", "vaccine $i 2")
                "dob" value Instant.now()
                "tags" values listOf("tag$i 1 ", "tag$i 2", "tag$i 3")
            }
            producer.sendCommand(cmd).block()
        }

        await().atMost(30, TimeUnit.SECONDS).until {
            numberOfPets() == 10L
        }
    }
}
