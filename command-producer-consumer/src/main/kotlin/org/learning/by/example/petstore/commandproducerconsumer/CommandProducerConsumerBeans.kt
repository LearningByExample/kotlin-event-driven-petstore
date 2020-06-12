package org.learning.by.example.petstore.commandproducerconsumer

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationPropertiesScan
class CommandProducerConsumerBeans(private val petCommandsProducerConfig: PetCommandsProducerConfig) {
    @Bean
    fun petCommandsProducer(): PetCommandsProducer = PetCommandsProducerImpl(petCommandsProducerConfig)
}
