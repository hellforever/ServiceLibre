package com.arcsolu.sopda.biz;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import com.arcsolu.sopda.entity.Order;
import com.arcsolu.sopda.entity.OrderDetail;
import com.arcsolu.sopda.entity.Parametres.ParaKey;
import com.arcsolu.sopda.entity.Formule;
import com.arcsolu.sopda.entity.Printer;
import com.arcsolu.sopda.entity.Script;
import com.arcsolu.sopda.entity.Table;
import com.arcsolu.sopda.entity.Table.TableState;
import com.arcsolu.sopda.entity.User;

@SuppressLint({ "SdCardPath", "SimpleDateFormat" })
class ImpBizOrder implements BizOrder, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static int turn = 1;

	/**
	 * try to read order from pad, if read, get the turn instead of the value
	 * default.
	 */
	public ImpBizOrder() {
		try {
			FileInputStream fs;
			Order order = null;
			fs = new FileInputStream(ImpBizApp.fileDir + "/Order.dat");
			ObjectInputStream oi = new ObjectInputStream(fs);
			Object o1 = oi.readObject();
			if (o1 instanceof Order) {
				order = (Order) o1;
			}
			oi.close();
			fs.close();
			int i = 1;
			for (OrderDetail or : order.Details) {
				if (or.turn > i) {
					i = or.turn;
				}
			}
			for (OrderDetail or : order.Details) {
				if (or.turn == i) {
					if (or.sent == true) {
						turn = i + 1;
						break;
					} else {
						turn = i;
						break;
					}
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @author user send this turn of the order to the kitchen, and print it.
	 * @param order
	 * @return
	 */
	@Override
	public boolean SendOrder(Order order) {
		List<OrderDetail> lod = new ArrayList<OrderDetail>();
		// if(turn<Integer.parseInt(BizCache.Params.get(ParaKey.TURN))
		for (OrderDetail detail : order.Details) {
			if (detail.sent == false) {
				detail.turn = turn;
				lod.add(detail);
			}
			// detail.sent = true;
		}

		ReportBuilder builder = new ReportBuilder();
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		Date c = new Date(System.currentTimeMillis());
		order.LastSendTime = formatter.format(c);
		String script;
		HashMap<Printer, List<OrderDetail>> detailMap = new HashMap<Printer, List<OrderDetail>>();
		for (Printer p : BizCache.Printers) {
			List<OrderDetail> ol = new ArrayList<OrderDetail>();
			for (OrderDetail od : lod) {

				if (od.Menu.Printers.contains(p)) {
					ol.add(od);
				}
			}
			if (ol.size() > 0) {
				detailMap.put(p, ol);
			}
		}
		PrinterTools pt = new PrinterTools();
		for (Printer p : detailMap.keySet()) {
			builder.InitParam();
			builder.AddParam("user", order.User);
			builder.AddParam("order", order);
			builder.AddParam("detail", detailMap.get(p));
			builder.AddParam("date", c);
			builder.AddParam("turn", lod.get(0).turn);

			for (Script s : p.Scripts) {
				script = s.ScriptText;
				builder.Parse(script);
				byte[] stream = builder.Run();
				if (pt.TestPrinter(p.Address.toString())) {
					pt.Connect(p.Address);
					System.out.println("printer connection ok!");
					// String s = "";
					 pt.SendStream(stream, stream.length);

					pt.Disconnect();
				} else {
					return false;

				}
			}
			for (OrderDetail od : detailMap.get(p)) {
				od.sent = true;
			}
		}
		/*
		 * for (OrderDetail od : order.Details) { System.out.print(od.Menu.Id +
		 * "\n"); for (Printer p : od.Menu.Printers) { System.out.print(p.Id +
		 * "\n"); for (Script s : p.Scripts) { script = s.ScriptText;
		 * builder.Parse(script); byte[] stream = builder.Run(); PrinterTools pt
		 * = new PrinterTools(); if (pt.Connect(p.Address.toString())) {
		 * System.out.println("printer connection ok!"); // String s = "";
		 * 
		 * pt.SendStream(stream, stream.length); pt.Disconnect(); } else {
		 * System.err.println("connection error!!!"); } } } }
		 */
		turn++;
		return true;

	}

	/**
	 * @author user insert a statement in the database as finish order.
	 * @param order
	 * @return
	 * @throws SQLException
	 */
	@Override
	public boolean PrintOrder(Order order) throws SQLException {
		ImpBizApp.fd.Connect();
		ImpBizApp.fd.StartTransAction();
		PreparedStatement ps;
		Date now = new Date(System.currentTimeMillis());
		Long deta = (now.getTime() - order.LastLadditionTime.getTime()) / 60000;
		order.LastLadditionTime = now;
		if (deta >= 3) {
			ParaKey key = ParaKey.PRINTER_DF_ID;
			String str = BizCache.Params.get(key);
			ps = ImpBizApp.fd.con
					.prepareStatement("INSERT INTO ORDER_PRINT_LIST (BILL_ID,BILL_TYPE,PRINT_STATE,PRINTER_ID) VALUES ('"
							+ order.Id + "', 10,'N','" + str + "')");
			ps.execute();
			ImpBizApp.fd.CommitTransAction();
			ps.close();
		}
		ImpBizApp.fd.Disconnect();
		return true;
	}

	/**
	 * @author user save prder in pad
	 * @param order
	 * @return
	 */
	@Override
	public boolean SaveOrder(Order order) {
		try {
			File file = new File(ImpBizApp.fileDir + "/Order.dat");
			file.createNewFile();
			ObjectOutputStream os = new ObjectOutputStream(
					new FileOutputStream(file));
			os.writeObject(order);
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
	 * @author user insert a statement in the database as call service, each
	 *         call is separated in 3 minutes.
	 * @param order
	 * @return
	 * @throws SQLException
	 */
	@Override
	public boolean CallService(Order order) throws SQLException {
		ImpBizApp.fd.Connect();
		ImpBizApp.fd.StartTransAction();
		PreparedStatement ps;
		Date now = new Date(System.currentTimeMillis());
		Long deta = (now.getTime() - order.LastCallTime.getTime()) / 60000;
		order.LastCallTime = now;
		if (deta >= 3) {

			ParaKey key = ParaKey.PRINTER_DF_ID;
			String str = BizCache.Params.get(key);
			ps = ImpBizApp.fd.con
					.prepareStatement("INSERT INTO ORDER_PRINT_LIST (BILL_ID,BILL_TYPE,PRINT_STATE,PRINTER_ID) VALUES ('"
							+ order.Id + "', 12,'N','" + str + "')");
			ps.execute();
			ImpBizApp.fd.CommitTransAction();
			ps.close();

		}
		ImpBizApp.fd.Disconnect();
		return true;
	}

	/**
	 * @author user create an order as to start order.
	 * @param user
	 * @param table
	 * @param customers
	 * @param formules
	 * @return order
	 * @throws SQLException
	 */
	@SuppressLint("SimpleDateFormat")
	@Override
	public Order CreateOrder(User user, Table table, int customers,
			Map<Formule, Integer> formules) throws SQLException {
		if (user == null || table == null) {
			throw new NullPointerException();
		}
		Order order = new Order(user, table);
		order.Details = new ArrayList<OrderDetail>();
		order.Table = table;
		order.User = user;
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		Date c = new Date(System.currentTimeMillis());
		order.StartTime = formatter.format(c);
		table.State = TableState.tsOccupied;
		order.ClientNumber = customers;
		Table t = table;

		ImpBizApp.fd.Connect();

		ResultSet rs = null;
		ImpBizApp.fd.StartTransAction();
		PreparedStatement ps;
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
							+ ImpBizTable.EnumToInt(t.State)
							+ ") MATCHING (REST_TABLE_ID)");

			ps.execute();
			} catch (SQLException e) {
				absDB.showSQLException(e);
				e.printStackTrace();
			}
		Statement proc = ImpBizApp.fd.con.createStatement();

		boolean valid = false;
		while (!valid) {
			rs = proc.executeQuery("select * from proc_uuid");
			rs.next();
			String uuid = rs.getString(1);
			String qury = "execute procedure PROC_PAD_NEW_ORDER ('"
					+ order.Table.Id + "'," + customers + ",'" + order.User.Id
					+ "','" + uuid + "');";
			if (proc.executeUpdate(qury) == 0) {
				valid = true;
				order.Id = uuid;
			}
		}
		valid = false;
		for (Formule forml : formules.keySet()) {
			while (!valid) {
				rs = proc.executeQuery("select * from proc_uuid");
				rs.next();
				String uuid = rs.getString(1);
				String query = "execute procedure PROC_MBL_EDIT_ORDER_DETAIL ('"
						+ order.Id
						+ "','"
						+ uuid
						+ "','"
						+ forml.Id
						+ "',"
						+ formules.get(forml)
						+ ","
						+ forml.Price
						+ ","
						+ 1
						+ ",null);";
				if (proc.executeUpdate(query) == 0) {
					valid = true;
				}
			}
			valid = false;
		}
		ImpBizApp.fd.CommitTransAction();

		ImpBizApp.fd.Disconnect();
		return order;

	}

	/**
	 * @author user start time of the order.
	 * @param order
	 * @return
	 */
	@Override
	public String GetStartTime(Order order) {
		if (order == null)
			return null;
		else
			return order.StartTime;
	}

	/**
	 * @author user last send order's time
	 * @param order
	 * @return
	 */
	@Override
	public String GetLastSendTime(Order order) {
		if (order == null)
			return null;
		else
			return order.LastSendTime;
	}

	/**
	 * @author user to active the software each week
	 * @return
	 */
	@Override
	public boolean IsActive() {
		URL url;
		long now = 0;
		Calendar cal = Calendar.getInstance();
		try {
			url = new URL("http://0.fr.pool.ntp.org");
			HttpURLConnection uc = (HttpURLConnection) url.openConnection();// 生成连接对象
			uc.setConnectTimeout(500);
			uc.setReadTimeout(1000);
			uc.connect(); // 发出连接
			now = uc.getDate(); // 取得网站日期时间
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			now = System.currentTimeMillis();
		}
		Date date = new Date(now);
		//SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		cal.setTime(date);
		cal.setTimeZone(TimeZone.getDefault());
		if (BizCache.FBParams.get(ParaKey.LASTCHECK) == null) {
			BizCache.FBParams.put(ParaKey.LASTCHECK, "0");
		}
		int terminal = Integer
				.valueOf(BizCache.FBParams.get(ParaKey.LASTCHECK));
		if (cal.get(Calendar.DAY_OF_WEEK) == 5)
			if ((cal.get(Calendar.HOUR_OF_DAY)<13||cal.get(Calendar.HOUR_OF_DAY)>12)
					|| ((cal.get(Calendar.HOUR_OF_DAY) >19||cal.get(Calendar.HOUR_OF_DAY) <20) && (!BizCache.FBParams
							.get(ParaKey.LASTCHECK).equals("0")))) {
				try {
					Util.sendRequest();
					String str =BizCache.FBParams.get(ParaKey.CHECK);
					str = str.trim();
					if (str.length() > 0) {
						int checknumber = Integer.parseInt(str);
						if (checknumber == 1) {
							BizCache.FBParams.put(ParaKey.LASTCHECK, "0");
							BizCache.FBParams.put(ParaKey.TRIAL, "5");
							try {
								ObjectOutputStream 	os = new ObjectOutputStream(new FileOutputStream(new File(ImpBizApp.fileDir + "/fbparams.dat")));
								os.writeObject(BizCache.FBParams);
								os.flush();
								os.close();
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
							return true;
						}
					}
					else return false;
				} catch (Exception e) {
					terminal++;
					BizCache.FBParams.put(ParaKey.LASTCHECK,
							String.valueOf(terminal));
				}
				if (terminal < 4) {
					BizCache.FBParams.put(ParaKey.TRIAL, "5");
					try {
						ObjectOutputStream 	os = new ObjectOutputStream(new FileOutputStream(new File(ImpBizApp.fileDir + "/fbparams.dat")));
						os.writeObject(BizCache.FBParams);
						os.flush();
						os.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return true;
				} else{
					try {
						ObjectOutputStream 	os = new ObjectOutputStream(new FileOutputStream(new File(ImpBizApp.fileDir + "/fbparams.dat")));
						os.writeObject(BizCache.FBParams);
						os.flush();
						os.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
			
					return false;
				}
			}
		try {
			ObjectOutputStream 	os = new ObjectOutputStream(new FileOutputStream(new File(ImpBizApp.fileDir + "/fbparams.dat")));
			os.writeObject(BizCache.FBParams);
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

		@Override
	public int getTrial() {
			
		if (BizCache.FBParams.get(ParaKey.TRIAL) == null) {
			BizCache.FBParams.put(ParaKey.TRIAL, "5");
		}
		int trial = Integer.valueOf(BizCache.FBParams.get(ParaKey.TRIAL));
		try{
		return trial;
	
	}finally{
		trial--;
		BizCache.FBParams.put(ParaKey.TRIAL, String.valueOf(trial));
		try {
			ObjectOutputStream 	os = new ObjectOutputStream(new FileOutputStream(new File(ImpBizApp.fileDir + "/fbparams.dat")));
			os.writeObject(BizCache.FBParams);
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		}
	@Override
	public boolean ActiveNow() {
		try {
			Util.sendRequest();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		String str = BizCache.FBParams.get(ParaKey.CHECK);
		str = str.trim();
		if (str.length() > 0) {
			int checknumber = Integer.parseInt(str);
			if (checknumber == 1) {
				BizCache.FBParams.put(ParaKey.LASTCHECK, "0");
				BizCache.FBParams.put(ParaKey.TRIAL, "5");
				try {
					ObjectOutputStream 	os = new ObjectOutputStream(new FileOutputStream(new File(ImpBizApp.fileDir + "/fbparams.dat")));
					os.writeObject(BizCache.FBParams);
					os.flush();
					os.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return true;
			}
		}
		return false;
	}
}
