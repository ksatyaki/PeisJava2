/*
    Copyright (C) 2008 - 2012 Federico Pecora
    
    Based on libpeiskernel (Copyright (C) 2005 - 2012  Mathias Broxvall).
    
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
    02110-1301 USA.
*/


package srnp;
import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public interface PeisJavaInterface extends Library {

	/*
	 * CALLBACK FUNCTION DEFS
	 */

	/**
	 * Callback hook for registering a tuple callback (triggered when value in tuple changes).
	 * @author Chittaranjan
	 */
	public interface PeisTupleCallback extends Callback {
		void callback(PeisTuple tuple, Pointer userdata);
	}
		
	/**
	 * 
	 * @author Chittaranjan
	 *
	 */
	public interface PeisCallbackHandle extends Callback {
		double callback();
	}
	

	/**
	 * 
	 * @author Chittaranjan
	 *
	 */
	public interface PeisSubscriberHandle extends Callback {
		int callback();
	}
	
	/**
	 * Wrapper to add a callback function to tuples with given fully
	 * qualified key and owner; which is called when tuple is changed in
	 * the local tuplespace. Provides backwards compatibility with kernel
	 * G3 and earlier.
	 * @param key
	 * @param owner
	 * @param userdata
	 * @param fn
	 * @return Callback handle if successful, {@code null}
	 * if unsuccessful.
	 */
	public double peiskmt_registerTupleCallback(int owner, String key, Pointer userdata, PeisTupleCallback fn);

	
	/**
	 * Unregister a tuple callback given its handle.
	 * @param tupleCallback The handle of the tuple callback to unregister.
	 */
	//DO EXCEPTION
	public int peiskmt_unregisterTupleCallback(double tupleCallback);

	/*
	 * PEISKERNEL FUNCTIONS    
	 */
	
	/**
	 * Initializes peiskernel using any appropriate commandline options.
	 * @param argc The number of parameters 
	 * @param args The parameters
	 */
	public void peiskmt_initialize(IntByReference argc, String[] args, String[] env);
	
	/**
	 * Prints a tuple in human readable format to stdout, used
	 * for debugging.
	 * @param tuple The tuple to print.
	 */
	public void peiskmt_printTuple(PeisTuple tuple);

	/**
	 * A wrapper function for setting tuples whose value are simple
	 * String. Provides backwards compatibility with kernel G3 and earlier.
	 * @param key The key of the tuple to be set.
	 * @param value The value to set the tuple with.
	 */
	public void peiskmt_setStringTuple(String key, String value);
	
	/**
	 * Ascertain if the peiskernel is running.
	 * @return {@code true} after initialization until we have performed a shutdown.
	 */
	public boolean peiskmt_isRunning();
	
	/**
	 * Wrapper for getting a value from the local tuplespace for the fully qualified given key and owner. 
	 * Provides backwards compatibility with kernel G3 and earlier.
	 * @param key The keyname of the tuple.
	 * @param owner The owner of the tuple.
	 * @param flags Flags modifying the behaviour of getTuple.
	 * Logical or of flags: peiskmt_TFLAG_OLDVAL,  peiskmt_TFLAG_BLOCKING.
	 * @return The tuple if successful {@code null} otherwise.
	 */
	public PeisTuple peiskmt_getTuple(int owner, String key, int flags);
	
	/**
	 * Free a tuple (including the data field).
	 * @param tuple
	 */
	public void peiskmt_deAllocateTuple(PeisTuple tuple);
	
	/**
	 * Wrapper for subscribing to tuples with given key from given owner
	 * (or -1 for wildcard on owner).
	 * @param key
	 * @param owner
	 * @return The handle for this tuple subscription if success, {@code null} otherwise.
	 */
	public PeisSubscriberHandle peiskmt_subscribe(int owner, String key);
	
	/**
	 * Unsubscribe to tuples.
	 * @param handle The handle of the subscription to unsunbscribe.
	 * @return Zero on success, error number otherwise.
	 */
	public boolean peiskmt_unsubscribe(PeisSubscriberHandle handle);

	
	/**
	 * Creates a subscription to the meta tuple given by metaKey and metaOwner.
	 * Neither metaKey nor metaOwner may contain wildcard.
	 * @param metaKey The key of the meta tuple to subscribe to.
	 * @param metaOwner The owner of the meta tuple to subscribe to.
	 * @return The subscription handle, {@code null} if the operation fails.
	 */
	//public PeisSubscriberHandle peiskmt_subscribeIndirect(String metaKey, int metaOwner);
	public void peiskmt_subscribeIndirectly(int metaOwner, String metaKey);
	
	/**
	 * Use the meta tuple given by (metaOwner,metaKey) to find a reference to a
	 * specific tuple. Returns this referenced tuple [metaOwner,metaKey] if found.
	 * Must previously have created a metaSubscription to metaKey and metaOwner.
	 * @param metaOwner The owner of the meta tuple. 
	 * @param metaKey The key of the meta tuple.
	 * @param flags Flags modifying the behaviour of internal getTuple call.
	 * @return Tuple [metaOwner,metaKey] if found, {@code null} otherwise.
	 */
	//public PeisTuple peiskmt_getIndirectTuple(String metaKey, int metaOwner, IntByReference len, PointerByReference ptr, int flags);
	public PeisTuple peiskmt_getTupleIndirectly(int metaOwner, String metaKey, int flags);
	
	/**
	 * Inserts value into the tuple referenced by the tuple (metaKey,metaOwner).
	 * Must already be subscribed to (metaKey,metaOwner) using at least a normal
	 * subscribe but also works with a metaSubscription.
	 * @param metaKey The key of the meta tuple.
	 * @param metaOwner The owner of the meta tuple.
	 * @param value The String value to set.
	 * @return Zero on success, error code otherwise.
	 */
	//public int peiskmt_setIndirectStringTuple(String metaKey, int metaOwner, String value);
	//DO EXCEPTION
	public int peiskmt_setStringTupleIndirectly(int metaOwner, String metaKey, String value);
	
	/**
	 * Initializes a meta tuple to reasonable default values.
	 * @param metaOwner The owner of the meta tuple.
	 * @param metaKey The key of the meta tuple.
	 */
	public void peiskmt_declareMetaTuple(int metaOwner, String metaKey);
	
	/**
	 * Sets the meta tuple to point to given real tuple.
	 * @param metaOwner The owner of the meta tuple.
	 * @param metaKey The key of the meta tuple.
	 * @param realOwner The owner of the real tuple.
	 * @param realKey The key of the real tuple.
	 */
	public void peiskmt_setMetaTuple(int metaOwner, String metaKey, int realOwner, String realKey);
	
	/**
	 * Get the id of this peiskernel.
	 * @return The id of this peiskernel.
	 */
	public int peiskmt_peisid();
	
	/**
	 * Stops the running peis kernel.
	 */
	public void peiskmt_shutdown();
	
	/**
	 * Simplifying wrapper for creating and setting a tuple in local tuplespace,
	 * propagating it to all subscribers. Provides backwards
	 * compatibility with kernel G3 and earlier.
	 * @param key The key of the tuple to set.
	 * @param len The length of the data to set.
	 * @param data The data to write into the tuple.
	 * @param mimetype The mimetype of the tuple to set.
	 */ //ADDED
	public void peiskmt_setTuple(String key, int len, Pointer data, String mimetype, int encoding);
	

	/**
	 * Wrapper for creating and setting a tuple in a tuplespace belonging
	 * to some other peis. Propagation is done by the other peis if
	 * successful. Provides backwards compatibility with kernel G3 and
	 * earlier.
	 * @param key The key of the tuple to set.
	 * @param owner The owner of the tuple to set.
	 * @param len The length of the data.
	 * @param data The data to set.
	 * @param mimetype The mimetype of the tuple to set.
	 */
	public void peiskmt_setRemoteTuple(int owner, String key, int len, Pointer data, String mimetype, int encoding);


	
	/**
	 * Wrapper for creating and setting a tuple in a tuplespace belonging
	 * to some other peis. Propagation is done by the other peis if
	 * successful. Provides backwards compatibility with kernel G3 and
	 * earlier.
	 * @param key The key of the tuple to set.
	 * @param owner The owner of the tuple to set.
	 * @param len The length of the data.
	 * @param value The data to set.
	 */
	public void peiskmt_setRemoteStringTuple(int owner, String key, String value);
	
	/**
	 * Register a callback on a meta tuple.
	 * 
	 * @param metaTupleOwner The owner of the meta tuple.
	 * @param metaTupleKey The key of the meta tuple.
	 * @param userdata The user data that you wish to pass to the Callback function.
	 * @param fn The callback function that would be called when the tuple changes.
	 */
	public void peiskmt_registerMetaTupleCallback(int metaTupleOwner, String metaTupleKey, Pointer userdata, PeisTupleCallback fn);
	
	/**
	 * Cancel an existing callback registration on a meta-tuple.
	 * 
	 * @param metaTupleOwner The owner of the meta tuple.
	 * @param metaTupleKey The key of the meta tuple.
	 */
	public void peiskmt_unregisterMetaTupleCallback(int metaTupleOwner, String metaTupleKey);


	public int peiskmt_setTupleIndirectly(int metaOwner, String metaKey,
			int length, Memory m, String mimetype, int enc);
}
