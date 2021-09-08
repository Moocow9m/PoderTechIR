package tech.poder.ir.data.base.linked

import tech.poder.ir.data.base.Container

data class LinkedContainer(val name: String, val entryPoint: UInt?, val packages: List<LinkedPackage>) : Container
