/*
    Copyright (C) 2015 Chittaranjan Srinivas
    
       Based on libpeiskernel 
    	(Copyright (C) 2005 - 2012  Mathias Broxvall).
    and RegisteringCallbacksExample.java 
    	(Copyright (c) 2005 - 2012 Federico Pecora).
    
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


package examples;

import srnp.CallbackObject;
import srnp.PeisJavaMT;
import srnp.PeisTuple;

public class MetaTupleCallback {

	public static void main(String[] args) {
		
		String[] env = { "SRNP_MASTER_IP=127.0.0.1", "SRNP_MASTER_PORT=12321" };
		String[] argss = {"StartProgram", "--owner-id", "12" }; 
		PeisJavaMT.peisjava_initialize(argss, env);
		System.out.println("Hello there, I am: " + PeisJavaMT.peisjava_peisid());

		PeisJavaMT.peisjava_registerMetaTupleCallback(
							  999,
							  "in.goal.type",
							  new CallbackObject() {
							      private int numInvocations = 0;
							      @Override
								  public void callback(PeisTuple tuple) {
							    	  if (tuple.getStringData() != null && !tuple.getStringData().equals("") && !tuple.getStringData().equals("()")) {
								      System.out.println("####### TAG KEY: " + tuple.getKey() + ", TAG VALUE: " + tuple.getStringData());
							    	  }
							      }
							  } //End CallbackObject definition
				
				);
		int i = 30;
		while(i > 0) {
			i--;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		
		PeisJavaMT.peisjava_shutdown();
		
	}
}
