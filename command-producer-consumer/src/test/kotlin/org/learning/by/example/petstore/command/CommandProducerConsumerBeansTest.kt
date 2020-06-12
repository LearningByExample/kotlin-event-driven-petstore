package org.learning.by.example.petstore.command

import org.junit.jupiter.api.Test
import org.learning.by.example.petstore.command.producer.PetCommandsProducer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class CommandProducerConsumerBeansTest(@Autowired petCommandsProducer: PetCommandsProducer) {
    @Test
    fun `test PetCommandsProducer injection`() {
    }
}
