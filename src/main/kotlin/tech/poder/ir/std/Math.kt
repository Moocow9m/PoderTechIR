package tech.poder.ir.std

object Math {
    /*val mathLib = Portable()

    val mathPackage = mathLib.newPackage("std", Visibility.PUBLIC)

    //Complexity is O(log(n))
    val pow = mathPackage.newFloatingMethod(
        "pow",
        Visibility.PUBLIC,
        Type.Primitive.Numeric.Basic::class,
        setOf(
            NamedType("input", Type.Primitive.Numeric.Basic::class),
            NamedType("powerOf", Type.Primitive.Numeric.Basic::class)
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
        it.dup()
        it.mul()
        it.setVar("pow")
        it.jmp(start)
        it.placeLabel(end)
        it.getVar("pow")
    }*/
}