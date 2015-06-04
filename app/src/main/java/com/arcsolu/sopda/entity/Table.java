package com.arcsolu.sopda.entity;

import java.io.Serializable;

public class Table implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public enum TableState {tsEmpty, tsOccupied,tsOnPayment,tsReserved}
	public Floor Floor;
	public String TableType;
	public String Id;
	public String CurrentOrderId;
	public int SeatCount;
	public String Number;
	public TableState State;
	public int Point_x;
	public int Point_y;
	public int TableWidth;
	public int TableHeight;
	
	public Table(Floor floor) {
		if (floor == null) {
			throw new NullPointerException();
		}
		this.Floor = floor;
		floor.Tables.add(this);
	}
	
	@Override
	public boolean equals(Object obj) {
		try {
			Table o = (Table) obj;
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
