package pl.umk.mat.zesp01.pz2022.researcher.controller.research

import com.google.gson.Gson
import org.bson.types.Binary
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.*
import org.springframework.http.HttpStatus.*
import org.springframework.test.context.ActiveProfiles
import pl.umk.mat.zesp01.pz2022.researcher.model.*
import pl.umk.mat.zesp01.pz2022.researcher.repository.ResearchRepository
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository
import pl.umk.mat.zesp01.pz2022.researcher.service.RefreshTokenService
import java.util.*


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
class ResearchControllerTests(
    @Autowired val restTemplate: TestRestTemplate,
    @Autowired val userRepository: UserRepository,
    @Autowired val researchRepository: ResearchRepository,
    @Autowired val refreshTokenService: RefreshTokenService
) {

    lateinit var researchTestObject: Research
    lateinit var testResearchCode: String
    lateinit var testResearchCreator: User
    lateinit var testCreatorLogin: String

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
            location = ResearchLocation(form = "testFORM", place = "testPLACE", address = "testAddress"),
            rewards = listOf(
                ResearchReward(type = "Cash", value =  500),
                ResearchReward(type = "Gift", value = "testGIFT")
            ),
            requirements = listOf(
                ResearchRequirement(type = "Monthly gross income in PLN", criteria = 8000),
                ResearchRequirement(type = "Job", criteria = "Politician")
            ),
            creatorFullName = "FullName Of Creator",
            creatorEmail = "testEMAIL@test.com",
            creatorPhone = "123456789",
            poster = Binary("poster1".toByteArray())
        )
        testResearchCode = researchTestObject.researchCode
        researchRepository.deleteAll()

        testResearchCreator = User(
            login = "testLOGIN",
            password = "testPASSWORD",
            firstName = "testFIRSTNAME",
            lastName = "testLASTNAME",
            email = "testEMAIL@test.com",
            phone = "123456789",
            birthDate = "01-01-1970",
            gender = "Male",
            location = "Bydgoszcz",
            isConfirmed = true
        )
        testCreatorLogin = testResearchCreator.login
        userRepository.deleteAll()

    }
//
//    @Test
//    fun `addResearch should add new Research and returns CREATED (201)`() {
//        // GIVEN (researchTestObject)
//        val researchRequest = ResearchRequest(
//            researchTestObject.title,
//            researchTestObject.description,
//            researchTestObject.creatorLogin,
//            researchTestObject.creatorFullName,
//            researchTestObject.creatorEmail,
//            researchTestObject.creatorPhone,
//            researchTestObject.begDate,
//            researchTestObject.endDate,
//            researchTestObject.participantLimit,
//            researchTestObject.location,
//            researchTestObject.rewards,
//            researchTestObject.requirements
//        )
//
//        // GIVEN (researchTestObject)
//        userRepository.save(testResearchCreator)
//        val validToken = refreshTokenService.createAccessToken(testCreatorLogin)
//
//        val posterImage = MockMultipartFile(
//            "posterImage", "poster.jpg", "image/jpeg",
//            "posterImage".toByteArray(StandardCharsets.UTF_8)
//        )
//
//        val headers = HttpHeaders()
//        headers["Authorization"] = validToken
//        headers.contentType = MediaType.MULTIPART_FORM_DATA
//
//
//        val requestEntity = HttpEntity(
//            LinkedMultiValueMap<String, Any>().apply {
//                add("posterImage", posterImage.resource)
//                add("researchProperties", Gson().toJson(researchTestObject))
//            },
//            headers
//        )
//
//        val responseEntity = restTemplate.postForEntity(
//            "/research/add",
//            requestEntity,
//            String::class.java
//        )
//
//        // THEN
//        assertEquals(CREATED, responseEntity.statusCsode)
//        assertTrue(researchRepository.findResearchByResearchCode(testResearchCode).isPresent)
//    }


        @Test
    fun `updateResearch should update research and returns OK (200)`() {
        // GIVEN
        val researchUpdateRequest = ResearchUpdateRequest(
            title = "New Title",
            description = "New Description",
            begDate = null,
            endDate = null,
            creatorEmail = null,
            creatorPhone = "123 456 789",
            participantLimit = null,
            location = null
        )

        userRepository.save(testResearchCreator)
        researchRepository.save(researchTestObject)

        val validToken = refreshTokenService.createAccessToken(testCreatorLogin)

        // WHEN
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        headers.add("Authorization", validToken)
        val request = HttpEntity(researchUpdateRequest, headers)

        val response = restTemplate.exchange(
            "/research/$testResearchCode/update",
            HttpMethod.PUT,
            request,
            ResearchUpdateRequest::class.java
        )

        // THEN
        assertEquals(OK, response.statusCode)

        // Verify that research was actually updated in the database
        val updatedResearch = researchRepository.findResearchByResearchCode(testResearchCode).get()
        assertEquals(researchUpdateRequest.title, updatedResearch.title)
        assertEquals(researchUpdateRequest.description, updatedResearch.description)
        assertEquals(researchUpdateRequest.creatorPhone, updatedResearch.creatorPhone)
        assertEquals(researchTestObject.begDate, updatedResearch.begDate)
        assertEquals(researchTestObject.endDate, updatedResearch.endDate)
        assertEquals(researchTestObject.creatorEmail, updatedResearch.creatorEmail)
    }

    @Test
    fun `enrollOnResearch should add current user to the participants list of the research`() {
        // GIVEN
        val newParticipantLogin = "newParticipant"
        researchRepository.save(researchTestObject)
        userRepository.save(testResearchCreator.copy(login = newParticipantLogin))
        val validToken = refreshTokenService.createAccessToken(newParticipantLogin)

        // WHEN
        val headers = HttpHeaders()
        headers["Authorization"] = validToken

        val responseEntity = restTemplate.exchange(
            "/research/${testResearchCode}/enroll",
            HttpMethod.PUT,
            HttpEntity(null, headers),
            String::class.java
        )

        // THEN
        assertEquals(OK, responseEntity.statusCode)
        assertTrue(researchRepository.findResearchByResearchCode(testResearchCode).get().participants.contains(newParticipantLogin))
    }



    @Test
    fun `checkCurrentUserEnrollment should return OK if current user is enrolled in the research`() {
        // GIVEN

        val participantLogin = "participantLogin"
        userRepository.save(testResearchCreator.copy(login = participantLogin))
        val validToken = refreshTokenService.createAccessToken(participantLogin)

        researchRepository.save(researchTestObject.copy(participants = listOf(participantLogin)))

        // WHEN
        val headers = HttpHeaders()
        headers["Authorization"] = validToken
        val responseEntity = restTemplate.exchange(
            "/research/${testResearchCode}/enrollCheck",
            HttpMethod.GET,
            HttpEntity(null, headers),
            String::class.java
        )

        // THEN
        assertEquals(OK, responseEntity.statusCode)
    }



    @Test
    fun `deleteCurrentUserFromResearch should remove current user from the participants list of the research`() {
        // GIVEN
        val participantLogin = "participantLogin"
        userRepository.save(testResearchCreator.copy(login = participantLogin))
        val validToken = refreshTokenService.createAccessToken(participantLogin)

        researchRepository.save(researchTestObject.copy(participants = listOf(participantLogin)))

        // WHEN
        val headers = HttpHeaders()
        headers["Authorization"] = validToken
        val responseEntity = restTemplate.exchange(
            "/research/${testResearchCode}/resign",
            HttpMethod.DELETE,
            HttpEntity(null, headers),
            String::class.java
        )

        // THEN
        assertEquals(OK, responseEntity.statusCode)
        assertFalse(researchRepository.findResearchByResearchCode(testResearchCode).get().participants.contains(participantLogin))
    }


    @Test
    fun `getAllResearches should return all researches and returns OK (200)`() {
        // GIVEN
        researchRepository.saveAll(listOf(researchTestObject, researchTestObject.copy(researchCode = "testResearchCODE2")))

        // WHEN
        val response = restTemplate.getForEntity("/research/all", List::class.java)

//        val response : ResponseEntity<Array<Research>> = restTemplate.getForEntity("/research/all", Array<Research>::class.java)

        // THEN
        assertEquals(OK, response.statusCode)
        assertEquals(2, response.body?.size)

    }

    @Test
    fun `getResearchByCode should return research and returns OK`() {
        // GIVEN
        researchRepository.save(researchTestObject)

        val request = HttpEntity(null, HttpHeaders())


        val res = researchRepository.findResearchByResearchCode(testResearchCode).get()
        println(res)

        // WHEN
        val responseEntity = restTemplate.getForEntity("/research/code/${testResearchCode}", String::class.java)

        // THEN
        assertEquals(OK, responseEntity.statusCode, "response doesnt have OK status code")
        assertTrue(responseEntity.hasBody(), "response doesnt have body")
    }

    @Test
    fun `getResearchesByCreatorLogin should return researches and returns OK`() {
        // GIVEN
        researchRepository.saveAll(listOf(researchTestObject, researchTestObject.copy(researchCode = "testResearchCODE2")))

        // WHEN
        val response = restTemplate.getForEntity(
            "/research/creator/${testCreatorLogin}",
            List::class.java
        )

        // THEN
        assertEquals(OK, response.statusCode)
        assertEquals(2, response.body?.size)
    }

    @Test
    fun `getResearchesByCreatorLogin with invalid login should not return any researches and returns NO_CONTENT`() {
        // GIVEN
        val login = "invalid-login"
        // WHEN
        val response = restTemplate.getForEntity(
            "/research/creator/${login}",
            List::class.java
        )

        // THEN
        assertEquals(NO_CONTENT, response.statusCode)
        assertNull(response.body)
    }

        @Test
    fun `deleteResearchByResearchCode and returns NO_CONTENT (204)`() {
        // GIVEN
        researchRepository.save(researchTestObject)
        userRepository.save(testResearchCreator)
        val validToken = refreshTokenService.createAccessToken(testCreatorLogin)

        // WHEN
        val headers = HttpHeaders()
        headers.add("Authorization", validToken)

        val request = HttpEntity(null, headers)

        val response = restTemplate.exchange("/research/$testResearchCode/delete", HttpMethod.DELETE, request, String::class.java)

        // THEN
        assertEquals(NO_CONTENT, response.statusCode)
        assertTrue(researchRepository.findResearchByResearchCode(testResearchCode).isEmpty)
    }
}