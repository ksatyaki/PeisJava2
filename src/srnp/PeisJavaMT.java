/*
    Copyright (C) 2015 Chittaranjan Srinivas Swaminathan
    
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
    02110-1301 USA.
 */


package srnp;

import srnp.PeisJavaInterface.PeisSubscriberHandle;
import srnp.PeisJavaInterface.PeisTupleCallback;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;

/**
 * This class implements the {@link srnp.PeisJavaInterface} interface, providing static access to
 * the peiskernel functionality.  The class provides convenient multithread wrappers for the
 * peiskernel library.  It allows the programmer to access core peiskernel functionality at two
 * levels:
 * <ul>
 * <li><i>thread-safe, high-level access to peiskernel functionality</i>, e.g., initializing,
 * reading and writing tuples, and various convenience methods;</li>
 * <li><i>non-thread-safe access to low level peiskernel functionalities</i>, e.g.,
 * accessing the underlying peer-to-peer network or broadcasting messages to
 * known hosts.</i></li>
 * </ul>
 * The first level of functionality is provided through static methods of this class
 * of the form {@code peisjava_XXX(...)}.  The second level of access is provided through
 * the {@link #INTANCE} interface, and specifically through the methods {@code INTANCE.peiskmt_XXX(...)}.
 * If these latter non-thread-safe methods are invoked, they must be synchronized on the
 * {@link #peiskCriticalSection} object.
 * <br><br>
 * This class is intended to be used mostly through its high-level methods ({@code peisjava_XXX(...)}).
 * One-to-one and thread-safe access to the peiskernel is possible, although it should not be necessary
 * for most applications.  Access to the non-thread-safe methods is never necessary to harness
 * all the features of the peiskernel, but is maintained as it allows to implement a single-thread
 * peis in Java.
 *  
 * @author fpa
 *
 */

public class PeisJavaMT {
	
	private static final PeiskThread peiskThread = new PeiskThread(); 
	

	
	/**
	 * Reference to PEISKernel library instance.  Use if you need to access the
	 * peiskmt_xxx() calls directly (otherwise just invoke the static methods
	 * of PEISJava). 
	 */
	public static PeisJavaInterface INSTANCE = (PeisJavaInterface)Native.loadLibrary("srnp_wrapper",PeisJavaInterface.class);
	
	
	static private String[] PEISJAVA_DEFAULT_ARGUMENTS = new String[]{}; 
	
	
	/**
	 * Initializes peiskernel using any appropriate command line options.
	 */
	public static void peisjava_initialize(String[] args, String[] env) {
		
		if(args == null) {
			args = PEISJAVA_DEFAULT_ARGUMENTS.clone();
		}
		
		INSTANCE.peiskmt_initialize(new IntByReference(args.length), args, env);
		INSTANCE.peiskmt_setStringTuple("kernel.PeisJava.version", "0.1.2.3");
	}
	/**
	 * Stops the running peis kernel.
	 */
	public static void peisjava_shutdown() {		
		//Release the reference to the thread
		INSTANCE.peiskmt_shutdown();
	}
	
	/**
	 * Get a tuple from the local tuplespace. The owner of the tuple must be the caller.  Note: this is a non-blocking read, and the same value can be read more than once.
	 * @param owner The owner of the tuple.
	 * @param key The fully qualified key of the tuple to get.
	 * @return The tuple matching the given key.
	 */
	public static PeisTuple peisjava_getTuple(int owner, String key) {
		return INSTANCE.peiskmt_getTuple(owner, key, 0);
	}
	
	/**
	 * Get the data in a tuple in the form of a String.
	 * @param name The name of the tuple.
	 * @param owner The owner of the tuple. 
	 * @param flags Flags indicating whether call should be blocking and/or filter old values. 
	 * @return A String containing the tuple data if the tuple was found, {@code null} otherwise.
	 */
	public static String peisjava_getStringTuple(int owner, String name) {
		final PeisTuple tup = peisjava_getTuple(owner, name);
		System.out.println("HERE");
		if (tup != null && tup.getStringData() != null)
			return tup.getStringData();
		return null;
	}
	
	/**
	 * Get the data in a tuple in the form of a String, given the name of the tuple (which should be in local tuplespace).
	 * Note: this is a non-blocking read, and the same value can be read more than once.
	 * @param name The name of the tuple.
	 * @return A String containing the tuple data if the tuple was found, {@code null} otherwise. 
	 */
	public static String peisjava_getStringTuple(String name) {
		return peisjava_getStringTuple(peisjava_peisid(), name);
	}
	
	/**
	 * Get the data in a tuple in the form of a byte array.
	 * @param name The name of the tuple.
	 * @param owner The owner of the tuple.
	 * @param flags Flags indicating whether call should be blocking and/or filter old values.
	 * @return A byte array containing the tuple data if the tuple was found, {@code null} otherwise. 
	 */
	public static synchronized byte[] peisjava_getByteTuple(int owner, String name) {
		PeisTuple tup = peisjava_getTuple(owner, name);
		if (tup != null && tup.getByteData() != null)
			return tup.getByteData();
		return null;
	}
	
	/**
	 * Get the data in a tuple in the form of a byte array.  The owner of the tuple must
	 * be the caller component.
	 * @param name The name of the tuple.
	 * @param flags Flags indicating whether call should be blocking and/or filter old values.
	 * @return A byte array containing the tuple data if the tuple was found, {@code null} otherwise. 
	 */
	public static byte[] peisjava_getByteTuple(String name) {
		return peisjava_getByteTuple(peisjava_peisid(), name);
	}
	
	
	/**
	 * Initializes a meta tuple to reasonable default values.
	 * @param metaOwner The owner of the meta tuple.
	 * @param metaKey The key of the meta tuple.
	 */
	public static void peisjava_declareMetaTuple(int metaOwner, String metaKey) {
		INSTANCE.peiskmt_declareMetaTuple(metaOwner, metaKey);
	}	
	
	/**
	 * Initializes a meta tuple to reasonable default values (in local tuple space).
	 * @param metaKey The key of the meta tuple.
	 */
	public static void peisjava_declareMetaTuple(String metaKey) {
		peisjava_declareMetaTuple(peisjava_peisid(), metaKey);
	}
	
	
	/**
	 * Free a tuple (including the data field).
	 * @param tuple
	 */
	public static void peisjava_freeTuple(PeisTuple tuple) {
		INSTANCE.peiskmt_deAllocateTuple(tuple);
	}
	
	
	/**
	 * Use the meta tuple given by (metaOwner,metaKey) to find a reference to a
	 * specific tuple. Returns this referenced tuple [metaOwner,metaKey] if found.
	 * Must previously have created a metaSubscription to metaKey and metaOwner.
	 * @param metaKey The key of the meta tuple.
	 * @param metaOwner The owner of the meta tuple.
	 * @param flags Flags indicating whether call should be blocking and/or filter old values, defaults to a non-blocking read. 
	 * @return Tuple [metaOwner,metaKey] if found, {@code null} otherwise.
	 */
	public static PeisTuple peisjava_getTupleIndirectly(int metaOwner, String metaKey) {
		return INSTANCE.peiskmt_getTupleIndirectly(metaOwner, metaKey, 0);
	}

	
	/**
	 * Ascertain if the peiskernel is running.
	 * @return {@code true} after initialization until we have performed a shutdown.
	 */
	public static boolean peisjava_isRunning() {
		return INSTANCE.peiskmt_isRunning();
	}
	
	/**
	 * Get the id of this peiskernel.
	 * @return The id of this peiskernel.
	 */
	public static int peisjava_peisid() {
		return INSTANCE.peiskmt_peisid();
	}
	
	
	
	/**
	 * Register a callback on a tuple.  The callback routine must be implemented in
	 * the method {@link CallbackObject#callback(PeisTuple tuple)} of a class which extends
	 * the abstract class {@link CallbackObject}. 
	 * @param key The key of the tuple to register the callback to.
	 * @param owner The owner of the tuple to register the callback to.
	 * @param co A class extending the {@link CallbackObject} abstract class.
	 * @return The {@link CallbackObject} if the a callback object was successfully instantiated
	 * and queued for registration, {@code null} otherwise. 
	 */
	public static double peisjava_registerTupleCallback(int owner, String key, CallbackObject co) {
		return peiskThread.registerCallback(key, owner, co);
	}
	
	
	/**
	 * Unregister a tuple callback given the {@link PeisJavaInterface.PeisTupleCallback} object returned
	 * by {@link PeisJavaMT#peisjava_registerTupleCallback(String, int, String)}.
	 * @param tupleCallback The callback to unregister.
	 */
	public static void peisjava_unregisterTupleCallback(double tupleCallback) {
		peiskThread.unregisterCallback(tupleCallback);
	}
	
	
	/**
	 * Inserts value into the tuple referenced by the tuple (metaKey,metaOwner).
	 * Must already be subscribed to (metaKey,metaOwner) using at least a normal
	 * subscribe but also works with a metaSubscription.
	 * @param metaKey The key of the meta tuple.
	 * @param metaOwner The owner of the meta tuple.
	 * @param value The String value to set.
	 * @return Zero on success, error code otherwise.
	 */
	public static void peisjava_setStringTupleIndirectly(int metaOwner, String metaKey, String value) {
		INSTANCE.peiskmt_setStringTupleIndirectly(metaOwner, metaKey, value);
	}
	
	
	/**
	 * A wrapper function for setting tuples whose value is a simple
	 * String.  The owner must be the caller. 
	 * @param name The name of the tuple to set.
	 * @param value The value to set.  A String value of "nil" is set if
	 * this parameter is {@code null}.
	 */
	public static void peisjava_setStringTuple(String name, String value) {
		if (value == null) {
			INSTANCE.peiskmt_setStringTuple(name,"nil");
		} else {
			INSTANCE.peiskmt_setStringTuple(name,value);
		}
	}
	
	/**
	 * Simplifying wrapper for creating and setting a tuple in local tuplespace,
	 * propagating it to all subscribers. Provides backwards
	 * compatibility with kernel G3 and earlier.
	 * @param key The key of the tuple to set.
	 * @param len The length of the data to set.
	 * @param value The data to write into the tuple.
	 */
	public static void peisjava_setTuple(String key, byte[] value, String mimetype) {
		Memory m = new Memory(value.length);
		m.write(0, value, 0, value.length);
		
		INSTANCE.peiskmt_setTuple(key, value.length, m, mimetype, 0);
	}
	
	
	/**
	 * Wrapper for subscribing to tuples with given key from given owner
	 * (or -1 for wildcard on owner).
	 * @param key The key of the tuple to subscribe.
	 * @param owner The owner of the tuple to subscribe (-1 for wildcard).
	 */
	public static synchronized PeisSubscriberHandle peisjava_subscribe(int owner, String key) {
		return PeisJavaMT.INSTANCE.peiskmt_subscribe(owner, key);
	}
	
	
	
	/**
	 * Creates a subscription to the meta tuple (in local tuple space) given by metaKey and metaOwner.
	 * Neither metaKey nor metaOwner may contain wildcard.
	 * @param metaKey The key of the meta tuple to subscribe to.
	 * @param metaOwner The owner of the meta tuple to subscribe to.
	 * @return The subscription handle, {@code null} if the operation fails.
	 */	
	public static synchronized void peisjava_subscribeIndirectly(String metaKey) {
		INSTANCE.peiskmt_subscribeIndirectly(INSTANCE.peiskmt_peisid(), metaKey);	
	}
	
	
	/**
	 * Creates a subscription to the meta tuple given by metaKey and metaOwner.
	 * Neither metaKey nor metaOwner may contain wildcard.
	 * @param metaKey The key of the meta tuple to subscribe to.
	 * @param metaOwner The owner of the meta tuple to subscribe to.
	 * @return The subscription handle, {@code null} if the operation fails.
	 */	
	public static synchronized void peisjava_subscribeIndirectly(int metaOwner, String metaKey) {
		INSTANCE.peiskmt_subscribeIndirectly(metaOwner, metaKey);	
	}

	
	
	/**
	 * Unsubscribe to tuples.
	 * @param handle The handle of the subscription to unsunbscribe.
	 */
	public static synchronized boolean peisjava_unsubscribe(PeisSubscriberHandle handle) {
		return !INSTANCE.peiskmt_unsubscribe(handle);
	}
	
	
	/**
	 * Sets the meta tuple to point to given real tuple.
	 * @param metaOwner The owner of the meta tuple.
	 * @param metaKey The key of the meta tuple.
	 * @param realOwner The owner of the real tuple.
	 * @param realKey The key of the real tuple.
	 */
	public static synchronized void peisjava_setMetaTuple(int metaOwner, String metaKey, int realOwner, String realKey) {
		INSTANCE.peiskmt_setMetaTuple(metaOwner, metaKey, realOwner, realKey);
	}
	

	
	
	/**
	 * Register a callback on a tuple.  The callback routine must be implemented in
	 * the method {@link CallbackObject#callback(PeisTuple tuple)} of a class which extends
	 * the abstract class {@link CallbackObject}. 
	 * @param tuple A tuple prototype describing the tuple to register against.
	 * @param co A class extending the {@link CallbackObject} abstract class.
	 * @return The {@link CallbackObject} if the a callback object was successfully instantiated
	 * and queued for registration, {@code null} otherwise. 
	 */
	public static double peisjava_registerTupleCallbackByAbstract(PeisTuple tuple, CallbackObject co) {
		return peiskThread.registerCallback(tuple.getKey(), tuple.owner, co);
	}
	
	/**
	 * Meta-callbacks! Finally!
	 * @author Chittaranjan Srinivas Swaminathan
	 * 
	 * @param metaTupleOwner The owner of the meta-tuple.
	 * @param metaTupleKey The key of the meta-tuple.
	 * @param co The Callback Object.
	 */
	public static void peisjava_registerMetaTupleCallback(int metaTupleOwner, String metaTupleKey, CallbackObject co) {
		peiskThread.registerMetaCallback(metaTupleKey, metaTupleOwner, co);
	}
	
	/**
	 * Remove a meta-callback.
	 * @author Chittaranjan Srinivas Swaminathan
	 * 
	 * @param metaTupleOwner The owner of the meta-tuple.
	 * @param metaTupleKey The key of the meta-tuple.
	 */
	public static void peisjava_unregisterMetaTupleCallback(int metaTupleOwner, String metaTupleKey) {
		INSTANCE.peiskmt_unregisterMetaTupleCallback(metaTupleOwner, metaTupleKey);
	}
	
	
	/**
	 * Wrapper for creating and setting a tuple in a tuplespace belonging
	 * to some other peis. Propagation is done by the other peis if
	 * successful. Provides backwards compatibility with kernel G3 and
	 * earlier.
	 * @param key The key of the tuple to set.
	 * @param owner The owner of the tuple to set.
	 * @param value The data to set.
	 */
	public static void peisjava_setRemoteStringTuple(int owner, String key, String value) {
		INSTANCE.peiskmt_setRemoteStringTuple(owner, key, value);
	}
	
	/**
	 * Inserts value into the tuple referenced by the tuple (metaKey,metaOwner).
	 * Must already be subscribed to (metaKey,metaOwner) using at least a normal
	 * subscribe but also works with a metaSubscription.
	 * @param metaKey The key of the meta tuple.
	 * @param metaOwner The owner of the meta tuple.
	 * @param value The value to set.
	 * @param mimetype The mimetype of the tuple. 
	 * @return {@code true} iff success.
	 */
	//ADDED
	//DO EXCEPTION
	public static void peisjava_setTupleIndirectly(int metaOwner, String metaKey, byte[] value, String mimetype) {
		Memory m = new Memory(value.length);
		m.write(0, value, 0, value.length);

		INSTANCE.peiskmt_setTupleIndirectly(metaOwner, metaKey, value.length, m, mimetype, 0);
	}
	
	
	/**
	 * Wrapper for creating and setting a tuple in a tuplespace belonging
	 * to some other peis. Propagation is done by the other peis if
	 * successful. Provides backwards compatibility with kernel G3 and
	 * earlier.
	 * @param owner The owner of the tuple to set.
	 * @param key The key of the tuple to set.
	 * @param data The data to set.
	 * @param mimetype The mimetype of the tuple to set.
	 * @param encoding Whether the data is ASCII or binary.
	 */ //ADDED
	public static void peisjava_setRemoteTuple(int owner, String key, byte[] data, String mimetype) {
		Memory m = new Memory(data.length);
		m.write(0, data, 0, data.length);
		INSTANCE.peiskmt_setRemoteTuple(owner, key, data.length, m, mimetype, 0);
	}
}
