package org.learning.by.example.petstore.reactor.dtovalidator

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class ValidDateValidator : ConstraintValidator<ValidDate, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext?) = if (value != null)
        try {
            ZonedDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME)
            true
        } catch (ex: Exception) {
            false
        } else true
}
