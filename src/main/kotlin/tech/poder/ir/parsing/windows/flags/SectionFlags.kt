package tech.poder.ir.parsing.windows.flags

enum class SectionFlags(val position: Int) {

	CNT_CODE(0x00000020),
	CNT_INITIALIZED_DATA(0x00000040),
	CNT_UNINITIALIZED_DATA(0x00000080),

	//LNK_OTHER(0x00000100),
	GPREL(0x00008000),

	//MEM_PURGEABLE(0x00020000),
	//MEM_16BIT(0x00020000),
	//MEM_LOCKED(0x00040000),
	//MEM_PRELOAD(0x00080000),
	LNK_NRELOC_OVFL(0x01000000),
	MEM_DISCARDABLE(0x02000000),
	MEM_NOT_CACHED(0x04000000),
	MEM_NOT_PAGED(0x08000000),
	MEM_SHARED(0x10000000),
	MEM_EXECUTE(0x20000000),
	MEM_READ(0x40000000),
	MEM_WRITE(0x80000000.toInt())
	;

	companion object {

		fun getFlags(flags: Int): List<SectionFlags> {
			return values().filter { it.position and flags != 0 }
		}

	}
}
