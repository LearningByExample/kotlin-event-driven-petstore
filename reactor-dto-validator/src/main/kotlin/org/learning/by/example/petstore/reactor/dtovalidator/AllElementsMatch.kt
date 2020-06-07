package org.learning.by.example.petstore.reactor.dtovalidator

import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass


@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ElementsPatternValidator::class])
annotation class AllElementsMatch(
        val message: String = "elements should match the pattern",
        val groups: Array<KClass<Any>> = [],
        val payload: Array<KClass<Payload>> = [],
        val regexp: String = ".*"
)
