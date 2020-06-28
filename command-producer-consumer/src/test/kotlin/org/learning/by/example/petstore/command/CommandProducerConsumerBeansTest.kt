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

@SpringBootTest
internal class CommandProducerConsumerBeansTest(@Autowired val applicationContext: ApplicationContext) {
    companion object {
        private val commandsProducerConfigBean = "${CommandsProducerConfig.CONFIG_PREFIX}-" +
            "${CommandsProducerConfig::class.qualifiedName}"
        private val commandsConsumerBean = "${CommandsConsumer::class.simpleName}"
        private val commandsConsumerConfigBean = "${CommandsConsumerConfig.CONFIG_PREFIX}-" +
            "${CommandsConsumerConfig::class.qualifiedName}"
        private val commandsProducerBean = "${CommandsProducer::class.simpleName}"
    }

    @Test
    fun `we should have the beans registered`() {
        Assertions.assertThat(applicationContext.containsBean(commandsProducerConfigBean)).isTrue()
        Assertions.assertThat(applicationContext.containsBean(commandsConsumerBean)).isTrue()
        Assertions.assertThat(applicationContext.containsBean(commandsConsumerConfigBean)).isTrue()
        Assertions.assertThat(applicationContext.containsBean(commandsProducerBean)).isTrue()
    }
}
