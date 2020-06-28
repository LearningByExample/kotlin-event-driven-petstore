package org.learning.by.example.petstore.command

import com.fasterxml.jackson.databind.ObjectMapper
import org.learning.by.example.petstore.command.consumer.CommandsConsumer
import org.learning.by.example.petstore.command.consumer.CommandsConsumerConfig
import org.learning.by.example.petstore.command.consumer.CommandsConsumerImpl
import org.learning.by.example.petstore.command.producer.CommandsProducer
import org.learning.by.example.petstore.command.producer.CommandsProducerConfig
import org.learning.by.example.petstore.command.producer.CommandsProducerImpl
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationPropertiesScan
class CommandProducerConsumerBeans(
    private val commandsProducerConfig: CommandsProducerConfig,
    private val commandsConsumerConfig: CommandsConsumerConfig,
    private val objectMapper: ObjectMapper
) {
    @Bean("CommandsProducer")
    fun commandsProducer(): CommandsProducer = CommandsProducerImpl(commandsProducerConfig, objectMapper)

    @Bean("CommandsConsumer")
    fun commandsConsumer(): CommandsConsumer = CommandsConsumerImpl(commandsConsumerConfig, objectMapper)
}
