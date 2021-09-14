package tech.poder.ir.vm

enum class CPUTemplate(val bits: Bits, val registers: List<RegisterType>) {
    STACK32(Bits.BIT_32, emptyList()), //don't do register optimization! But do fun long math XD
    STACK(Bits.BIT_64, emptyList()), //don't do register optimization!
    AMD64(Bits.BIT_64, listOf(RegisterType(64, 16..16, RegisterData.BASIC))),
    AMD64_FP(
        Bits.BIT_64,
        listOf(RegisterType(64, 16..16, RegisterData.BASIC), RegisterType(128, 16..16, RegisterData.FLOATING_POINT))
    ),
    ARM(Bits.BIT_32, listOf(RegisterType(32, 15..15, RegisterData.BASIC))),
    ARM_FP(
        Bits.BIT_32,
        listOf(RegisterType(32, 15..15, RegisterData.BASIC), RegisterType(64, 0..32, RegisterData.FLOATING_POINT))
    ),
    AARCH64(
        Bits.BIT_64,
        listOf(RegisterType(64, 31..31, RegisterData.BASIC), RegisterType(128, 32..32, RegisterData.FLOATING_POINT))
    )
}