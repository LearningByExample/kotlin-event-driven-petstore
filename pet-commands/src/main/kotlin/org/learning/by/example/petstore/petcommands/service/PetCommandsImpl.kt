package org.learning.by.example.petstore.petcommands.service

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.learning.by.example.petstore.petcommands.configuration.KafkaConfig
import org.learning.by.example.petstore.petcommands.model.Pet
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import reactor.kafka.sender.SenderRecord
import reactor.kotlin.core.publisher.toMono
import java.util.*

@Service
class PetCommandsImpl(private val kafkaConfig: KafkaConfig) : PetCommands {

    private final val producer: KafkaSender<String, String> = KafkaSender.create(SenderOptions.create(hashMapOf<String, Any>(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaConfig.serverUri,
        ProducerConfig.CLIENT_ID_CONFIG to kafkaConfig.producerId,
        ProducerConfig.ACKS_CONFIG to kafkaConfig.ack,
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java
    )))

    private fun send(id: String) = producer.send(Flux.just(SenderRecord.create(ProducerRecord(kafkaConfig.topic, id, id), id)))
        .single().flatMap { id.toMono() }

    override fun sendPetCreate(pet: Pet): Mono<String> = send(UUID.randomUUID().toString())
}
