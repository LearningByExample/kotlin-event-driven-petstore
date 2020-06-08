package org.learning.by.example.petstore.petcommands.service

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
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
class PetCommandsImpl : PetCommands {
    companion object {
        private const val PRODUCER_ID = "pet_commands_producer"
        private const val SERVER_CONFIG = "localhost:9092"
        private const val TOPIC = "pet_commands"
        private const val ALL_ACK = "all"
    }

    private final val producer: KafkaSender<String, String>

    init {
        producer = KafkaSender.create(SenderOptions.create(hashMapOf<String, Any>(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to SERVER_CONFIG,
            ProducerConfig.CLIENT_ID_CONFIG to PRODUCER_ID,
            ProducerConfig.ACKS_CONFIG to ALL_ACK,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java
        )))
    }

    private fun send(id: String) = producer.send(Flux.just(SenderRecord.create(ProducerRecord(TOPIC, id, id), id)))
        .single().flatMap { id.toMono() }

    override fun sendPetCreate(pet: Pet): Mono<String> = send(UUID.randomUUID().toString())
}
