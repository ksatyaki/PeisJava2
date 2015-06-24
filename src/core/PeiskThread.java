package core;

import java.util.HashMap;

import core.PeisJavaInterface.PeisCallbackHandle;
import core.PeisJavaInterface.PeisTupleCallback;


class PeiskThread {
	
	//private HashMap<PeisTupleCallback, PeisCallbackHandle> callbacks = new HashMap<PeisTupleCallback, PeisCallbackHandle>();
	
	private class Callback {
		public final int owner;
		public final String key;
		public final PeisTupleCallback tupleCallback;
		
		public Callback(int o, String k, PeisTupleCallback tcb) {
			owner = o;
			key = k;
			tupleCallback = tcb;
		}
		
		@Override
		public String toString() {
			return ("Callback <" + key + "," + owner + "," + tupleCallback.getClass().getName() + ">");
		}
	}
	
	protected double registerCallback(String key, int owner, PeisTupleCallback coi) {
		
		if(coi == null) {
			throw new IllegalArgumentException("PeisTupleCallback is null");
		}	
		Callback cb = new Callback(owner,key,coi);		
		double hndl = PeisJavaMT.INSTANCE.peiskmt_registerTupleCallback(cb.owner, cb.key, null, cb.tupleCallback);
		System.out.println("REGISTERED (M) " + cb);
		//callbacks.put(cb.tupleCallback, hndl);
		
		return hndl; //coi;
	}
	
	protected void registerMetaCallback(String key, int owner, PeisTupleCallback coi) {
		if(coi == null) {
			throw new IllegalArgumentException("PeisTupleCallback is null");
		}
		
		Callback cb = new Callback(owner, key, coi);
		PeisJavaMT.INSTANCE.peiskmt_registerMetaTupleCallback(cb.owner, cb.key, null, cb.tupleCallback);
	}
	
	protected void unregisterCallback(final double tupleCallback) {
		
		if(tupleCallback == 0.0) {
			throw new IllegalArgumentException("PeisTupleCallback is null");
		}
		
		//PeisCallbackHandle hndl = callbacks.get(tupleCallback);
		PeisJavaMT.INSTANCE.peiskmt_unregisterTupleCallback(tupleCallback);
	}
}

