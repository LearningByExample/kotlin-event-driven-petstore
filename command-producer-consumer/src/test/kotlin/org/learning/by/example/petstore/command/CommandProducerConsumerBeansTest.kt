package org.learning.by.example.petstore.command

import org.junit.jupiter.api.Test
import org.learning.by.example.petstore.command.consumer.CommandsConsumer
import org.learning.by.example.petstore.command.producer.CommandsProducer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class CommandProducerConsumerBeansTest(
    @Suppress("UNUSED_PARAMETER") @Autowired commandsProducer: CommandsProducer,
    @Suppress("UNUSED_PARAMETER") @Autowired commandsConsumer: CommandsConsumer
) {

    @Test
    fun `test Commands Producer & Consumer injection`() {
    }
}
