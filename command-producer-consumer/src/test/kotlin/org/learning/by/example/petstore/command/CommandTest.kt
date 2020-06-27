package org.learning.by.example.petstore.command

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
internal class CommandTest(@Autowired val objectMapper: ObjectMapper) {
    companion object {
        private val CLASS_CAST_MESSAGE = "attribute 'attribute1' is not of ${Int::class} is ${String::class}"
        private const val NO_SUCH_ELEMENT_MESSAGE = "attribute 'attribute2' not found"
    }

    @Test
    fun `we can not do a get to an incorrect type`() {
        val command = Command("example command", hashMapOf("attribute1" to "value1"))

        val throws = assertThrows<ClassCastException> {
            assertThat(command.get<Int>("attribute1"))
        }
        assertThat(throws.message).isEqualTo(CLASS_CAST_MESSAGE)
    }

    @Test
    fun `we can check if an attribute exists`() {
        val command = Command("example command", hashMapOf("attribute1" to "value1"))

        assertThat(command.contains("attribute1")).isTrue()
        assertThat(command.contains("attribute2")).isFalse()
    }

    @Test
    fun `we can not get an invalid attribute`() {
        val command = Command("example command", hashMapOf("attribute1" to "value1"))

        val throws = assertThrows<NoSuchElementException> {
            assertThat(command.get<String>("attribute2"))
        }
        assertThat(throws.message).isEqualTo(NO_SUCH_ELEMENT_MESSAGE)
    }

    @Test
    fun `we can serialize and deserialize`() {
        val command = Command(
            "example command",
            hashMapOf(
                "attribute1" to "value1",
                "attribute2" to 123,
                "attribute3" to false,
                "attribute4" to 125.5
            )
        )

        val bytes = objectMapper.writeValueAsBytes(command)
        val newCommand = objectMapper.readValue<Command>(bytes)

        assertThat(command).isEqualTo(newCommand)
    }
}
