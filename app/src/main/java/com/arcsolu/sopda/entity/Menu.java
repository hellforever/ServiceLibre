package com.arcsolu.sopda.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Menu implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String Id;
	public String Display;
	public String Name;
	public boolean Deleted=false;
	public byte[] Pic;
	public int Index;
	public int Limit;
	public String Catalog;
	public List<Printer> Printers;
	public boolean IsVailable=true;
	public boolean Top5=false;
	public boolean Chef=false;
	public boolean NEW=false;
	public Menu() {
		Printers=new ArrayList<Printer>();
		}
	
	@Override
	public boolean equals(Object obj) {
		try {
			Menu o = (Menu) obj;
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