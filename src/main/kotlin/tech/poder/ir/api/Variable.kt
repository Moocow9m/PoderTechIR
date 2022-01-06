package tech.poder.ir.api

import tech.poder.ptir.PTIR

@JvmInline
value class Variable private constructor(internal val id: List<Any>) {
	companion object {
		private var last = 1u
		internal val VOID = Variable(listOf(false, 0u)).toPTIR()
		val ARGS = Variable(listOf(true, 0u))

		internal fun newLocal(builder: MethodBuilder): Variable {
			return Variable(listOf(true, builder.varId++))
		}

		internal fun resetEnv() {
			last = 1u
		}

		internal fun presetEnv(variable: UInt) {
			last = variable
		}

		internal fun lastGlobalId(): UInt {
			return last - 1u
		}

		internal fun newGlobal(): Variable {
			return Variable(listOf(false, last++))
		}
	}


	fun toPTIR(): PTIR.Variable {
		return PTIR.Variable(id[0] as Boolean, id[1] as UInt)
	}
}
