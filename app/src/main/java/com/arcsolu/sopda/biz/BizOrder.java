package com.arcsolu.sopda.biz;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Map;

import com.arcsolu.sopda.entity.Formule;
import com.arcsolu.sopda.entity.Order;
import com.arcsolu.sopda.entity.Table;
import com.arcsolu.sopda.entity.User;

public interface BizOrder extends Serializable{
	public boolean SendOrder(Order order);
	public boolean PrintOrder(Order order) throws SQLException ;
	public boolean SaveOrder(Order order);
	public boolean CallService(Order order) throws SQLException;
	public Order CreateOrder(User user,Table table,int customer,Map<Formule,Integer> formules) throws SQLException ;
	public String GetStartTime(Order order);
	public String GetLastSendTime(Order order);
	public boolean IsActive();
	public int getTrial();
	public boolean ActiveNow();
}
