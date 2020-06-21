package org.learning.by.example.petstore.command.consumer

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.kotlin.core.publisher.toMono

class PetCommandsConsumerImpl : PetCommandsConsumer {
    
    @Value("\${service.pet-commands.consumer.bootstrap-server}")
    lateinit var server: String

    override fun receiveCommands() = getKafkaReceiver().receive().flatMap {
        val receiverOffset = it.receiverOffset()
        receiverOffset.acknowledge()
        it.value().toMono()
    }

    private fun getKafkaReceiver() = KafkaReceiver.create(ReceiverOptions.create<String, String>(hashMapOf<String, Any>(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to server,
        ConsumerConfig.CLIENT_ID_CONFIG to "pet_commands_consumer",
        ConsumerConfig.GROUP_ID_CONFIG to "pet_commands_consumers",
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest"
    )).subscription(setOf("test")))

}
