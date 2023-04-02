package pl.umk.mat.zesp01.pz2022.researcher.service


import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import pl.umk.mat.zesp01.pz2022.researcher.model.*
import pl.umk.mat.zesp01.pz2022.researcher.repository.ResearchRepository
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
class ResearchServiceTests {

    @Autowired lateinit var researchService: ResearchService
    @Autowired lateinit var researchRepository: ResearchRepository
    lateinit var researchTestObject: Research
    lateinit var testResearchCode: String
    lateinit var testUser1: User
    lateinit var testUser2: User


    @BeforeEach
    fun setup() {
        testUser1 = User(
            login = "testLOGIN",
            password = "testPASSWORD",
            firstName = "testFIRSTNAME",
            lastName = "testLASTNAME",
            email = "testEMAIL@test.com",
            phone = "123456789",
            birthDate = "01-01-1970",
            gender = "Male",
            avatarImage = "testAVATARIMAGE.IMG",
            location = "Bydgoszcz",
            isConfirmed = false
        )
        testUser2 = User(
            login = "testLOGIN2",
            password = "testPASSWORD2",
            firstName = "testFIRSTNAME2",
            lastName = "testLASTNAME2",
            email = "testEMAIL@test.com2",
            phone = "1234567892",
            birthDate = "02-01-1970",
            gender = "Female",
            avatarImage = "testAVATARIMAGE2.IMG",
            location = "Warszawa",
            isConfirmed = false
        )
        researchTestObject = Research(
            researchCode = "testResearchCODE",
            creatorLogin = "testLOGIN",
            title = "testTITLE",
            description = "testDESCRIPTION",
            participantLimit = 100,
            participants = listOf("testUser1", "testUser2"),
            begDate = "01-01-2025",
            endDate = "31-01-2025",
            location = ResearchLocation("testFORM", "testPLACE"),
            rewards = listOf(
                ResearchReward("Cash", 500),
                ResearchReward("Gift","testGIFT")
            ),
            requirements = listOf(
                ResearchRequirement("Monthly gross income in PLN", 8000),
                ResearchRequirement("Job","Politician")
            ))

        researchRepository.deleteAll()
        testResearchCode = researchTestObject.researchCode
    }

    @Test
    fun `add new Research by ResearchService`() {
        // GIVEN (researchTestObject)

        // WHEN
        val addedResearch = researchService.addResearch(researchTestObject)
        testResearchCode = addedResearch.researchCode

        // THEN
        assertTrue(
            researchTestObject == researchRepository.findResearchByResearchCode(testResearchCode).get(),
            "Researches are not the same (addResearch failed)."
        )
    }

    @Test
    fun `delete existing research by ResearchService`() {
        // GIVEN
        researchRepository.save(researchTestObject)

        // WHEN
        researchService.deleteResearchByResearchCode(testResearchCode)

        // THEN
        assertTrue(researchRepository.findResearchByResearchCode(testResearchCode).isEmpty, "Research has not been deleted (deleteResearchByResearchCode failed).")
    }


    @Test
    fun `update existing Research data by ResearchService`() {
        // GIVEN
        researchRepository.save(researchTestObject)

        val newResearchTitle = "updated title"
        val newResearchParticipantLimit = 20

        val updatedResearch = ResearchUpdateRequest(
            title = newResearchTitle,
            participantLimit = newResearchParticipantLimit,
            description = researchTestObject.description,
            location = researchTestObject.location)


        // WHEN
        researchTestObject.title = newResearchTitle
        researchTestObject.participantLimit = newResearchParticipantLimit

        researchService.updateResearch(researchTestObject, updatedResearch)

        // THEN
        assertTrue(
            researchTestObject == researchRepository.findResearchByResearchCode(testResearchCode).get(),
            "Research has not been changed (update failed)."
        )
    }

    @Test
    fun `get all research Codes using ResearchService`() {
        // GIVEN
        val researchTestObject2 = Research(
            researchCode = "testResearchCODE2",
            creatorLogin = "testLOGIN2",
            title = "testTITLE2",
            description = "testDESCRIPTION2",
            participantLimit = 1002,
            participants = listOf("testUser1"),
            begDate = "01-02-2025",
            endDate = "28-02-2025",
            location = ResearchLocation("testFORM2", "testPLACE2"),
            rewards = listOf(
                ResearchReward("Cash", 5002),
                ResearchReward("Gift","testGIFT2")
            ),
            requirements = listOf(
                ResearchRequirement("Monthly gross income in PLN", 80002),
                ResearchRequirement("Job","Musician")
            ))

        researchRepository.saveAll(listOf(researchTestObject, researchTestObject2))

        // WHEN
        val result = researchService.getAllResearchCodes()

        // THEN
        assertEquals(listOf("{\"researchCode\": \"testResearchCODE\"}", "{\"researchCode\": \"testResearchCODE2\"}"), result)
    }

    @Test
    fun `get research by Code using ResearchService`() {
        // GIVEN
        researchRepository.save(researchTestObject)

        // WHEN
        val result = Optional.of(researchService.getResearchByResearchCode(testResearchCode))

        // THEN
        assertEquals(researchTestObject, result.get())
    }

    @Test
    fun `get research by creator login using ResearchService`() {
        // GIVEN
        researchRepository.save(researchTestObject)
        val testResearchCreatorLogin = researchTestObject.creatorLogin

        // WHEN
        val result = researchService.getResearchesByCreatorLogin(testResearchCreatorLogin)

        // THEN
        assertEquals(listOf(researchTestObject), result)
    }


    @Test
    fun `get researches sorted by title using ResearchService`() {
        // GIVEN
        val researchTestObject2 = Research(
            researchCode = "testResearchCODE2",
            creatorLogin = "testLOGIN2",
            title = "testTITLE2",
            description = "testDESCRIPTION2",
            participantLimit = 1002,
            participants = listOf("testUser1"),
            begDate = "01-02-2025",
            endDate = "28-02-2025",
            location = ResearchLocation("testFORM2", "testPLACE2"),
            rewards = listOf(
                ResearchReward("Cash", 5002),
                ResearchReward("Gift","testGIFT2")
            ),
            requirements = listOf(
                ResearchRequirement("Monthly gross income in PLN", 80002),
                ResearchRequirement("Job","Musician")
            ))

        researchRepository.saveAll(listOf(researchTestObject, researchTestObject2))

        // WHEN
        val result = researchService.sortResearchesByTitle()

        // THEN
        assertEquals(listOf(researchTestObject, researchTestObject2), result)
    }


}

