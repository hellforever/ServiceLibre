package com.arcsolu.sopda.biz;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.arcsolu.sopda.entity.OrderPrinter;

class ImpBizOrderPrinter implements BizOrderPrinter, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	List<OrderPrinter> PrinterOrderList = new ArrayList<OrderPrinter>();
	int[] mode;

	public ImpBizOrderPrinter() {
		mode = new int[2];
		mode[0] = 6;
		mode[1] = 5;
	}

	/**
	 * @author user
	 * to update the show in the database
	 * @param os
	 * @return
	 * @throws SQLException
	 */
	@Override
	public boolean UpdateShow(OrderPrinter os) throws SQLException {
		ImpBizApp.fd.Connect();
		Statement ps;
			ps = ImpBizApp.fd.con.createStatement();
			if (os.ShowType == 12) {
				ImpBizApp.fd.StartTransAction();
				ps.execute("delete  from ORDER_PRINT_LIST where bill_id='"
						+ os.OrderId + "'and bill_type=12;");
				ImpBizApp.fd.CommitTransAction();
				ps.close();
			} else if (os.ShowType == 10) {
				ImpBizApp.fd.StartTransAction();
				ps.execute("update  ORDER_PRINT_LIST set BILL_TYPE=0 where BILL_ID='"
						+ os.OrderId + "'and bill_type=10;");
				ImpBizApp.fd.CommitTransAction();
				ps.close();
			}
			ImpBizApp.fd.Disconnect();

		return true;
	}

	/**
	 * @author user
	 * get orderprinter from database
	 * @return
	 * @throws SQLException
	 */
	@Override
	public List<OrderPrinter> Select() throws SQLException {
		ImpBizApp.fd.Connect();
		Statement ps;

			ps = ImpBizApp.fd.con.createStatement();
			ResultSet rs = (ResultSet) ps
					.executeQuery("select op.BILL_TYPE, op.ORDER_TIME,op.bill_id,ob.customer_count,rt.rest_table_number from ORDER_PRINT_LIST op inner join order_bill ob inner join REST_TABLE rt on op.bill_id=ob.order_id on ob.REST_TABLE_ID=rt.REST_TABLE_ID order by op.ORDER_TIME; ");

			PrinterOrderList.clear();
			if (rs == null) {
				System.out.println("Download OrderPrinter list error!");
				return null;
			}
			while (rs.next()) {
				if (rs.getInt("BILL_TYPE") != 0) {
					OrderPrinter os = new OrderPrinter();
					os.Time = rs.getString(2);
					os.ShowType = rs.getInt("BILL_TYPE");
					os.ClientNumber = rs.getInt("customer_count");
					os.TableNumber = rs.getString("rest_table_number");
					os.OrderId = rs.getString("bill_id");
					PrinterOrderList.add(os);
				}
			}
			ps.close();
			rs.close();
			ImpBizApp.fd.Disconnect();
		
		return PrinterOrderList;
	}

	/**
	 * @author user
	 * set orderprinter's time
	 * @param os
	 * @param Time
	 * @return
	 */
	@Override
	public boolean setTime(OrderPrinter os, String Time) {
		os.Time = Time;
		return true;
	}

	/**
	 * @author user
	 * save show mode from pad
	 * @param number
	 * @return
	 */
	@Override
	public boolean setShowMode(int[] number) {
		mode[1] = number[1];
		mode[0] = number[0];

		try {
			File file = new File(
					"data/data/com.example.servicelibretableinfo/files/OrderPrinter.dat");
			file.createNewFile();
			ObjectOutputStream os = new ObjectOutputStream(
					new FileOutputStream(file));
			os.writeObject(mode);
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * @author user
	 * read show mode from pad
	 * @return
	 */
	@SuppressWarnings("resource")
	@Override
	public int[] getShowMode() {
		FileInputStream fs;
		try {
			fs = new FileInputStream(
					"data/data/com.example.servicelibretableinfo/files/OrderPrinter.dat");

			ObjectInputStream oi = new ObjectInputStream(fs);
			Object o1 = oi.readObject();
			if (o1 instanceof int[]) {
				mode = (int[]) o1;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return mode;
	}

}
