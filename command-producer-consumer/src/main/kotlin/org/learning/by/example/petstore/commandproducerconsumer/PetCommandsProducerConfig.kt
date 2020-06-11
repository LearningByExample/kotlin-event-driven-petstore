package org.learning.by.example.petstore.commandproducerconsumer

import org.learning.by.example.petstore.commandproducerconsumer.PetCommandsProducerConfig.Constants.CONFIG_PREFIX
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(CONFIG_PREFIX)
data class PetCommandsProducerConfig(
    val bootstrapServer: String,
    val topic: String,
    val clientId: String,
    val ack: String
) {
    companion object Constants {
        const val CONFIG_PREFIX = "service.pet-commands.producer"
    }
}
