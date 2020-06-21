package org.learning.by.example.petstore.command.consumer

import org.learning.by.example.petstore.command.consumer.PetCommandsConsumerConfig.Constants.CONFIG_PREFIX
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(CONFIG_PREFIX)
data class PetCommandsConsumerConfig(
    val bootstrapServer: String,
    val topic: String,
    val clientId: String,
    val groupId: String,
    val offsetEarliest: String
) {
    companion object Constants {
        const val CONFIG_PREFIX = "service.pet-commands.consumer"
    }
}
