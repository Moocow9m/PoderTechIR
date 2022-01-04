package tech.poder.ir.api

import kotlin.reflect.KClass

object TranslationHolder {

	private val codex = mutableMapOf<KClass<out CodeHolder>, MutableMap<KClass<out CodeHolder>, Translator<*, *>>>()


	fun register(translator: Translator<*, *>) {
		codex.getOrPut(translator.iClass) { mutableMapOf() }[translator.oClass] = translator
	}

	fun get(input: KClass<out CodeHolder>, output: KClass<out CodeHolder>): Translator<*, *>? {
		return codex[input]?.get(output)
	}


	fun translate(from: KClass<CodeHolder>, to: KClass<CodeHolder>): CodeHolder {
		return checkNotNull(codex[from]?.get(to)) {
			"Could not find translator for ${from.simpleName}"
		}::translate.call(to)
	}

}