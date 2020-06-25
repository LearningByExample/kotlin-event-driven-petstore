package org.learning.by.example.petstore.command.producer

import org.learning.by.example.petstore.command.producer.CommandsProducerConfig.Constants.CONFIG_PREFIX
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(CONFIG_PREFIX)
data class CommandsProducerConfig(
    val bootstrapServer: String,
    val topic: String,
    val clientId: String,
    val ack: String
) {
    companion object Constants {
        const val CONFIG_PREFIX = "service.commands.producer"
    }
}
