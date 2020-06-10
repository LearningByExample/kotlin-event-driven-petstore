package org.learning.by.example.petstore.petcommands.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "kafka.properties")
data class KafkaConfig(
    val serverUri: String,
    val topic: String,
    val producerId: String,
    val ack: String
)
