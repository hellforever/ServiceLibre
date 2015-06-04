package com.arcsolu.sopda.entity;

import java.io.Serializable;


public class OrderDetail implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String Id;
	public Order Order;
	public Menu Menu;
	public int turn;
	public boolean sent;
	public int nb;
	
	public OrderDetail(Order order){
		if (order == null) {
			throw new NullPointerException();
		}
		this.Order = order;
		Order.Details.add(this);
		sent=false;
		turn=-1;
	}
}
