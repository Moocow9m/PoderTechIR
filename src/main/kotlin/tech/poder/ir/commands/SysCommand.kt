package tech.poder.ir.commands

import tech.poder.ir.data.Type

enum class SysCommand(val return_: Type = Type.Unit, val args: List<Type> = emptyList()) {
	PRINT(args = listOf(Type.Primitive.CharBased.String)),
	YIELD,
	SUSPEND(args = listOf(Type.Primitive.Numeric.Basic.Long)),
	LOAD_LIB(Type.Primitive.Numeric.Basic.Byte, listOf(Type.Primitive.CharBased.String)), //boolean returned as byte
}