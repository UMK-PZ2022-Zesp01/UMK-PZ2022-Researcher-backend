package pl.umk.mat.zesp01.pz2022.researcher.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.mail.SimpleMailMessage

import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.stereotype.Component
import pl.umk.mat.zesp01.pz2022.researcher.model.User


val MAIL : String = System.getenv("MAIL")
val MAIL_PWD : String = System.getenv("MAIL_PASSWORD")

@Bean
fun getJavaMailSender(): JavaMailSender {
    val mailSender = JavaMailSenderImpl()
    mailSender.host ="smtp.gmail.com"
    mailSender.port = 587

    mailSender.username = MAIL
    mailSender.password = MAIL_PWD

    val properties = mailSender.javaMailProperties
    properties["mail.transport.protocol"] = "smtp"
    properties["mail.smtp.auth"] = "true"
    properties["mail.smtp.starttls.enable"] = "true"
    properties["mail.debug"] = "true"

    return mailSender
}

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
    val appUrl:String,
    val user: User
) : ApplicationEvent(user)

@Component
class RegistrationListener(
    @Autowired val tokenService: VerificationTokenService,
    @Autowired val javaMailSender: JavaMailSender
) : ApplicationListener<OnRegistrationCompleteEvent> {
    override fun onApplicationEvent(event: OnRegistrationCompleteEvent) {
        this.confirmRegistration(event)
    }

    fun confirmRegistration(event:OnRegistrationCompleteEvent){
        val user = event.user
        val token = tokenService.createToken(user.login)
        val recipientAddress = user.email
        val subject = "Potwierdzenie rejestracji"

        val confirmationUrl = event.appUrl + "/user/confirm?token=" + token
        val message = "Naciśnij link poniżej aby aktywować konto Researcher.\r\n$confirmationUrl"


        val mail = SimpleMailMessage()
        mail.setTo(recipientAddress)
        mail.subject=subject
        mail.text=message
        javaMailSender.send(mail)
    }
}
