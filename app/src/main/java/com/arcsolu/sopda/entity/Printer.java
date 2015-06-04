package com.arcsolu.sopda.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Printer implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String Id;
	public String Name;
	public String Address;
	public List<Script> Scripts;
	public Printer() {
		Scripts=new ArrayList<Script>();
	}
	
	@Override
	public boolean equals(Object obj) {
		try {
			Printer o = (Printer) obj;
			if (o.Id.contentEquals(this.Id)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

}
