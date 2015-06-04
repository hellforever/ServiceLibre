package com.arcsolu.sopda.biz;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.arcsolu.sopda.entity.Floor;

class ImpBizFloor implements BizFloor, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public List<Floor> Select() {
		List<Floor> floors = BizCache.Floors;
		return floors;
	}

	public boolean Save(ArrayList<Floor> values) {
		/*
		 * try { ObjectOutputStream os = new ObjectOutputStream(new
		 * FileOutputStream( new File("ArcResto/floors.dat")));
		 * os.writeObject(values); os.flush(); os.close(); } catch
		 * (FileNotFoundException e) { e.printStackTrace(); } catch (IOException
		 * e) { e.printStackTrace(); }
		 */
		return true;
	}

	/**
	 * @author user
	 * remove the floor from the cache
	 * @param value
	 * @return
	 */
	public boolean Delete(Floor value) {
		BizCache.Floors.remove(value);
		return true;
	}

	/**
	 * @author user
	 * get a bizTable
	 * @return
	 */
	public BizTable getBizTable() {
		return bizTable;
	}
	
	
	/**
	 * @author user
	 * insert a floor in the database
	 * @param floor
	 * @return
	 */
	public boolean InsertFloor(Floor f) {
		ImpBizApp.fd.Connect();
		ImpBizApp.fd.StartTransAction();
		PreparedStatement ps;
		InputStream iss = null;
		try {
			if (f.Img != null) {
				ps = ImpBizApp.fd.con
						.prepareStatement("UPDATE OR INSERT INTO REST_FLOOR (REST_FLOOR_ID,REST_FLOOR,BG_IMAGE) VALUES ('"
								+ f.Id
								+ "', '"
								+ f.Floor
								+ "',?) MATCHING (REST_FLOOR_ID)");
				iss = new ByteArrayInputStream(f.Img);
				ps.setBlob(1, iss);
				ps.execute();
			} else {
				ps = ImpBizApp.fd.con
						.prepareStatement("UPDATE OR INSERT INTO REST_FLOOR (REST_FLOOR_ID,REST_FLOOR) VALUES ('"
								+ f.Id
								+ "', '"
								+ f.Floor
								+ "') MATCHING (REST_FLOOR_ID)");
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

	public ImpBizFloor() {
		bizTable = new ImpBizTable();

	}

	private ImpBizTable bizTable;

}