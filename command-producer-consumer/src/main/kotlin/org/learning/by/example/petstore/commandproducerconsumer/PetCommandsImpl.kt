package org.learning.by.example.petstore.commandproducerconsumer

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import reactor.kafka.sender.SenderRecord
import reactor.kotlin.core.publisher.toMono
import java.util.*

@Service
class PetCommandsImpl(private val petCommandsProducerConfig: PetCommandsProducerConfig) : PetCommands {
    private final val producer: KafkaSender<String, String> = KafkaSender.create(SenderOptions.create(
        hashMapOf<String, Any>(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to petCommandsProducerConfig.bootstrapServer,
            ProducerConfig.CLIENT_ID_CONFIG to petCommandsProducerConfig.clientId,
            ProducerConfig.ACKS_CONFIG to petCommandsProducerConfig.ack,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java
        )
    ))

    private fun send(id: String) = producer.send(
        SenderRecord.create(ProducerRecord(petCommandsProducerConfig.topic, id, id), id).toMono()
    ).single().flatMap { id.toMono() }

    override fun sendPetCreate(pojo: Any): Mono<String> = send(UUID.randomUUID().toString())
}
