package org.learning.by.example.petstore.commandproducerconsumer

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class DTOConfigurationTest(@Autowired petCommandsProducerConfig: PetCommandsProducerConfig) {
    @Test
    fun `test dtoValidator injection`() {
    }
}
