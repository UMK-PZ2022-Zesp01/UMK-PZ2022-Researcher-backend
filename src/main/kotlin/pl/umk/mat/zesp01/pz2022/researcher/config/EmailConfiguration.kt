package pl.umk.mat.zesp01.pz2022.researcher.config

import org.springframework.boot.autoconfigure.AutoConfigureOrder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import pl.umk.mat.zesp01.pz2022.researcher.service.*

@Configuration
class EmailConfiguration {

    @Profile("!integration")
    @Bean
    fun mailConfigureProd(): JavaMailSender {
        MAIL = System.getenv("MAIL")
        MAIL_PWD = System.getenv("MAIL_PASSWORD")
        FRONT_URL = System.getenv("FRONT_URL")

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

    @Profile("integration")
    @Bean
    fun mailConfigureTests(): JavaMailSender {
        MAIL = ""
        MAIL_PWD = ""
        FRONT_URL= "localhost:8080/"

        val mailSender = JavaMailSenderImpl()
        mailSender.host ="127.0.0.1"
        mailSender.port = 3025

        mailSender.username = "username"
        mailSender.password = "secret"

        val properties = mailSender.javaMailProperties
        properties["mail.transport.protocol"] = "smtp"
        properties["mail.smtp.auth"] = "false"
        properties["mail.smtp.starttls.enable"] = "false"
        properties["mail.debug"] = "true"

        return mailSender
    }


}