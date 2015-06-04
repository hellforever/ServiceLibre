package com.example.servicelibre;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsolu.sopda.biz.BizApp;
import com.arcsolu.sopda.biz.BizFactory;
import com.arcsolu.sopda.biz.BizFloor;
import com.arcsolu.sopda.biz.BizMenu;
import com.arcsolu.sopda.biz.BizOrder;
import com.arcsolu.sopda.biz.BizPrinter;
import com.arcsolu.sopda.biz.BizTable;
import com.arcsolu.sopda.entity.Order;
import com.arcsolu.sopda.entity.Parametres;
import com.arcsolu.sopda.entity.Parametres.ParaKey;
import com.arcsolu.sopda.entity.User;

import java.util.Locale;

public class MainActivity extends Activity {

	String password = "";
	String crypt = "";
	Order thisOrder = null;
	BizApp bizApp;
	BizOrder bizOrder;
	BizFloor bizFloor;
	BizMenu bizMenu;
	BizTable bizTable;
	BizPrinter bizPrinter;
	Resources res;
	ImageButton flag1;
	ImageButton flag2;
	ImageButton flag3;
	ImageButton flag4;
	String floorview;
	boolean isActive = false;
	Dialog dialogActiver;
	Handler handler;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// 设置语言
		SharedPreferences sharedPreferences = this.getSharedPreferences("com",
				Context.MODE_PRIVATE);

		String local = sharedPreferences.getString("locale", "fr");
		String pic = sharedPreferences.getString("map", "map1");
		Locale locale = null;
		Locale locales[] = Locale.getAvailableLocales();
		for (Locale l : locales) {
			if (l.toString().equalsIgnoreCase(local)) {
				locale = l;
			}
		}

		Configuration config = new Configuration();
		config.locale = locale;

		this.getResources().updateConfiguration(config,
				this.getResources().getDisplayMetrics());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectAll().build());

		bizApp = BizFactory.getBizApp(getFilesDir());
		bizOrder = BizFactory.getBizOrder();
		bizFloor = BizFactory.getBizFloor();
		bizMenu = BizFactory.getBizMenu();
		bizTable = BizFactory.getBizTable();
		bizPrinter = BizFactory.getBizPrinter();
		res = this.getResources();

		flag1 = (ImageButton) findViewById(R.id.login_imageButton1);
		flag2 = (ImageButton) findViewById(R.id.login_imageButton2);
		flag3 = (ImageButton) findViewById(R.id.login_imageButton3);
		flag4 = (ImageButton) findViewById(R.id.login_imageButton4);
		flag1.setOnClickListener(new LocaleSetClickListener("en", this));
		flag2.setOnClickListener(new LocaleSetClickListener("zh_CN", this));
		flag3.setOnClickListener(new LocaleSetClickListener("fr_FR", this));
		flag4.setOnClickListener(new LocaleSetClickListener("de", this));

		int bbg = R.drawable.map1;
		if (pic.contentEquals("map1")) {
			bbg = R.drawable.map1;
		} else if (pic.contentEquals("map2")) {
			bbg = R.drawable.map2;
		}

		else if (pic.contentEquals("map3")) {
			bbg = R.drawable.map3;
		} else if (pic.contentEquals("map4")) {
			bbg = R.drawable.map4;
		}

		View bg = findViewById(R.id.LinearLayout1);
		bg.setBackground(res.getDrawable(bbg));

		bizApp.GetParam(ParaKey.CHECK);
		try {
			isActive = bizOrder.IsActive();
		} catch (Exception e) {
			isActive = false;
		}

		byte[] logo = null;
		try {
			logo = bizApp.getLogo();
		} catch (Exception e) {

		}

		TextView logocase = (TextView) findViewById(R.id.login_imageButton10);

		if (logo != null) {
			logocase.setVisibility(ImageButton.VISIBLE);
			Bitmap bitmap = BitmapFactory.decodeByteArray(logo, 0, logo.length);
			Drawable d = new BitmapDrawable(getResources(), bitmap);
			logocase.setBackground(d);
		} else {
			logocase.setVisibility(ImageButton.INVISIBLE);
		}

		if (false) {
			int nbTime;
			try {
				nbTime = bizOrder.getTrial();
			} catch (Exception e) {
				nbTime = 0;
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Enregistrement")
					.setMessage(
							"Il vous reste "
									+ nbTime
									+ " fois à l'essai, veuillez nous contacter avec ce numéro +33(0)1-48-11-95-59");
			Button btn1 = new Button(this);
			Button btn2 = new Button(this);
			Button btn3 = new Button(this);
			btn1.setText("Activer maintenant");
			btn2.setText("Continuer");
			btn3.setText("Quitter");
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					170, 50);
			btn1.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					try {
						isActive = bizOrder.ActiveNow();
					} catch (Exception e) {
						isActive = false;
					}
					if (!isActive) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								MainActivity.this);
						builder.setTitle("Echec").show();
					} else {
						isActive = true;
						dialogActiver.dismiss();
						MainActivity.this.sendMsg(0);
					}
				}

			});
			btn2.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					isActive = true;
					dialogActiver.dismiss();
					MainActivity.this.sendMsg(0);
				}

			});

			btn3.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					MainActivity.this.sendMsg(1);
				}

			});
			LinearLayout ln = new LinearLayout(this);
			ln.addView(btn1, params);
			if (nbTime != 0) {
				ln.addView(btn2, params);
			}
			ln.addView(btn3, params);
			builder.setView(ln);
			dialogActiver = builder.show();
			dialogActiver.setCancelable(false);

		}

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// 定义一个Handler，用于处理下载线程与UI间通讯
				if (!Thread.currentThread().isInterrupted()) {
					switch (msg.what) {
					case 0:
						boolean network = false;
						try {
							String _address = bizApp
									.GetParam(Parametres.ParaKey.ADDRESS);
							String _database = bizApp
									.GetParam(Parametres.ParaKey.DATABASE);
							String _usename = bizApp
									.GetParam(Parametres.ParaKey.MASTER);
							String _password = bizApp
									.GetParam(Parametres.ParaKey.DB_PASSWORD);
							network = bizApp.GetFBDB(_address, _database,
									_usename, _password);
							floorview = bizApp
									.GetParam(Parametres.ParaKey.MAPMODE);
						} catch (Exception e) {
							AlertDialog.Builder builder = new AlertDialog.Builder(
									MainActivity.this);
							builder.setTitle(
									res.getText(R.string.connect_exception))
									.show();
						}

						if (!network) {
							AlertDialog.Builder builder = new AlertDialog.Builder(
									MainActivity.this);
							builder.setTitle(res.getText(R.string.connect_fail))
									.show();
						}

						else {

							try {
								bizApp.DownloadUsers();
							} catch (Exception e) {
								AlertDialog.Builder builder = new AlertDialog.Builder(
										MainActivity.this);
								builder.setTitle("DownloadUsers Exception")
										.show();
							}

							try {
								thisOrder = bizApp.CheckOrder();
							} catch (Exception e) {
								AlertDialog.Builder builder = new AlertDialog.Builder(
										MainActivity.this);
								builder.setMessage("CheckOrder Exception")
										.show();
							}

							if (thisOrder == null) {

							}

							else {
								Intent intent = new Intent();
								Bundle mBundle = new Bundle();
								mBundle.putSerializable("BIZAPP", bizApp);
								mBundle.putSerializable("BIZORDER", bizOrder);
								mBundle.putSerializable("BIZFLOOR", bizFloor);
								mBundle.putSerializable("BIZMENU", bizMenu);
								mBundle.putSerializable("BIZTABLE", bizTable);
								mBundle.putSerializable("BIZPRINTER",
										bizPrinter);

								intent.putExtras(mBundle);
								intent.setClass(MainActivity.this,
										UITable.class);
								startActivity(intent);
								finish();
							}
						}
						break;

					case 1:
						dialogActiver.dismiss();
						finish();
						break;

					case 2:

					}
				}

				super.handleMessage(msg);
			}
		};

		if (isActive) {
			this.sendMsg(0);
		}

	}

	/**
	 * 密码按键响应
	 * 
	 * @param view
	 */
	public void OnNumButtonClick(View view) {

		Button btn = (Button) view;
		TextView edt = (TextView) findViewById(R.id.login_TextView1);
		String affichage = "";

		password = password + btn.getTag().toString();
		for (int i = 0; i < password.length(); i++)
			affichage += "*";
		edt.setText(affichage);
	}

	/**
	 * 退格响应
	 * 
	 * @param view
	 */
	public void OnSupButtonClick(View view) {

		TextView edt = (TextView) findViewById(R.id.login_TextView1);
		String temp = edt.getText().toString();
		if (temp.length() == 0)
			return;
		password = password.substring(0, temp.length() - 1);
		temp = temp.substring(0, temp.length() - 1);
		edt.setText(temp.toString());

	}

	/**
	 * 密码确认响应
	 * 
	 * @param view
	 */
	public void OnOkButtonClick(View view) {
		User user = null;
		try {
			user = bizApp.Login(password);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("No networks, please configure the parameters")
					.show();
			return;
		}

		TextView edt = (TextView) findViewById(R.id.login_TextView1);
		if (user == null) {
			password = "";
			edt.setText(password);
		}

		else {
			Intent intent = new Intent();
			Bundle mBundle = new Bundle();

			mBundle.putSerializable("BIZAPP", bizApp);
			mBundle.putSerializable("BIZORDER", bizOrder);
			mBundle.putSerializable("BIZFLOOR", bizFloor);
			mBundle.putSerializable("BIZMENU", bizMenu);
			mBundle.putSerializable("BIZTABLE", bizTable);
			mBundle.putSerializable("BIZPRINTER", bizPrinter);
			mBundle.putSerializable("USER", user);
			intent.putExtras(mBundle);
			try {
				if (floorview.contentEquals("map")) {
					intent.setClass(MainActivity.this, UIFloor.class);
				} else {
					intent.setClass(MainActivity.this, UIFloorMap.class);
				}
			} catch (Exception e) {
				intent.setClass(MainActivity.this, UIFloorMap.class);
			}
			startActivity(intent);
			finish();
		}
	}

	/**
	 * 进入设置界面
	 * 
	 * @param view
	 */
	public void OnConfigButtonClick(View view) {
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, AdminConfig.class);
		Bundle mBundle = new Bundle();
		mBundle.putSerializable("BIZAPP", bizApp);
		mBundle.putSerializable("BIZORDER", bizOrder);
		mBundle.putSerializable("BIZFLOOR", bizFloor);
		mBundle.putSerializable("BIZMENU", bizMenu);
		mBundle.putSerializable("BIZTABLE", bizTable);
		mBundle.putSerializable("BIZPRINTER", bizPrinter);
		intent.putExtras(mBundle);
		startActivity(intent);
		finish();
	}

	/**
	 * @author ZHU zijian
	 * 
	 */
	class LocaleSetClickListener implements OnClickListener {
		private Locale locale = Locale.SIMPLIFIED_CHINESE;
		private Activity activity = null;

		public LocaleSetClickListener(String locale, Activity activity) {
			this.activity = activity;
			Locale locales[] = Locale.getAvailableLocales();
			for (Locale l : locales) {
				if (l.toString().equalsIgnoreCase(locale)) {
					this.locale = l;
				}
			}
		}

		@Override
		public void onClick(View v) {
			Locale.setDefault(locale);
			Configuration config = new Configuration();
			config.locale = locale;
			if (activity != null) {
				Toast.makeText(activity, "" + locale, Toast.LENGTH_SHORT).show();
				activity.getResources().updateConfiguration(config,
						activity.getResources().getDisplayMetrics());
				activity.finish();
				activity.startActivity(activity.getIntent());
				SharedPreferences sharedPreferences = activity
						.getSharedPreferences("com", Context.MODE_PRIVATE);
				sharedPreferences.edit().putString("locale", locale.toString())
						.apply();

			}

		}
	}

	private void sendMsg(int indice) {
		Message msg = new Message();
		msg.what = indice;
		handler.sendMessage(msg);
	}

}
