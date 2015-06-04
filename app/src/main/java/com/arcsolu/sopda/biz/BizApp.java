package com.arcsolu.sopda.biz;

import java.io.File;
import java.io.Serializable;
import java.sql.SQLException;

import android.content.Context;

import com.arcsolu.sopda.entity.Order;
import com.arcsolu.sopda.entity.Parametres.ParaKey;
import com.arcsolu.sopda.entity.User;

public interface BizApp extends Serializable{
	public Order CheckOrder();
	public User Login(String password) throws Exception ;
	public boolean AdminLogin(String password) ;
	public String GetParam(ParaKey key);
	public boolean CloseOrder(Order order);
	public boolean SetAdminPassword(String password);
	public boolean SetParam(ParaKey key, String value);
	public  boolean DownloadParams();
	public boolean DownloadUsers();
	public boolean UpdateParams(ParaKey Key);
	public boolean DownloadFloors() throws Exception;
	public boolean DownloadMenus();
	public boolean DownloadPrinters();
	public boolean Download() throws Exception;
	public boolean Update();
	public boolean IsConnect(Context context);
	public boolean DownloadFormules();
	public boolean GetFBDB(String server,String db,String user,String pwd);
	public boolean SetFileDir(File file);
	public String getDeviceId();
	public boolean IsMactchedSerieNumber(String deviceID) throws SQLException;
	public byte[] getLogo();
	public boolean setLogo(byte[] logoPic);
	public boolean setBackground(byte[] backPic);
	public byte[] getBackground();
	public void SetOnProgressCaller(OnProgressCaller caller);
	public double getProgressDownload();
	public double getProgressUpdate();
	}



