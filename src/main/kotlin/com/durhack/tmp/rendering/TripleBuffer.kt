package com.durhack.tmp.rendering

import com.durhack.tmp.CachePoint
import java.util.concurrent.atomic.AtomicInteger

class TripleBuffer(val size: Int) {
    private val buffers = List(3) { Array(this.size) { CachePoint(-1, 0.0) } }

    private var writeIndex = 0
    private var readIndex = 1

    private val shared = AtomicInteger(2)

    val writeBuffer: Array<CachePoint> get() = buffers[writeIndex]

    fun publish() {
        writeIndex = shared.getAndSet(writeIndex or FRESH) and INDEX_MASK
    }

    fun latest(): Array<CachePoint> {
        if (shared.get() and FRESH != 0) {
            readIndex = shared.getAndSet(readIndex) and INDEX_MASK
        }
        return buffers[readIndex]
    }

    private companion object {
        const val INDEX_MASK = 0b011
        const val FRESH = 0b100
    }
}