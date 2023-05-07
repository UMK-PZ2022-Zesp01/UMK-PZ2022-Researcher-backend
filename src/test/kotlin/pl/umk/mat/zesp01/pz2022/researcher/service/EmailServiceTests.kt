package pl.umk.mat.zesp01.pz2022.researcher.service

import com.icegreen.greenmail.configuration.GreenMailConfiguration
import com.icegreen.greenmail.junit5.GreenMailExtension
import com.icegreen.greenmail.util.GreenMailUtil
import com.icegreen.greenmail.util.ServerSetupTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.context.ActiveProfiles
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository


@ActiveProfiles("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EmailServiceTests {
    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var eventPublisher: ApplicationEventPublisher

    @Test
    fun `send verification email when user profile is not confirmed`() {
        // GIVEN
        val userTestObject = User(
            login = "testLOGIN",
            password = "testPASSWORD",
            firstName = "testFIRSTNAME",
            lastName = "testLASTNAME",
            email = "testEMAIL@test.com",
            phone = "123456789",
            birthDate = "01-01-1970",
            gender = "Male",
//            avatarImage = "testAVATARIMAGE.IMG",
            location = "Bydgoszcz",
            isConfirmed = false)

        userRepository.deleteAll()
        userRepository.save(userTestObject)

        // WHEN
        eventPublisher.publishEvent(OnRegistrationCompleteEvent(userTestObject))

        // THEN
        greenMail.waitForIncomingEmail(15000, 1)

        val receivedMail = greenMail.receivedMessages[0]

        assertEquals(1, receivedMail.allRecipients.size)
        assertEquals("testEMAIL@test.com", receivedMail.allRecipients[0].toString())
        assertEquals("noreply@researcher.pz2022.gmail.com", receivedMail.from[0].toString())
        assertEquals("Researcher | Potwierdzenie rejestracji", receivedMail.subject)

        assertTrue(GreenMailUtil.getBody(receivedMail).contains(FRONT_URL))
    }

    companion object {
        @JvmField
        @RegisterExtension
        var greenMail: GreenMailExtension = GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("username", "secret"))
            .withPerMethodLifecycle(true)
    }
}