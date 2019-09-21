package io.noisyfox.moe.natj

import apple.corefoundation.c.CoreFoundation
import apple.corefoundation.opaque.CFStringRef
import apple.foundation.NSString
import org.moe.natj.c.OpaquePtr
import org.moe.natj.objc.ObjCAutoreleasePool

/** Release the given Core Foundation object after [action] returns. */
inline fun <T : OpaquePtr, R> T.use(action: (T) -> R): R = try {
    action(this)
} finally {
    release()
}

/** Retains the given Core Foundation object. */
fun <T : OpaquePtr> T.retain(): T = apply { CoreFoundation.CFRetain(this) }

/** Release the given Core Foundation object at the end of a autorelease block. */
fun <T : OpaquePtr> T.autorelease(): T = apply { CoreFoundation.CFAutorelease(this) }

/** Release the given Core Foundation object. */
fun OpaquePtr.release() = CoreFoundation.CFRelease(this)

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
