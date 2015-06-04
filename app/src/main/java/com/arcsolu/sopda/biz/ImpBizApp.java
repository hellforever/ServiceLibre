package com.arcsolu.sopda.biz;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.arcsolu.sopda.entity.Floor;
import com.arcsolu.sopda.entity.Formule;
import com.arcsolu.sopda.entity.Menu;
import com.arcsolu.sopda.entity.Order;
import com.arcsolu.sopda.entity.Printer;
import com.arcsolu.sopda.entity.Script;
import com.arcsolu.sopda.entity.Table;
import com.arcsolu.sopda.entity.Parametres.ParaKey;
import com.arcsolu.sopda.entity.User;

/**
 * @return  ImpBizApp 
 *
 * @author user
 *
 */
/**
 * @author user
 *
 */
/**
 * @author user
 *
 */
/**
 * @author user
 *
 */
@SuppressLint({ "DefaultLocale", "SdCardPath" })
class ImpBizApp implements BizApp, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static FBDB fd = new FBDB();

private OnProgressCaller _caller;
	/*
	 * public static File fileDir = new File(
	 * "data/data/com.example.servicelibre/files");
	 */
	public static File fileDir;
	private double currentDownload;
	private double maxDownload = 0;
	private double currentUpdate;
	private double maxUpdate = 0;

	public ImpBizApp() {

	}

	
	/**
	 * @author user
	 * to check if there is already an order running in the pad if there were
	 * ,return the order if not ,return null
	 * @return 
	 */
	@Override
	public Order CheckOrder() {
		Order order = null;
		try {
			FileInputStream fs;
			fs = new FileInputStream(fileDir + "/Order.dat");
			ObjectInputStream oi = new ObjectInputStream(fs);
			Object o1 = oi.readObject();
			if (o1 instanceof Order) {
				order = (Order) o1;
			}
			oi.close();
			fs.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return order;
	}

	/**
	 * @author user
	 * 
	 * download the information of the waiters and floors, try to read the 
	 * information of the parametres and waiters in pad 
	 * to check the password of the waiter or waitress, if the input is correct,
	 * return the information of the waiter if not, return null
	 * @param password
	 * @return 
	 * @throws Exception
	 */
	@SuppressLint("DefaultLocale")
	@SuppressWarnings({ "unchecked", "resource" })
	@Override
	public User Login(String password) throws Exception {
		FileInputStream fs;
		Map<ParaKey, String> params = null;
		Map<ParaKey, String> fbparams = null;
		Object o1;
		try {
			fs = new FileInputStream(fileDir + "/params.dat");
			ObjectInputStream oi = new ObjectInputStream(fs);
			o1 = oi.readObject();
			if (o1 instanceof Map) {
				params = (Map<ParaKey, String>) o1;
			}
			fs = new FileInputStream(fileDir + "/fbparams.dat");
			oi = new ObjectInputStream(fs);
			o1 = oi.readObject();
			if (o1 instanceof Map) {
				fbparams = (Map<ParaKey, String>) o1;
			}
			oi.close();

		
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (params != null) {
			BizCache.Params = params;
			BizCache.FBParams = fbparams;
			fs = new FileInputStream(fileDir + "/users.dat");
			ObjectInputStream oii = new ObjectInputStream(fs);
			o1 = oii.readObject();
			if (o1 instanceof List) {
				BizCache.Users = (List<User>) o1;
			}
			oii.close();
			fs.close();
		} else {
			throw new Exception();

		}
		List<User> UserList = BizCache.Users;
		String str = Util.psdToMd5(password);
		for (User user : UserList) {
			if (password != null) {
				if (str.toUpperCase()
						.contentEquals(user.password.toUpperCase()))
					return user;
			}
		}
		return null;
	}

	/**
	 * @author user
	 * download all the datas
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean Download() throws Exception {

		DownloadFloors();
		// DownloadPrinters();
		//DownloadMenus();
		DownloadUsers();
		DownloadParams();
		DownloadFormules();
		return true;
	}

	/**
	 * @author user
	 * delete all the menus and relationship between menu and printer in the
	 * database, then update all the menus in the cache and their relations to
	 * the database
	 * @return
	 */
	@Override
	public boolean Update() {
		fd.Connect();
		fd.StartTransAction();
		BizCache.DeletedMenus.clear();
		this.currentUpdate = 0;
		this.maxUpdate = BizCache.Menus.size();
		PreparedStatement ps;
		PreparedStatement ps1 = null;
		try {
			ps = ImpBizApp.fd.con
					.prepareStatement("DELETE FROM PAD_MENU_PRINTER_RELAT;");
			ps.execute();
			ps = ImpBizApp.fd.con.prepareStatement("DELETE FROM PAD_MENU;");
			ps.execute();
			for (Menu m : BizCache.Menus) {
				InputStream iss = null;
				if (m.Pic != null) {
					if (_caller != null) {
										_caller.OnProgress(this.currentUpdate, this.maxUpdate);
									}
									
					iss = new ByteArrayInputStream(m.Pic);
					ps = ImpBizApp.fd.con
							.prepareStatement("UPDATE OR INSERT INTO PAD_MENU (F_ID,F_CATALOGE,F_INDEX,F_LIMIT,F_DISPLAY,F_CHEF,F_NEW,F_PIC,F_TOP_5,F_NAME,F_DELETED,F_AVAILABLE) VALUES (?,?,?,?,?,?,?,?,?,?,?,?) MATCHING (F_ID)");
					ps.setString(1, m.Id);
					ps.setString(2, m.Catalog);
					ps.setInt(3, m.Index);
					ps.setInt(4, m.Limit);
					ps.setString(5, m.Display);
					ps.setBoolean(6, m.Chef);
					ps.setBoolean(7, m.NEW);
					ps.setBlob(8, iss);
					ps.setBoolean(9, m.Top5);
					ps.setString(10, m.Name);
					ps.setBoolean(11, m.Deleted);
					ps.setBoolean(12, m.IsVailable);
					ps.execute();
					this.currentUpdate = this.currentUpdate + 1;
				} else {
				if (_caller != null) {
										_caller.OnProgress(this.currentUpdate, this.maxUpdate);
									}
					ps = ImpBizApp.fd.con
							.prepareStatement("UPDATE OR INSERT INTO PAD_MENU (F_ID,F_CATALOGE,F_INDEX,F_LIMIT,F_DISPLAY,F_CHEF,F_NEW,F_TOP_5,F_NAME,F_DELETED,F_AVAILABLE) VALUES (?,?,?,?,?,?,?,?,?,?,?) MATCHING (F_ID)");
					ps.setString(1, m.Id);
					ps.setString(2, m.Catalog);
					ps.setInt(3, m.Index);
					ps.setInt(4, m.Limit);
					ps.setString(5, m.Display);
					ps.setBoolean(6, m.Chef);
					ps.setBoolean(7, m.NEW);
					ps.setBoolean(8, m.Top5);
					ps.setString(9, m.Name);
					ps.setBoolean(10, m.Deleted);
					ps.setBoolean(11, m.IsVailable);
					ps.execute();
					this.currentUpdate = this.currentUpdate + 1;
				}
			}
			for (Menu m : BizCache.Menus) {
				for (Printer p : m.Printers) {
					ps1 = ImpBizApp.fd.con
							.prepareStatement("UPDATE OR INSERT INTO PAD_MENU_PRINTER_RELAT (F_MENU_ID,F_PRINTER_ID) VALUES (?,?) MATCHING (F_MENU_ID,F_PRINTER_ID);");
					ps1.setString(1, m.Id);
					ps1.setString(2, p.Id);
					ps1.execute();
				}
			}
			if (ps1 != null) {
				ps1.close();
			}
			ImpBizApp.fd.CommitTransAction();
			ps.close();

				if (_caller != null) {
								_caller.OnFinished();
							}
		} catch (SQLException e) {
			absDB.showSQLException(e);
			e.printStackTrace();

		}
		fd.Disconnect();
		return true;
	}

	
	/**
	 * @author user
	 *delete all the floors and relationship between floors and tables in the
	 * cache, then download all the floors in database and their relations to
	 * the cache
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean DownloadFloors() throws Exception {
		ImpBizApp.fd.Connect();
		Statement stmt;

		stmt = ImpBizApp.fd.con.createStatement();

		ResultSet rs = stmt
				.executeQuery("select * from REST_FLOOR f full join REST_TABLE t on t.REST_FLOOR_ID=f.REST_FLOOR_ID ORDER BY t.REST_FLOOR_ID;");
		if (rs == null) {
			System.out.println("select floor error!");
			return false;
		}
		String fid = "";
		Floor currentFloor = null;
		BizCache.Floors.clear();
		while (rs.next()) {

			if (fid.compareTo(rs.getString(1)) == 0) {
				Table t = new Table(currentFloor);
				t.Id = rs.getString("rest_table_id");
				t.State = ImpBizTable.IntToEnum(rs
						.getInt("rest_table_state_id"));
				t.Number = rs.getString("rest_table_number");
				t.SeatCount = rs.getShort("rest_table_seat_count");
				t.CurrentOrderId = rs.getString("CURRENT_ORDER_ID");
				t.Point_x = rs.getInt("point_x");
				t.Point_y = rs.getInt("point_y");
				t.TableType = rs.getString("rest_table_type_id");
				t.TableHeight = rs.getInt("table_height");
				t.TableWidth = rs.getInt("table_width");
				// currentFloor.Tables.add(t);
			} else {
				if (currentFloor != null) {
					BizCache.Floors.add(currentFloor);
				}
				currentFloor = new Floor();

				currentFloor.Id = rs.getString(1);
				fid = currentFloor.Id;
				currentFloor.Floor = rs.getString("rest_floor");
				currentFloor.Img = Util.blob2ByteArr((rs.getBlob("bg_image")));
				currentFloor.Tables = new ArrayList<Table>();
				if(rs.getString("rest_table_id")!=null){
				Table t = new Table(currentFloor);
				t.Id = rs.getString("rest_table_id");
				t.State = ImpBizTable.IntToEnum(rs
						.getInt("rest_table_state_id"));
				t.Number = rs.getString("rest_table_number");
				t.SeatCount = rs.getInt("rest_table_seat_count");
				t.Point_x = rs.getInt("point_x");
				t.Point_y = rs.getInt("point_y");
				t.TableType = rs.getString("rest_table_type_id");
				t.TableHeight = rs.getInt("table_height");
				t.TableWidth = rs.getInt("table_width");
				// currentFloor.Tables.add(t);
				}
			}
		}
		BizCache.Floors.add(currentFloor);
		rs.close();
		stmt.close();

		ImpBizApp.fd.Disconnect();
		return true;
	}

	
	/**
	 * @author user
	 * download printers from the database delete all the menus and relationship
	 * between menu and printer in the cache, then download all the menus in the
	 * database and their relations to the cache
	 * finally save the information into the pad
	 * @return
	 */
	@SuppressLint("SdCardPath")
	@SuppressWarnings("resource")
	@Override
	public boolean DownloadMenus() {
		DownloadPrinters();
		ImpBizApp.fd.Connect();
		this.currentDownload = 0;
		Statement ps;
		Statement ps1;
		try {
			ps = ImpBizApp.fd.con.createStatement();
			ps1 = ImpBizApp.fd.con.createStatement();
			ResultSet rs = (ResultSet) ps
					.executeQuery("select count(*) total from pad_menu m ");
if (rs != null) {
			rs.next();
			this.maxDownload = rs.getInt("total");
}
			rs = (ResultSet) ps.executeQuery("select * from pad_menu m ");
			if (rs == null) {
				System.out.println("select menu error!");
				return false;
			}

			Menu currentMenu = null;
			BizCache.Menus.clear();
			while (rs.next()) {
if (_caller != null) {
					_caller.OnProgress(this.currentDownload, this.maxDownload);
				}
				currentMenu = new Menu();
				currentMenu.Id = rs.getString("f_id");
				currentMenu.Display = rs.getString("f_display");
				currentMenu.Index = rs.getInt("f_index");
				currentMenu.Limit = rs.getInt("f_limit");
				currentMenu.Pic = Util.blob2ByteArr((rs.getBlob("f_pic")));
				currentMenu.Catalog = rs.getString("f_cataloge");
				currentMenu.Top5 = rs.getBoolean("f_top_5");
				currentMenu.Chef = rs.getBoolean("f_chef");
				currentMenu.NEW = rs.getBoolean("f_new");
				currentMenu.Name = rs.getString("f_name");
				currentMenu.Deleted = rs.getBoolean("f_deleted");
				currentMenu.IsVailable = rs.getBoolean("f_available");
				this.currentDownload = this.currentDownload + 1;
				if (currentMenu.Deleted == false) {
					BizCache.Menus.add(currentMenu);
				} else
					BizCache.DeletedMenus.add(currentMenu);

			}
if (_caller != null) {
				_caller.OnFinished();
			}
			ResultSet rs1 = (ResultSet) ps1
					.executeQuery("select * from PAD_MENU_PRINTER_RELAT order by f_menu_id");
			while (rs1.next()) {
				String str = rs1.getString("F_printer_ID");
				String st = rs1.getString("f_menu_id");
				Menu menu = null;
				Printer printer = null;
				for (Menu m : BizCache.Menus) {
					if (m.Id.equals(st)) {
						menu = m;
					}
				}
				for (Printer p : BizCache.Printers) {
					if (p.Id.equals(str)) {
						printer = p;
					}
				}
				menu.Printers.add(printer);
			}

			rs.close();
			ps.close();
			rs1.close();
			ps1.close();
			ImpBizApp.fd.Disconnect();
		} catch (SQLException e) {
			absDB.showSQLException(e);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}
		try {
			File file = new File(fileDir + "/menus.dat");
			if (file.getParentFile().mkdirs()) {
				file.createNewFile();
			}
			file.setWritable(true);
			ObjectOutputStream os = new ObjectOutputStream(
					new FileOutputStream(file));
			os.writeObject(BizCache.Menus);
			os.flush();
			File file2 = new File(fileDir + "/deletedmenus.dat");
			if (file2.getParentFile().mkdirs()) {
				file2.createNewFile();
			}
			file2.setWritable(true);
			os = new ObjectOutputStream(new FileOutputStream(file2));
			os.writeObject(BizCache.DeletedMenus);
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * @author user
	 * delete printers in the cache ,then download printers,scripts from the database
	 * and the relationship between printer and script.
	 *  finally save the information into the pad
	 * @return
	 */
	@Override
	public boolean DownloadPrinters() {
		ImpBizApp.fd.Connect();
		Statement ps;
		Statement ps1;
		try {
			ps = ImpBizApp.fd.con.createStatement();
			ps1 = ImpBizApp.fd.con.createStatement();

			ResultSet rs = (ResultSet) ps
					.executeQuery("select *  from PAD_SCRIPT; ");
			BizCache.Scripts.clear();
			if (rs == null) {
				System.out.println("Download script error!");
				return false;
			}
			while (rs.next()) {
				Script script = new Script();
				script.Description = rs.getString("F_DESCRIPTION");
				script.Id = rs.getString("f_id");
				Blob t = rs.getBlob("f_Script");
				script.ScriptText = new String(t.getBytes(1, (int) t.length()));
				BizCache.Scripts.add(script);
			}
			rs = (ResultSet) ps.executeQuery("select *  from Printer;");
			BizCache.Printers.clear();
			if (rs == null) {
				System.out.println("Download printer error!");
				return false;
			}
			while (rs.next()) {
				Printer currentPr = new Printer();
				currentPr.Id = rs.getString("printer_id");
				currentPr.Address = rs.getString("printer_address");
				currentPr.Name = rs.getString("printer_name");

				BizCache.Printers.add(currentPr);
			}
			rs.close();
			ps.close();
			ResultSet rs1 = (ResultSet) ps1
					.executeQuery("select * from PAD_PRINTER_SCRIPT_RELAT order by F_printer_ID;");
			while (rs1.next()) {
				String str;
				str = rs1.getString("F_printer_ID");
				String st;
				st = rs1.getString("F_script_id");
				Printer printer = null;
				Script script = null;
				for (Printer ptr : BizCache.Printers) {
					if (ptr.Id.equals(str)) {
						printer = ptr;
					}
				}
				for (Script srt : BizCache.Scripts) {
					if (srt.Id.equals(st)) {
						script = srt;
					}
				}
				printer.Scripts.add(script);
			}
			rs1.close();
			ps1.close();
			ImpBizApp.fd.Disconnect();
		} catch (SQLException e) {
			absDB.showSQLException(e);
			e.printStackTrace();
		}

		ObjectOutputStream os;
		try {
			File file = new File(fileDir + "/printers.dat");
			if (file.getParentFile().mkdirs()) {
				file.createNewFile();
			}
			file.setWritable(true);
			os = new ObjectOutputStream(new FileOutputStream(file));
			os.writeObject(BizCache.Printers);
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}


	/**
	 * @author user
	 * read parametre from the pad ,check the password of the master, if
	 * parametre is null, set the password 888888
	 * @param password
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean AdminLogin(String password) {
		Map<ParaKey, String> params = null;
		try {
			FileInputStream fs;
			fs = new FileInputStream(fileDir + "/params.dat");
			ObjectInputStream oi = new ObjectInputStream(fs);
			Object o1 = oi.readObject();
			if (o1 instanceof Map) {
				params = (Map<ParaKey, String>) o1;
			}
			oi.close();
			fs.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if (params == null) {
			ParaKey key = ParaKey.PASSWORD;
			BizCache.Params.put(key, Util.psdToMd5("888888"));
			ObjectOutputStream os;
			try {
				File file = new File(fileDir + "/params.dat");
				if (file.getParentFile().mkdirs()) {
					file.createNewFile();
				}
				file.setWritable(true);
				os = new ObjectOutputStream(new FileOutputStream(file));
				os.writeObject(BizCache.Params);
				os.flush();
				os.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String str = Util.psdToMd5(password);
		if (password != null)
			return str.toUpperCase().contentEquals(
					GetParam(ParaKey.PASSWORD).toUpperCase());
		else
			return false;

	}

	/**
	 * @author user
	 * get the parameter of the system
	 * @param key
	 * @return parakey
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String GetParam(ParaKey key) {
		if (key.toString().equals("ADDRESS")
				|| key.toString().equals("DATABASE")
				|| key.toString().equals("MASTER")
				|| key.toString().equals("DB_PASSWORD")
				|| key.toString().equals("MAPMODE")
				|| key.toString().equals("CHECK")
				|| key.toString().equals("LASTCHECK")
				|| key.toString().equals("TRIAL")) {
			try {
				FileInputStream fs;
				fs = new FileInputStream(fileDir + "/fbparams.dat");

				Map<ParaKey, String> fbparams = null;

				ObjectInputStream oi = new ObjectInputStream(fs);
				Object o1 = oi.readObject();
				if (o1 instanceof Map) {
					fbparams = (Map<ParaKey, String>) o1;
				}
				oi.close();
				fs.close();
				if (fbparams != null) {
					BizCache.FBParams = fbparams;
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

			return BizCache.FBParams.get(key);
		} else {
			try {
				FileInputStream fs = new FileInputStream(fileDir
						+ "/params.dat");
				Map<ParaKey, String> params = null;

				ObjectInputStream oi = new ObjectInputStream(fs);
				Object o1 = oi.readObject();
				if (o1 instanceof Map) {
					params = (Map<ParaKey, String>) o1;
				}
				oi.close();
				fs.close();
				if (params != null) {
					BizCache.Params = params;
				}

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (StreamCorruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return BizCache.Params.get(key);
		}
	}

	/**
	 * @author user
	 * delete the file of order in pad, make the turn as 1
	 * @param order
	 * @return
	 */
	@SuppressLint("SdCardPath")
	@Override
	public boolean CloseOrder(Order order) {
		order = null;
		File fs = new File(fileDir + "/Order.dat");
		fs.delete();
		ImpBizOrder.turn = 1;
		return true;
	}

	
	/**
	 * @author user
	 *  set the password of the master, and save it in the cache 
	 * @param password
	 * @return
	 */
	@Override
	public boolean SetAdminPassword(String password) {
		String str = Util.psdToMd5(password);
		SetParam(ParaKey.PASSWORD, str);
		return true;
	}
	
	/**
	 * @author user
	 *  set the parameters of the master, and save it in the cache and pad.
	 * @param key
	 * @param value
	 * @return
	 */
	@Override
	public boolean SetParam(ParaKey key, String value) {
		if (key.toString().equals("ADDRESS")
				|| key.toString().equals("DATABASE")
				|| key.toString().equals("MASTER")
				|| key.toString().equals("DB_PASSWORD")
				|| key.toString().equals("MAPMODE")
				|| key.toString().equals("CHECK")
				|| key.toString().equals("LASTCHECK")
				|| key.toString().equals("TRIAL")) {
			if (BizCache.FBParams.containsKey(key)) {
				BizCache.FBParams.remove(key);
			}
			BizCache.FBParams.put(key, value);
			File file = new File(fileDir + "/fbparams.dat");
			ObjectOutputStream os;
			try {
				if (file.getParentFile().mkdirs()) {
					file.createNewFile();
				}
				file.setWritable(true);

				os = new ObjectOutputStream(new FileOutputStream(file));
				os.writeObject(BizCache.FBParams);
				os.flush();
				os.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			if (BizCache.Params.containsKey(key)) {
				BizCache.Params.remove(key);
			}
			BizCache.Params.put(key, value);
			ObjectOutputStream os;
			try {
				File file = new File(fileDir + "/params.dat");
				if (file.getParentFile().mkdirs()) {
					file.createNewFile();
				}
				file.setWritable(true);
				os = new ObjectOutputStream(new FileOutputStream(file));

				os.writeObject(BizCache.Params);
				os.flush();
				os.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return true;
	}
	
	/**
	 * @author user
	 * clean the parameters in the cache, download the parameters 
	 * from the database to the cache, and write into pad,
	 * if the password is null, set it as 888888
	 * @return
	 */
	@Override
	public boolean DownloadParams() {
		ImpBizApp.fd.Connect();
		Statement ps;
		try {
			ps = ImpBizApp.fd.con.createStatement();
			ResultSet rs = (ResultSet) ps
					.executeQuery("select * from pad_param");
			BizCache.Params.clear();
			while (rs.next()) {
				BizCache.Params.put(Util.StringToEnum((rs.getString("f_key"))),
						rs.getString("f_value"));
			}
			rs.close();
			ps.close();
			ImpBizApp.fd.Disconnect();
		} catch (SQLException e) {
			absDB.showSQLException(e);
			e.printStackTrace();
		}
		ParaKey key = ParaKey.PASSWORD;
		if (BizCache.Params.get(key) == null) {
			BizCache.Params.remove(key);
			BizCache.Params.put(key, Util.psdToMd5("888888"));

		}
		try {
			File file = new File(fileDir + "/params.dat");
			if (file.getParentFile().mkdirs()) {
				file.createNewFile();
			}
			file.setWritable(true);
			ObjectOutputStream os = new ObjectOutputStream(
					new FileOutputStream(file));
			os.writeObject(BizCache.Params);
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	/**
	 * @author user
	 * update the parakey to the database
	 * @param Key
	 * @return
	 */
	@Override
	public boolean UpdateParams(ParaKey Key) {
		ImpBizApp.fd.Connect();
		ImpBizApp.fd.StartTransAction();
		Statement ps;
		try {
			ps = ImpBizApp.fd.con.createStatement();
			ps.execute("UPDATE OR INSERT INTO pad_param (F_KEY,F_VALUE) VALUES ('"
					+ Key.toString()
					+ "', '"
					+ BizCache.Params.get(Key)
					+ "') MATCHING (F_KEY)");
			ps.close();
			ImpBizApp.fd.CommitTransAction();
			ImpBizApp.fd.Disconnect();
		} catch (SQLException e) {
			absDB.showSQLException(e);
			e.printStackTrace();
		}
		return true;
	}
	/**
	 * @author user
	 * download the information of the waiters to the cache ,and save it in pad.
	 * @return
	 */
	@Override
	public boolean DownloadUsers() {
		ImpBizApp.fd.Connect();
		Statement ps;
		try {
			ps = ImpBizApp.fd.con.createStatement();

			ResultSet rs = ps
					.executeQuery("select employee_id,employee_password,employee_name from employee");
			BizCache.Users.clear();
			while (rs.next()) {
				User item = new User();
				item.Id = rs.getString("employee_id");
				item.Name = rs.getString("employee_name");
				item.password = rs.getString("employee_password");
				BizCache.Users.add(item);
			}
			rs.close();
			ps.close();
			ImpBizApp.fd.Disconnect();
		} catch (SQLException e) {
			absDB.showSQLException(e);
			e.printStackTrace();
		}
		try {
			File file = new File(fileDir + "/users.dat");
			if (file.getParentFile().mkdirs()) {
				file.createNewFile();
			}
			file.setWritable(true);
			ObjectOutputStream os = new ObjectOutputStream(
					new FileOutputStream(file));
			os.writeObject(BizCache.Users);
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * @author user
	 * construct a database in the server, and try to connect it and disconnect it.
	 * return true if database can be connect, false if not
	 * @param server
	 * @param db
	 * @param user
	 * @param pwd
	 * @return 
	 */
	@Override
	public boolean GetFBDB(String server, String db, String user, String pwd) {
		fd = new FBDB(server, db, user, pwd);
		/*ParaKey p = ParaKey.ADDRESS;
		SetParam(p, server);
		p = ParaKey.DATABASE;
		SetParam(p, db);
		p = ParaKey.MASTER;
		SetParam(p, user);
		p = ParaKey.DB_PASSWORD;
		SetParam(p, pwd);*/
		boolean bl = fd.Connect();
		fd.Disconnect();
		return bl;
	}

	/**
	 * @author user
	 * return true if database can be connect, false if not
	 * @return 
	 */
	public boolean IsConnect(Context context) {
	/*	if (fd == null) {
			return false;
		} else {
			boolean bl = fd.Connect();
			fd.Disconnect();
			return bl;
		}*/
		ConnectivityManager con=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo network=con.getActiveNetworkInfo();
		if(network==null||!network.isConnected()){
			return false;
		}
		if(network.isConnected()){
			return true;
		}
		return false;

	}

	/**
	 * @author user
	 * set the directory of the file we are going to save.
	 * @param file
	 * @return 
	 */
	@Override
	public boolean SetFileDir(File file) {
		fileDir = new File(file.getAbsolutePath());
		return true;
	}

	/**
	 * @author user
	 * clean the cache, download the Packages from the database and save it in pad
	 * @return
	 */
	@Override
	public boolean DownloadFormules() {
		ImpBizApp.fd.Connect();
		Statement ps;
		try {
			ps = ImpBizApp.fd.con.createStatement();
			ResultSet rs = (ResultSet) ps
					.executeQuery("select m.menu_id, m.menu_name_display,mn.menu_price from menu m inner join menu_price mn on m.menu_id=mn.menu_id where OPERATION_TYPE_ID=0 ");
			BizCache.FormulesAvailable.clear();
			BizCache.Formules.clear();
			while (rs.next()) {
				Formule fml = new Formule();
				fml.Id = rs.getString(1);
				fml.Name = rs.getString(2);
				fml.Price = rs.getDouble(3);
				BizCache.Formules.add(fml);
			}

			rs.close();
			ps.close();
			ImpBizApp.fd.Disconnect();
		} catch (SQLException e) {
			absDB.showSQLException(e);
			e.printStackTrace();
		}
		try {
			File file = new File(fileDir + "/formules.dat");
			if (file.getParentFile().mkdirs()) {
				file.createNewFile();
			}
			file.setWritable(true);
			ObjectOutputStream os = new ObjectOutputStream(
					new FileOutputStream(file));
			os.writeObject(BizCache.Formules);
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * @author user
	 * get the RSAprivateKey 
	 * @return
	 */
	public RSAPrivateKey getPrivateKey() {
		try {
			String privateKeyStr = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALj0Alvn2772Wd1tr5WgxhtyaVkYLZdwhxGMJFWbXagLCcoA85F6Hb+dY3Svx8bTc7+i8TeasB8LeXOTspCmCTOyZe5PrkBX172ehL+1219Y1HxVFUjt5SeiwDU73sC5J/cS+I2KryKM18OYLN5IS6IqGg2CUGhA6bKqs9dRQWNlAgMBAAECgYEAoykn15ogQkOjnXLA/jf57IavJxjsR3fOwA0olJjeM3uhZCIYvVLEYaDf9zWHAQDTUNDXCZ2eF5UQzIQUeATxgbjH8O52OpNpIxaErQKNLJyVCTM5OqPiBGzzm9Zs4wkSueN8hvitvmjyv4C/4Jig/FE0Tp1T/oqT4VPwiop7e0ECQQDanOs38chqxZND6BfpT48oZiWFJxjMDBPeTDHeRw3uwqo8Dx3nXi6iBLTjCWvmxu37+S28jbDXlD6PqjLs+qQ1AkEA2JVuu8x1Hs+e2XtNU6nH2RrucM7aLVA3wfSnZuxkv65jNDcPU084mZ2h9KskqO8EslNV4CFuH1rPzdwShLBIcQJAd+AuN4o0rR/URhth6UUAKlYA4wfyWAmTZ4V+nvV1lWRXdwGPE00Y4y0Th5+l9HFHFLGpu8gynbQjriHVNQ+ntQJBAKStCiMUlEC3EErAK81fHCsBBScUwGMPyTVZ9iaVuwzbZWaALtDjPV/fsRK9RgSEqAeGwZbHFFoWzEXz/MiS2KECQCxxwJCA7lsj//cPhf/vFk7D7Ri9cpkdy2Gx+Bvt5Bhc0XTtv3OjB37UYaSOeVz4UcBRvd7SxF5drQUn9U8wzv8=";
			Base64 base = new Base64();
			byte[] keyBytes = base.decode(privateKeyStr.getBytes());
			// keyBytes = new BASE64Decoder().decodeBuffer(publicKeyStr);
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory
					.generatePrivate(keySpec);
			return privateKey;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @author user
	 * test if the serial number is correct
	 * @param strss
	 * @return
	 */
	@Override
	public boolean IsMactchedSerieNumber(String strss)  throws SQLException{
		long now = System.currentTimeMillis();
		Date date = new Date(now);
		//SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		Calendar  cal=Calendar.getInstance();
		cal.setTime(date);
		if (cal.get(Calendar.DAY_OF_WEEK)!=6){
			return true;
		}
		fd.Connect();
		String str = "";
		Statement stmt;
			stmt = fd.con.createStatement();
			ResultSet rs = (ResultSet) stmt
					.executeQuery("select F_message from pad_message where f_device_id='"
							+ strss + "';");
			rs.next();
			str = new String(rs.getBlob(1).getBytes(1,
					(int) rs.getBlob(1).length()));
	

		fd.Disconnect();
		RSAPrivateKey pri = getPrivateKey();
		String strs = null;

		try {
			strs = Util.decrypt(str, pri);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (strs != null) {
			if (strs.equals(strss))
				return true;
			else
				return false;
		}
		return false;
	}

	/**
	 * @author user
	 * get the device id.
	 * @return
	 */
	@Override
	public String getDeviceId() {
		return android.os.Build.SERIAL;
	}

	/**
	 * @author user
	 * save the logo picture 
	 * @param logoPic
	 * @return
	 */
	@Override
	public boolean setLogo(byte[] logoPic) {
		try {
			File file = new File(fileDir + "/Logo.dat");
			if (file.getParentFile().mkdirs()) {
				file.createNewFile();
			}
			file.setWritable(true);
			ObjectOutputStream os = new ObjectOutputStream(
					new FileOutputStream(file));
			os.writeObject(logoPic);
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * @author user
	 * get the logo picture from pad
	 * @return
	 */
	@Override
	public byte[] getLogo() {
		byte[] bts = null;
		try {
			FileInputStream fs;
			fs = new FileInputStream(fileDir + "/Logo.dat");
			ObjectInputStream oi = new ObjectInputStream(fs);
			Object o1 = oi.readObject();
			if (o1 instanceof byte[]) {
				bts = (byte[]) o1;
			}
			oi.close();
			fs.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return bts;
	}

	/**
	 * @author user
	 * show the percent of the progress download
	 * @return
	 */
	@Override
	public double getProgressDownload() {
		if (this.maxDownload == 0)
			return 1;
		else
			return this.currentDownload / this.maxDownload;
	}

	/**
	 * @author user
	 * show the percent of the progress download
	 * @return
	 */
	@Override
	public double getProgressUpdate() {
		if (this.maxUpdate == 0)
			return 1;
		else
			return this.currentUpdate / this.maxUpdate;
	}

	/**
	 * @author user
	 * save the background picture
	 * @param backPic
	 * @return
	 */
	@Override
	public boolean setBackground(byte[] backPic) {
		try {
			File file = new File(fileDir + "/Back.dat");
			if (file.getParentFile().mkdirs()) {
				file.createNewFile();
			}
			file.setWritable(true);
			ObjectOutputStream os = new ObjectOutputStream(
					new FileOutputStream(file));
			os.writeObject(backPic);
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * @author user
	 * get the background picture
	 * @return
	 */
	@Override
	public byte[] getBackground() {
		byte[] bts = null;
		try {
			FileInputStream fs;
			fs = new FileInputStream(fileDir + "/Back.dat");
			ObjectInputStream oi = new ObjectInputStream(fs);
			Object o1 = oi.readObject();
			if (o1 instanceof byte[]) {
				bts = (byte[]) o1;
			}
			oi.close();
			fs.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return bts;
	}

	/*
	 * private void sendMsg(int flag) { Handler handler = new Handler(); Message
	 * msg = new Message(); msg.what = flag; handler.sendMessage(msg); }
	 */

	@Override
	public void SetOnProgressCaller(OnProgressCaller caller) {
		_caller = caller;
	}
}



