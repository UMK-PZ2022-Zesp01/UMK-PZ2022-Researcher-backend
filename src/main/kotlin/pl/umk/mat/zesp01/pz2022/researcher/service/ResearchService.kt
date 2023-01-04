package pl.umk.mat.zesp01.pz2022.researcher.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import pl.umk.mat.zesp01.pz2022.researcher.model.Research
import pl.umk.mat.zesp01.pz2022.researcher.repository.ResearchRepository

@Service
class ResearchService(@Autowired val researchRepository: ResearchRepository) {
    /* ___________________________________ADD METHODS___________________________________*/
    fun addResearch(research: Research): Research = researchRepository.insert(research)

    /* ___________________________________DELETE METHODS___________________________________*/
    fun deleteResearchById(id: String) = researchRepository.deleteById(id)

    /* ___________________________________GET METHODS___________________________________*/
    fun getAllResearches(): List<Research> = researchRepository.findAll()
    fun getResearchById(id: String): Research =
        researchRepository.findById(id).orElseThrow { throw RuntimeException("Cannot find User by Id") }

    fun getResearchesByUserId(userId: String): List<Research> =
        researchRepository.findResearchesById(userId).orElseThrow { throw RuntimeException("Cannot find User by Id") }
}