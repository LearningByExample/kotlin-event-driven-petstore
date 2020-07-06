package org.learning.by.example.petstore.command.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.serialization.StringDeserializer
import org.learning.by.example.petstore.command.Command
import reactor.core.publisher.Flux
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.kotlin.core.publisher.toMono

class CommandsConsumerImpl(commandsConsumerConfig: CommandsConsumerConfig, objectMapper: ObjectMapper) :
    CommandsConsumer {
    private val options = hashMapOf<String, Any>(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to commandsConsumerConfig.bootstrapServer,
        ConsumerConfig.CLIENT_ID_CONFIG to commandsConsumerConfig.clientId,
        ConsumerConfig.GROUP_ID_CONFIG to commandsConsumerConfig.groupId,
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to CommandsDeserializer::class.java,
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to commandsConsumerConfig.offsetEarliest,
        ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG to commandsConsumerConfig.timeoutMS,
        CommandsDeserializer.OBJECT_MAPPER_CONFIG_KEY to objectMapper
    )

    private val receiver: KafkaReceiver<String, Command> = KafkaReceiver.create(
        ReceiverOptions.create<String, Command>(options).subscription(setOf(commandsConsumerConfig.topic))
    )

    override fun receiveCommands(): Flux<Command> {
        // KafkaConsumer handle connection errors as retryable errors, so if we first subscribe to receive messages
        // from a topic and the broker is down we will wait forever since the consumer will retry indefinitely.
        //
        // To avoid this we will ask for the existing topics in Kafka, only during a set of configurable time, if we
        // can't get a response in that time we will return an error. We don't need to check what topics they are
        // since we are not really interested in them.
        //
        // However if we get topics first and them subscribed, we may get disconnected latter, but the KafkaConsumer
        // will handle that recovery automatically and we do not need to check for topics in future connections.
        try {
            topics()
        } catch (ex: ConnectingToKafkaException) {
            return Flux.error(ex)
        }
        return receiver.receive().flatMap {
            val receiverOffset = it.receiverOffset()
            receiverOffset.acknowledge()
            it.value().toMono()
        }
    }

    @Throws(ConnectingToKafkaException::class)
    fun topics() = try {
        KafkaConsumer<String, String>(options).use {
            it.listTopics().keys.toList()
        }
    } catch (ex: KafkaException) {
        throw ConnectingToKafkaException(ex)
    }
}
