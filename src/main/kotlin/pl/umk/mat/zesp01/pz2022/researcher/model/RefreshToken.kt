package pl.umk.mat.zesp01.pz2022.researcher.model

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document ("RefreshTokens")
class RefreshToken (
    @Field val expires: String = "",
    @Field val jwt: String = "",
)