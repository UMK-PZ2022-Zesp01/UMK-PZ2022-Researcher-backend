package pl.umk.mat.zesp01.pz2022.researcher.codegenerator

class CodeGenerator {
	companion object {
		fun generateResearchCode(): String {
			val codeLetters = listOf(('0'..'9'), ('A'..'Z'), ('a'..'z')).flatten()
			var researchCode = ""

			for(i in 1..8) {
				researchCode += codeLetters.random()
			}

			return researchCode
		}
	}
}