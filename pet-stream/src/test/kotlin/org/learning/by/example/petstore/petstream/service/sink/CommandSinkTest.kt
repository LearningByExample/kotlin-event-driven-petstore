package org.learning.by.example.petstore.petstream.service.sink

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import org.learning.by.example.petstore.command.consumer.CommandsConsumer
import org.learning.by.example.petstore.petstream.service.handler.CommandHandler
import org.learning.by.example.petstore.petstream.test.BasicTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux

@SpringBootTest
class CommandSinkTest : BasicTest() {
    @MockBean
    lateinit var consumer: CommandsConsumer

    @MockBean
    lateinit var handler: CommandHandler

    @Test
    fun `sink should sent to handler commands`() {
        doReturn(listOf(Mono.empty<Void>(), Mono.empty<Void>()).toFlux()).whenever(consumer).receiveCommands(any())
        doReturn(Mono.empty<Void>()).whenever(handler).handle(any())

        val sink = CommandSink(consumer, handler)

        sink.run()
        sink.end()

        verify(consumer, times(1)).receiveCommands(handler::handle)
    }
}
