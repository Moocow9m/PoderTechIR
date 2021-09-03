package tech.poder.ir.api

object GlobalSpeaker {
    private val codex = mutableMapOf<Language, MutableMap<Language, Translator>>()

    fun registerTranslator(from: Language, to: Language, translator: Translator) {
        val mapping = codex.getOrPut(from) {
            mutableMapOf()
        }
        mapping[to] = translator
    }

    fun translate(code: CodeHolder, to: Language): CodeHolder {
        return codex[code.lang]!![to]!!.translate(code)
    }
}