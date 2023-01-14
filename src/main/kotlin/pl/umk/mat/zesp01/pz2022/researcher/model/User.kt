package pl.umk.mat.zesp01.pz2022.researcher.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.util.StringJoiner

@Document("Users")
class User {
    @Id var id: String = ""
    @Field var login: String = ""
    @Field var password: String = ""
    @Field var firstName: String = ""
    @Field var lastName: String = ""
    @Field var email: String = ""
    @Field var phone: String = ""
    @Field var birthDate: String = ""
    @Field var gender: String = ""
    @Field var avatarImage: String = ""
}

class LoginData{
    var login: String = ""
    var password: String =""
}