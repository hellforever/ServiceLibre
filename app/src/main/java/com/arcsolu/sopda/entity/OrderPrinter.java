package com.arcsolu.sopda.entity;

import java.io.Serializable;

public class OrderPrinter implements Serializable{

	private static final long serialVersionUID = 9195783548717684589L;
	
	public int ShowType;
	public String Time;
	public int ClientNumber;
	public String TableNumber;
	public String OrderId;
	public OrderPrinter() {
	}

}
