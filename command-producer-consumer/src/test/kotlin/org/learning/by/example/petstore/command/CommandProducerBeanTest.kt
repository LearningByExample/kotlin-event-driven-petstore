package org.learning.by.example.petstore.command

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.learning.by.example.petstore.command.consumer.CommandsConsumer
import org.learning.by.example.petstore.command.consumer.CommandsConsumerConfig
import org.learning.by.example.petstore.command.producer.CommandsProducer
import org.learning.by.example.petstore.command.producer.CommandsProducerConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("producer")
internal class CommandProducerBeanTest(@Autowired val applicationContext: ApplicationContext) {
    companion object {
        private val commandsProducerConfigBean = "${CommandsProducerConfig.PRODUCER_CONFIG_PREFIX}-" +
            "${CommandsProducerConfig::class.qualifiedName}"
        private val commandsConsumerBean = "${CommandsConsumer::class.simpleName}"
        private val commandsConsumerConfigBean = "${CommandsConsumerConfig.CONSUMER_CONFIG_PREFIX}-" +
            "${CommandsConsumerConfig::class.qualifiedName}"
        private val commandsProducerBean = "${CommandsProducer::class.simpleName}"
    }

    @Test
    fun `we should have only the producer beans`() {
        Assertions.assertThat(applicationContext.containsBean(commandsProducerConfigBean)).isTrue()
        Assertions.assertThat(applicationContext.containsBean(commandsProducerBean)).isTrue()
        Assertions.assertThat(applicationContext.containsBean(commandsConsumerConfigBean)).isFalse()
        Assertions.assertThat(applicationContext.containsBean(commandsConsumerBean)).isFalse()
    }
}
