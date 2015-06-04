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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.annotation.SuppressLint;

import com.arcsolu.sopda.entity.Formule;
import com.arcsolu.sopda.entity.Menu;
import com.arcsolu.sopda.entity.Printer;

@SuppressLint("SdCardPath")
class ImpBizMenu implements BizMenu, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * read menus ,deletemenus ,formules, availableformules from pad to cache. 
	 */
	@SuppressLint("SdCardPath")
	@SuppressWarnings({ "resource", "unchecked" })
	public ImpBizMenu() {
		FileInputStream fs;
		try {
			fs = new FileInputStream(ImpBizApp.fileDir + "/menus.dat");
			ObjectInputStream oi = new ObjectInputStream(fs);
			Object o1 = oi.readObject();
			if (o1 instanceof List) {
				BizCache.Menus = (List<Menu>) o1;
			}
			fs = new FileInputStream(ImpBizApp.fileDir + "/deletedmenus.dat");
			oi = new ObjectInputStream(fs);
			o1 = oi.readObject();
			if (o1 instanceof List) {
				BizCache.DeletedMenus = (List<Menu>) o1;
			}
			fs = new FileInputStream(ImpBizApp.fileDir + "/formules.dat");
			oi = new ObjectInputStream(fs);
			o1 = oi.readObject();
			if (o1 instanceof List) {
				BizCache.Formules = (List<Formule>) o1;
			}

			fs = new FileInputStream(ImpBizApp.fileDir
					+ "/availableformules.dat");

			oi = new ObjectInputStream(fs);
			o1 = oi.readObject();
			if (o1 instanceof List) {
				BizCache.FormulesAvailable = (List<Formule>) o1;
			}
			oi.close();
			fs.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @author user
	 * a list of menu from cache
	 * @return 
	 */
	public List<Menu> Select() {
		List<Menu> Menus = BizCache.Menus;
		return Menus;
	}

	/**
	 * @author user
	 * delete the menu in  cache.
	 * @param m
	 * @return 
	 */
	public boolean Delete(Menu m) {
		BizCache.Menus.remove(m);
		m.Deleted = true;
		BizCache.DeletedMenus.add(m);
		return true;
	}

	/**
	 * @author user
	 * clean up the deletedmenus in cache
	 * @return 
	 */
	public boolean EmptyTrush() {
		BizCache.DeletedMenus.clear();
		return true;
	}

	/**
	 * @author user
	 * In cache, undelete the menu and add it in the  menu list.
	 * @param m
	 * @return
	 */
	public boolean Undelete(Menu m) {
		BizCache.DeletedMenus.remove(m);
		Menu mn = GetMenu();
		m.Deleted = false;
		m.Index = mn.Index;
		BizCache.Menus.add(m);
		return true;
	}

	/**
	 * @author user
	 * a list of deleted menus from cache.
	 * @return 
	 */
	public List<Menu> SelectDeletedMenus() {
		return BizCache.DeletedMenus;
	}

	/**
	 * @author user
	 * save the lists of menus and deleted menus in pad.
	 * @param m
	 * @return
	 */
	@SuppressWarnings("resource")
	@Override
	public boolean Save(List<Menu> m) {
		try {
			File file = new File(ImpBizApp.fileDir + "/menus.dat");
			file.createNewFile();
			ObjectOutputStream os = new ObjectOutputStream(
					new FileOutputStream(file));
			os.writeObject(m);
			BizCache.Menus = m;
			os.flush();
			file = new File(ImpBizApp.fileDir + "/deletedmenus.dat");
			file.createNewFile();
			os = new ObjectOutputStream(new FileOutputStream(file));
			os.writeObject(SelectDeletedMenus());
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
	 * no use
	 * @param m
	 * @return
	 */
	public boolean InsertMenu(Menu m) {
		ImpBizApp.fd.Connect();
		ImpBizApp.fd.StartTransAction();
		PreparedStatement ps;
		try {
			InputStream iss = null;
			if (m.Pic != null) {
				iss = new ByteArrayInputStream(m.Pic);
			}
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
			for (Printer p : m.Printers) {
				ps = ImpBizApp.fd.con
						.prepareStatement("UPDATE OR INSERT INTO PAD_MENU_PRINTER_RELAT (F_MENU_ID,F_PRINTER_ID) VALUES (?,?) MATCHING (F_MENU_ID,F_PRINTER_ID);");
				ps.setString(1, m.Id);
				ps.setString(2, p.Id);
				ps.execute();
			}
			ImpBizApp.fd.CommitTransAction();
			ps.close();
		} catch (SQLException e) {
			absDB.showSQLException(e);
			e.printStackTrace();
		}
		ImpBizApp.fd.Disconnect();
		return true;
	}

	/**
	 * @author user
	 * construct a new menu whose index is the max of all. but not yet add it into the list of menu
	 * @return menu
	 */
	@Override
	public Menu GetMenu() {
		Menu m = new Menu();
		int x = -1;
		Random rd = new Random();
		m.Id = String.valueOf(rd.nextGaussian());
		for (Menu mn : BizCache.Menus) {
			if (x < mn.Index)
				x = mn.Index;
			while (mn.Id.contentEquals(m.Id)) {
				m.Id = String.valueOf(rd.nextGaussian());
			}
		}
		m.Index = x + 1;
		return m;
	}

	/**
	 * @author user
	 * get all the formules from cache.
	 * @return
	 */
	@Override
	public List<Formule> SelectFormule() {
		List<Formule> fmls = BizCache.Formules;
		return fmls;
	}

	/**
	 * @author user
	 * set the formule as an available one.
	 * @param fml
	 * @return
	 */
	@Override
	public boolean setFormule(Formule fml) {
		BizCache.FormulesAvailable.add(fml);
		return true;
	}
	
	/**
	 * @author user
	 * In cache ,remove the formule from the list of available
	 * @param fml
	 * @return
	 */
	@Override
	public boolean removeFormule(Formule fml) {
		BizCache.FormulesAvailable.remove(fml);
		return true;
	}

	/**
	 * @author user
	 * save the list of available formules in pad and update to the database
	 * @param m
	 * @return
	 */
	@Override
	public boolean SaveAvailableFormules(List<Formule> m) {
		try {
			File file = new File(ImpBizApp.fileDir + "/availableformules.dat");
			if (file.getParentFile().mkdirs()) {
				file.createNewFile();
			}
			file.setWritable(true);
			ObjectOutputStream os = new ObjectOutputStream(
					new FileOutputStream(file));
			os.writeObject(BizCache.FormulesAvailable);
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Statement stmt;
		try {
			ImpBizApp.fd.Connect();
			ImpBizApp.fd.StartTransAction();
			stmt = ImpBizApp.fd.con.createStatement();

			stmt.execute("delete from pad_param where f_key like 'fid_%';");
			for (Formule fml : m) {
				stmt.execute("INSERT INTO pad_param (F_KEY,F_VALUE) VALUES ('fid_"
						+ fml.Id + "','" + fml.Id + "' )");
			}
			stmt.close();
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
	 * get all the available formules from cache.
	 * @return
	 */
	@Override
	public List<Formule> SelectAvailableFormule() {
		List<Formule> fmls = BizCache.FormulesAvailable;
		return fmls;
	}

	/**
	 * @author user
	 * get all the names of available formules from database.
	 * @return
	 */
	@Override
	public List<String> getAvailableFormules() {
		ImpBizApp.fd.Connect();
		List<String> StringList = new ArrayList<String>();
		Statement ps;
		try {
			ps = ImpBizApp.fd.con.createStatement();
			ResultSet rs = (ResultSet) ps
					.executeQuery("select f_value from pad_param where  f_key like 'fid_%'");
			BizCache.FormulesAvailable.clear();
			while (rs.next()) {
				String str = rs.getString(1);
				StringList.add(str);
				for (Formule fml : BizCache.Formules) {
					if (fml.Id.equals(str)) {
						BizCache.FormulesAvailable.add(fml);
					}
				}
			}

			rs.close();
			ps.close();
			ImpBizApp.fd.Disconnect();
		} catch (SQLException e) {
			absDB.showSQLException(e);
			e.printStackTrace();
		}
		return StringList;
	}

	/**
	 * @author user
	 * get all the available menus in cache.
	 * @return
	 */
	@Override
	public List<Menu> SelectAvailableMenu() {
		List<Menu> lsa = new ArrayList<Menu>();
		for (Menu mn : BizCache.Menus) {
			if(mn.Printers.isEmpty()==true){
				mn.IsVailable=false;
			}
			if (mn.IsVailable == true) {
				lsa.add(mn);
			}
		}
		return lsa;
	}
}