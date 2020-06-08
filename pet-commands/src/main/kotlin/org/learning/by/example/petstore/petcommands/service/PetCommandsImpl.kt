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
        val props: MutableMap<String, Any> = HashMap()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = SERVER_CONFIG
        props[ProducerConfig.CLIENT_ID_CONFIG] = PRODUCER_ID
        props[ProducerConfig.ACKS_CONFIG] = ALL_ACK
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        producer = KafkaSender.create(SenderOptions.create(props))
    }

    override fun sendPetCreate(pet: Pet): Mono<String> {
        val id = UUID.randomUUID().toString()
        return producer.send(Flux.just(SenderRecord.create(ProducerRecord(TOPIC, id, id), id)))
            .single().map { id }
    }
}
