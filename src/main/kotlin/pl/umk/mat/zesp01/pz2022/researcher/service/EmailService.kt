package pl.umk.mat.zesp01.pz2022.researcher.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Service
import pl.umk.mat.zesp01.pz2022.researcher.model.User

lateinit var MAIL: String
lateinit var MAIL_PWD: String
lateinit var FRONT_URL: String


class OnRegistrationCompleteEvent(
    val user: User
) : ApplicationEvent(user)



@Service
@EnableAsync
class RegistrationListener : ApplicationListener<OnRegistrationCompleteEvent> {
    @Autowired
    lateinit var verificationTokenService: VerificationTokenService
    @Autowired
    lateinit var javaMailSender: JavaMailSender

    @Async
    override fun onApplicationEvent(event: OnRegistrationCompleteEvent) {
        this.sendConfirmationEmail(event)
    }



    fun sendConfirmationEmail(event: OnRegistrationCompleteEvent) {
        val user = event.user
        val recipientAddress = user.email
        val subject = "JustResearch | Potwierdzenie rejestracji"

        val token = verificationTokenService.createToken(user.login)
        val confirmationUrl = "${FRONT_URL}confirmEmail/$token"
        val message = "Użyj link poniżej aby aktywować konto JustResearch.\r\n$confirmationUrl"

        val mail = SimpleMailMessage()
        mail.setTo(recipientAddress)
        mail.from = "noreply@justresearch.pz2022.gmail.com"
        mail.subject = subject
        mail.text = message

        javaMailSender.send(mail)

    }
}

class OnPasswordResetRequestEvent(
    val user: User
) : ApplicationEvent(user)

@Service
@EnableAsync
class PasswordResetListener:ApplicationListener<OnPasswordResetRequestEvent>{
    @Autowired
    lateinit var refreshTokenService: RefreshTokenService
    @Autowired
    lateinit var javaMailSender: JavaMailSender


    @Async
    override fun onApplicationEvent(event: OnPasswordResetRequestEvent){
        this.sendPasswordResetEmail(event)
    }

    fun sendPasswordResetEmail(event: OnPasswordResetRequestEvent) {
        val user = event.user
        val recipientAddress = user.email
        val subject = "JustResearch | Odzyskiwanie hasła"

        val token = refreshTokenService.createResetToken(user.login)
        val message =
            "Użyj kodu poniżej, aby zresetować hasło\r\n\r\n$token\r\n\r\nJeżeli nie żądałeś zmiany hasła niezwłocznie poinformuj nas o tym."


        val mail = SimpleMailMessage()
        mail.setTo(recipientAddress)
        mail.from = "noreply@justresearch.pz2022.gmail.com"
        mail.subject = subject
        mail.text = message

        javaMailSender.send(mail)
    }
}
