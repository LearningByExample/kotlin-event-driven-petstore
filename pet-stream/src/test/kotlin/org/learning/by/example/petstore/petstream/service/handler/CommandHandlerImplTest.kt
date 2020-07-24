package org.learning.by.example.petstore.petstream.service.handler

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import org.learning.by.example.petstore.command.dsl.command
import org.learning.by.example.petstore.petstream.service.processor.CreatePetCommandProcessor
import org.learning.by.example.petstore.petstream.test.BasicTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import reactor.core.publisher.Mono
import reactor.kotlin.test.expectError
import reactor.test.StepVerifier

@SpringBootTest
internal class CommandHandlerImplTest(@Autowired val commandHandlerImpl: CommandHandlerImpl) : BasicTest() {
    @SpyBean
    lateinit var createPetCommandProcessor: CreatePetCommandProcessor

    @Test
    fun `we should handle correct commands`() {
        val cmd = command("pet_create") {
            "name" value "name"
        }

        doReturn(Mono.empty<Void>()).whenever(createPetCommandProcessor).process(any())
        doReturn(true).whenever(createPetCommandProcessor).validate(any())

        StepVerifier.create(commandHandlerImpl.handle(cmd))
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()

        verify(createPetCommandProcessor, times(1)).validate(any())
        verify(createPetCommandProcessor, times(1)).process(any())

        reset(createPetCommandProcessor)
    }

    @Test
    fun `we should error on invalid commands names`() {
        val cmd = command("invalid_command") {
            "name" value "name"
        }

        doReturn(Mono.empty<Void>()).whenever(createPetCommandProcessor).process(any())
        doReturn(true).whenever(createPetCommandProcessor).validate(any())

        StepVerifier.create(commandHandlerImpl.handle(cmd))
            .expectError<ProcessorNotFoundException>()
            .verify()

        verify(createPetCommandProcessor, times(0)).validate(any())
        verify(createPetCommandProcessor, times(0)).process(any())

        reset(createPetCommandProcessor)
    }

    @Test
    fun `we should error on commands that do not validate`() {
        val cmd = command("pet_create") {
            "name" value "name"
        }

        doReturn(Mono.empty<Void>()).whenever(createPetCommandProcessor).process(any())
        doReturn(false).whenever(createPetCommandProcessor).validate(any())

        StepVerifier.create(commandHandlerImpl.handle(cmd))
            .expectError<CommandHandlingException>()
            .verify()

        verify(createPetCommandProcessor, times(1)).validate(any())
        verify(createPetCommandProcessor, times(0)).process(any())

        reset(createPetCommandProcessor)
    }
}
