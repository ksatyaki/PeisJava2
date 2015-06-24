/*
    Copyright (C) 2008 - 2011 Federico Pecora
    
    Based on libpeiskernel (Copyright (C) 2005 - 2011  Mathias Broxvall).
    
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



package core;


import java.util.Date;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;

/**
 * Represents a tuple in the local tuplespace. Also used to instantiate
 * (partial) tuple as used when doing searches in the local and in
 * the distributed tuple space. Wildcards on specific fields are
 * specified using {@code null} values (or -1 for integers).
 * The preferred method for creating tuples are the factory
 * methods {@link PeisJavaMT#peisjava_createPeisTuple(String name)},
 * {@link PeisJavaMT#peisjava_createAbstractPeisTuple(String name)} and
 * {@link PeisJavaMT#peisjava_createMetaPeisTuple(String metaKey, String realKey, int realOwner)}
 * as they also takes care of tuple initialization.
 * @author fpa
 *
 */
public class PeisTuple extends Structure {
	
	/**
	 * The owner (controlling peis) of the data represented by this tuple, or -1 as wildcard.
	 */
	public int owner;
	
	/**
	 * Data of tuple or {@code null} if wildcard.
	 */
	public PointerByReference key;
	
	/**
	 * Data of tuple or {@code null} if wildcard.
	 */
	public PointerByReference data;
	
	/**
	 * Used length of data for this tuple if data is non-null, otherwise -1.
	 */
	public int datalen;

	/**
	 * Timestamp assigned when first written into owner's tuplespace or -1 as wildcard.
	 */
	public int[] ts_write;
	

	/**
	 * Timestamp when tuple expires. Zero expires never, -1 as wildcard.
	 */
	public int[] ts_expire;

	/**
	 * Create a new tuple.  The preferred method for creating tuples are the factory
	 * methods {@link PeisJavaMT#peisjava_createPeisTuple(String name)},
	 * {@link PeisJavaMT#peisjava_createAbstractPeisTuple(String name)} and
	 * {@link PeisJavaMT#peisjava_createMetaPeisTuple(String metaKey, String realKey, int realOwner)}
	 * as they also takes care of tuple initialization.
	 */
	public PeisTuple() {
		ts_write = new int[2];
		key = new PointerByReference();
		ts_expire = new int[2];
		datalen = -1;
		data = new PointerByReference();
	}
	
	/**
	 * Get the data in this tuple as a String. 
	 * @return A string containing the data in this tuple.
	 */
	public String getStringData() {
		if (this.data != null)
			return this.data.getPointer().getString(0);
		return null;
	}
	
	/**
	 * Get the data in this tuple as a byte array. 
	 * @return A byte array containing the data in this tuple.
	 */
	public byte[] getByteData() {
		if (this.data != null)
			return this.data.getPointer().getByteArray(0, this.datalen);
		return null;
	}
	
	/**
	 * Return the key of this tuple.
	 * @return The key of this tuple.
	 */
	public String getKey() {
		if (this.key != null)
			return this.key.getPointer().getString(0);
		return null;
	}
	
	/**
	 * Returns the owner of this tuple.
	 * @return The owner of this tuple.
	 */
	public int getOwner() {
		return this.owner;
	}
	
	/**
	 * Set the owner of this tuple.
	 * @param owner The owner of this tuple.
	 */
	public void setOwner(int owner) {
		this.owner = owner;
	}
	
	/**
	 * Set the data of this tuple as a String.
	 * @param data The String containing the data.
	 */
	public void setStringData(String data) {
		Memory m = new Memory(data.length()+1);
		m.write(0, data.getBytes(), 0, data.length());
		byte[] terminator = {0x0};
		m.write(data.length(), terminator, 0, 1);
		if (this.data == null) this.data = new PointerByReference(); 
		this.data.setPointer(m);
		this.datalen = data.length()+1;
	}
	

	/**
	 * Set the data of this tuple as a byte array.
	 * @param data The byte array containing the data.
	 */
	public void setByteData(byte[] data) {
		Memory m = new Memory(data.length);
		m.write(0, data, 0, data.length);
		this.data.setPointer(m);
		this.datalen = data.length;
	}
	
		
	/**
	 * @return The write time stamp as a {@link Date} or <code>null</code> in case the time is undefined.
	 */
	public Date getTsWrite() {
		return PeisJavaUtilities.getJavaDateFromPeisDate(ts_write[0], ts_write[1]);
	}
	
	/**
	 * Sets the write time stamp of the tuple
	 * @param time the new time stamp
	 * @return the old time stamp
	 */
	public Date setTsWrite(Date time) {
		return PeisJavaUtilities.setPeisDateFromJavaDate(time, ts_write);
	}
	
	
	/**
	 * @return The expire time stamp as a {@link Date} or <code>null</code> in case the time is undefined.
	 */
	public Date getTsExpire() {
		return PeisJavaUtilities.getJavaDateFromPeisDate(ts_expire[0], ts_expire[1]);
	}
	
	/**
	 * Sets the expire time stamp of the tuple
	 * @param time the new time stamp
	 * @return the old time stamp
	 */
	public Date setTsExpire(Date time) {
		return PeisJavaUtilities.setPeisDateFromJavaDate(time, ts_expire);
	}

}
