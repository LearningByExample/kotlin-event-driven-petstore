package org.learning.by.example.petstore.command

import org.learning.by.example.petstore.command.producer.PetCommandsProducer
import org.learning.by.example.petstore.command.producer.PetCommandsProducerConfig
import org.learning.by.example.petstore.command.producer.PetCommandsProducerImpl
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationPropertiesScan
class CommandProducerConsumerBeans(private val petCommandsProducerConfig: PetCommandsProducerConfig) {
    @Bean
    fun petCommandsProducer(): PetCommandsProducer = PetCommandsProducerImpl(petCommandsProducerConfig)
}
