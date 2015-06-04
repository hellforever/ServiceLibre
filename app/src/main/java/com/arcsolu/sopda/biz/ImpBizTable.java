package com.arcsolu.sopda.biz;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;

import android.annotation.SuppressLint;
import com.arcsolu.sopda.entity.Floor;
import com.arcsolu.sopda.entity.Table;
import com.arcsolu.sopda.entity.Table.TableState;

class ImpBizTable implements BizTable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7105525658024210838L;

	public ImpBizTable() {
	}

	@SuppressLint("SdCardPath")
	public boolean Save(ArrayList<Table> values) {
		/*
		 * try { ObjectOutputStream os = new ObjectOutputStream(new
		 * FileOutputStream( new
		 * File("/data/data/com.arcsolu.sopda.biz/files/tables.dat")));
		 * os.writeObject(values); os.flush(); os.close(); } catch
		 * (FileNotFoundException e) { e.printStackTrace(); } catch (IOException
		 * e) { e.printStackTrace(); }
		 */
		return true;
	}

	/**
	 * @author user
	 * delete table from cache.
	 * @param value
	 * @return
	 */
	public boolean Delete(Table value) {
		Floor fl = value.Floor;
		fl.Tables.remove(value);
		return true;
	}

	/**
	 * tablestate from enum to int
	 * @param ts
	 * @return
	 */
	public static int EnumToInt(TableState ts) {
		if (ts.equals(TableState.tsEmpty))
			return 0;
		else if (ts.equals(TableState.tsOccupied))
			return 1;
		else if (ts.equals(TableState.tsOnPayment))
			return 2;
		else if (ts.equals(TableState.tsReserved))
			return 3;
		else
			return 4;
	}

	/**
	 * tablestate from int to enum
	 * @param no
	 * @return
	 */
	public static TableState IntToEnum(int no) {
		switch (no) {
		case 0:
			return TableState.tsEmpty;
		case 1:
			return TableState.tsOccupied;
		case 2:
			return TableState.tsOnPayment;
		case 3:
			return TableState.tsReserved;
		default:
			return TableState.tsEmpty;
		}
	}

	/**
	 * @author user
	 * insert or update a table in database
	 * @param t
	 * @return
	 */
	public boolean InsertTable(Table t) {
		PreparedStatement ps;
		Statement stmt;
		ImpBizApp.fd.Connect();
		ArrayList<String> als=new ArrayList<String>();
		try {
			stmt=ImpBizApp.fd.con.createStatement();
			java.sql.ResultSet rs=stmt.executeQuery("SELECT REST_TABLE_NUMBER FROM REST_TABLE");
			
			while(rs.next()){
				als.add(rs.getString(1));
			}
			for(String str:als){
				if(t.Number.equals(str)){
					return false;
				}
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		ImpBizApp.fd.StartTransAction();
		
		try {
			ps = ImpBizApp.fd.con
					.prepareStatement("UPDATE OR INSERT INTO REST_TABLE (REST_TABLE_ID,REST_TABLE_TYPE_ID,REST_FLOOR_ID,REST_TABLE_NUMBER,REST_TABLE_SEAT_COUNT,POINT_X,POINT_Y,CURRENT_ORDER_ID,REST_TABLE_STATE_ID) VALUES ('"
							+ t.Id
							+ "', '"
							+ t.TableType
							+ "','"
							+ t.Floor.Id
							+ "','"
							+ t.Number
							+ "',"
							+ t.SeatCount
							+ ","
							+ t.Point_x
							+ ","
							+ t.Point_y
							+ ",'"
							+ t.CurrentOrderId
							+ "',"
							+ EnumToInt(t.State)
							+ ") MATCHING (REST_TABLE_ID)");

			ps.execute();
			ImpBizApp.fd.CommitTransAction();
			ps.close();
		} catch (SQLException e) {
			absDB.showSQLException(e);
			e.printStackTrace();
			return false;
		}
		ImpBizApp.fd.Disconnect();
		return true;
	}

	/**
	 * @author user
	 * create a new table in the list mode,which the default number of seat is 2.
	 * @param floor
	 * @param number
	 * @return
	 */
	@Override
	public Table CreateTable(Floor floor, String number) {
		Table table=new Table(floor);
		table.Number=number;
		table.State=TableState.tsEmpty;
		table.SeatCount=2;
		Random rd=new Random();
			table.TableType ="00000000000000000000000000000001";
		table.Id = String.valueOf(rd.nextGaussian());
		return table;
	}
}