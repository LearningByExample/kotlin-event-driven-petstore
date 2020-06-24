package org.learning.by.example.petstore.command

import com.fasterxml.jackson.databind.ObjectMapper
import org.learning.by.example.petstore.command.consumer.PetCommandsConsumer
import org.learning.by.example.petstore.command.consumer.PetCommandsConsumerConfig
import org.learning.by.example.petstore.command.consumer.PetCommandsConsumerImpl
import org.learning.by.example.petstore.command.producer.PetCommandsProducer
import org.learning.by.example.petstore.command.producer.PetCommandsProducerConfig
import org.learning.by.example.petstore.command.producer.PetCommandsProducerImpl
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationPropertiesScan
class CommandProducerConsumerBeans(
    private val petCommandsProducerConfig: PetCommandsProducerConfig,
    private val petCommandsConsumerConfig: PetCommandsConsumerConfig,
    private val objectMapper: ObjectMapper
) {
    @Bean
    fun petCommandsProducer(): PetCommandsProducer = PetCommandsProducerImpl(petCommandsProducerConfig, objectMapper)

    @Bean
    fun petCommandsConsumer(): PetCommandsConsumer = PetCommandsConsumerImpl(petCommandsConsumerConfig)
}
