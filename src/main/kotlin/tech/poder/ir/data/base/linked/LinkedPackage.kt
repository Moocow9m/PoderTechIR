package tech.poder.ir.data.base.linked

import tech.poder.ir.data.base.Method
import tech.poder.ir.data.base.Object
import tech.poder.ir.data.base.Package

data class LinkedPackage(val methods: List<Method>, val objects: List<Object>) : Package
