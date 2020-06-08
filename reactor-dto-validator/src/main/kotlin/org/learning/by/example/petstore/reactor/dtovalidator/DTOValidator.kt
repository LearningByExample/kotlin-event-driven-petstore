package org.learning.by.example.petstore.reactor.dtovalidator


import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import javax.validation.ConstraintViolation
import javax.validation.Validator
import reactor.kotlin.core.publisher.toMono

@Service
class DTOValidator(private val validator: Validator) {
    fun <Type : Any> validate(obj: Type): Mono<Type> {
        val validate: Set<ConstraintViolation<Type>> = validator.validate(obj)
        return if (validate.isNotEmpty()) {
            var message = ""
            validate.forEach {
                message += "Invalid ${it.propertyPath}, ${it.message}. "
            }
            message = message.trim()
            Mono.error(InvalidDtoException(message))
        } else {
            obj.toMono()
        }
    }
}
