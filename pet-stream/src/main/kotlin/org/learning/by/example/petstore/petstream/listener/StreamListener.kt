package org.learning.by.example.petstore.petstream.listener

import org.learning.by.example.petstore.petstream.service.sink.CommandSink
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class StreamListener(val commandSink: CommandSink) : ApplicationListener<ApplicationReadyEvent> {
    override fun onApplicationEvent(applicationReadyEvent: ApplicationReadyEvent) {
        Thread(commandSink).start()
    }
}
