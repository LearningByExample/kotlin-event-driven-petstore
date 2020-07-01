package org.learning.by.example.petstore.command.dsl

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class CommandDslTest {
    companion object {
        private const val VALID_UUID = "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"
        private const val VALID_TIMESTAMP = "\\d{4}-(?:0[1-9]|1[0-2])-(?:0[1-9]|[1-2]\\d|3[0-1])T(?:[0-1]\\d|2[0-3])" +
            ":[0-5]\\d:[0-5]\\d(?:\\.\\d+|)(?:Z|(?:\\+|\\-)(?:\\d{2}):?(?:\\d{2}))"
    }

    @Test
    fun `we can create a command with attributes`() {
        val command = command("example command") {
            "attribute1" value "value1"
            "attribute2" value 123
            "attribute3" value false
            "attribute4" value 125.5
            "attribute5" values listOf("one", "two", "three")
        }

        Assertions.assertThat(command.commandName).isEqualTo("example command")
        Assertions.assertThat(command.id.toString()).matches(VALID_UUID)
        Assertions.assertThat(command.timestamp.toString()).matches(VALID_TIMESTAMP)
        Assertions.assertThat(command.get<String>("attribute1")).isEqualTo("value1")
        Assertions.assertThat(command.get<Int>("attribute2")).isEqualTo(123)
        Assertions.assertThat(command.get<Boolean>("attribute3")).isEqualTo(false)
        Assertions.assertThat(command.get<Double>("attribute4")).isEqualTo(125.5)
        Assertions.assertThat(command.getList<String>("attribute5")).isEqualTo(listOf("one", "two", "three"))
    }
}
