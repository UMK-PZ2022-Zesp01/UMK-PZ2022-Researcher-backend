package pl.umk.mat.zesp01.pz2022.researcher.codegenerator

import org.bson.types.ObjectId

class CodeGenerator {
	private val codeLetters = listOf(('0'..'9'), ('A'..'Z'), ('a'..'z')).flatten()

	fun generateResearchCode(id: ObjectId): String {
		var researchCode = ""
		id.toString().chunked(2).forEach { digit ->
			val digitDec = Integer.parseInt(digit, 16)
			researchCode += when (digitDec) {
				in 0..247 -> codeLetters[digitDec / 4]
				in 248..251 -> 'A'
				in 252..255 -> 'a'
				else -> '0'
			}
		}
		return researchCode.substring(4)
	}
}