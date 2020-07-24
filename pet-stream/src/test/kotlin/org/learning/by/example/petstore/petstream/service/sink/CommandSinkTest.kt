package org.learning.by.example.petstore.petstream.service.sink

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import org.learning.by.example.petstore.command.consumer.CommandsConsumer
import org.learning.by.example.petstore.command.dsl.command
import org.learning.by.example.petstore.petstream.service.handler.CommandHandler
import org.learning.by.example.petstore.petstream.test.BasicTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import java.time.LocalDateTime

@SpringBootTest
class CommandSinkTest : BasicTest() {
    @MockBean
    lateinit var consumer: CommandsConsumer

    @MockBean
    lateinit var handler: CommandHandler

    @Test
    fun `sink should sent to handler commands`() {
        val cmd1 = command("pet_create") {
            "name" value "name1"
            "category" value "category"
            "breed" value "breed"
            "vaccines" values listOf("vaccine1", "vaccine2")
            "dob" value LocalDateTime.now()
            "tags" values listOf("tag1", "tag2", "tag3")
        }
        val cmd2 = command("pet_create") {
            "name" value "name2"
            "category" value "category"
            "breed" value "breed"
            "vaccines" values listOf("vaccine2")
            "dob" value LocalDateTime.now()
            "tags" values listOf("tag1", "tag3")
        }

        doReturn(listOf(cmd1, cmd2).toFlux()).whenever(consumer).receiveCommands()
        doReturn(Mono.empty<Void>()).whenever(handler).handle(any())

        CommandSink(consumer, handler).run()

        verify(handler, times(2)).handle(any())
        verify(handler, times(1)).handle(cmd1)
        verify(handler, times(1)).handle(cmd2)
    }
}
