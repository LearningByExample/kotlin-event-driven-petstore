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
        private val LIST_CAST_MESSAGE = "attribute 'attribute1' is not a List of ${Int::class}"
        private const val NO_SUCH_ELEMENT_MESSAGE = "attribute 'attribute2' not found"
    }

    @Test
    fun `we can not do a get to an incorrect type`() {
        val command = Command("example command", hashMapOf("attribute1" to "value1"))

        val throws = assertThrows<ClassCastException> {
            command.get<Int>("attribute1")
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
    fun `we can work with lists`() {
        val command = Command(
            "example command",
            hashMapOf(
                "attribute1" to listOf("1", "2", "3"),
                "attribute2" to listOf(1, 2, 3),
                "attribute3" to listOf(1.5, 2.5, 3.5)
            )
        )

        assertThat(command.get<List<String>>("attribute1")).isEqualTo(listOf("1", "2", "3"))
        assertThat(command.get<List<Int>>("attribute2")).isEqualTo(listOf(1, 2, 3))
        assertThat(command.get<List<Double>>("attribute3")).isEqualTo(listOf(1.5, 2.5, 3.5))

        assertThat(command.getList<String>("attribute1")).isEqualTo(listOf("1", "2", "3"))
        assertThat(command.getList<Int>("attribute2")).isEqualTo(listOf(1, 2, 3))
        assertThat(command.getList<Double>("attribute3")).isEqualTo(listOf(1.5, 2.5, 3.5))
    }

    @Test
    fun `we can not do a getList to an incorrect type`() {
        val command = Command(
            "example command",
            hashMapOf(
                "attribute1" to listOf("1", "2", "3")
            )
        )

        val throws = assertThrows<ClassCastException> {
            command.getList<Int>("attribute1")
        }
        assertThat(throws.message).isEqualTo(LIST_CAST_MESSAGE)
    }

    @Test
    fun `we can not get an invalid attribute`() {
        val command = Command("example command", hashMapOf("attribute1" to "value1"))

        val throws = assertThrows<NoSuchElementException> {
            command.get<String>("attribute2")
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
                "attribute4" to 125.5,
                "attribute5" to listOf("one", "two", "three")
            )
        )

        val bytes = objectMapper.writeValueAsBytes(command)
        val newCommand = objectMapper.readValue<Command>(bytes)

        assertThat(command).isEqualTo(newCommand)
    }
}
