package org.learning.by.example.petstore.petstream.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.learning.by.example.petstore.command.dsl.command
import org.learning.by.example.petstore.petstream.listener.StreamListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.test.StepVerifier
import java.time.Instant

@SpringBootTest
internal class CommandProcessorImplTest(@Autowired val commandProcessorImpl: CommandProcessorImpl) {
    @MockBean
    lateinit var streamListener: StreamListener

    @BeforeEach
    fun setUp() {
        doNothing().whenever(streamListener).onApplicationEvent(any())
    }

    @Test
    fun `should process command and save a pet in the database`() {
        val monoCommand = command("pet_create") {
            "name" value "name"
            "category" value "category"
            "breed" value "breed"
            "vaccines" values listOf("vaccine1", "vaccine2")
            "dob" value Instant.now()
            "tags" values listOf("tag1")
        }.toMono()
        val mono: Mono<Void> = commandProcessorImpl.process(monoCommand)

        StepVerifier.create(mono)
            .expectSubscription()
            .verifyComplete()
    }
}
