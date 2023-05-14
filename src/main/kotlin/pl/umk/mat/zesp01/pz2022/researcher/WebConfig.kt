package pl.umk.mat.zesp01.pz2022.researcher

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableWebMvc
class WebConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins(
//                "https://justresearch.netlify.app"
                "http://localhost:3000"
            )
            .allowedMethods("*")
            .allowCredentials(true)
    }
}