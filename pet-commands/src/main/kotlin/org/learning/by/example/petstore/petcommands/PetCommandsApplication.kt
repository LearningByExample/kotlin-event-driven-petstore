package org.learning.by.example.petstore.petcommands

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean


@SpringBootApplication
class PetCommandsApplication {
    @Bean
    @Primary
    fun validator(): LocalValidatorFactoryBean {
        return LocalValidatorFactoryBean()
    }
}

fun main(args: Array<String>) {
    runApplication<PetCommandsApplication>(*args)


}
