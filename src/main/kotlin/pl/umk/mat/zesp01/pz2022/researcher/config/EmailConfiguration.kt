package pl.umk.mat.zesp01.pz2022.researcher.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import pl.umk.mat.zesp01.pz2022.researcher.service.*

@Configuration
class EmailConfiguration {

	@Bean
	@Profile("!integration")
	fun configureEmailProd() {
		MAIL = System.getenv("MAIL")
		MAIL_PWD = System.getenv("MAIL_PASSWORD")
		FRONT_URL = System.getenv("FRONT_URL")
	}

	@Bean
	@Profile("integration")
	fun configureEmailTest() {
		MAIL = ""
		MAIL_PWD = ""
		FRONT_URL = ""
	}

//	@Profile("!integration")
//	@Bean
//	fun getJavaMailSender(): JavaMailSender {
//		val mailSender = JavaMailSenderImpl()
//		mailSender.host = "smtp.gmail.com"
//		mailSender.port = 587
//
//		mailSender.username = MAIL
//		mailSender.password = MAIL_PWD
//
//		val properties = mailSender.javaMailProperties
//		properties["mail.transport.protocol"] = "smtp"
//		properties["mail.smtp.auth"] = "true"
//		properties["mail.smtp.starttls.enable"] = "true"
//		properties["mail.debug"] = "true"
//
//		return mailSender
//	}
}