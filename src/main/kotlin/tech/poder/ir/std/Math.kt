package tech.poder.ir.std

import tech.poder.ir.data.Type
import tech.poder.ir.data.base.unlinked.UnlinkedContainer
import tech.poder.ir.metadata.NamedType
import tech.poder.ir.metadata.Visibility

object Math { //todo containers will link against this lib, but a linked and optimized version will be made for runtime(exporting the std as modules)
    val mathLib = UnlinkedContainer("std.math")
    val api by lazy { mathLib.linkAndOptimize().first }

    val mathPackage = mathLib.newPackage("base", Visibility.PUBLIC)

    //Complexity is O(log(n))
    val pow = mathPackage.newFloatingMethod(
        "pow",
        Visibility.PUBLIC,
        Type.Primitive.Numeric.Basic.Long, //todo add method of allowing any numeric
        setOf(
            NamedType("input", Type.Primitive.Numeric.Basic.Long),
            NamedType("powerOf", Type.Primitive.Numeric.Basic.Long)
        )
    ) {
        val start = it.newLabel()
        val end = it.newLabel()
        it.push(1.toLong())
        it.setVar("pow")
        it.placeLabel(start)
        it.getVar("powerOf")
        it.push(0)
        it.ifNotEquals(end)
        it.getVar("powerOf")
        it.push(1)
        it.and()
        it.push(1)
        val after = it.newLabel()
        it.ifEquals(after)
        it.getVar("pow")
        it.getVar("input")
        it.mul()
        it.setVar("pow")
        it.placeLabel(after)
        it.getVar("powerOf")
        it.push(1)
        it.signedShiftRight()
        it.setVar("powerOf")
        it.getVar("pow")
        it.duplicate()
        it.mul()
        it.setVar("pow")
        it.jmp(start)
        it.placeLabel(end)
        it.getVar("pow")
        it.return_()
    }
}