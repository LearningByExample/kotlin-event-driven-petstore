package org.learning.by.example.petstore.command.consumer

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.kotlin.core.publisher.toMono

class PetCommandsConsumerImpl(private val petCommandsConsumerConfig: PetCommandsConsumerConfig) : PetCommandsConsumer {
    override fun receiveCommands() = getKafkaReceiver().receive().flatMap {
        val receiverOffset = it.receiverOffset()
        receiverOffset.acknowledge()
        it.value().toMono()
    }

    private fun getKafkaReceiver() = KafkaReceiver.create(ReceiverOptions.create<String, String>(hashMapOf<String, Any>(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to petCommandsConsumerConfig.bootstrapServer,
        ConsumerConfig.CLIENT_ID_CONFIG to petCommandsConsumerConfig.clientId,
        ConsumerConfig.GROUP_ID_CONFIG to petCommandsConsumerConfig.groupId,
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to petCommandsConsumerConfig.offsetEarliest
    )).subscription(setOf(petCommandsConsumerConfig.topic)))
}
