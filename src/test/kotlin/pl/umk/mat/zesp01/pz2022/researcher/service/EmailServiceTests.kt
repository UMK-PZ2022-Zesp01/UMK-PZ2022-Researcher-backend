package pl.umk.mat.zesp01.pz2022.researcher.service

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetup
import com.icegreen.greenmail.util.ServerSetup.PROTOCOL_SMTP
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.test.context.ActiveProfiles
import pl.umk.mat.zesp01.pz2022.researcher.model.User


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("integration")
class EmailServiceTests(
    @Autowired val verificationTokenService: VerificationTokenService
){
    private val mailSender = JavaMailSenderImpl()
    private lateinit var greenMail: GreenMail





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


        mailSender.host = "localhost"
        mailSender.port = 3025
        mailSender.username = "username"
        mailSender.password = "secret"

        val properties = mailSender.javaMailProperties
        properties["mail.transport.protocol"] = "smtp"
        properties["mail.smtp.auth"] = "true"
        properties["mail.smtp.starttls.enable"] = "true"
        properties["mail.debug"] = "false"

        greenMail = GreenMail(ServerSetup(mailSender.port, null, PROTOCOL_SMTP))
        greenMail.setUser("username", "secret")
        greenMail.start()



        // given
        val user = User(id = "123", login = "john.doe", email = "john.doe@gmail.com")
        val event = OnRegistrationCompleteEvent(user)
        val listener = RegistrationListener(verificationTokenService, mailSender)

        // when

        val t = Thread.currentThread()
        val ccl = t.contextClassLoader
        t.contextClassLoader = GreenMail::class.java.classLoader
        try {
            listener.sendConfirmationEmail(event)
        } finally {
            t.contextClassLoader = ccl
        }

        // then
        val receivedMessages = greenMail.receivedMessages
        assertThat(receivedMessages).hasSize(1)

        val receivedMessage = receivedMessages[0]
        assertThat(receivedMessage.subject).isEqualTo("Researcher | Potwierdzenie rejestracji")
//        assertThat(receivedMessage.content).contains("Naciśnij link poniżej aby aktywować konto Researcher.")
//        assertThat(receivedMessage.to[0].address).isEqualTo("john.doe@example.com")

        greenMail.stop()
    }


}




