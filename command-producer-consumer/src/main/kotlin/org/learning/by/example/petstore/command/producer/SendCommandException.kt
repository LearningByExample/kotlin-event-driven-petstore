package org.learning.by.example.petstore.command.producer

class SendCommandException(cause: Throwable) : Exception(ERROR_SENDING, cause) {
    companion object {
        const val ERROR_SENDING = "Error sending the command"
    }
}
