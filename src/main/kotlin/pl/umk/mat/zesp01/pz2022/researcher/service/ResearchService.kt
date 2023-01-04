package pl.umk.mat.zesp01.pz2022.researcher.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import pl.umk.mat.zesp01.pz2022.researcher.model.Research
import pl.umk.mat.zesp01.pz2022.researcher.model.User
import pl.umk.mat.zesp01.pz2022.researcher.repository.ResearchRepository
import pl.umk.mat.zesp01.pz2022.researcher.repository.UserRepository

@Service
class ResearchService(@Autowired val researchRepository: ResearchRepository) {
    fun addResearch(research: Research): Research = researchRepository.insert(research)
    fun getAll(): List<Research> = researchRepository.findAll()
    fun getResearchById(id: String): Research = researchRepository.findById(id).orElseThrow { throw RuntimeException("Cannot find User by Id") }
}