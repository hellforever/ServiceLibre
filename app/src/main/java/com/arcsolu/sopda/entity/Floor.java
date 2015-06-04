package com.arcsolu.sopda.entity;

import java.io.Serializable;
import java.util.*;

public class Floor implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String Id;
	public String Floor;
	public List<Table> Tables;
	public byte[] Img;
	public Floor() {
		Tables = new ArrayList<Table>();
	}
	
	@Override
	public boolean equals(Object obj) {
		try {
			Floor o = (Floor) obj;
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

