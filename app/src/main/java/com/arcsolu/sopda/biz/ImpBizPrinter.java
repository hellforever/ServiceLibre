package com.arcsolu.sopda.biz;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import android.annotation.SuppressLint;

import com.arcsolu.sopda.entity.Menu;
import com.arcsolu.sopda.entity.Printer;
import com.arcsolu.sopda.entity.Script;

@SuppressLint("SdCardPath")
class ImpBizPrinter implements BizPrinter, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4674427982078397385L;

	/**
	 * read printer from pad
	 */
	@SuppressWarnings("unchecked")
	public ImpBizPrinter() {
		FileInputStream fs;
		try {
			fs = new FileInputStream(ImpBizApp.fileDir + "/printers.dat");

			ObjectInputStream oi = new ObjectInputStream(fs);
			Object o1 = oi.readObject();
			if (o1 instanceof List) {
				BizCache.Printers = (List<Printer>) o1;
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
	 * get printers from cache
	 * @return
	 */
	@Override
	public List<Printer> Select() {
		List<Printer> printers = BizCache.Printers;
		return printers;
	}

	/**
	 * @author user
	 * update the printer and its relationship.
	 * @param p
	 * @return
	 */
	public boolean InsertPrinter(Printer p) {
		ImpBizApp.fd.Connect();
		ImpBizApp.fd.StartTransAction();
		PreparedStatement ps;
		try {
			ps = ImpBizApp.fd.con
					.prepareStatement("UPDATE OR INSERT INTO printer (PRINTER_ID,PRINTER_NAME,PRINTER_ADDRESS) VALUES ('"
							+ p.Id
							+ "', '"
							+ p.Name
							+ "','"
							+ p.Address
							+ "') MATCHING (PRINTER_ID)");
			ps.execute();
			ps = ImpBizApp.fd.con
					.prepareStatement("Delete from PAD_PRINTER_SCRIPT_RELAT where f_printer_id='"
							+ p.Id + "';");
			ps.execute();
			ps = ImpBizApp.fd.con
					.prepareStatement("Delete from PAD_MENU_PRINTER_RELAT where f_printer_id='"
							+ p.Id + "';");
			ps.execute();
			for (Script s : p.Scripts) {
				ps = ImpBizApp.fd.con
						.prepareStatement("UPDATE OR insert into PAD_PRINTER_SCRIPT_RELAT (f_printer_id,f_script_id) values (?,?) MATCHING(f_printer_id,f_script_id)");
				ps.setString(1, p.Id);
				ps.setString(2, s.Id);
				ps.execute();
			}
			for (Menu m : BizCache.Menus) {
				if (m.Printers.contains(p)) {
					ps = ImpBizApp.fd.con
							.prepareStatement("UPDATE OR INSERT INTO PAD_MENU_PRINTER_RELAT (F_MENU_ID,F_PRINTER_ID) VALUES (?,?) MATCHING (F_MENU_ID,F_PRINTER_ID);");
					ps.setString(1, m.Id);
					ps.setString(2, p.Id);
					ps.execute();
				}
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
	 * test printer and printer a test script
	 * @param printer
	 * @return
	 */
	@Override
	public boolean Test(Printer printer) {
		String script = "<rep> <ctrl code='INIT'/> <value type='text' value='Printer Test'  length='40' align='CENTER'/><ctrl code='CR'/><value type='text' source='printer:Name'  length='40' align='CENTER'/><ctrl code='CR'/><value type='text' source='printer:Address'  length='40' align='CENTER'/><ctrl code='CR'/><ctrl code='CUT'/></rep>";
		ReportBuilder builder = new ReportBuilder();
		builder.InitParam();
		builder.AddParam("printer", printer);
		PrinterTools pt = new PrinterTools();
		boolean bool=pt.TestPrinter(printer.Address);
		if(bool==true){
		builder.Parse(script);
		byte[] stream = builder.Run();
		if (pt.Connect(printer.Address)) {
			System.out.println("printer connection ok!");

			pt.SendStream(stream, stream.length);
		} else {
			System.err.println("connection error!!!");
		}
		}
		return bool;
	}

	/**
	 * @author user
	 * update a script to the database.
	 * @param spt
	 * @return
	 */
	@Override
	public boolean UpdateScript(Script spt) {

		ImpBizApp.fd.Connect();
		ImpBizApp.fd.StartTransAction();
		PreparedStatement ps;
		try {
			ps = ImpBizApp.fd.con
					.prepareStatement("UPDATE OR INSERT INTO pad_script (F_DESCRIPTION,F_ID,F_SCRIPT) VALUES ('"
							+ spt.Description
							+ "', '"
							+ spt.Id
							+ "','"
							+ spt.ScriptText + "') MATCHING (f_ID)");
			ps.execute();
			ImpBizApp.fd.CommitTransAction();
			ps.close();
		} catch (SQLException e) {
			absDB.showSQLException(e);
			e.printStackTrace();
		}

		ImpBizApp.fd.Disconnect();
		BizPrinter bp = BizFactory.getBizPrinter();
		for (Printer p : bp.Select()) {
			p.Scripts.add(spt);
			bp.InsertPrinter(p);
		}

		return true;
	}

	/**
	 * @author user
	 * get list of script from cache.
	 * @return
	 */
	@Override
	public List<Script> SelectScript() {
		List<Script> scriptlist = BizCache.Scripts;
		return scriptlist;
	}

}
