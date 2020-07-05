package org.learning.by.example.petstore.petcommands.service

class SendPetCreateException(cause: Throwable) : Exception(ERROR_SENDING_PET_CREATE, cause) {
    companion object {
        const val ERROR_SENDING_PET_CREATE = "Error sending pet create command"
    }
}
