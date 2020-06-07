package org.learning.by.example.petstore.reactor.dtovalidator

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.validation.Validator

@Configuration
class DTOConfiguration (private val validator: Validator) {
    @Bean
    fun dtoValidator() = DTOValidator(validator)
}
