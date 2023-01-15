package pl.umk.mat.zesp01.pz2022.researcher.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document ("Tokens")
class Token {
    @Id var id: String = ""
    @Field var login: String = ""
    @Field var expires: String = ""
    @Field var jwt: String = ""
}