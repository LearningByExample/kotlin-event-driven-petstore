package org.learning.by.example.petstore.petcommands.handlers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.extension.ExtendWith
import org.learning.by.example.petstore.petcommands.model.ErrorResponse
import org.learning.by.example.petstore.petcommands.model.Result
import org.learning.by.example.petstore.petcommands.testing.verify
import org.learning.by.example.petstore.petcommands.utils.ServerConstants.Companion.INVALID_RESOURCE
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.reactive.function.server.HandlerStrategies
import org.springframework.web.reactive.function.server.ServerRequest


@ExtendWith(SpringExtension::class)
@SpringBootTest
class PetHandlerTest(@Autowired private val petHandler: PetHandler) {
    companion object {
        private const val PET_URL = "/pet"
        private const val VALID_UUID = "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"
        const val VALID_PET_URL = "$PET_URL/$VALID_UUID"

        const val VALID_PET = """
            {
              "name": "dogie",
              "category": "dog"
            }
        """
        const val INVALID_PET_WITH_EMPTY_NAME = """
            {
              "name": "",
              "category": "dog"
            }
        """
        const val INVALID_PET_WITH_NO_NAME = """
            {
              "category": "dog"
            }
        """
        const val INVALID_PET_WITH_EMPTY_CATEGORY = """
            {
              "name": "dogie",
              "category": ""
            }
        """
        const val INVALID_PET_WITH_NO_CATEGORY = """
            {
                "name": "dogie"
            }
        """
    }

    data class TestCase(val name: String, val parameters: Parameters, val expect: Expect) {
        data class Parameters(val body: String)
        data class Expect(val errorDescription: String)
    }

    @TestFactory
    fun `We should handle bad request when posting a pet with incorrect input`() = listOf(
        TestCase(
            name = "we should get a bad request when trying to add a pet with empty name",
            parameters = TestCase.Parameters(
                body = INVALID_PET_WITH_EMPTY_NAME
            ),
            expect = TestCase.Expect(
                errorDescription ="Invalid name, size must be between 3 and 64."
            )
        ),
        TestCase(
            name = "we should get a bad request when trying to add a pet with no name",
            parameters = TestCase.Parameters(
                body = INVALID_PET_WITH_NO_NAME
            ),
            expect = TestCase.Expect(
                errorDescription = "Invalid name, must not be null."
            )
        ),
        TestCase(
            name = "we should get a bad request when trying to add a pet with empty category",
            parameters = TestCase.Parameters(
                body = INVALID_PET_WITH_EMPTY_CATEGORY
            ),
            expect = TestCase.Expect(
                errorDescription ="Invalid category, size must be between 3 and 64."
            )
        ),
        TestCase(
            name = "we should get a bad request when trying to add a pet with no category",
            parameters = TestCase.Parameters(
                body = INVALID_PET_WITH_NO_CATEGORY
            ),
            expect = TestCase.Expect(
                errorDescription = "Invalid category, must not be null."
            )
        )
    ).map {
        DynamicTest.dynamicTest(it.name) {
            val httpRequest = MockServerHttpRequest
                .post("/pet")
                .contentType(MediaType.APPLICATION_JSON)
                .body(it.parameters.body)
            val webExchange = MockServerWebExchange.from(httpRequest)
            val request = ServerRequest.create(webExchange, HandlerStrategies.withDefaults().messageReaders())

            petHandler.postPet(request).verify { response, result: ErrorResponse ->
                assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
                assertThat(response.headers().contentType).isEqualTo(MediaType.APPLICATION_JSON)
                assertThat(response.headers().location).isNull()

                assertThat(result.message).isEqualTo(INVALID_RESOURCE)
                assertThat(result.description).isEqualTo(it.expect.errorDescription)
            }
        }
    }

    @Test
    fun `we should get the result and headers when adding a pet`() {
        // we may need to change this if this issue isn't fix :
        // https://github.com/spring-projects/spring-framework/issues/25087
        val httpRequest = MockServerHttpRequest
            .post("/pet")
            .contentType(MediaType.APPLICATION_JSON)
            .body(VALID_PET)
        val webExchange = MockServerWebExchange.from(httpRequest)
        val request = ServerRequest.create(webExchange, HandlerStrategies.withDefaults().messageReaders())

        petHandler.postPet(request).verify { response, result: Result ->
            assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED)
            assertThat(response.headers().location.toString()).matches(VALID_PET_URL)
            assertThat(response.headers().contentType).isEqualTo(MediaType.APPLICATION_JSON)

            assertThat(result.id).matches(VALID_UUID)
        }
    }

}
