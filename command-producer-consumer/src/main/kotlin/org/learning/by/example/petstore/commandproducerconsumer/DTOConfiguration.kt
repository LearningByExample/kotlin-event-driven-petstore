package org.learning.by.example.petstore.commandproducerconsumer

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DTOConfiguration (private val petCommandsProducerConfig: PetCommandsProducerConfig) {
    @Bean
    fun petCommands() =
        PetCommandsImpl(petCommandsProducerConfig)
}
