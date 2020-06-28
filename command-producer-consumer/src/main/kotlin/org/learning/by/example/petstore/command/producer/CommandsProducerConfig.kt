package org.learning.by.example.petstore.command.producer

import org.learning.by.example.petstore.command.producer.CommandsProducerConfig.Constants.PRODUCER_CONFIG_PREFIX
import org.learning.by.example.petstore.command.producer.CommandsProducerConfig.Constants.PRODUCER_VALIDATE_PROPERTY
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(PRODUCER_CONFIG_PREFIX)
@ConditionalOnProperty(PRODUCER_VALIDATE_PROPERTY)
data class CommandsProducerConfig(
    val bootstrapServer: String,
    val topic: String,
    val clientId: String,
    val ack: String
) {
    companion object Constants {
        const val PRODUCER_CONFIG_PREFIX = "service.commands.producer"
        const val PRODUCER_VALIDATE_PROPERTY = "$PRODUCER_CONFIG_PREFIX.bootstrap-server"
    }
}
