package org.learning.by.example.petstore.command.test

import org.testcontainers.containers.BindMode
import org.testcontainers.containers.KafkaContainer

class CustomKafkaContainer : KafkaContainer() {
    companion object {
        private const val SCRIPT_PATH = "scripts"
        private const val SCRIPT_SEND_MESSAGE = "send_message.sh"
        private const val SCRIPT_READ_MESSAGE = "read_message.sh"
        private const val SCRIPT_CREATE_TOPIC = "create_topic.sh"
        private const val CONTAINER_PATH = "/usr/helpers"
        private const val CONTAINER_MESSAGE_SEND_COMMAND = "$CONTAINER_PATH/$SCRIPT_SEND_MESSAGE"
        private const val CONTAINER_MESSAGE_READ_COMMAND = "$CONTAINER_PATH/$SCRIPT_READ_MESSAGE"
        private const val CONTAINER_TOPIC_COMMAND = "$CONTAINER_PATH/$SCRIPT_CREATE_TOPIC"
        private val CHMOD_CMD = arrayOf("chmod", "+xX")
    }

    private val readOffsetPerTopic: HashMap<String, Int> = hashMapOf()

    init {
        withClasspathResourceMapping(SCRIPT_PATH, CONTAINER_PATH, BindMode.READ_ONLY)
    }

    override fun start() {
        super.start()
        execInContainer(*CHMOD_CMD, CONTAINER_MESSAGE_SEND_COMMAND)
        execInContainer(*CHMOD_CMD, CONTAINER_MESSAGE_READ_COMMAND)
        execInContainer(*CHMOD_CMD, CONTAINER_TOPIC_COMMAND)
    }

    fun createTopic(topic: String) = with(execInContainer(CONTAINER_TOPIC_COMMAND, topic).exitCode == 0) {
        if (this) readOffsetPerTopic[topic] = 0
        this
    }

    fun sendMessage(topic: String, message: String): Boolean =
        execInContainer(CONTAINER_MESSAGE_SEND_COMMAND, topic, message).exitCode == 0

    fun getMessage(topic: String): String {
        val offset = readOffsetPerTopic.getOrDefault(topic, 0)
        with(execInContainer(CONTAINER_MESSAGE_READ_COMMAND, topic, offset.toString())) {
            return if (this.exitCode == 0) {
                readOffsetPerTopic[topic] = offset + 1
                this.stdout.trimIndent()
            } else {
                ""
            }
        }
    }
}
