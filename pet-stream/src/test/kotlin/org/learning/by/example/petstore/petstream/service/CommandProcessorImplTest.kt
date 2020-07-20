@file:Suppress("DEPRECATION")

package org.learning.by.example.petstore.petstream.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.learning.by.example.petstore.command.Command
import org.learning.by.example.petstore.command.dsl.command
import org.learning.by.example.petstore.petstream.listener.StreamListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.isEquals
import org.springframework.data.r2dbc.query.Criteria.where
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.test.StepVerifier
import java.time.LocalDateTime

@SpringBootTest
@Testcontainers
internal class CommandProcessorImplTest(
    @Autowired val commandProcessorImpl: CommandProcessorImpl,
    @Autowired val databaseClient: DatabaseClient
) {
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
    fun setUp() {
        doNothing().whenever(streamListener).onApplicationEvent(any())
    }

    @Test
    fun `should process create command and save a pet in the database`() {
        val cmd = command("pet_create") {
            "name" value "name"
            "category" value "category"
            "breed" value "breed"
            "vaccines" values listOf("vaccine1", "vaccine2")
            "dob" value LocalDateTime.now()
            "tags" values listOf("tag1")
        }

        StepVerifier.create(commandProcessorImpl.process(cmd))
            .expectSubscription()
            .verifyComplete()

        verifyPetIsSaved(cmd)
    }

    fun verifyPetIsSaved(cmd: Command) {
        StepVerifier.create(
            databaseClient
                .select().from("pets")
                .project("id", "name", "dob", "category")
                .matching(where("id").isEquals(cmd.id.toString()))
                .fetch().one()
        ).expectSubscription().consumeNextWith {
            assertThat(it["id"]).isEqualTo(cmd.id.toString())
            assertThat(it["name"]).isEqualTo(cmd.get("name"))
            assertThat(it["dob"]).isEqualTo(cmd.get<LocalDateTime>("dob"))
            assertThat(it["category"] as Int).isNotZero()
            verifyCategoryIsCorrect(it["category"] as Int, cmd.get("category"))
        }.verifyComplete()
    }

    fun verifyCategoryIsCorrect(id: Int, category: String) {
        StepVerifier.create(
            databaseClient.select().from("categories")
                .project("name")
                .matching(where("id").isEquals(id))
                .fetch().one()
        ).expectSubscription().consumeNextWith {
            assertThat(it["name"]).isEqualTo(category)
        }.verifyComplete()
    }

    @Test
    fun `we should create categories`() {
        var firstCategory = -1

        StepVerifier.create(commandProcessorImpl.insertCategory("one"))
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).isNotZero()
                firstCategory = it
                verifyCategoryIsCorrect(it, "one")
            }
            .verifyComplete()

        StepVerifier.create(commandProcessorImpl.insertCategory("two"))
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).isNotZero()
                assertThat(it).isNotEqualTo(firstCategory)
                verifyCategoryIsCorrect(it, "two")
            }
            .verifyComplete()

        StepVerifier.create(commandProcessorImpl.insertCategory("one"))
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).isNotZero()
                assertThat(it).isEqualTo(firstCategory)
                verifyCategoryIsCorrect(it, "one")
            }
            .verifyComplete()
    }
}
