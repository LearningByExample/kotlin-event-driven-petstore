package org.learning.by.example.petstore.petstream.listener

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class StreamListener : ApplicationListener<ApplicationReadyEvent> {
    override fun onApplicationEvent(applicationReadyEvent: ApplicationReadyEvent) {
        while (true) {
        }
    }
}
