package org.learning.by.example.petstore.reactor.dtovalidator

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory

internal class ValidDateTest {
    companion object {
        private const val ERROR_MESSAGE = "it an invalid date"

        private val factory: ValidatorFactory = Validation.buildDefaultValidatorFactory()
        private val validator: Validator = factory.validator
    }

    data class DateString(
        @field:ValidDate(
            message = ERROR_MESSAGE
        )
        val value: String?
    )

    data class TestCase(val name: String, val parameters: Parameters, val expect: Expect) {
        data class Parameters(val objectToValidate: Any)
        data class Expect(val error: Boolean)
    }

    @TestFactory
    fun `test tags with invalid format`() = listOf(
        TestCase(
            name = "we should not get any error when it is a valid date",
            parameters = TestCase.Parameters(
                objectToValidate = DateString("2020-02-15T00:00:00.0Z")
            ),
            expect = TestCase.Expect(
                error = false
            )
        ),
        TestCase(
            name = "we should not validate anything when it is null",
            parameters = TestCase.Parameters(
                objectToValidate = DateString(null)
            ),
            expect = TestCase.Expect(
                error = false
            )
        ),
        TestCase(
            name = "we should get an error when it is not a valid date",
            parameters = TestCase.Parameters(
                objectToValidate = DateString("2020-02-30T00:00:00.0Z")
            ),
            expect = TestCase.Expect(
                error = true
            )
        ),
        TestCase(
            name = "we should get an error when it is a text",
            parameters = TestCase.Parameters(
                objectToValidate = DateString("hello")
            ),
            expect = TestCase.Expect(
                error = true
            )
        ),
        TestCase(
            name = "we should not validate anything when it is empty",
            parameters = TestCase.Parameters(
                objectToValidate = DateString("")
            ),
            expect = TestCase.Expect(
                error = true
            )
        )
    ).map {
        DynamicTest.dynamicTest(it.name) {
            val constraintViolations: Set<ConstraintViolation<Any>> = validator.validate(it.parameters.objectToValidate)
            Assertions.assertThat(constraintViolations.size).isEqualTo(if (it.expect.error) 1 else 0)
            constraintViolations.iterator().forEach {
                Assertions.assertThat(it.message).isEqualTo(ERROR_MESSAGE)
            }
        }
    }
}
