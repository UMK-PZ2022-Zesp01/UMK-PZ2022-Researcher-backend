package pl.umk.mat.zesp01.pz2022.researcher.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import pl.umk.mat.zesp01.pz2022.researcher.service.ACCESS_TOKEN_SECRET
import pl.umk.mat.zesp01.pz2022.researcher.service.REFRESH_TOKEN_SECRET
import pl.umk.mat.zesp01.pz2022.researcher.service.VERIFICATION_TOKEN_SECRET

@Configuration
class TokenConfiguration {

    @Bean
    @Profile("!integration")
    fun configureTokenProd(){
        VERIFICATION_TOKEN_SECRET = System.getenv("VERIFICATION_TOKEN_SECRET")
        ACCESS_TOKEN_SECRET = System.getenv("ACCESS_TOKEN_SECRET")
        REFRESH_TOKEN_SECRET = System.getenv("REFRESH_TOKEN_SECRET")
    }


    @Bean
    @Profile("integration")
    fun configureTokenTest(){
        VERIFICATION_TOKEN_SECRET = "beed319b8d87854c699a7ee5b7682dda42f5e85bf373d86774eb3ece227099eb87ad77e72e7a1facc2f2ebad4bef340d188fa01c2cfefd7380857e9aeff268b2"
        ACCESS_TOKEN_SECRET = "63e9521e659df476c4ab20fc35f327cb35abe3295e85f3a371d0b653ec386a3bf3006988e04d7e20b9a58c5fc3970a587c3af509823c1028b3fd3c7094653539"
        REFRESH_TOKEN_SECRET = "e1fbff5dbd892fe2edf56fab91ef92a1c6a0b93ed165da4bfcb6c7583d2d2a149bd5c4c4b564314c3107b683c4021d4e8775af8edb6256da73a64d392ac7c852"
    }
}