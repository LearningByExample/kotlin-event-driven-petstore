package org.learning.by.example.petstore.petstream.configuration

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.learning.by.example.petstore.petstream.listener.StreamListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.test.StepVerifier

@SpringBootTest
@Testcontainers
internal class DBInitializerTest(@Autowired val databaseClient: DatabaseClient) {
    companion object {
        @Container
        val container: PostgreSQLContainer<Nothing> = PostgreSQLContainer<Nothing>().apply {
            withDatabaseName("pets")
            withUsername("petuser")
            withPassword("petpwd")
        }

        @DynamicPropertySource
        @JvmStatic
        fun setupProperties(registry: DynamicPropertyRegistry) = with(container) {
            registry.add("spring.r2dbc.url") {
                "r2dbc:postgresql://$host:$firstMappedPort/$databaseName"
            }
            registry.add("spring.r2dbc.username", ::getUsername)
            registry.add("spring.r2dbc.password", ::getPassword)
            registry.add("db.initialize") { "true" }
        }
    }

    @MockBean
    lateinit var streamListener: StreamListener

    @BeforeEach
    fun setup() {
        doNothing().whenever(streamListener).onApplicationEvent(any())
    }

    fun checkIfTableExist(name: String): Mono<Boolean> = databaseClient.execute(
        """
           SELECT
             table_name
           FROM
             information_schema.tables
           WHERE
             table_name = '$name'
             AND table_schema = 'public'
        """.trimIndent()
    ).fetch().first().map { it.getOrDefault("table_name", "") == name }.switchIfEmpty(false.toMono())

    @Test
    fun `we should have the tables created`() {
        StepVerifier
            .create(checkIfTableExist("pets"))
            .expectSubscription()
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `we should not have a no existing tables created`() {
        StepVerifier
            .create(checkIfTableExist("no_pets"))
            .expectSubscription()
            .expectNext(false)
            .verifyComplete()
    }
}
