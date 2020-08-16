package org.learning.by.example.petstore.petqueries.service

import org.junit.jupiter.api.Test
import org.learning.by.example.petstore.petqueries.testing.DatabaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.r2dbc.core.DatabaseClient
import reactor.test.StepVerifier
import java.util.UUID

@SpringBootTest
internal class PetServiceImplKoTest(
    @Autowired val petServiceImpl: PetServiceImpl,
    @Autowired val databaseClient: DatabaseClient
) : DatabaseTest() {
    companion object {
        const val EXISTING_UUID = "4cb5294b-1034-4bc4-9b3d-542adb232a21"
    }

    fun renameTable(old: String, new: String) {
        databaseClient.execute("ALTER TABLE $old RENAME TO $new").fetch().one().block()
    }

    fun runWithRenameTable(old: String, new: String, receiver: () -> Unit) {
        renameTable(old, new)
        receiver()
        renameTable(new, old)
    }

    @Test
    fun `we should get a get vaccines exception when error on the database`() {
        runWithRenameTable("vaccines", "new_vaccines") {
            StepVerifier.create(petServiceImpl.findPetById(UUID.fromString(EXISTING_UUID)))
                .expectSubscription()
                .expectErrorMatches {
                    it is GettingPetException &&
                        it.message == "Error getting pet"
                    it.cause is GettingVaccinesException &&
                        it.cause!!.message == "Error getting pet vaccines"
                }
                .verify()
        }
    }

    @Test
    fun `we should get a get tags exception when error on the database`() {
        runWithRenameTable("tags", "new_tags") {
            StepVerifier.create(petServiceImpl.findPetById(UUID.fromString(EXISTING_UUID)))
                .expectSubscription()
                .expectErrorMatches {
                    it is GettingPetException &&
                        it.message == "Error getting pet"
                    it.cause is GettingTagsException &&
                        it.cause!!.message == "Error getting pet tags"
                }
                .verify()
        }
    }

    @Test
    fun `we should get a get pet exception when error on the database`() {
        runWithRenameTable("pets", "new_pets") {
            StepVerifier.create(petServiceImpl.findPetById(UUID.fromString(EXISTING_UUID)))
                .expectSubscription()
                .expectErrorMatches {
                    it is GettingPetException &&
                        it.message == "Error getting pet"
                }
                .verify()
        }
    }
}
