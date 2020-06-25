package org.learning.by.example.petstore.command.producer

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.common.serialization.Serializer
import org.learning.by.example.petstore.command.Command

class CommandsSerializer : Serializer<Command> {
    companion object {
        const val OBJECT_MAPPER_CONFIG_KEY = "org.learning.by.example.petstore.command.objectMapper"
    }

    private lateinit var objectMapper: ObjectMapper
    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) = with(configs[OBJECT_MAPPER_CONFIG_KEY]) {
        if (this is ObjectMapper) {
            objectMapper = this
        }
    }

    override fun serialize(topic: String, data: Command): ByteArray = objectMapper.writeValueAsBytes(data)

    override fun close() {}
}
