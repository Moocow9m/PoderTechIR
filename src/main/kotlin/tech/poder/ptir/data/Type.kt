package tech.poder.ptir.data

import tech.poder.ptir.data.storage.NamedType

sealed interface Type {

    object Unit : Type //Users cannot use this type on stack! Return type only!

    @JvmInline
    value class RuntimeArray(val type: Type) : Type //Array with size determined on runtime

    data class Array(val type: Type, val size: Int) : Type //Array with known size

    @JvmInline
    value class ConstArray(val data: List<Any>) :
        Type //Array with all data known (still technically mutable... need a method of securing immutability so copies are not created)

    data class Struct(val name: String, val types: List<NamedType>) : Type


    // Don't make these data classes unless if you want pain
    sealed interface Primitive : Type {

        sealed interface Numeric : Primitive {

            sealed interface Basic : Numeric {
                object Byte : Basic

                object Short : Basic

                object Int : Basic

                object Long : Basic
            }

            sealed interface FloatingPoint : Numeric {

                object Float : FloatingPoint

                object Double : FloatingPoint
            }
        }

        object String : Primitive

    }

}