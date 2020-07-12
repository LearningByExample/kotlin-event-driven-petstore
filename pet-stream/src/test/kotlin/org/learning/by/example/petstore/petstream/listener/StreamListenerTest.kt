package org.learning.by.example.petstore.petstream.listener

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest
internal class StreamListenerTest {
    @MockBean
    lateinit var streamListener: StreamListener

    @Test
    fun onApplicationEvent() {
        doNothing().whenever(streamListener).onApplicationEvent(any())
        verify(streamListener, times(1)).onApplicationEvent(any())
    }
}
