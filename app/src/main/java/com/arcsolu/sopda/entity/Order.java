package com.arcsolu.sopda.entity;

import android.annotation.SuppressLint;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressLint("SimpleDateFormat")
public class Order implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String Id;
	public Table Table;
	public User User;
	public String LastSendTime;
	public Date LastCallTime;
	public Date LastLadditionTime;
	public String StartTime;
	public int ClientNumber;
	public List<OrderDetail> Details;

	public Order(User user, Table table) {
		if (user == null || table == null) {
			throw new NullPointerException();
		}
		Details = new ArrayList<OrderDetail>();
		this.Table = table;
		this.User = user;
		LastCallTime = new Date(System.currentTimeMillis() - 180000);
		LastLadditionTime = new Date(System.currentTimeMillis() - 180000);
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		Date c = new Date(0);
		LastSendTime = formatter.format(c);
	}

	@Override
	public boolean equals(Object obj) {
		try {
			Order o = (Order) obj;
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
