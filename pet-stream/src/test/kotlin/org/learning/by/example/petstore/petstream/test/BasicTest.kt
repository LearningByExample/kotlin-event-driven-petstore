package org.learning.by.example.petstore.petstream.test

import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.learning.by.example.petstore.petstream.service.sink.CommandSink
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext

@DirtiesContext
abstract class BasicTest {
    @MockBean
    lateinit var commandSink: CommandSink

    @BeforeEach
    fun setupMock() {
        doNothing().whenever(commandSink).run()
    }

    @AfterEach
    fun tearDownMock() {
        reset(commandSink)
    }
}
