package org.learning.by.example.petstore.command.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.learning.by.example.petstore.command.Command
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.kotlin.core.publisher.toMono

class CommandsConsumerImpl(
    commandsConsumerConfig: CommandsConsumerConfig,
    objectMapper: ObjectMapper
) : CommandsConsumer {
    private val receiver: KafkaReceiver<String, Command> = KafkaReceiver.create(
        ReceiverOptions.create<String, Command>(
            hashMapOf<String, Any>(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to commandsConsumerConfig.bootstrapServer,
                ConsumerConfig.CLIENT_ID_CONFIG to commandsConsumerConfig.clientId,
                ConsumerConfig.GROUP_ID_CONFIG to commandsConsumerConfig.groupId,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to CommandsDeserializer::class.java,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to commandsConsumerConfig.offsetEarliest,
                CommandsDeserializer.OBJECT_MAPPER_CONFIG_KEY to objectMapper
            )
        ).subscription(setOf(commandsConsumerConfig.topic))
    )

    override fun receiveCommands() = receiver.receive().flatMap {
        val receiverOffset = it.receiverOffset()
        receiverOffset.acknowledge()
        it.value().toMono()
    }
}
