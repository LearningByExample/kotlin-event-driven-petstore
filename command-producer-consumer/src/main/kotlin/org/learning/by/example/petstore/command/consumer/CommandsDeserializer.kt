package org.learning.by.example.petstore.command.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.kafka.common.serialization.Deserializer
import org.learning.by.example.petstore.command.Command

class CommandsDeserializer : Deserializer<Command> {
    companion object {
        const val OBJECT_MAPPER_CONFIG_KEY = "org.learning.by.example.petstore.command.objectMapper"
    }

    private lateinit var objectMapper: ObjectMapper
    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) = with(configs[OBJECT_MAPPER_CONFIG_KEY]) {
        if (this is ObjectMapper) {
            objectMapper = this
        }
    }

    override fun deserialize(topic: String, data: ByteArray): Command {
        return objectMapper.readValue(data)
    }

    override fun close() {}
}
