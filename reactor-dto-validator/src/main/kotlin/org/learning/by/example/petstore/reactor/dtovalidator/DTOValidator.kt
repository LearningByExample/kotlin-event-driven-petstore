package org.learning.by.example.petstore.reactor.dtovalidator


import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import javax.validation.ConstraintViolation
import javax.validation.Validator

@Service
class DTOValidator(private val validator: Validator) {
    fun <Type> validate(monoObject: Mono<Type>): Mono<Type> {
        return monoObject.flatMap { obj ->
            val validate: Set<ConstraintViolation<Type>> = validator.validate(obj)
            if (validate.isNotEmpty()) {
                var message = ""
                validate.forEach {
                    message += "Invalid ${it.propertyPath}, ${it.message}. "
                }
                message = message.trim()
                Mono.error(InvalidDtoException(message))
            } else {
                monoObject
            }
        }
    }
}
