package com.arcsolu.sopda.biz;

import android.content.Context;

import com.arcsolu.sopda.entity.Order;
import com.arcsolu.sopda.entity.Parametres.ParaKey;
import com.arcsolu.sopda.entity.User;

import java.io.File;
import java.io.Serializable;
import java.sql.SQLException;

public interface BizApp extends Serializable {
     Order CheckOrder();

     User Login(String password) throws Exception;

     boolean AdminLogin(String password);

     String GetParam(ParaKey key);

     boolean CloseOrder(Order order);

     boolean SetAdminPassword(String password);

     boolean SetParam(ParaKey key, String value);

     boolean DownloadParams();

     boolean DownloadUsers();

     boolean UpdateParams(ParaKey Key);

     boolean DownloadFloors() throws Exception;

     boolean DownloadMenus();

     boolean DownloadPrinters();

     boolean Download() throws Exception;

     boolean Update();

     boolean IsConnect(Context context);

     boolean DownloadFormules();

     boolean GetFBDB(String server, String db, String user, String pwd);

     boolean SetFileDir(File file);

     String getDeviceId();

     boolean IsMactchedSerieNumber(String deviceID) throws SQLException;

     byte[] getLogo();

     boolean setLogo(byte[] logoPic);

     boolean setBackground(byte[] backPic);

     byte[] getBackground();

     void SetOnProgressCaller(OnProgressCaller caller);

     double getProgressDownload();

     double getProgressUpdate();
}



