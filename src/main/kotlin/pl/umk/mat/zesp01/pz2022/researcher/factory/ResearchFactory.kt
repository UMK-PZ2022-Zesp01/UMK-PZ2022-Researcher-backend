package pl.umk.mat.zesp01.pz2022.researcher.factory

import pl.umk.mat.zesp01.pz2022.researcher.model.Research

class ResearchFactory(
    val oldResearch: Research,
    val newResearch: Research
){
    fun updateResearch(){
        this.newResearch.id = this.oldResearch.id
        // TODO
    }
}