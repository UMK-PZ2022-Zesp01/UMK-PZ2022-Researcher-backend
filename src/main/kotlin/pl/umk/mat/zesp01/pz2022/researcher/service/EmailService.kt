package pl.umk.mat.zesp01.pz2022.researcher.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.mail.SimpleMailMessage

import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import pl.umk.mat.zesp01.pz2022.researcher.model.User

lateinit var MAIL: String
lateinit var MAIL_PWD: String
lateinit var FRONT_URL: String



//@Service
//class EmailService(@Autowired val emailSender: JavaMailSender){
//    fun sendVerificationEmail(to:String){
//
//        val uri = VEFIFICATION_URI_PREFIX
//        val message = SimpleMailMessage()
//        message.setTo(to)
//        message.subject = "Confirm your email address"
//        message.text =
//            "Click the link below to activate your account\n $uri"
//
//        emailSender.send(message)
//    }
//}

class OnRegistrationCompleteEvent(
    val user: User
) : ApplicationEvent(user)


@Configuration
@EnableAsync
class RegistrationListener(
    @Autowired val tokenService: VerificationTokenService,
    @Autowired val javaMailSender: JavaMailSender
) : ApplicationListener<OnRegistrationCompleteEvent> {
    @Async
    override fun onApplicationEvent(event: OnRegistrationCompleteEvent) {
        this.sendConfirmationEmail(event)
    }

    fun sendConfirmationEmail(event:OnRegistrationCompleteEvent){
        val user = event.user
        val recipientAddress = user.email
        val subject = "Researcher | Potwierdzenie rejestracji"

        val token = tokenService.createToken(user.login)
        val confirmationUrl = "${FRONT_URL}confirmEmail/$token"
        val message = "Naciśnij link poniżej aby aktywować konto Researcher.\r\n$confirmationUrl"

        val mail = SimpleMailMessage()
        mail.setTo(recipientAddress)
//        mail.from = "noreply@researcher.pz2022.gmail.com"
        mail.subject=subject
        mail.text=message
        javaMailSender.send(mail)
    }
}
