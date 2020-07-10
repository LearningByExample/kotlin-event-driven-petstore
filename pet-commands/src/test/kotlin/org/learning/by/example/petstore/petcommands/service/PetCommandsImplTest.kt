package org.learning.by.example.petstore.petcommands.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.learning.by.example.petstore.command.producer.CommandsProducer
import org.learning.by.example.petstore.command.producer.SendCommandException
import org.learning.by.example.petstore.petcommands.model.Pet
import org.learning.by.example.petstore.petcommands.service.PetCommandsImpl.Companion.CREATE_PET_COMMAND
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.test.StepVerifier
import java.util.UUID

@SpringBootTest
class PetCommandsImplTest(@Autowired val petCommandsImpl: PetCommandsImpl) {
    companion object {
        private const val PET_NAME = "fluffy"
        private const val PET_CATEGORY = "dog"
        private const val BREED = "german shepherd"
        private const val DOB = "2020-06-28T00:00:00.0Z"
        private val VACCINES = listOf("rabies", "parvovirus")
        private val PET_TAGS = listOf("puppy", "black")
    }

    @MockBean
    lateinit var commandsProducer: CommandsProducer

    @Test
    fun `we should send commands`() {
        val uuid = UUID.randomUUID()
        given(commandsProducer.sendCommand(any())).willReturn(uuid.toMono())

        StepVerifier
            .create(petCommandsImpl.sendPetCreate(Pet(PET_NAME, PET_CATEGORY, BREED, DOB, VACCINES, PET_TAGS)))
            .expectSubscription()
            .thenRequest(Long.MAX_VALUE)
            .expectNext(uuid)
            .verifyComplete()

        verify(commandsProducer, times(1)).sendCommand(
            argThat {
                assertThat(commandName).isEqualTo(CREATE_PET_COMMAND)
                assertThat(get<String>("name")).isEqualTo(PET_NAME)
                assertThat(get<String>("category")).isEqualTo(PET_CATEGORY)
                assertThat(getList<String>("tags")).isEqualTo(PET_TAGS)
                true
            }
        )
    }

    @Test
    fun `we should send an exception when something goes wrong with the commands producer`() {
        doReturn(Mono.error<Any>(SendCommandException(RuntimeException("throwable cause"))))
            .whenever(commandsProducer).sendCommand(any())

        StepVerifier
            .create(petCommandsImpl.sendPetCreate(Pet(PET_NAME, PET_CATEGORY, BREED, DOB, VACCINES, PET_TAGS)))
            .expectSubscription()
            .thenRequest(Long.MAX_VALUE)
            .consumeErrorWith {
                assertThat(it).isInstanceOf(SendPetCreateException::class.java)
                assertThat(it.message).isEqualTo("Error sending pet create command")
                assertThat(it.localizedMessage).isEqualTo("Error sending pet create command")
                assertThat(it.cause?.cause).isInstanceOf(RuntimeException::class.java)
            }
            .verify()

        verify(commandsProducer, times(1)).sendCommand(
            argThat {
                assertThat(commandName).isEqualTo(CREATE_PET_COMMAND)
                assertThat(get<String>("name")).isEqualTo(PET_NAME)
                assertThat(get<String>("category")).isEqualTo(PET_CATEGORY)
                assertThat(getList<String>("tags")).isEqualTo(PET_TAGS)
                true
            }
        )
    }
}
