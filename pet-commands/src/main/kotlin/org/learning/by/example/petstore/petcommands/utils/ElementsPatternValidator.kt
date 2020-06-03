package org.learning.by.example.petstore.petcommands.utils

import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext


class ElementsPatternValidator : ConstraintValidator<AllElementsMatch, Iterable<Any>> {
    private var matcher = Regex(".*")
    override fun initialize(constraintAnnotation: AllElementsMatch?) {
        super.initialize(constraintAnnotation)
        if (constraintAnnotation != null)
            matcher = Regex(constraintAnnotation.regexp)
    }

    override fun isValid(elements: Iterable<Any>?, context: ConstraintValidatorContext?): Boolean = elements?.none {
        !it.toString().matches(matcher)
    } != false
}
