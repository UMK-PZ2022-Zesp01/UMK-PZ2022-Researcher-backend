package pl.umk.mat.zesp01.pz2022.researcher.controller

import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import pl.umk.mat.zesp01.pz2022.researcher.model.Image
import pl.umk.mat.zesp01.pz2022.researcher.model.ImageRequest
import pl.umk.mat.zesp01.pz2022.researcher.service.ImageService

@RestController
class ImageController(
    @Autowired val imageService: ImageService
) {

    @PostMapping("/photo/upload")
    fun uploadImage(@ModelAttribute imageRequest: ImageRequest): ResponseEntity<String> {
        val image = imageRequest.toImage()
        imageService.addImage(image)
        return ResponseEntity.status(HttpStatus.CREATED).body(Gson().toJson(image.id))
    }

    @GetMapping("/photos/{id}")
    fun getImageById(@PathVariable id: String): ResponseEntity<Image> =
        ResponseEntity.status(HttpStatus.OK).body(imageService.getImage(id))
}