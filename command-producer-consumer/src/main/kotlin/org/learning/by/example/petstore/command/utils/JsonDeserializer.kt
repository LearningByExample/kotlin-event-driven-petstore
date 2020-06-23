package org.learning.by.example.petstore.command.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.kafka.common.serialization.Deserializer
import org.learning.by.example.petstore.command.Command

class JsonDeserializer : Deserializer<Command> {
    companion object {
        private val objectMapper = ObjectMapper().apply {
            registerKotlinModule()
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            dateFormat = StdDateFormat()
        }
    }

    override fun configure(config: MutableMap<String, *>?, isKey: Boolean) {}

    override fun deserialize(topic: String, data: ByteArray?): Command? {
        if (data == null) return null
        return objectMapper.readValue(data, Command::class.java)
    }

    override fun close() {}
}
