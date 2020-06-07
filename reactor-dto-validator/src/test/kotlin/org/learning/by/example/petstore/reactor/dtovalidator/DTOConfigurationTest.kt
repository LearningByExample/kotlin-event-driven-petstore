package org.learning.by.example.petstore.reactor.dtovalidator

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class DTOConfigurationTest(@Autowired dtoValidator: DTOValidator) {
    @Test
    fun `test dtoValidator injection`() {
    }
}
