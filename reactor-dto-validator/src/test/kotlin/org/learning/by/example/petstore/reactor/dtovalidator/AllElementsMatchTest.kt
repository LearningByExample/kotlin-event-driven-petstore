package org.learning.by.example.petstore.reactor.dtovalidator

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory

internal class AllElementsMatchTest {
    companion object {
        private const val ERROR_MESSAGE = "it does not fit with the regex expression"
        private const val REGEX_PATTERN_LETTERS = "[a-zA-Z\\-]{3,15}"
        private const val REGEX_PATTERN_NUMBERS = "[\\d]{3,15}"

        private val factory: ValidatorFactory = Validation.buildDefaultValidatorFactory()
        private val validator: Validator = factory.validator
    }

    data class ArrayListOfString(
        @field:AllElementsMatch(
            regexp = REGEX_PATTERN_LETTERS,
            message = ERROR_MESSAGE
        )
        val value: ArrayList<String>
    )

    data class ListOfInteger(
        @field:AllElementsMatch(
            regexp = REGEX_PATTERN_NUMBERS,
            message = ERROR_MESSAGE
        )
        val value: List<Int>
    )

    data class SetOfString(
        @field:AllElementsMatch(
            regexp = REGEX_PATTERN_LETTERS,
            message = ERROR_MESSAGE
        )
        val value: Set<String>?
    )

    data class TestCase(val name: String, val parameters: Parameters, val expect: Expect) {
        data class Parameters(val objectToValidate: Any)
        data class Expect(val error: Boolean)
    }

    @TestFactory
    fun `test tags with invalid format`() = listOf(
        TestCase(
            name = "list<string> we should get an error when the value does not fit the regex expresion",
            parameters = TestCase.Parameters(
                objectToValidate = ArrayListOfString(arrayListOf("one value"))
            ),
            expect = TestCase.Expect(
                error = true
            )
        ),
        TestCase(
            name = "list<int> we should get an error when the value does not fit the regex expresion",
            parameters = TestCase.Parameters(
                objectToValidate = ListOfInteger(listOf(1, 9877))
            ),
            expect = TestCase.Expect(
                error = true
            )
        ),
        TestCase(
            name = "list<int> we should get an error when the value does not fit the regex expresion",
            parameters = TestCase.Parameters(
                objectToValidate = ListOfInteger(listOf(1234, 98))
            ),
            expect = TestCase.Expect(
                error = true
            )
        ),
        TestCase(
            name = "set<string> we should get an error when the value does not fit the regex expresion",
            parameters = TestCase.Parameters(
                objectToValidate = SetOfString(setOf("correct-value", "incorrect value *", "another value"))
            ),
            expect = TestCase.Expect(
                error = true
            )
        ),
        TestCase(
            name = "set<string> we should not get an error when the value fits the regex expresion",
            parameters = TestCase.Parameters(
                objectToValidate = SetOfString(setOf("first-ok", "second-ok", "third-ok"))
            ),
            expect = TestCase.Expect(
                error = false
            )
        ),
        TestCase(
            name = "ArrayList<string> we should not get an error when the value fits the regex expresion",
            parameters = TestCase.Parameters(
                objectToValidate = ArrayListOfString(arrayListOf())
            ),
            expect = TestCase.Expect(
                error = false
            )
        ),
        TestCase(
            name = "set<string> we should not get an error when the value is null",
            parameters = TestCase.Parameters(
                objectToValidate = SetOfString(null)
            ),
            expect = TestCase.Expect(
                error = false
            )
        )
    ).map {
        DynamicTest.dynamicTest(it.name) {
            val constraintViolations: Set<ConstraintViolation<Any>> = validator.validate(it.parameters.objectToValidate)
            assertThat(constraintViolations.size).isEqualTo(if (it.expect.error) 1 else 0)
            constraintViolations.iterator().forEach {
                assertThat(it.message).isEqualTo(ERROR_MESSAGE)
            }
        }
    }
}
