package org.learning.by.example.petstore.petcommands.utils

import org.learning.by.example.petstore.petcommands.exceptions.InvalidParametersException
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import javax.validation.ConstraintViolation
import javax.validation.Validator

@Service
class DTOHelper(val validator: Validator) {
    fun <Type> validate(monoObject: Mono<Type>): Mono<Type> {
        return monoObject.flatMap { obj ->
            val validate: Set<ConstraintViolation<Type>> = validator.validate(obj)
            if (validate.isNotEmpty()) {
                var message = ""
                validate.forEach {
                    message += "Invalid ${it.propertyPath}, ${it.message}. "
                }
                message = message.trim()
                Mono.error(InvalidParametersException(message))
            } else {
                monoObject
            }
        }
    }
}
