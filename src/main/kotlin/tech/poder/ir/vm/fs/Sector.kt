package tech.poder.ir.vm.fs

@JvmInline
value class Sector(val size: Long) {
	init {
		check(size > 0) {
			"$size < 1!"
		}
	}
}
