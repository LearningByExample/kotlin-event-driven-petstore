package org.learning.by.example.petstore.petcommands.handlers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.learning.by.example.petstore.petcommands.model.ErrorResponse
import org.learning.by.example.petstore.petcommands.model.Result
import org.learning.by.example.petstore.petcommands.testing.verify
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

    @Test
    fun `we should get a bad request when trying to add a pet with empty name`() {
        val httpRequest = MockServerHttpRequest
            .post("/pet")
            .contentType(MediaType.APPLICATION_JSON)
            .body(INVALID_PET_WITH_EMPTY_NAME)
        val webExchange = MockServerWebExchange.from(httpRequest)
        val request = ServerRequest.create(webExchange, HandlerStrategies.withDefaults().messageReaders())

        petHandler.postPet(request).verify { response, result: ErrorResponse ->
            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
            assertThat(response.headers().location).isNull()
            assertThat(response.headers().contentType).isEqualTo(MediaType.APPLICATION_JSON)

            assertThat(result.message).isEqualTo("Invalid pet")
            assertThat(result.description).isEqualTo("Invalid name, size must be between 3 and 64.")
        }
    }

    @Test
    fun `we should get a bad request when trying to add a pet with no name`() {
        val httpRequest = MockServerHttpRequest
            .post("/pet")
            .contentType(MediaType.APPLICATION_JSON)
            .body(INVALID_PET_WITH_NO_NAME)
        val webExchange = MockServerWebExchange.from(httpRequest)
        val request = ServerRequest.create(webExchange, HandlerStrategies.withDefaults().messageReaders())

        petHandler.postPet(request).verify { response, result: ErrorResponse ->
            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
            assertThat(response.headers().location).isNull()
            assertThat(response.headers().contentType).isEqualTo(MediaType.APPLICATION_JSON)

            assertThat(result.message).isEqualTo("Invalid pet")
            assertThat(result.description).isEqualTo("Invalid name, must not be null.")
        }
    }
}
