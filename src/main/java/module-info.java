module PoderTechIR.main {
	requires jdk.incubator.foreign;
	requires kotlin.stdlib.jdk8;
	requires kotlin.stdlib.jdk7;

	exports tech.poder.ir.commands;
	exports tech.poder.ir.api;
	exports tech.poder.ir.data.base;
	exports tech.poder.ir.data;
	exports tech.poder.ir.data.base.linked;
	exports tech.poder.ir.data.base.unlinked;
	exports tech.poder.ir.data.base.api;
	exports tech.poder.ir.std;
	exports tech.poder.ir.util;
	exports tech.poder.ir.v2.api;
}