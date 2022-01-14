module PoderTechIR.main {
	requires jdk.incubator.foreign;
	requires kotlin.stdlib.jdk8;
	requires kotlin.stdlib.jdk7;
	//requires java.net.http;

	exports tech.poder.ir.api;
	exports tech.poder.ir.util;
	exports tech.poder.ir.util.encode;
	exports tech.poder.ir.util.encode.dynamic;
	exports tech.poder.ir.util.encode.normal;
	exports tech.poder.ir.vm;
	exports tech.poder.ir.vm.std;
	exports tech.poder.proto;
	exports tech.poder.ptir;
}