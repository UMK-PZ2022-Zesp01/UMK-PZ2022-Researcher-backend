package pl.umk.mat.zesp01.pz2022.researcher.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.net.URL
import java.util.Date

@Document("Users")
class User {
    @Id
    var id: String = ""
    var login: String = ""
    var password: String = ""
    var firstName: String = ""
    var lastName: String = ""
    var email: String = ""
    var phone: String = ""
    var birthDate: String = ""
    var gender: String = ""
    var avatarImage: String = ""
}