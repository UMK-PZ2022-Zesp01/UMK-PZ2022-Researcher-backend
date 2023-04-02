package pl.umk.mat.zesp01.pz2022.researcher.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document("VerificationTokens")
class VerificationToken(
	@Id val id: ObjectId = ObjectId(),
	@Field val login: String = "",
	@Field val expires: String = "",
	@Field val jwt: String = ""
)