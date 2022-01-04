package tech.poder.ir.vm.std

import tech.poder.ir.api.CodeFile
import tech.poder.ir.api.Variable

object Math {
	val mathLib = CodeFile("std.math")

	//Complexity is O(log(n))
	val powInt = mathLib.addMethod {
		val pow = Variable()
		setVar(pow, 1)
		val powerOf = Variable()
		val input = Variable()
		val result = Variable()
		setVar(result, 1)
		getArrayVar(Variable.ARGS, 0, input)
		getArrayVar(Variable.ARGS, 1, powerOf)
		val condition = Variable()
		ifEquals(powerOf, 0) {
			setVar(condition, false)
		}
		else_ {
			setVar(condition, true)
		}
		loop(condition) {
			val rem = Variable()
			modulo(rem, powerOf, 2)
			ifNotEquals(rem, 0) {
				subtract(powerOf, powerOf, 1)
				multiply(result, result, input)
			}
			divide(powerOf, powerOf, 2)
			multiply(input, input, input)

			ifEquals(powerOf, 0) {
				setVar(condition, false)
			}
		}
		return_(result)
	}
}