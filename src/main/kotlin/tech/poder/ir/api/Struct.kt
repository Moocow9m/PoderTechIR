package tech.poder.ir.api

import tech.poder.ptir.PTIR

@JvmInline
value class Struct(val types: Array<PTIR.Type>)
