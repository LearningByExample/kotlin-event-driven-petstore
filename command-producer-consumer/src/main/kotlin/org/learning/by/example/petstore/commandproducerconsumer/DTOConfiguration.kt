package org.learning.by.example.petstore.commandproducerconsumer

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationPropertiesScan
class DTOConfiguration(private val petCommandsProducerConfig: PetCommandsProducerConfig) {
    @Bean
    fun petCommands(): PetCommands = PetCommandsImpl(petCommandsProducerConfig)
}
