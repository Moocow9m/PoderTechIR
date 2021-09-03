package tech.poder.ir.parsing.windows

import tech.poder.ir.parsing.generic.Arch

enum class CoffMachine(val id: Short, val arch: Arch) {
    MACHINE_UNKNOWN(0, Arch.UNKNOWN),
    MACHINE_AMD64(0x8664.toShort(), Arch.AMD64),
    MACHINE_I386(0x14c, Arch.I32),
    MACHINE_AM33(0x1d3, Arch.UNKNOWN),
    MACHINE_ARM(0x1c0, Arch.ARM),
    MACHINE_ARM64(0xaa64.toShort(), Arch.ARM64),
    MACHINE_ARMNT(0x1c4, Arch.UNKNOWN),
    MACHINE_EBC(0xebc, Arch.UNKNOWN),
    MACHINE_IA64(0x200, Arch.UNKNOWN),
    MACHINE_M32R(0x0941, Arch.UNKNOWN),
    MACHINE_MIPS16(0x266, Arch.UNKNOWN),
    MACHINE_MIPSFPU(0x366, Arch.UNKNOWN),
    MACHINE_MIPSFPU16(0x466, Arch.UNKNOWN),
    MACHINE_POWERPC(0x1f0, Arch.UNKNOWN),
    MACHINE_POWERPCFP(0x1f1, Arch.UNKNOWN),
    MACHINE_R4000(0x166, Arch.UNKNOWN),
    MACHINE_RISCV32(0x5032, Arch.UNKNOWN),
    MACHINE_RISCV64(0x5064, Arch.UNKNOWN),
    MACHINE_RISCV128(0x5128, Arch.UNKNOWN),
    MACHINE_SH3(0x1a2, Arch.UNKNOWN),
    MACHINE_SH3DSP(0x1a3, Arch.UNKNOWN),
    MACHINE_SH4(0x1a6, Arch.UNKNOWN),
    MACHINE_SH5(0x1a8, Arch.UNKNOWN),
    MACHINE_THUMB(0x1c2, Arch.UNKNOWN),
    MACHINE_WCEMIPSV2(0x169, Arch.UNKNOWN),
}