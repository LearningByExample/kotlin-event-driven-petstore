package org.learning.by.example.petstore.petstream.listener

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Test
import org.learning.by.example.petstore.petstream.test.BasicTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest
internal class StreamListenerTest : BasicTest() {
    @MockBean
    lateinit var streamListener: StreamListener

    @Test
    fun `when application starts StreamListener receives an event`() {
        verify(streamListener, times(1)).onApplicationEvent(any())
    }
}
