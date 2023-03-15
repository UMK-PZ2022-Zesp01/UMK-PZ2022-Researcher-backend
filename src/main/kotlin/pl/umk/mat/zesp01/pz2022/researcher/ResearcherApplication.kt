package pl.umk.mat.zesp01.pz2022.researcher

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import pl.umk.mat.zesp01.pz2022.researcher.idgenerator.IdGenerator

@SpringBootApplication
class ResearcherApplication

fun main(args: Array<String>) {
	runApplication<ResearcherApplication>(*args)
}
