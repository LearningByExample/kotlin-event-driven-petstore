package org.learning.by.example.petstore.petqueries.testing

import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@DirtiesContext
abstract class DatabaseTest {
    companion object {
        @Container
        val container: PostgreSQLContainer<Nothing> = PostgreSQLContainer<Nothing>().apply {
            withDatabaseName("pets")
            withUsername("petuser")
            withPassword("petpwd")
            withReuse(false)
        }

        @DynamicPropertySource
        @JvmStatic
        fun setupDBProperties(registry: DynamicPropertyRegistry) = with(container) {
            registry.add("spring.r2dbc.url") {
                "r2dbc:postgresql://$host:$firstMappedPort/$databaseName"
            }
            registry.add("spring.r2dbc.username", ::getUsername)
            registry.add("spring.r2dbc.password", ::getPassword)
            registry.add("db.initialize") { "true" }
        }
    }
}
