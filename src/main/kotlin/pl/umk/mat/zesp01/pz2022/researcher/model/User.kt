package pl.umk.mat.zesp01.pz2022.researcher.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document("Users")
class User(
        @Field val login: String,
        @Field var password: String,
        @Field val firstName: String,
        @Field val lastName: String,
        @Field val email: String,
        @Field val birthDate: String,
        @Field val gender: Gender,
        /* @Field val avatarImage: URL */
) {
    @Id var id: String = ""
}