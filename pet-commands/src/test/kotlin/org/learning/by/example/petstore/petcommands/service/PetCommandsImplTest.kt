package org.learning.by.example.petstore.petcommands.service

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.learning.by.example.petstore.petcommands.model.Pet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.test.StepVerifier
import java.util.*

@SpringBootTest
class PetCommandsImplTest(@Autowired val petCommandsImpl: PetCommandsImpl) {
    companion object {
        private const val CLIENT_ID = "pet_commands_consumer"
        private const val GROUP_ID = "pet_commands_consumers"
        private const val OFFSET_EARLIEST = "earliest"
        private const val SERVER_CONFIG = "localhost:9092"
        private const val TOPIC = "pet_commands"
        private const val VALID_UUID = "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"
    }

    @Test
    fun `we should send commands`() {
        var id = ""
        StepVerifier
            .create(petCommandsImpl.sendPetCreate(Pet("fluffy", "dog", listOf())))
            .expectSubscription()
            .thenRequest(Long.MAX_VALUE)
            .consumeNextWith {
                id = it
                assertThat(id).matches(VALID_UUID)
            }
            .verifyComplete()

        StepVerifier.create(getStrings())
            .expectSubscription()
            .thenRequest(Long.MAX_VALUE)
            .expectNext(id)
            .expectNextCount(0L)
            .thenCancel()
            .verify()
    }

    private fun getStrings() = getKafkaReceiver().receive().map {
        val receiverOffset = it.receiverOffset()
        receiverOffset.acknowledge()
        it.value()
    }

    private fun getKafkaReceiver(): KafkaReceiver<String, String> {
        val props: MutableMap<String, Any> = HashMap()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = SERVER_CONFIG
        props[ConsumerConfig.CLIENT_ID_CONFIG] = CLIENT_ID
        props[ConsumerConfig.GROUP_ID_CONFIG] = GROUP_ID
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = OFFSET_EARLIEST
        return KafkaReceiver.create(ReceiverOptions.create<String, String>(props).subscription(setOf(TOPIC)))
    }
}
