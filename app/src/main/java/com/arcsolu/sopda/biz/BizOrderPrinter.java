package com.arcsolu.sopda.biz;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import com.arcsolu.sopda.entity.OrderPrinter;

public interface BizOrderPrinter extends Serializable{
	public boolean UpdateShow(OrderPrinter os) throws SQLException;
	public List<OrderPrinter> Select() throws SQLException;
	public boolean setTime(OrderPrinter os,String Time);
	public boolean setShowMode(int[] number);
	public int[] getShowMode();

}
