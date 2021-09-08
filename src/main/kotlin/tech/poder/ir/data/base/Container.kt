package tech.poder.ir.data.base

import tech.poder.ir.data.base.unlinked.UnlinkedContainer

interface Container {
    companion object {
        fun newContainer(name: String): UnlinkedContainer {
            return UnlinkedContainer(name)
        }
    }
}