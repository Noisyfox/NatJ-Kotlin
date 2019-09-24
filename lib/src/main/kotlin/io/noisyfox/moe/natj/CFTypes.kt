package io.noisyfox.moe.natj

import apple.corefoundation.c.CoreFoundation
import apple.corefoundation.opaque.CFStringRef
import apple.foundation.NSString
import org.moe.natj.c.CRuntime
import org.moe.natj.c.OpaquePtr
import org.moe.natj.general.ptr.ConstVoidPtr
import org.moe.natj.objc.ObjCRuntime
import java.util.concurrent.atomic.AtomicLong

/** Release the given Core Foundation object after [action] returns. */
inline fun <T : OpaquePtr, R> T.use(action: (T) -> R): R = try {
    action(this)
} finally {
    release()
}

/** Retains the given Core Foundation object. */
fun <T : OpaquePtr> T.retain(): T = apply { CoreFoundation.CFRetain(this) }

/** Release the given Core Foundation object at the end of a autorelease block. */
fun <T : OpaquePtr> T.autorelease(): T = apply { ObjCRuntime.autoreleaseObject(peer.peer) }

/** Release the given Core Foundation object. */
fun OpaquePtr.release() = CoreFoundation.CFRelease(this)

/** Cast given void ptr to a more specific [OpaquePtr]. */
inline fun <reified T : OpaquePtr> ConstVoidPtr.cast(): T = CRuntime.cast(this, T::class.java)

/**
 * A utility class for using NSAutoreleasePools.
 *
 * The difference to [org.moe.natj.objc.ObjCAutoreleasePool] is this one doesn't raise any
 * exception in [close] so it can be used in Java without the need of any catch clause:
 *
 * try (ObjCAutoreleasePool pool = new ObjCAutoreleasePool()) {
 *    // Use any auto release you want
 * }
 */
class ObjCAutoreleasePool : AutoCloseable {
    /**
     * Peer of the autorelease pool.
     */
    private val peer: AtomicLong = AtomicLong(ObjCRuntime.createAutoreleasePool())

    /**
     * Releases all objects int this pool.
     */
    fun release() {
        val peer = this.peer.getAndSet(0)
        if (peer == 0L) {
            throw RuntimeException("pool was already released")
        }
        ObjCRuntime.releaseAutoreleasePool(peer)
    }

    override fun close() {
        release()
    }
}

/**
 * Run the [action] inside an AutoreleasePool.
 *
 * You don't need this for managing any NSType objects since they will be handled by
 * Java's GC. However for a CFType instance you have to manage the memory manually where
 * the autorelease pool could be useful.
 */
inline fun <R> autoreleasepool(action: () -> R): R = ObjCAutoreleasePool().use { action() }

/** Convert a [CFStringRef] to java [String]. */
fun CFStringRef.toJavaString(): String = this.bridge<NSString>().toString()
