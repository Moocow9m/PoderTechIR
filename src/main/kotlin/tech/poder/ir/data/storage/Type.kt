package tech.poder.ir.data.storage

import kotlin.reflect.full.createInstance

sealed interface Type {

    fun defaultValue(): Any

    object Unit : Type {
        override fun defaultValue(): Any {
            return this
        }
    }

    data class Array(val type: Type, val size: Int) : Type {
        override fun defaultValue(): Any {
            return Array(size) { type.defaultValue() }
        }
    }

    data class Struct(val name: String, val types: List<NamedType>) : Type {
        override fun defaultValue(): Any {
            return Array(types.size) { types[it].type.createInstance().defaultValue() }
        }
    }


    // Don't make these data classes unless if you want pain
    sealed interface Primitive : Type {

        sealed interface Numeric : Primitive {

            sealed interface Basic : Numeric {
                object Byte : Basic {
                    override fun defaultValue(): Any {
                        return 0.toByte()
                    }
                }

                object Short : Basic {
                    override fun defaultValue(): Any {
                        return 0.toShort()
                    }
                }

                object Int : Basic {
                    override fun defaultValue(): Any {
                        return 0
                    }
                }

                object Long : Basic {
                    override fun defaultValue(): Any {
                        return 0.toLong()
                    }
                }
            }

            sealed interface FloatingPoint : Numeric {

                object Float : FloatingPoint {
                    override fun defaultValue(): Any {
                        return 0.toFloat()
                    }
                }

                object Double : FloatingPoint {
                    override fun defaultValue(): Any {
                        return 0.toDouble()
                    }
                }
            }
        }

        object String : Primitive {
            override fun defaultValue(): Any {
                return ""
            }
        }

    }

}