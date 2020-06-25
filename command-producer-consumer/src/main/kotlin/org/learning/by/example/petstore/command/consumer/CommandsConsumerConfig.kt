package org.learning.by.example.petstore.command.consumer

import org.learning.by.example.petstore.command.consumer.CommandsConsumerConfig.Constants.CONFIG_PREFIX
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(CONFIG_PREFIX)
data class CommandsConsumerConfig(
    val bootstrapServer: String,
    val topic: String,
    val clientId: String,
    val groupId: String,
    val offsetEarliest: String
) {
    companion object Constants {
        const val CONFIG_PREFIX = "service.commands.consumer"
    }
}
