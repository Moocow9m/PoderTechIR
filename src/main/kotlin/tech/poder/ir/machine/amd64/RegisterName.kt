package tech.poder.ir.machine.amd64

enum class RegisterName {
	RAX, RBX, RCX, RDX, RAX_HIGH, RBX_HIGH, RCX_HIGH, RDX_HIGH, RSI, RDI, RBP, RSP, R8, R9, R10, R11, R12, R13, R14, R15;

	companion object {
		fun realName(reg: RegisterName, size: RegisterSize): String {
			val result = when (reg) {
				RAX -> when (size) {
					RegisterSize.I8 -> "al"
					RegisterSize.I16 -> "ax"
					RegisterSize.I32 -> "eax"
					RegisterSize.I64 -> "rax"
					else -> "?"
				}
				RBX -> when (size) {
					RegisterSize.I8 -> "bl"
					RegisterSize.I16 -> "bx"
					RegisterSize.I32 -> "ebx"
					RegisterSize.I64 -> "rbx"
					else -> "?"
				}
				RCX -> when (size) {
					RegisterSize.I8 -> "cl"
					RegisterSize.I16 -> "cx"
					RegisterSize.I32 -> "ecx"
					RegisterSize.I64 -> "rcx"
					else -> "?"
				}
				RDX -> when (size) {
					RegisterSize.I8 -> "dl"
					RegisterSize.I16 -> "dx"
					RegisterSize.I32 -> "edx"
					RegisterSize.I64 -> "rdx"
					else -> "?"
				}
				RAX_HIGH -> when (size) {
					RegisterSize.I8 -> "ah"
					else -> realName(RAX, size)
				}
				RBX_HIGH -> when (size) {
					RegisterSize.I8 -> "bh"
					else -> realName(RBX, size)
				}
				RCX_HIGH -> when (size) {
					RegisterSize.I8 -> "ch"
					else -> realName(RCX, size)
				}
				RDX_HIGH -> when (size) {
					RegisterSize.I8 -> "dh"
					else -> realName(RDX, size)
				}
				RSI -> when (size) {
					RegisterSize.I8 -> "sil"
					RegisterSize.I16 -> "si"
					RegisterSize.I32 -> "esi"
					RegisterSize.I64 -> "rsi"
					else -> "?"
				}
				RDI -> when (size) {
					RegisterSize.I8 -> "dil"
					RegisterSize.I16 -> "di"
					RegisterSize.I32 -> "edi"
					RegisterSize.I64 -> "rdi"
					else -> "?"
				}
				RBP -> when (size) {
					RegisterSize.I8 -> "bpl"
					RegisterSize.I16 -> "bp"
					RegisterSize.I32 -> "ebp"
					RegisterSize.I64 -> "rbp"
					else -> "?"
				}
				RSP -> when (size) {
					RegisterSize.I8 -> "spl"
					RegisterSize.I16 -> "sp"
					RegisterSize.I32 -> "esp"
					RegisterSize.I64 -> "rsp"
					else -> "?"
				}
				R8 -> when (size) {
					RegisterSize.I8 -> "r8b"
					RegisterSize.I16 -> "r8w"
					RegisterSize.I32 -> "r8d"
					RegisterSize.I64 -> "r8"
					else -> "?"
				}
				R9 -> when (size) {
					RegisterSize.I8 -> "r9b"
					RegisterSize.I16 -> "r9w"
					RegisterSize.I32 -> "r9d"
					RegisterSize.I64 -> "r9"
					else -> "?"
				}
				R10 -> when (size) {
					RegisterSize.I8 -> "r10b"
					RegisterSize.I16 -> "r10w"
					RegisterSize.I32 -> "r10d"
					RegisterSize.I64 -> "r10"
					else -> "?"
				}
				R11 -> when (size) {
					RegisterSize.I8 -> "r11b"
					RegisterSize.I16 -> "r11w"
					RegisterSize.I32 -> "r11d"
					RegisterSize.I64 -> "r11"
					else -> "?"
				}
				R12 -> when (size) {
					RegisterSize.I8 -> "r12b"
					RegisterSize.I16 -> "r12w"
					RegisterSize.I32 -> "r12d"
					RegisterSize.I64 -> "r12"
					else -> "?"
				}
				R13 -> when (size) {
					RegisterSize.I8 -> "r13b"
					RegisterSize.I16 -> "r13w"
					RegisterSize.I32 -> "r13d"
					RegisterSize.I64 -> "r13"
					else -> "?"
				}
				R14 -> when (size) {
					RegisterSize.I8 -> "r14b"
					RegisterSize.I16 -> "r14w"
					RegisterSize.I32 -> "r14d"
					RegisterSize.I64 -> "r14"
					else -> "?"
				}
				R15 -> when (size) {
					RegisterSize.I8 -> "r15b"
					RegisterSize.I16 -> "r15w"
					RegisterSize.I32 -> "r15d"
					RegisterSize.I64 -> "r15"
					else -> "?"
				}
			}
			if (result == "?") {
				error("Unknown register name: $reg of size $size")
			}
			return result.uppercase()
		}
	}
}