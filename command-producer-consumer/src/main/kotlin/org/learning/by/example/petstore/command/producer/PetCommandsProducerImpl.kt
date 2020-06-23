package org.learning.by.example.petstore.command.producer

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.learning.by.example.petstore.command.Command
import org.learning.by.example.petstore.command.utils.JsonSerializer
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import reactor.kafka.sender.SenderRecord
import reactor.kotlin.core.publisher.toMono
import java.util.*

internal class PetCommandsProducerImpl(private val petCommandsProducerConfig: PetCommandsProducerConfig) : PetCommandsProducer {
    private val sender: KafkaSender<String, Command> = KafkaSender.create(
        SenderOptions.create(
            hashMapOf<String, Any>(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to petCommandsProducerConfig.bootstrapServer,
                ProducerConfig.CLIENT_ID_CONFIG to petCommandsProducerConfig.clientId,
                ProducerConfig.ACKS_CONFIG to petCommandsProducerConfig.ack,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
            )
        )
    )

    private fun send(command: Command) = sender.send(
        SenderRecord.create(ProducerRecord(petCommandsProducerConfig.topic, command.id.toString(), command), command.id).toMono()
    ).single().flatMap { command.id.toMono() }

    override fun sendCommand(command: Command): Mono<UUID> = send(command)
}
