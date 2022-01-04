package tech.poder.ir.api

import tech.poder.ir.data.storage.segment.Segment

interface Optimizer { //TODO add method and object visit methods... maybe

	fun visitSegment(segment: Segment)
}