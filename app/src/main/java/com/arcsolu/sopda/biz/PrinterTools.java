package com.arcsolu.sopda.biz;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class PrinterTools {
	
	private static Socket socket=null;
	private OutputStream out = null;
	public PrinterTools(){
		//socket=new Socket();
	}
	/**
	 * connect to the printer's address.
	 * @param addr
	 * @return
	 */
	public boolean Connect(String addr){
		try {
			socket=new Socket();
			socket.connect(new InetSocketAddress(InetAddress.getByName(addr),9100), 10000);
			socket.setSoTimeout(10000);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("connection failed");
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * disconnect the connection
	 * @return
	 */
	public boolean Disconnect(){
		try {
			if(socket.isConnected())
				socket.close();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("close socket failed");
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * send stream to the printer.
	 * @param datas
	 * @param count
	 * @return
	 */
	public int SendStream(byte[] datas,int count){
		try {
			out=socket.getOutputStream();
			out.write(datas,0,count);
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	/**
	 * test if the printer is in good state.
	 * @param addr
	 * @return
	 */
	public boolean TestPrinter(String addr){
		boolean res = false;
		ByteArrayOutputStream buff=new ByteArrayOutputStream();
		buff.write(16);
		buff.write(4);
		buff.write(1);
		if(this.Connect(addr)){
			InputStream in=null;
			try {
				in = socket.getInputStream();
				out=socket.getOutputStream();
				out.write(buff.toByteArray(),0,buff.size());
				out.flush();
				int b = in.read();
				out.close();
				in.close();
				//this.Disconnect();
				//socket.close();
				if(b==22 || b==28){
					res=true;
					//return true;
				}else{
					res=false;
					//return false;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Printer paper out error!!!");
				try {
					out.close();
					in.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				//return false;
				res=false;
			}
		}else{
			
			System.out.println("Printer "+addr+" connection faild!!");
			//return false;
			res = false;
		}
		return res;
	}
}
