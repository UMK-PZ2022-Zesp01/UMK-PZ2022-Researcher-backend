package pl.umk.mat.zesp01.pz2022.researcher.repository

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import pl.umk.mat.zesp01.pz2022.researcher.model.*
import pl.umk.mat.zesp01.pz2022.researcher.service.ResearchService
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
class ResearchRepositoryTests {

    @Autowired lateinit var researchService: ResearchService
    @Autowired lateinit var researchRepository: ResearchRepository
    lateinit var researchTestObject: Research
    lateinit var testResearchCode: String

    @BeforeEach
    fun setup() {
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
        testResearchCode = researchTestObject.researchCode
        researchRepository.deleteAll()
    }

    @Test
    fun `add new Research by researchRepository`() {
        // GIVEN (userTestObject)

        // WHEN
        researchRepository.save(researchTestObject)

        // THEN
        assertTrue(researchTestObject == researchService.getResearchByCode(testResearchCode).get())
    }

    @Test
    fun `delete existing research by researchRepository`() {
        // GIVEN (userTestObject)
        val addedResearch = researchService.addResearch(researchTestObject)
        testResearchCode = addedResearch.researchCode

        // WHEN
        researchRepository.deleteResearchByResearchCode(testResearchCode)

        // THEN
        assertTrue(researchService.getResearchByCode(testResearchCode).isEmpty)
    }

    @Test
    fun `get research by code using researchRepository`() {
        // GIVEN
        val addedResearch = researchService.addResearch(researchTestObject)
        testResearchCode = addedResearch.researchCode
        researchTestObject = researchTestObject.copy(researchCode = testResearchCode)

        // WHEN
        val result = researchRepository.findResearchByResearchCode(testResearchCode)

        // THEN
        assertEquals(researchTestObject, result.get())
    }

}

