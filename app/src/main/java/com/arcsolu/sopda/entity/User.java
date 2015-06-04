package com.arcsolu.sopda.entity;

import java.io.Serializable;

public class User implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String Id;
	public String Name;
	public String password;

	
	public User() {
	}
	
	@Override
	public boolean equals(Object obj) {
		try {
			User o = (User) obj;
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
