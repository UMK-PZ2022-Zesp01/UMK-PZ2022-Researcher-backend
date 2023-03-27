package pl.umk.mat.zesp01.pz2022.researcher.service

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetup
import org.assertj.core.api.Assertions.assertThat
import javax.mail.Message
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.test.context.ActiveProfiles
import pl.umk.mat.zesp01.pz2022.researcher.model.User

@ActiveProfiles("integration")
class EmailServiceTests{
    private val mailSender = JavaMailSenderImpl()
    private lateinit var greenMail: GreenMail
    private lateinit var verificationTokenService: VerificationTokenService

    @BeforeAll
    fun beforeAll(){

        println("aa")
        mailSender.host ="localhost"
        mailSender.port = 3025
        mailSender.username = "username"
        mailSender.password = "secret"

        val properties = mailSender.javaMailProperties
        properties["mail.transport.protocol"] = "smtp"
        properties["mail.smtp.auth"] = "true"
        properties["mail.smtp.starttls.enable"] = "true"
        properties["mail.debug"] = "true"

        greenMail = GreenMail(ServerSetup.SMTP)
        greenMail.start()

    }



    @AfterAll
    fun teardown() {
        greenMail.stop()
    }


    @Test
    fun `sendConfirmationEmail should send email with confirmation link`() {
        // Given
        val user = User("testUser", "test@example.com")
        val event = OnRegistrationCompleteEvent(user)
        val expectedSubject = "Researcher | Potwierdzenie rejestracji"
        val expectedMessage = "Naciśnij link poniżej aby aktywować konto Researcher.\r\nhttp://example.com/confirmEmail/testToken"

        // When
//        registrationListener.sendConfirmationEmail(event)

//        val transport = greenMail.getSmtp().createTransport()
//        transport.connect()
//        transport.sendMessage(message, message.allRecipients)
//        transport.close()

        // Then
        val messages = greenMail.receivedMessages.toList()
        assertEquals(1, messages.size)
        assertEquals(user.email, messages[0].allRecipients[0].toString())
        assertEquals(expectedSubject, messages[0].subject)
        assertEquals(expectedMessage, messages[0].content.toString())
    }


    @Test
    fun `should send confirmation email`() {
        // given
        val user = User("John Doe", "john.doe@example.com")
        val event = OnRegistrationCompleteEvent(user)
        val listener = RegistrationListener(verificationTokenService, mailSender)

        // when
        listener.sendConfirmationEmail(event)

        // then
        val receivedMessages = greenMail.receivedMessages
        assertThat(receivedMessages).hasSize(1)

        val receivedMessage = receivedMessages[0]
        assertThat(receivedMessage.subject).isEqualTo("Researcher | Potwierdzenie rejestracji")
//        assertThat(receivedMessage.content).contains("Naciśnij link poniżej aby aktywować konto Researcher.")
//        assertThat(receivedMessage.to[0].address).isEqualTo("john.doe@example.com")
    }

}




