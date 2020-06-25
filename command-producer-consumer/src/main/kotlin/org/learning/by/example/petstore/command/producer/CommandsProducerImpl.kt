package org.learning.by.example.petstore.command.producer

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.learning.by.example.petstore.command.Command
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import reactor.kafka.sender.SenderRecord
import reactor.kotlin.core.publisher.toMono
import java.util.UUID

internal class CommandsProducerImpl(
    private val commandsProducerConfig: CommandsProducerConfig,
    objectMapper: ObjectMapper
) : CommandsProducer {
    private val sender: KafkaSender<String, Command> = KafkaSender.create(
        SenderOptions.create(
            hashMapOf<String, Any>(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to commandsProducerConfig.bootstrapServer,
                ProducerConfig.CLIENT_ID_CONFIG to commandsProducerConfig.clientId,
                ProducerConfig.ACKS_CONFIG to commandsProducerConfig.ack,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to CommandsSerializer::class.java,
                CommandsSerializer.OBJECT_MAPPER_CONFIG_KEY to objectMapper
            )
        )
    )

    private fun send(command: Command): Mono<UUID> = sender.send(
        SenderRecord.create(
            ProducerRecord(commandsProducerConfig.topic, command.id.toString(), command), command.id
        ).toMono()
    ).single().flatMap { command.id.toMono() }

    override fun sendCommand(command: Command): Mono<UUID> = send(command)
}
