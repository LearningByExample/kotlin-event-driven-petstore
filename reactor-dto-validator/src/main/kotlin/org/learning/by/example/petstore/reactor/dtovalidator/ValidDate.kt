package org.learning.by.example.petstore.reactor.dtovalidator

import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidDateValidator::class])
annotation class ValidDate(
    val message: String = "is not a valid date",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)
