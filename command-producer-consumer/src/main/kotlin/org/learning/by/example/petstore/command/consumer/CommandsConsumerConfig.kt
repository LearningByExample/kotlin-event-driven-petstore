package org.learning.by.example.petstore.command.consumer

import org.learning.by.example.petstore.command.consumer.CommandsConsumerConfig.Constants.CONSUMER_CONFIG_EXIST_PROPERTY
import org.learning.by.example.petstore.command.consumer.CommandsConsumerConfig.Constants.CONSUMER_CONFIG_PREFIX
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(CONSUMER_CONFIG_PREFIX)
@ConditionalOnProperty(CONSUMER_CONFIG_EXIST_PROPERTY)
data class CommandsConsumerConfig(
    val bootstrapServer: String,
    val topic: String,
    val clientId: String,
    val groupId: String,
    val offsetEarliest: String
) {
    companion object Constants {
        const val CONSUMER_CONFIG_PREFIX = "service.commands.consumer"
        const val CONSUMER_CONFIG_BOOSTRAP_SERVER = "$CONSUMER_CONFIG_PREFIX.bootstrap-server"
        const val CONSUMER_CONFIG_EXIST_PROPERTY = CONSUMER_CONFIG_BOOSTRAP_SERVER
    }
}
