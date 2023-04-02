package pl.umk.mat.zesp01.pz2022.researcher.service

import com.icegreen.greenmail.configuration.GreenMailConfiguration
import com.icegreen.greenmail.junit5.GreenMailExtension
import com.icegreen.greenmail.util.ServerSetupTest
import org.json.JSONException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatusCode
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.test.context.ActiveProfiles
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository


@ActiveProfiles("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class EmailControllerTest {
    @Autowired lateinit var testRestTemplate: TestRestTemplate
    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var mailSender: JavaMailSender


    @Test
    fun `send test email`(){

        val recipientAddress = "testEMAIL@test.com"
        val subject = "Researcher | Potwierdzenie rejestracji"

        val token = "testtoken"
        val confirmationUrl = "${FRONT_URL}confirmEmail/$token"
        val message = "Naciśnij link poniżej aby aktywować konto Researcher.\r\n$confirmationUrl"

        val mail = SimpleMailMessage()
        mail.setTo(recipientAddress)
        mail.from = "noreply@researcher.pz2022.gmail.com"
        mail.subject = subject
        mail.text = message

        mailSender.send(mail)



        val receivedMessage = greenMail.receivedMessages[0]
        assertEquals(1, receivedMessage.allRecipients.size)
        assertEquals(recipientAddress, receivedMessage.allRecipients[0].toString())
        assertEquals("noreply@researcher.pz2022.gmail.com", receivedMessage.from[0].toString())
        assertEquals(subject, receivedMessage.subject)
    }

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
            avatarImage = "testAVATARIMAGE.IMG",
            location = "Bydgoszcz",
            isConfirmed = false)

        userRepository.save(userTestObject)


        // WHEN
        val responseEntity = testRestTemplate.getForEntity(
            "/user/sendVerificationMail?username=${userTestObject.login}",
            String::class.java
        )


        // Assert
        assertEquals(HttpStatusCode.valueOf(201), responseEntity.statusCode)

        val isMailArrived = greenMail.waitForIncomingEmail(20000, 1)
        println(isMailArrived)

        val receivedMessage = greenMail.receivedMessages[0]
            assertEquals(1, receivedMessage.allRecipients.size)
            assertEquals("testEMAIL@test.com", receivedMessage.allRecipients[0].toString())
            assertEquals("noreply@researcher.pz2022.gmail.com", receivedMessage.from[0].toString())
            assertEquals("Researcher | Potwierdzenie rejestracji", receivedMessage.subject)
//            assertEquals("Hello this is a simple email message", GreenMailUtil.getBody(receivedMessage.content as Part))
    }

    companion object {
        @JvmField
        @RegisterExtension
        var greenMail: GreenMailExtension = GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("username", "secret"))
            .withPerMethodLifecycle(false)
    }
}