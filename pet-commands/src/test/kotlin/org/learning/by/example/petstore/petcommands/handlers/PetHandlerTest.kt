package org.learning.by.example.petstore.petcommands.handlers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.learning.by.example.petstore.petcommands.model.ErrorResponse
import org.learning.by.example.petstore.petcommands.model.Pet
import org.learning.by.example.petstore.petcommands.model.Result
import org.learning.by.example.petstore.petcommands.testing.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.reactive.function.server.MockServerRequest
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.core.publisher.toMono


@ExtendWith(SpringExtension::class)
@SpringBootTest
class PetHandlerTest(@Autowired private val petHandler: PetHandler) {
    companion object {
        private const val PET_URL = "/pet"
        private const val VALID_UUID = "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"
        const val VALID_PET_URL = "$PET_URL/$VALID_UUID"

        val VALID_PET = Pet("dogie", "dog")
        val INVALID_PET_WITHOUT_NAME = Pet(name = "", category = "dog")
    }

    @Test
    fun `we should get the result and headers when adding a pet`() {
        val request = MockServerRequest.builder()
            .method(HttpMethod.POST)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(VALID_PET.toMono())

        petHandler.postPet(request).verify { response, result: Result ->
            assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED)
            assertThat(response.headers().location.toString()).matches(VALID_PET_URL)
            assertThat(response.headers().contentType).isEqualTo(MediaType.APPLICATION_JSON)

            assertThat(result.id).matches(VALID_UUID)
        }
    }

    @Test
    fun `we should get a bad request when trying to add a pet without name`() {
        val request = MockServerRequest.builder()
            .method(HttpMethod.POST)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(INVALID_PET_WITHOUT_NAME.toMono())

        petHandler.postPet(request).verify { response, result: ErrorResponse ->
            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
            assertThat(response.headers().location).isNull()
            assertThat(response.headers().contentType).isEqualTo(MediaType.APPLICATION_JSON)

            assertThat(result.message).isEqualTo("invalid pet")
            assertThat(result.description).isEqualTo("invalid name, size must be between 3 and 64.")
        }
    }
}
