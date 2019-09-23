@file:Suppress("UNCHECKED_CAST")

package io.noisyfox.moe.natj

import apple.foundation.c.Foundation
import org.moe.natj.c.CRuntime
import org.moe.natj.c.OpaquePtr
import org.moe.natj.objc.ObjCObject
import org.moe.natj.objc.ObjCRuntime

/** __bridge transfers a pointer between Objective-C and Core Foundation with no transfer of ownership. */
fun <T : OpaquePtr> bridge(obj: ObjCObject, type: Class<T>): T = ObjCRuntime.cast(obj, type)

/** __bridge transfers a pointer between Objective-C and Core Foundation with no transfer of ownership. */
inline fun <reified T : OpaquePtr> ObjCObject.bridge(): T = bridge(this, T::class.java)

/**
 * __bridge transfers a pointer between Objective-C and Core Foundation with no transfer of ownership.
 *
 * You still need to release the original CFType instance if it's owned by you afterwards.
 */
fun <T : ObjCObject> bridge(ptr: OpaquePtr, type: Class<T>): T = ObjCRuntime.cast(ptr, type)

/**
 * __bridge transfers a pointer between Objective-C and Core Foundation with no transfer of ownership.
 *
 * You still need to release the original CFType instance if it's owned by you afterwards.
 */
inline fun <reified T : ObjCObject> OpaquePtr.bridge(): T = bridge(this, T::class.java)

/**
 * __bridge_retained or CFBridgingRetain casts an Objective-C pointer to a Core Foundation pointer and
 * also transfers ownership to you.
 * You are responsible for calling CFRelease or a related function to relinquish ownership of the object.
 */
fun <T : OpaquePtr> bridgeRetained(obj: ObjCObject, type: Class<T>): T = CRuntime.cast(Foundation.CFBridgingRetain(obj), type)

/**
 * __bridge_retained or CFBridgingRetain casts an Objective-C pointer to a Core Foundation pointer and
 * also transfers ownership to you.
 * You are responsible for calling CFRelease or a related function to relinquish ownership of the object.
 */
inline fun <reified T : OpaquePtr> ObjCObject.bridgeRetained(): T = bridgeRetained(this, T::class.java)

/**
 * __bridge_transfer or CFBridgingRelease moves a non-Objective-C pointer to Objective-C and also transfers
 * ownership to ARC.
 * ARC is responsible for relinquishing ownership of the object.
 */
fun <T : ObjCObject> bridgeTransfer(ptr: OpaquePtr, type: Class<T>): T = ObjCRuntime.cast(Foundation.CFBridgingRelease(ptr), type)

/**
 * __bridge_transfer or CFBridgingRelease moves a non-Objective-C pointer to Objective-C and also transfers
 * ownership to ARC.
 * ARC is responsible for relinquishing ownership of the object.
 */
inline fun <reified T : ObjCObject> OpaquePtr.bridgeTransfer(): T = bridgeTransfer(this, T::class.java)
