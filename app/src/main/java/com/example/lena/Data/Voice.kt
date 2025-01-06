package com.example.lena.Data

data class Voice(val name: String, val displayName: String)

object Voices {
    val allVoices = listOf(
        Voice(name = "wit\$cockney", displayName = "Cockney Accent"),
        Voice(name = "wit\$BritishButler", displayName = "British Butler"),
        Voice(name = "wit\$CartoonVillain", displayName = "Cartoon Villain"),
        Voice(name = "wit\$KenyanAccent", displayName = "Kenyan Accent"),
        Voice(name = "wit\$Rubie", displayName = "Rubie"),
        Voice(name = "wit\$Vampire", displayName = "Vampire"),
        Voice(name = "wit\$Cooper", displayName = "Cooper")
    )
}
