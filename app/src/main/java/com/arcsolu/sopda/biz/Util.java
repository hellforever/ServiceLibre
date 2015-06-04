package com.arcsolu.sopda.biz;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.crypto.Cipher;   


import android.annotation.SuppressLint;
import com.arcsolu.sopda.entity.Parametres.ParaKey;

public final class Util {
	public static final String ADD_URL2 = "http://auth.rdbcloud.fr/auth/sopad_1.php";
	/**
	 * 搴忓垪鍖栧璞� 锛爐hrows IOException
	 */

	/**
	 * serialize object to byteArray
	 * @param object
	 * @return
	 * @throws IOException
	 */
	static byte[] serializeObject(Object object) throws IOException {

		ByteArrayOutputStream saos = new ByteArrayOutputStream();

		ObjectOutputStream oos = new ObjectOutputStream(saos);

		oos.writeObject(object);

		oos.flush();
		oos.close();
		byte[] b = saos.toByteArray();
		saos.close();

		return b;

	}


	/**
	 * deserialize byteArray to object
	 * @param buf
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	static Object deserializeObject(byte[] buf) throws IOException,
			ClassNotFoundException {

		Object object = null;

		ByteArrayInputStream sais = new ByteArrayInputStream(buf);

		ObjectInputStream ois = new ObjectInputStream(sais);

		object = ois.readObject();
		sais.close();
		ois.close();

		return object;

	}

	/**
	 * blob to byteArray
	 * @param blob
	 * @return
	 * @throws Exception
	 */
	static byte[] blob2ByteArr(Blob blob) throws Exception {

		byte[] b = null;
		try {
			if (blob != null) {
				long in = 1;
				b = blob.getBytes(in, (int) (blob.length()));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("fault");
		}

		return b;
	}

	/**
	 * byteArray to hex
	 * @param bts
	 * @return
	 */
	private static String bytes2Hex(byte[] bts) {
		String des = "";
		String s = "0" + Integer.toHexString(0);
		for (int i = 0; i < bts.length; i += 4) {
			s = "0" + Integer.toHexString(bts[i + 1]);
			des += s.substring(s.length() - 2);
			s = "0" + Integer.toHexString(bts[i + 0]);
			des += s.substring(s.length() - 2);
			s = "0" + Integer.toHexString(bts[i + 3]);
			des += s.substring(s.length() - 2);
			s = "0" + Integer.toHexString(bts[i + 2]);
			des += s.substring(s.length() - 2);
			// des += (Integer.toHexString(bts[i + 1] & 0xFF));;
			// des += (Integer.toHexString(bts[i + 0] & 0xFF));;
			// des += (Integer.toHexString(bts[i + 3] & 0xFF));;
			// des += (Integer.toHexString(bts[i + 2] & 0xFF));;
		}

		return des;
	}

	/**
	 * String to md5
	 * @param str
	 * @return
	 */
	static String psdToMd5(String str) {
		MessageDigest m;
		String s = "";
		try {
			m = MessageDigest.getInstance("MD5");
			m.update(str.getBytes());
			byte[] b = m.digest();
			s = bytes2Hex(b);

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}

	/**
	 * for parakey from string to enum
	 * @param str
	 * @return
	 */
	public static ParaKey StringToEnum(String str) {
		if (str.equals("ADDRESS")) {
			return ParaKey.ADDRESS;
		} else if (str.equals("COMMANDE_PAR_PERSON")) {
			return ParaKey.COMMANDE_PAR_PERSON;
		} else if (str.equals("DATABASE")) {
			return ParaKey.DATABASE;
		} else if (str.equals("DB_PASSWORD")) {
			return ParaKey.DB_PASSWORD;
		} else if (str.equals("MASTER")) {
			return ParaKey.MASTER;
		} else if (str.equals("MAXTIME")) {
			return ParaKey.MAXTIME;
		} else if (str.equals("PASSWORD")) {
			return ParaKey.PASSWORD;
		} else if (str.equals("TIME_OF_TURN")) {
			return ParaKey.TIME_OF_TURN;
		} else if (str.equals("PRINTER_DF_ID")) {
			return ParaKey.PRINTER_DF_ID;
		} else if (str.equals("TURN")) {
			return ParaKey.TURN;
		} else if (str.equals("MAPMODE")) {
				return ParaKey.MAPMODE;
		}else if (str.equals("LASTCHECK")) {
			return ParaKey.LASTCHECK;
		}else if (str.equals("CHECK")) {
			return ParaKey.CHECK;
		}else if (str.equals("TRIAL")) {
			return ParaKey.TRIAL;
	}else
				return null;
		}

	/**
	 * to create tables on the database
	 * @return
	 */
	@SuppressWarnings("resource")
	public static boolean CreateTable() {
		File file;
		file = new File(ImpBizApp.fileDir + "/createTable.txt");
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");

		try {
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String str = stringBuilder.toString();
		String[] strs = str.split("##");
		for (int i = 0; i < strs.length; i++) {
			ImpBizApp.fd.Connect();
			ImpBizApp.fd.StartTransAction();
			PreparedStatement ps;
			try {
				ps = ImpBizApp.fd.con.prepareStatement(strs[i]);
				ps.execute();
				ImpBizApp.fd.CommitTransAction();
				ps.close();
			} catch (SQLException e) {
				absDB.showSQLException(e);
				e.printStackTrace();
			}

			ImpBizApp.fd.Disconnect();
		}

		return true;

	}

    /**
     * to decrypt the serial number
     * @param data
     * @param rk
     * @return
     */
    public final static String decrypt(String data, PrivateKey rk) {
        String rrr = "";
        StringBuffer sb = new StringBuffer(100);
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(
                    data.getBytes());

             //此处之所以是 256，而不是128的原因是因为有一个16进行的转换，所以由128变为了256

             byte[] readByte = new byte[256]; 
            int n = 0;
            while ((n = bais.read(readByte)) > 0) {
                if (n >= 256) {
                    sb.append(new String(decrypt(hex2byte(readByte), rk)));
                } else {

                }
            }
            rrr = URLDecoder.decode(sb.toString(), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rrr;
    }

    /**
     * @param src
     * @param rk
     * @return
     * @throws Exception
     */
    private static byte[] decrypt(byte[] src, PrivateKey rk) throws Exception {
    	 Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, rk);
        return cipher.doFinal(src);
    }

    /**
     * byteArray to hex
     * @param b
     * @return
     */
    @SuppressLint("DefaultLocale")
	public static String byte2hex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            if (stmp.length() == 1)
                hs += ("0" + stmp);
            else
                hs += stmp;
        }
        return hs.toUpperCase();
    }

    /**
     * hex to byteArray
     * @param b
     * @return
     */
    public static byte[] hex2byte(byte[] b) {
        if ((b.length % 2) != 0)
            throw new IllegalArgumentException("长度不是偶数");

        byte[] b2 = new byte[b.length / 2];

        for (int n = 0; n < b.length; n += 2) {
            String item = new String(b, n, 2);
            b2[n / 2] = (byte) Integer.parseInt(item, 16);
        }
        return b2;
    }

	/**
	 * send request to check the software
	 * @throws IOException
	 */
	public static void sendRequest() throws IOException {
		HttpURLConnection connection = null;
		try {
			// 创建连接
			URL url = new URL(ADD_URL2);
			connection = (HttpURLConnection) url.openConnection();

			// 设置http连接属性

			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST"); // 可以根据需要 提交
													// GET、POST、DELETE、INPUT等http提供的功能
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(true);

			
			connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			connection.setRequestProperty("Accept",
					"application/x-www-form-urlencoded");
			
			connection.connect();
			OutputStream out = connection.getOutputStream();
			String rr = "device="
					+ URLEncoder.encode(android.os.Build.SERIAL, "utf-8");
			out.write(rr.getBytes());
			// out.write(query.toString().getBytes());
			out.flush();
			out.close();
		
		// 读取响应
					BufferedReader reader = new BufferedReader(new InputStreamReader(
							connection.getInputStream()));
					String lines;
					StringBuffer sb = new StringBuffer("");
					while ((lines = reader.readLine()) != null) {
						lines = new String(lines.getBytes(), "utf-8");
						sb.append(lines);
					}
					ParaKey key=ParaKey.CHECK;
					BizCache.FBParams.put(key, sb.toString());
					reader.close();
					// // 断开连接
					connection.disconnect();
			
			// // 断开连接
			connection.disconnect();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
