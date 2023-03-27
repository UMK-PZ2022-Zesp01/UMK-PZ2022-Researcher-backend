package pl.umk.mat.zesp01.pz2022.researcher.model

import org.bson.BsonBinarySubType
import org.bson.types.Binary
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.web.multipart.MultipartFile

@Document("Images")
class Image {
    @Id var id: String = ""
    @Field var type: String = ""
    @Field var image: Binary = Binary(ByteArray(0))
}

class ImageRequest(
    private val type: String,
    private val image: MultipartFile
) {
    fun toImage(): Image {
        val image = Image()

        image.type = this.type
        image.image = Binary(BsonBinarySubType.BINARY, this.image.bytes)

        return image
    }
}

class ImageResponse(private val type: String, private val image: Binary)
