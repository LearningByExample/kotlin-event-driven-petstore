package org.learning.by.example.petstore.petqueries.handler

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.learning.by.example.petstore.petqueries.model.Pet
import org.learning.by.example.petstore.petqueries.testing.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.reactive.function.server.HandlerStrategies
import org.springframework.web.reactive.function.server.ServerRequest

@SpringBootTest
internal class PetHandlerTest(@Autowired private val petHandler: PetHandler) {
    companion object {
        const val PET_GET_PATH = "/pet/4cb5294b-1034-4bc4-9b3d-542adb232a21"
    }

    @Test
    fun `when ask to get a pet it should return the pet details`() {
        val httpRequest = MockServerHttpRequest
            .get(PET_GET_PATH)
            .accept(MediaType.APPLICATION_JSON)
        val webExchange = MockServerWebExchange.from(httpRequest)
        val request = ServerRequest.create(webExchange, HandlerStrategies.withDefaults().messageReaders())

        petHandler.getPet(request).verify { response, pet: Pet ->
            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK)
            assertThat(response.headers().contentType).isEqualTo(MediaType.APPLICATION_JSON)

            assertThat(pet.name).isEqualTo("fluffy")
            assertThat(pet.category).isEqualTo("dog")
            assertThat(pet.breed).isEqualTo("german shepherd")
            assertThat(pet.dob).isEqualTo("2020-06-28T00:00:00.0Z")
        }
    }
}
