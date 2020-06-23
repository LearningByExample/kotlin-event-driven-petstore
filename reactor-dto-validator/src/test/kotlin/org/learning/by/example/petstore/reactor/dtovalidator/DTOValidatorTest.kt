package org.learning.by.example.petstore.reactor.dtovalidator

import org.junit.jupiter.api.Test
import reactor.test.StepVerifier
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

internal class DTOValidatorTest {
    companion object {
        private val factory: ValidatorFactory = Validation.buildDefaultValidatorFactory()
        private val validator: Validator = factory.validator

        private const val ONE_ERROR = "Invalid .*, .*\\."
        private const val TWO_ERRORS = "$ONE_ERROR $ONE_ERROR"
    }

    data class ObjectToValidate(
        @field:Min(5)
        val field: Int,
        @field:NotNull
        val otherField: String?
    )

    @Test
    fun `validate an object should return a mono error with two messages`() {
        val dtoValidator = DTOValidator(validator)
        val validate = dtoValidator.validate(ObjectToValidate(4, null))

        StepVerifier.create(validate)
            .expectErrorMatches {
                (it is InvalidDtoException) && (it.message!!.matches(Regex((TWO_ERRORS))))
            }
            .verify()
    }

    @Test
    fun `validate an object should return a mono error with one message`() {
        val dtoValidator = DTOValidator(validator)
        val validate = dtoValidator.validate(ObjectToValidate(4, "hello"))

        StepVerifier.create(validate)
            .expectErrorMatches {
                (it is InvalidDtoException) && (it.message!!.matches(Regex((ONE_ERROR))))
            }
            .verify()
    }

    @Test
    fun `validate an object should return the input object`() {
        val dtoValidator = DTOValidator(validator)
        val objectToValidate = ObjectToValidate(45, "other value ")
        val validate = dtoValidator.validate(objectToValidate)

        StepVerifier.create(validate)
            .expectNextMatches(objectToValidate::equals)
            .expectComplete()
            .verify()
    }
}
