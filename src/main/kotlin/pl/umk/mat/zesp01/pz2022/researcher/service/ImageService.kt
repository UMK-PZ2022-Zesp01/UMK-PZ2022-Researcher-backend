package pl.umk.mat.zesp01.pz2022.researcher.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.stereotype.Service
import pl.umk.mat.zesp01.pz2022.researcher.idgenerator.IdGenerator
import pl.umk.mat.zesp01.pz2022.researcher.model.Image
import pl.umk.mat.zesp01.pz2022.researcher.repository.ImageRepository

@Service
class ImageService(
    @Autowired val imageRepository: ImageRepository,
    @Autowired val mongoOperations: MongoOperations
) {
    fun addImage(image: Image): Image{
        image.id = IdGenerator().generatePhotoId(getAllPhotoIds())
        return imageRepository.insert(image)
    }

    fun getImage(id: String): Image =
        imageRepository.findById(id).orElseThrow { throw RuntimeException("Cannot find Image by 'id'") }

    fun getAllPhotoIds(): List<String> =
        mongoOperations.aggregate(
            Aggregation.newAggregation(
                Aggregation.project("_id")
            ),
            "Images", String::class.java
        ).mappedResults
}