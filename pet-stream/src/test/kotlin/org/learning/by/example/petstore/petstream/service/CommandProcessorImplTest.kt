package org.learning.by.example.petstore.petstream.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.learning.by.example.petstore.command.dsl.command
import org.learning.by.example.petstore.petstream.listener.StreamListener
import org.learning.by.example.petstore.petstream.respository.PetRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.test.StepVerifier
import java.time.Instant

@SpringBootTest
@Testcontainers
internal class CommandProcessorImplTest(
    @Autowired val commandProcessorImpl: CommandProcessorImpl,
    @Autowired val petRepository: PetRepository
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
    fun `should process command and save a pet in the database`() {
        val petCommand = command("pet_create") {
            "name" value "name"
            "category" value "category"
            "breed" value "breed"
            "vaccines" values listOf("vaccine1", "vaccine2")
            "dob" value Instant.now()
            "tags" values listOf("tag1")
        }
        val mono: Mono<Void> = commandProcessorImpl.process(petCommand.toMono())

        StepVerifier.create(mono)
            .expectSubscription()
            .verifyComplete()

        StepVerifier.create(petRepository.findById(petCommand.id.toString()))
            .expectSubscription()
            .consumeNextWith {
                assertThat(it._id).isEqualTo(petCommand.id.toString())
                assertThat(it.name).isEqualTo(petCommand.get("name"))
                assertThat(it.dob).isEqualTo(petCommand.get<Instant>("dob"))
            }
            .verifyComplete()
    }
}
