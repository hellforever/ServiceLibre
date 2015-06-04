package com.arcsolu.sopda.entity;

import java.io.Serializable;

public class Script implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6482717258793755670L;
	public String Id;
	public String Description;
	public String ScriptText;
	public Script() {
	
	}
	@Override
	public boolean equals(Object obj) {
		try {
			Script o = (Script) obj;
			if (o.Id.equals(this.Id)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
}
