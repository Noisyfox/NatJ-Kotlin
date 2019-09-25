@file:Suppress("UNCHECKED_CAST")

package io.noisyfox.moe.natj

import apple.NSObject
import apple.foundation.NSArray
import apple.foundation.NSData
import apple.foundation.NSDictionary
import apple.foundation.NSMutableDictionary
import apple.foundation.NSNumber
import apple.foundation.NSString
import apple.foundation.enums.Enums.NSUTF8StringEncoding
import apple.foundation.struct.NSRange
import org.moe.natj.c.OpaquePtr
import org.moe.natj.general.ptr.impl.PtrFactory
import org.moe.natj.objc.ObjCObject

/**
 * This is equivalent to:
 *
 * val v = this.bridgeRetained()
 * try {
 *     return action(v)
 * } finally {
 *     v.release()
 * }
 *
 * Since this function keeps a reference of [this] so the Cocoa object won't be GC'd until
 * the [action] complete, hens we don't need to do an extra retain-then-release.
 *
 * However if the bridged [T] instance escaped the scope of [action] (for example returned as the result),
 * you need to retain it yourself before [action] returns:
 *
 * val bridged = someNSObject.bridgeUse<CFType>{ cfObj ->
 *     doSomething(cfObj)
 *     ...
 *     cfObj.retain() // cfObj escapes the code block
 * }
 *
 * doSomething(bridged)
 * ...
 * bridged.release() // Don't forget to release!
 */
inline fun <reified T : OpaquePtr, R> ObjCObject.bridgeUse(action: (T) -> R): R = action(this.bridge())

/** Convert the given object to a [NSObject]. */
private fun Any.toNSObject(): NSObject = when (this) {
    is NSObject -> this
    is OpaquePtr -> this.bridge()
    is Boolean -> NSNumber.numberWithBool(this)
    is Byte -> NSNumber.numberWithChar(this)
    is Short -> NSNumber.numberWithShort(this)
    is Int -> NSNumber.numberWithInt(this)
    is Long -> NSNumber.numberWithLongLong(this)
    is Float -> NSNumber.numberWithFloat(this)
    is Double -> NSNumber.numberWithDouble(this)
    is String -> this.toNSString()
    is Map<*, *> -> (this as Map<Any, Any>).toNSDictionary()
    else -> throw IllegalArgumentException("Unsupported object type: ${this.javaClass}")
}

/** Convert a Java [Map] to [NSDictionary]. */
fun <K : Any, V : Any> Map<K, V>.toNSDictionary(): NSDictionary<Any, Any> {
    val dict = NSMutableDictionary.alloc().initWithCapacity(this.size.toLong()) as NSDictionary<Any, Any>
    this.forEach { (k, v) ->
        // Key and value of a NSDictionary must all be [NSObject]s
        dict[k.toNSObject()] = v.toNSObject()
    }

    return dict
}

/** Convert a Java [Collection] to [NSDictionary]. */
fun <T : Any> Collection<T>.toNSArray(): NSArray<Any> = when {
    isEmpty() -> NSArray.array<NSObject>()
    size == 1 -> NSArray.arrayWithObject(single().toNSObject())
    else -> {
        val objects = this.map { it.toNSObject() }.toTypedArray()
        val remains = arrayOfNulls<NSObject>(objects.size) // this will add a null at the end
        System.arraycopy(objects, 1, remains, 0, objects.size - 1)

        NSArray.arrayWithObjects(objects[0], *remains)
    }
} as NSArray<Any>

/** Convert a Java [String] to [NSData] using given [encoding]. */
fun String.toNSData(encoding: Long = NSUTF8StringEncoding): NSData = NSString.stringWithString(this)
    .dataUsingEncoding(encoding)

/** Convert a Java [String] to [NSString]. */
fun String.toNSString(): NSString = NSString.stringWithString(this)

/** Convert a Java [ByteArray] to [NSData]. */
fun ByteArray.toNSData(): NSData = NSData.dataWithBytesLength(PtrFactory.newByteArray(this), size.toLong())

/** Convert a [NSData] to Java [ByteArray]. */
fun NSData.toArray(offset: Int = 0, length: Int = this.length().toInt()): ByteArray {
    require(offset >= 0)
    require(length >= 0)

    val ptr = PtrFactory.newByteArray(length)

    when (offset) {
        0 -> getBytesLength(ptr, length.toLong())
        else -> getBytesRange(ptr, NSRange(offset.toLong(), length.toLong()))
    }

    val bytes = ByteArray(length)
    ptr.copyFrom(bytes)

    return bytes
}
