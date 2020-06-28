package org.learning.by.example.petstore.command

import com.fasterxml.jackson.databind.ObjectMapper
import org.learning.by.example.petstore.command.consumer.CommandsConsumer
import org.learning.by.example.petstore.command.consumer.CommandsConsumerConfig
import org.learning.by.example.petstore.command.consumer.CommandsConsumerImpl
import org.learning.by.example.petstore.command.producer.CommandsProducer
import org.learning.by.example.petstore.command.producer.CommandsProducerConfig
import org.learning.by.example.petstore.command.producer.CommandsProducerImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationPropertiesScan
class CommandProducerConsumerBeans(private val objectMapper: ObjectMapper) {
    @Autowired(required = false) lateinit var commandsProducerConfig: CommandsProducerConfig
    @Autowired(required = false) lateinit var commandsConsumerConfig: CommandsConsumerConfig

    @Bean("CommandsProducer")
    @ConditionalOnProperty(CommandsProducerConfig.PRODUCER_VALIDATE_PROPERTY)
    fun commandsProducer(): CommandsProducer = CommandsProducerImpl(commandsProducerConfig, objectMapper)

    @Bean("CommandsConsumer")
    @ConditionalOnProperty(CommandsConsumerConfig.CONSUMER_VALIDATE_PROPERTY)
    fun commandsConsumer(): CommandsConsumer = CommandsConsumerImpl(commandsConsumerConfig, objectMapper)
}
