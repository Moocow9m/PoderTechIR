package tech.poder.ir.parsing.machine.common

interface Code {
	val prefixes: List<Code>
	val main: Code
	val args: List<Code>
}