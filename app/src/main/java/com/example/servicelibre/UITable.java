package com.example.servicelibre;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.arcsolu.sopda.biz.BizApp;
import com.arcsolu.sopda.biz.BizFloor;
import com.arcsolu.sopda.biz.BizMenu;
import com.arcsolu.sopda.biz.BizOrder;
import com.arcsolu.sopda.biz.BizPrinter;
import com.arcsolu.sopda.biz.BizTable;
import com.arcsolu.sopda.entity.Menu;
import com.arcsolu.sopda.entity.Order;
import com.arcsolu.sopda.entity.OrderDetail;
import com.arcsolu.sopda.entity.Parametres;
import com.arcsolu.sopda.entity.User;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class UITable extends Activity {
	ListAdapter listAdapter;
	GridAdapter gridAdapter;
	GridView gridView;
	ListView listView;

	BizApp bizApp;
	BizOrder bizOrder;
	BizFloor bizFloor;
	BizMenu bizMenu;
	BizTable bizTable;
	BizPrinter bizPrinter;
	Intent intent;
	Bundle bundle;

	List<Menu> listMenu;
	List<String> cata;
	Order order;
	int turn;
	int limit_turn;
	boolean indicateur = false;
	boolean state = true;
	String password = "";

	long totaltime;
	long turntime;
	int limitperson;
	int nbpersonne;
	int nbmenuinturn = 0;
	int nbtotalmenu;

	Handler handler;
	Handler handlerall;
	Runnable runnable;
	Runnable runnableall;
	TextView tv;
	Dialog dialog;
	Dialog dialog2;
	SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	long _totaltime;
	List<Button> listTurnButton;
	Button buttonClicked;
	MenuAddListener addButtonlistener;
	MenuSupListener moinsButtonlistener;
	Resources res;
	Locale locale;
	BitmapFactory.Options opts;
	ProgressDialog progressDialog;
	Typeface tf;
	Button btn11;
	Button btn22;
	Button btn33;
	Button btn44;
	Button btnall;
	Button buttontous;

	Map<String, List<Menu>> lstCats;

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	protected void onCreate(Bundle savedInstanceState) {

		SharedPreferences sharedPreferences = this.getSharedPreferences("com",
				Context.MODE_PRIVATE);
		String local = sharedPreferences.getString("locale", "en");
		Locale locales[] = Locale.getAvailableLocales();
		for (Locale l : locales) {
			if (l.toString().equalsIgnoreCase(local)) {
				locale = l;
				break;
			}
		}

		tf = Typeface.createFromAsset(getAssets(),
				"fonts/ProximaNova_Semibold.otf");

		opts = new BitmapFactory.Options();
		opts.inSampleSize = 4;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.table);
		LinearLayout coverflow = (LinearLayout) findViewById(R.id.table_coverflow);
		coverflow.setVisibility(View.GONE);

		intent = getIntent();
		bundle = intent.getExtras();
		bizMenu = (BizMenu) bundle.getSerializable("BIZMENU");
		order = (Order) bundle.getSerializable("ORDER");
		bizOrder = (BizOrder) bundle.getSerializable("BIZORDER");
		bizApp = (BizApp) bundle.getSerializable("BIZAPP");

		res = this.getResources();



		try {
			totaltime = Integer.parseInt(bizApp
					.GetParam(Parametres.ParaKey.MAXTIME));
			turntime = Integer.parseInt(bizApp
					.GetParam(Parametres.ParaKey.TIME_OF_TURN));
			limitperson = Integer.parseInt(bizApp
					.GetParam(Parametres.ParaKey.COMMANDE_PAR_PERSON));
			limit_turn = Integer.parseInt(bizApp
					.GetParam(Parametres.ParaKey.TURN));
		} catch (Exception e) {
			e.printStackTrace();
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("GetParam Exception").show();
			try {
				bizApp.CloseOrder(order);
			} catch (Exception e2) {
				AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
				builder2.setMessage(
						"Fatal error, please reinstall the software").show();
			}

			return;
		}
		if (limit_turn <= 0) {
			limit_turn = 99;
		}

		try {
			ImageView logo = (ImageView) findViewById(R.id.table_imageView1);
			byte[] image = bizApp.getLogo();
			Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0,
					image.length);
			logo.setImageBitmap(bitmap);
		} catch (Exception e) {
		}

		handler = new Handler();
		handlerall = new Handler();
		try {
			if (order == null) {
				order = bizApp.CheckOrder();
				turnTimeClock();
			}
		} catch (Exception e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("CheckOrder Exception").show();
			return;
		}

		nbpersonne = order.ClientNumber;
		LinearLayout catalayout = (LinearLayout) this
				.findViewById(R.id.table_linear);
		LinearLayout turnlayout = (LinearLayout) this
				.findViewById(R.id.table_turn);
		gridView = (GridView) findViewById(R.id.table_gridview);
		listView = (ListView) findViewById(R.id.table_listview);
		try {
			listMenu = bizMenu.SelectAvailableMenu();
		} catch (Exception e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("SelectMenu Exception").show();
			try {
				bizApp.CloseOrder(order);
			} catch (Exception e2) {
				AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
				builder2.setMessage(
						"Fatal error, please reinstall the software").show();
			}

			return;
		}
		turn = 0;
		nbtotalmenu = 0;
		cata = new ArrayList<String>();
		listTurnButton = new ArrayList<Button>();

		buttonClicked = null;
		Button _coverflowback = (Button) findViewById(R.id.coverflow_button3);
		TextView _coverflownum = (TextView) findViewById(R.id.coverflow_textView1);
		TextView _coverflowtotalnum = (TextView) findViewById(R.id.coverflow_textView11);
		TextView _coverflowname = (TextView) findViewById(R.id.coverflow_textView0);
		btn11 = (Button) findViewById(R.id.table_button1);
		btn22 = (Button) findViewById(R.id.table_button2);
		btn33 = (Button) findViewById(R.id.table_button3);
		btn44 = (Button) findViewById(R.id.table_button4);
		btnall = (Button) findViewById(R.id.table_buttonAll);
		_coverflowback.setTypeface(tf);
		_coverflownum.setTypeface(tf);
		_coverflowtotalnum.setTypeface(tf);
		_coverflowname.setTypeface(tf);
		btn11.setTypeface(tf);
		btn22.setTypeface(tf);
		btn33.setTypeface(tf);
		btn44.setTypeface(tf);
		btnall.setTypeface(tf);
		TextView _limiturn = (TextView) findViewById(R.id.table_textViewlimit);
		TextView _tableId = (TextView) findViewById(R.id.table_TextView1);
		TextView _couvert = (TextView) findViewById(R.id.table_TextView2);

		_limiturn.setTypeface(tf);
		_tableId.setTypeface(tf);
		_couvert.setTypeface(tf);
		_tableId.setText(_tableId.getText().toString() + ":   "
				+ order.Table.Number);
		_couvert.setText(_couvert.getText().toString() + ":   "
				+ String.valueOf(nbpersonne));
		_limiturn.setText(String.valueOf(nbpersonne * limitperson));

		// ��ʼ��ʱ�����ò�ʱ��
		Date _currentTime = new Date(System.currentTimeMillis());
		Date _startTime = null;
		try {
			_startTime = formatter.parse(order.StartTime);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Time exception").show();
			bizApp.CloseOrder(order);
			finish();
		}

		long _kk = _currentTime.getTime() - _startTime.getTime();
		final long _usedtime = _kk / 1000;
		_totaltime = totaltime - _usedtime;
		final TextView showTime = (TextView) findViewById(R.id.table_TextTime);
		showTime.setTypeface(tf);

		if (totaltime > 0) {
			runnableall = new Runnable() {

				@Override
				public void run() {
					if (_totaltime > -1) {
						showTime.setText(UITable.this.toTime(_totaltime));
						_totaltime--;
						handlerall.postDelayed(this, 1000);
					} else {
						showTime.setText(UITable.this.toTime(0));
						View v = (View) findViewById(R.id.table_button4);
						Button btn = (Button) v;
						btn.setClickable(false);
						btn.setText("Time Over");
						handlerall.postDelayed(this, 1000);
					}
				}
			};
			handlerall.postDelayed(runnableall, 0);
		}

		Button buttonaddition = (Button) findViewById(R.id.table_button2);
		buttonaddition.setLongClickable(true);
		buttonaddition.setOnLongClickListener(new OnLongClickAdditionButton());

		try {
			for (OrderDetail detail : order.Details) {
				this.turn = Math.max(this.turn, detail.turn);

				if (!detail.sent) {
					nbmenuinturn += detail.nb;
				} else {
					this.nbtotalmenu += detail.nb;
				}
			}

			TextView txnbtotale = (TextView) findViewById(R.id.table_textViewlimit);
			txnbtotale.setTypeface(tf);
			txnbtotale.setText(String.valueOf(limitperson * nbpersonne
					- nbmenuinturn));
			TextView txt = (TextView) findViewById(R.id.table_TextViewTotal);
			txt.setTypeface(tf);
			txt.setText(res.getText(R.string.total) + " "
					+ String.valueOf(nbtotalmenu));

			turn++;
			for (OrderDetail detail : order.Details) {
				if (!detail.sent) {
					turn--;
					break;
				}
			}
		} catch (Exception e) {

			AlertDialog.Builder builder = new AlertDialog.Builder(UITable.this);
			builder.setMessage("OrderDetail Exception").show();

			try {
				bizApp.CloseOrder(order);
			} catch (Exception e2) {
				AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
				builder2.setMessage(
						"Fatal error, please reinstall the software").show();
			}
			return;
		}

		Button buttonlist = (Button) findViewById(R.id.table_button3);
		buttonlist.setOnClickListener(new TurnListener());

		gridView.setVisibility(View.GONE);
		gridAdapter = new GridAdapter(this, R.layout.menuicon, listMenu, turn);
		gridView.setAdapter(gridAdapter);
		listAdapter = new ListAdapter(this, R.layout.menulist, listMenu, turn);
		listView.setAdapter(listAdapter);

		LinearLayout.LayoutParams layoutparams2 = new LinearLayout.LayoutParams(
				UITable.this.Dp2Px(UITable.this, 58), UITable.this.Dp2Px(
						UITable.this, 59));
		for (int i = 1; i <= this.turn; i++) {
			Button button = new Button(this);
			button.setBackgroundResource(R.drawable.tourbtn);
			button.setText(String.valueOf(i));
			turnlayout.addView(button, layoutparams2);
			button.setOnClickListener(new TurnListener());
			button.setTypeface(tf);
			listTurnButton.add(button);
			if (i == this.turn) {
				button.setBackgroundResource(R.drawable.tour_red);
			}
		}

		Button buttonAll = (Button) findViewById(R.id.table_buttonAll);
		buttonAll.setOnClickListener(new TurnListener());

		buttontous = new Button(this);

		buttontous.setHeight(Dp2Px(this, 60));
		buttontous.setWidth(Dp2Px(this, 120));
		LinearLayout.LayoutParams layoutparams = new LinearLayout.LayoutParams(
				Dp2Px(this, 120), Dp2Px(this, 60));
		layoutparams.setMargins(Dp2Px(this, 18), 0, 0, Dp2Px(this, 14));
		buttontous.setText(res.getText(R.string.all));
		buttontous.setBackground(this.getResources().getDrawable(
				R.drawable.redbtn));
		buttontous.setTextColor(Color.WHITE);
		buttontous.setTypeface(tf);
		catalayout.addView(buttontous, layoutparams);
		buttonClicked = buttontous;
		buttontous.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				setButtonClicked((Button) arg0);

				gridAdapter = new GridAdapter(UITable.this, R.layout.menuicon,
						listMenu, UITable.this.turn);
				gridView.setAdapter(gridAdapter);

				listAdapter = new ListAdapter(UITable.this, R.layout.menulist,
						listMenu, UITable.this.turn);
				listView.setAdapter(listAdapter);

			}
		});

		for (Menu menu : listMenu) {
			if (!cata.contains(menu.Catalog)) {
				cata.add(menu.Catalog);
				Button button = new Button(this);
				button.setHeight(Dp2Px(this, 60));
				button.setWidth(Dp2Px(this, 125));
				button.setText(menu.Catalog);
				button.setTypeface(tf);
				button.setTextColor(Color.WHITE);
				button.setBackground(this.getResources().getDrawable(
						R.drawable.blackbtn));
				catalayout.addView(button, layoutparams);

				button.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						setButtonClicked((Button) arg0);
						List<Menu> listtemp = null;
						Button btn = (Button) arg0;
						String s = btn.getText().toString();

						for (String cl : lstCats.keySet()) {
							if (cl.equals(s)) {
								listtemp = lstCats.get(cl);
								break;
							}
						}

						gridAdapter = new GridAdapter(UITable.this,
								R.layout.menuicon, listtemp, UITable.this.turn);
						gridView.setAdapter(gridAdapter);

						listAdapter = new ListAdapter(UITable.this,
								R.layout.menulist, listtemp, UITable.this.turn);
						listView.setAdapter(listAdapter);
					}
				});
			}
		}

		for (int i = 0; i < 3; i++) {

			Button button = new Button(this);

			switch (i) {
			case 0:
				button.setText(res.getText(R.string.top5));
				break;
			case 1:
				button.setText(res.getText(R.string.new_plate));
				break;
			case 2:
				button.setText(res.getText(R.string.chef));
				break;
			}
			button.setTextColor(Color.WHITE);
			button.setTypeface(tf);
			button.setBackground(this.getResources().getDrawable(
					R.drawable.blackbtn));
			catalayout.addView(button, layoutparams);

			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					boolean indicateur = false;
					List<Menu> goodlist = new ArrayList<Menu>();
					Button btn = (Button) arg0;
					String name = btn.getText().toString();

					setButtonClicked((Button) arg0);

					for (Menu me : listMenu) {
						if (name.contentEquals(res.getText(R.string.top5)))
							indicateur = me.Top5;
						else if (name.contentEquals(res
								.getText(R.string.new_plate)))
							indicateur = me.NEW;
						else if (name.contentEquals(res.getText(R.string.chef)))
							indicateur = me.Chef;
						if (indicateur) {
							goodlist.add(me);
						}
					}
					gridAdapter = new GridAdapter(UITable.this,
							R.layout.menuicon, goodlist, UITable.this.turn);
					gridView.setAdapter(gridAdapter);

					listAdapter = new ListAdapter(UITable.this,
							R.layout.menulist, goodlist, UITable.this.turn);
					listView.setAdapter(listAdapter);
				}
			});

		}

		lstCats = new HashMap<String, List<Menu>>();

		for (Menu me : listMenu) {
			String cat = null;
			for (String lc : lstCats.keySet()) {
				if (lc.equals(me.Catalog)) {
					cat = lc;
					lstCats.get(cat).add(me);
					break;
				}
			}
			if (cat == null) {
				cat = me.Catalog;
				lstCats.put(cat, new ArrayList<Menu>());
				lstCats.get(cat).add(me);
			}

		}
	}

	private void setButtonClicked(Button newButton) {
		if (buttonClicked != null) {
			buttonClicked.setBackgroundResource(R.drawable.blackbtn);
		}
		newButton.setBackgroundResource(R.drawable.redbtn);
		buttonClicked = newButton;
	}

	public void onClickIconButton(View view) {
		gridAdapter = new GridAdapter(UITable.this, R.layout.menuicon,
				listAdapter.list, UITable.this.turn);
		gridView.setAdapter(gridAdapter);
		gridView.setVisibility(View.VISIBLE);

		listView.setVisibility(View.GONE);
		view.setBackgroundResource(R.drawable.form_on);
		View v2 = findViewById(R.id.table_button6);
		v2.setBackgroundResource(R.drawable.list);
	}

	public void onClickListButton(View view) {

		gridView.setVisibility(View.GONE);

		listAdapter = new ListAdapter(UITable.this, R.layout.menulist,
				gridAdapter.list, UITable.this.turn);
		listView.setAdapter(listAdapter);

		listView.setVisibility(View.VISIBLE);

		view.setBackgroundResource(R.drawable.list_on);
		View v2 = findViewById(R.id.table_button5);
		v2.setBackgroundResource(R.drawable.form);
	}

	public class ListAdapter extends ArrayAdapter<Object> {
		int mTextViewResourceID = 0;
		private Context mContext;
		List<Menu> list;
		int turn;

		public ListAdapter(Context context, int textViewResourceId,
				List<Menu> list, int turn) {
			super(context, textViewResourceId);
			mTextViewResourceID = textViewResourceId;
			mContext = context;
			this.list = list;
			this.turn = turn;
		}

		public int getCount() {
			return list.size();
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			Holder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(
						mTextViewResourceID, null);
				holder = new Holder();
				holder.image = (ImageView) convertView
						.findViewById(R.id.menulist_imageView1);
				holder.text = (TextView) convertView
						.findViewById(R.id.menulist_textView1);
				holder.num = (TextView) convertView
						.findViewById(R.id.menulist_textView2);
				holder.buttonmoins = (Button) convertView
						.findViewById(R.id.menulist_button1);
				holder.buttonplus = (Button) convertView
						.findViewById(R.id.menulist_button2);
				holder.text.setTypeface(tf);
				holder.num.setTypeface(tf);
				holder.menu = list.get(position);
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
				holder.menu = list.get(position);
			}

			if (turn != 0) {
				Bitmap bitmap = BitmapFactory.decodeByteArray(
						list.get(position).Pic, 0,
						list.get(position).Pic.length, opts);
				holder.image.setImageBitmap(bitmap);
				holder.image.setClickable(true);
				holder.image.setOnClickListener(new OnClickImageButton());
				holder.image.setTag(list.get(position));
				if (list.get(position).Display != null) {
					holder.text.setText(list.get(position).Name + "\n"
							+ list.get(position).Display);
				} else {
					holder.text.setText(list.get(position).Name);
				}
				int count = 0;
				for (OrderDetail de : order.Details) {
					if (de.Menu.equals(list.get(position)) && de.turn == turn) {
						count++;
						holder.num.setText(String.valueOf(de.nb));
					}
				}
				if (count == 0) {
					holder.num.setText("0");
				}

			} else {
				Bitmap bitmap = BitmapFactory.decodeByteArray(
						list.get(position).Pic, 0,
						list.get(position).Pic.length, opts);
				holder.image.setImageBitmap(bitmap);
				holder.image.setClickable(true);
				holder.image.setOnClickListener(new OnClickImageButton());
				holder.image.setTag(list.get(position));
				holder.num
						.setText(String.valueOf(order.Details.get(position).nb));
				if (list.get(position).Display != null) {
					holder.text.setText(list.get(position).Name + "\n"
							+ list.get(position).Display);
				} else {
					holder.text.setText(list.get(position).Name);
				}
				if (order.Details.get(position).turn % 2 == 0) {
					convertView.setBackgroundResource(R.drawable.turn);
				} else {
					convertView.setBackground(null);
				}
				if (order.Details.get(position).turn == UITable.this.turn) {
					holder.buttonplus.setOnClickListener(new MenuAddListener(
							order, order.Details.get(position).Menu,
							holder.num, null));
					holder.buttonmoins.setOnClickListener(new MenuSupListener(
							order, order.Details.get(position).Menu,
							holder.num, null));
				}
			}
			if (turn == UITable.this.turn) {
				holder.buttonplus.setClickable(true);
				holder.buttonmoins.setClickable(true);
				holder.buttonplus.setOnClickListener(new MenuAddListener(order,
						list.get(position), holder.num, null));
				holder.buttonmoins.setOnClickListener(new MenuSupListener(
						order, list.get(position), holder.num, null));
			} else {
				holder.buttonplus.setClickable(false);
				holder.buttonmoins.setClickable(false);
			}
			return convertView;
		}

	}

	class Holder {
		ImageView image;
		TextView text;
		TextView num;
		Button buttonplus;
		Button buttonmoins;
		Menu menu;
	}

	public class GridAdapter extends ArrayAdapter<Object> {
		// ����Context
		private Context mContext;
		int mTextViewResourceID = 0;
		List<Menu> list;
		int turn;

		// ������������ ��ͼƬԴ

		public GridAdapter(Context c, int textViewResourceId, List<Menu> list,
				int turn) {
			super(c, textViewResourceId);
			mContext = c;
			mTextViewResourceID = textViewResourceId;
			this.list = list;
			this.turn = turn;
		}

		public int getCount() {
			return list.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			Holder2 holder;

			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(
						mTextViewResourceID, null);

				holder = new Holder2();
				holder.image = (ImageView) convertView
						.findViewById(R.id.menuicon_imageView1);
				holder.num = (TextView) convertView
						.findViewById(R.id.menuicon_textView1);
				holder.buttonmoins = (Button) convertView
						.findViewById(R.id.menuicon_button1);
				holder.buttonplus = (Button) convertView
						.findViewById(R.id.menuicon_button2);
				holder.name = (TextView) convertView
						.findViewById(R.id.menuicon_textView11);
				holder.menu = list.get(position);
				holder.name.setTypeface(tf);
				holder.num.setTypeface(tf);
				convertView.setTag(holder);
			} else {
				holder = (Holder2) convertView.getTag();
				holder.menu = list.get(position);
			}

			int count = 0;
			for (OrderDetail de : order.Details) {
				if (de.Menu.equals(list.get(position)) && de.turn == turn) {

					holder.num.setText(String.valueOf(de.nb));
					count++;
					break;
				}
			}
			if (count == 0)
				holder.num.setText("0");

			holder.buttonplus.setOnClickListener(new MenuAddListener(order,
					list.get(position), holder.num, null));
			holder.buttonmoins.setOnClickListener(new MenuSupListener(order,
					list.get(position), holder.num, null));
			Bitmap bitmap = BitmapFactory.decodeByteArray(
					list.get(position).Pic, 0, list.get(position).Pic.length,
					opts);
			String name;
			if (list.get(position).Name.length() > 38) {
				name = list.get(position).Name.substring(0, 30) + "...";
			} else {
				name = list.get(position).Name;
			}
			holder.name.setText(name);
			holder.image.setImageBitmap(bitmap);
			holder.image.setClickable(true);
			holder.image.setOnClickListener(new OnClickImageButton());
			holder.image.setTag(list.get(position));
			return convertView;
		}

	}

	class Holder2 {
		Menu menu;
		ImageView image;
		TextView num;
		TextView name;
		Button buttonplus;
		Button buttonmoins;
	}

	class MenuAddListener implements OnClickListener {
		Order order;
		Menu menu;
		TextView text;
		View view;

		public MenuAddListener(Order order, Menu menu, TextView text, View v) {
			this.order = order;
			this.menu = menu;
			this.text = text;
			view = v;
		}

		@Override
		public void onClick(View v) {
			int temp = 0;
			for (OrderDetail ordered : order.Details) {
				if (ordered.Menu.equals(menu) && ordered.turn == turn) {
					if (ordered.nb < ordered.Menu.Limit
							&& nbmenuinturn < limitperson * nbpersonne) {
						ordered.nb++;
						temp++;
						UITable.this.nbmenuinturn++;
						TextView _limiturn = (TextView) findViewById(R.id.table_textViewlimit);
						_limiturn.setText(String.valueOf(limitperson
								* nbpersonne - nbmenuinturn));
						TextView txnbtotale = (TextView) findViewById(R.id.coverflow_textView0);
						txnbtotale.setText(String.valueOf(limitperson
								* nbpersonne - nbmenuinturn));
						text.setText(String.valueOf(ordered.nb));
						break;
					}

					else
						return;
				}
			}

			if (temp == 0 && nbmenuinturn < limitperson * nbpersonne) {
				OrderDetail unMenu = new OrderDetail(order);
				unMenu.Menu = menu;
				unMenu.nb = 1;
				unMenu.turn = turn;
				UITable.this.nbmenuinturn++;
				TextView _limiturn = (TextView) findViewById(R.id.table_textViewlimit);
				_limiturn.setText(String.valueOf(limitperson * nbpersonne
						- nbmenuinturn));
				text.setText("1");
			}

			if (view != null) {
				view.setBackgroundColor(Color.YELLOW);
			}
			try {
				bizOrder.SaveOrder(order);
			} catch (Exception e) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						UITable.this);
				builder.setMessage("SaveOrder Exception").show();
			}

		}
	}

	class MenuSupListener implements OnClickListener {
		Order order;
		Menu menu;
		TextView text;
		View view;

		public MenuSupListener(Order order, Menu menu, TextView text, View v) {
			this.order = order;
			this.menu = menu;
			this.text = text;
			this.view = v;
		}

		@Override
		public void onClick(View v) {
			OrderDetail temp = null;
			for (OrderDetail ordered : order.Details) {
				if (ordered.Menu.equals(menu) && ordered.turn == turn) {
					if (ordered.nb > 0) {
						ordered.nb--;
						TextView _limiturn = (TextView) findViewById(R.id.table_textViewlimit);

						UITable.this.nbmenuinturn--;
						_limiturn.setText(String.valueOf(limitperson
								* nbpersonne - nbmenuinturn));
						TextView txnbtotale = (TextView) findViewById(R.id.coverflow_textView0);
						txnbtotale.setText(String.valueOf(limitperson
								* nbpersonne - nbmenuinturn));
						if (ordered.nb > 0) {
							text.setText(String.valueOf(ordered.nb));
							if (view != null) {
								view.setBackgroundColor(Color.YELLOW);
							}
							break;
						} else if (ordered.nb == 0) {
							temp = ordered;
							break;
						}
					}
				}
			}

			if (temp != null) {
				order.Details.remove(temp);
				text.setText("0");
				if (view != null)
					view.setBackgroundColor(Color.BLACK);
			}

			try {
				bizOrder.SaveOrder(order);
			} catch (Exception e) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						UITable.this);
				builder.setMessage("SaveOrder Exception").show();
			}
		}
	}

	class TurnListener implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			Button bt = (Button) findViewById(R.id.table_button3);
			Button btn = (Button) arg0;
			Button b = (Button) findViewById(R.id.table_buttonAll);

			for (Button btt : UITable.this.listTurnButton) {
				btt.setBackgroundResource(R.drawable.tourbtn);
			}

			String s = btn.getText().toString();

			String _listString = res.getText(R.string.list).toString();
			String _continueString = res.getText(R.string._continue).toString();

			if (s.contentEquals(res.getText(R.string.all))) {
				b.setBackgroundResource(R.drawable.red_all);
			} else if (!(s.contentEquals(_listString) || s
					.contentEquals(_continueString))) {
				b.setBackgroundResource(R.drawable.allbtn);
				btn.setBackgroundResource(R.drawable.tour_red);
			} else if ((s.contentEquals(_listString))) {
				Button bbtn = listTurnButton.get(UITable.this.turn - 1);
				bbtn.setBackgroundResource(R.drawable.tour_red);
			}

			else {

				b.setBackgroundResource(R.drawable.allbtn);
			}

			if (s.contentEquals(_listString)
					|| s.contentEquals(_continueString)) {
				s = String.valueOf(turn);
				indicateur = true;
			} else {
				indicateur = false;
			}

			int thisturn = 0;
			if (!s.contentEquals(res.getText(R.string.all))) {
				thisturn = Integer.parseInt(s);
			}
			if (!indicateur || (indicateur && state)) {

				View cata0 = findViewById(R.id.table_linear0);
				cata0.setVisibility(View.GONE);

				Button bbbtn1 = (Button) findViewById(R.id.table_button5);
				Button bbbtn2 = (Button) findViewById(R.id.table_button6);
				bbbtn1.setClickable(false);
				bbbtn2.setClickable(false);

				GridView grid = (GridView) findViewById(R.id.table_gridview);
				grid.setVisibility(View.GONE);

				ListView list = (ListView) findViewById(R.id.table_listview);
				list.setVisibility(View.VISIBLE);

				List<Menu> listtemp = new ArrayList<Menu>();

				for (OrderDetail det : order.Details) {
					if (det.turn == thisturn
							|| s.contentEquals(res.getText(R.string.all))) {
						listtemp.add(det.Menu);
					}
				}

				ListAdapter listadapter = new ListAdapter(UITable.this,
						R.layout.menulist, listtemp, thisturn);
				list.setAdapter(listadapter);

				bt.setText(res.getText(R.string._continue));
				Button btn11 = (Button) findViewById(R.id.table_button6);
				Button btn22 = (Button) findViewById(R.id.table_button5);
				btn11.setBackgroundResource(R.drawable.list_on);
				btn22.setBackgroundResource(R.drawable.form);
				state = false;
			} else {

				Button bbbtn1 = (Button) findViewById(R.id.table_button5);
				Button bbbtn2 = (Button) findViewById(R.id.table_button6);
				bbbtn1.setClickable(true);
				bbbtn2.setClickable(true);
				View cata0 = findViewById(R.id.table_linear0);
				cata0.setVisibility(View.VISIBLE);

				ListView list = (ListView) findViewById(R.id.table_listview);
				list.setVisibility(View.VISIBLE);

				GridView grid = (GridView) findViewById(R.id.table_gridview);
				grid.setVisibility(View.GONE);

				listAdapter = new ListAdapter(UITable.this, R.layout.menulist,
						listMenu, UITable.this.turn);
				list.setAdapter(listAdapter);

				gridAdapter = new GridAdapter(UITable.this, R.layout.menuicon,
						listMenu, UITable.this.turn);
				grid.setAdapter(gridAdapter);
				bt.setText(res.getText(R.string.list));
				Button btn11 = (Button) findViewById(R.id.table_button6);
				Button btn22 = (Button) findViewById(R.id.table_button5);
				btn11.setBackgroundResource(R.drawable.list_on);
				btn22.setBackgroundResource(R.drawable.form);
				buttontous.callOnClick();
				state = true;
			}
		}
	}

	public void onOrderingButton(View view) {

		boolean loop = false;
		for (OrderDetail det : order.Details) {
			if (det.turn == turn)
				loop = true;
		}

		if (turn > limit_turn) {
			loop = false;
		}

		if (loop && limitperson * nbpersonne - nbmenuinturn <= 0) {
			nextOrder();

		}

		else if (loop && limitperson * nbpersonne - nbmenuinturn > 0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(res.getText(R.string.order)).setMessage(
					String.valueOf(limitperson * nbpersonne - nbmenuinturn)
							+ " " + res.getText(R.string.warn_order));
			builder.setPositiveButton(res.getText(R.string.OK),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							nextOrder();
						}
					});
			builder.setNegativeButton(res.getText(R.string.cancel), null);
			builder.show();
		}
	}

	private void sendMsg(int indice) {
		Message msg = new Message();
		msg.what = indice;
		handler2.sendMessage(msg);
	}

	private Handler handler2 = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// 定义一个Handler，用于处理下载线程与UI间通讯
			if (!Thread.currentThread().isInterrupted()) {
				switch (msg.what) {
				case 0:
					progressDialog.dismiss();
					AlertDialog.Builder builder2 = new AlertDialog.Builder(
							UITable.this);
					builder2.setMessage("Can not send the order, please call service");
					builder2.show();
					break;

				case 1:
					LinearLayout.LayoutParams layoutparams = new LinearLayout.LayoutParams(
							UITable.this.Dp2Px(UITable.this, 75),
							UITable.this.Dp2Px(UITable.this, 76));
					progressDialog.dismiss();
					Toast.makeText(UITable.this,
							res.getString(R.string.success), 1000).show();

					nbtotalmenu += nbmenuinturn;
					nbmenuinturn = 0;
					TextView _limiturn = (TextView) findViewById(R.id.table_textViewlimit);
					_limiturn.setText(String.valueOf(limitperson * nbpersonne
							- nbmenuinturn));
					TextView txnbtotale = (TextView) findViewById(R.id.coverflow_textView0);
					txnbtotale.setText(String.valueOf(limitperson * nbpersonne
							- nbmenuinturn));
					TextView txt = (TextView) findViewById(R.id.table_TextViewTotal);
					txt.setText(res.getText(R.string.total) + " "
							+ String.valueOf(nbtotalmenu));
					turn++;
					for (Button btn : listTurnButton) {
						btn.setBackgroundResource(R.drawable.tourbtn);
					}

					if (turn <= limit_turn) {
						Button button = new Button(UITable.this);
						button.setTypeface(tf);
						button.setText(String.valueOf(turn));
						button.setOnClickListener(new TurnListener());
						LinearLayout line = (LinearLayout) findViewById(R.id.table_turn);
						button.setWidth(UITable.this.Dp2Px(UITable.this, 75));
						button.setHeight(UITable.this.Dp2Px(UITable.this, 75));

						button.setBackgroundResource(R.drawable.tour_red);
						listTurnButton.add(button);
						line.addView(button, layoutparams);
					}

					gridAdapter = new GridAdapter(UITable.this,
							R.layout.menuicon, listMenu, turn);
					gridView.setAdapter(gridAdapter);
					listAdapter = new ListAdapter(UITable.this,
							R.layout.menulist, listMenu, turn);
					listView.setAdapter(listAdapter);
					View cata0 = findViewById(R.id.table_linear0);
					cata0.setVisibility(View.VISIBLE);
					LinearLayout sw = (LinearLayout) findViewById(R.id.table_switch);
					sw.setVisibility(View.VISIBLE);
					GridView grid = (GridView) findViewById(R.id.table_gridview);
					grid.setVisibility(View.GONE);
					ListView list = (ListView) findViewById(R.id.table_listview);
					list.setVisibility(View.VISIBLE);
					buttontous.callOnClick();
					Button btn1 = (Button) findViewById(R.id.table_button5);
					Button btn2 = (Button) findViewById(R.id.table_button6);
					btn1.setClickable(true);
					btn2.setClickable(true);
					turnTimeClock();
					break;

				}
			}
			super.handleMessage(msg);
		}
	};

	private void nextOrder() {
		progressDialog = new ProgressDialog(UITable.this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("Sending");
		progressDialog.setCancelable(false);
		progressDialog.show();

		new Thread() {
			public void run() {
				try {
					boolean _save = false;
					boolean _send = false;
					_send = bizOrder.SendOrder(order);
					_save = bizOrder.SaveOrder(order);

					if (!_save) {
						UITable.this.sendMsg(0);
						return;
					}
					if (!_send) {
						UITable.this.sendMsg(0);
						return;
					}

					UITable.this.sendMsg(1);
				} catch (Exception e) {
					UITable.this.sendMsg(0);
				}
			}
		}.start();

	}

	private void turnTimeClock() {
		Date _currentTime = new Date(System.currentTimeMillis());
		Date _startTurnTime = null;
		try {
			_startTurnTime = formatter.parse(order.LastSendTime);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Time exception").show();
			bizApp.CloseOrder(order);
			finish();
		}
		long _kk = _currentTime.getTime() - _startTurnTime.getTime();
		final long _usedturntime = _kk / 1000;
		View view = (View) findViewById(R.id.table_button4);
		view.setClickable(false);
		runnable = new Runnable() {
			long _turntime = UITable.this.turntime - _usedturntime;

			@Override
			public void run() {

				View v = (View) findViewById(R.id.table_button4);
				Button btn = (Button) v;
				if (_turntime >= _totaltime) {
					_turntime = _totaltime;
				}
				btn.setClickable(false);
				btn.setText(toTime(_turntime));
				_turntime--;
				handler.postDelayed(this, 1000);
				if (_turntime <= -1) {
					btn.setClickable(true);
					btn.setText(res.getText(R.string.order));
					handler.removeCallbacks(this);
				}

			}
		};
		handler.postDelayed(runnable, 0);
	}

	public void onCallServiceButton(View view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(res.getText(R.string.call_service));
		builder.setPositiveButton(res.getText(R.string.OK),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						try {
							bizOrder.CallService(order);
						} catch (Exception e) {
							AlertDialog.Builder builder = new AlertDialog.Builder(
									UITable.this);
							builder.setMessage("Connect error").show();
							return;
						}
					}
				});

		builder.setNegativeButton(res.getText(R.string.cancel),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

					}

				});

		builder.show();

	}

	public void onPrintOrderButton(View view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(res.getText(R.string.payment));
		builder.setPositiveButton(res.getText(R.string.OK),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						try {
							bizOrder.SaveOrder(order);
							bizOrder.PrintOrder(order);
						} catch (Exception e) {
							AlertDialog.Builder builder = new AlertDialog.Builder(
									UITable.this);
							builder.setMessage("Connect error").show();
							return;
						}
					}
				});

		builder.setNegativeButton(res.getText(R.string.cancel),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

					}
				});

		builder.show();
	}

	private String toTime(long time) {
		long hh = time / 3600;
		long min = (time % 3600) / 60;
		long sec = time % 60;
		if (hh != 0) {
			return String.valueOf(hh) + " : " + String.valueOf(min) + " : "
					+ String.valueOf(sec);
		} else if (min != 0) {
			return String.valueOf(min) + " : " + String.valueOf(sec);
		} else {
			return String.valueOf(sec);
		}
	}

	public void OnNumButtonClick(View view) {

		Button btn = (Button) view;
		TextView edt = tv;
		String affichage = "";

		password = password + btn.getText().toString();
		for (int i = 0; i < password.length(); i++)
			affichage += "*";
		edt.setText(affichage);
	}

	public void OnSupButtonClick(View view) {

		TextView edt = tv;
		String temp = edt.getText().toString();
		if (temp.length() == 0)
			return;
		password = password.substring(0, temp.length() - 1);
		temp = temp.substring(0, temp.length() - 1);
		edt.setText(temp.toString());
	}

	public void OnOkButtonClick(View view) {
		User user = null;
		try {
			user = bizApp.Login(password);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Please check the Internet and parameters").show();
			e1.printStackTrace();
			return;
		}
		TextView edt = tv;
		if (user == null) {
			password = "";
			edt.setText(password);
		}

		else if (user.Id.contentEquals(order.User.Id)) {
			try {

				AlertDialog.Builder builder = new AlertDialog.Builder(
						UITable.this);
				builder.setNegativeButton(res.getText(R.string.logout),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								try {
									bizOrder.SaveOrder(order);
									bizApp.CloseOrder(order);
									dialog.dismiss();
									dialog2.dismiss();
									finish();
								} catch (Exception e) {
									AlertDialog.Builder builder = new AlertDialog.Builder(
											UITable.this);
									builder.setTitle(
											"Please reinstall the software")
											.show();
								}
							}
						});
				builder.setTitle(res.getText(R.string.finish_order));
				dialog = builder.show();
			} catch (Exception e) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("l'addition Exception").show();
			}
		} else {
			password = "";
			edt.setText(password);
		}
	}

	class OnLongClickAdditionButton implements OnLongClickListener {
		@Override
		public boolean onLongClick(View arg0) {
			// TODO Auto-generated method stub
			password = "";
			final View layout = LayoutInflater.from(UITable.this).inflate(
					R.layout.addition, null);
			tv = (TextView) layout.findViewById(R.id.addition_TextView1);
			AlertDialog.Builder builder = new AlertDialog.Builder(UITable.this);
			builder.setTitle(res.getText(R.string.tape_password)).setView(
					layout);
			dialog2 = builder.show();
			return true;
		}

	}

	class OnClickImageButton implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			LinearLayout coverflow = (LinearLayout) findViewById(R.id.table_coverflow);
			coverflow.setVisibility(View.VISIBLE);
			LinearLayout uitable = (LinearLayout) findViewById(R.id.table_uitable);
			uitable.setVisibility(View.GONE);
			CoverFlow reflectingCoverFlow = (CoverFlow) findViewById(R.id.coverflowReflect);
			DisplayMetrics metric = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metric);
			reflectingCoverFlow.setImageHeight(metric.heightPixels / 3 * 2);
			reflectingCoverFlow.setImageWidth(metric.widthPixels / 3 * 2);

			Button btn1 = (Button) findViewById(R.id.coverflow_button1);
			Button btn2 = (Button) findViewById(R.id.coverflow_button2);
			TextView txnbmenu = (TextView) findViewById(R.id.coverflow_textView1);
			TextView txnbtotale = (TextView) findViewById(R.id.coverflow_textView0);
			TextView name = (TextView) findViewById(R.id.coverflow_textView11);
			Menu _m = null;
			try {
				_m = (Menu) arg0.getTag();
			} catch (Exception e) {
				_m = null;
			}
			if (_m == null) {
				_m = listMenu.get(0);

				setupCoverFlow(reflectingCoverFlow, 0);
			} else {

				setupCoverFlow(reflectingCoverFlow, listMenu.indexOf(_m));
			}
			int _temp = 0;
			for (OrderDetail od : order.Details) {
				if (od.turn == UITable.this.turn) {
					if (od.Menu.Id.contentEquals(_m.Id)) {
						txnbmenu.setText(String.valueOf(od.nb));
						_temp++;
					}
				}
			}
			if (_temp == 0) {
				txnbmenu.setText(String.valueOf(0));
			}
			txnbtotale.setText(String.valueOf(limitperson * nbpersonne
					- nbmenuinturn));
			name.setText(_m.Name.toString());

			addButtonlistener = new MenuAddListener(order, _m, txnbmenu, null);
			moinsButtonlistener = new MenuSupListener(order, _m, txnbmenu, null);
			btn2.setOnClickListener(addButtonlistener);
			btn1.setOnClickListener(moinsButtonlistener);
		}
	}

	@Override
	public void onBackPressed() {

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		newConfig.locale = locale;

		super.onConfigurationChanged(newConfig);

		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

		}

		else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

		}

		if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
		} else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
		}

		res.getConfiguration().locale = locale;
		Resources res = this.getResources();
		Configuration con = res.getConfiguration();
		getResources().updateConfiguration(con,
				getResources().getDisplayMetrics());

	}

	private void setupCoverFlow(final CoverFlow mCoverFlow, int position) {
		BaseAdapter coverImageAdapter;
		coverImageAdapter = new ReflectingImageAdapter(
				new ResourceImageAdapter(this, listMenu, order));
		mCoverFlow.setAdapter(coverImageAdapter);
		mCoverFlow.setSelection(position, true);
		setupListeners(mCoverFlow);
	}

	private void setupListeners(final CoverFlow mCoverFlow) {
		mCoverFlow.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(final AdapterView<?> parent,
					final View view, final int position, final long id) {
			}
		});
		mCoverFlow.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(final AdapterView<?> parent,
					final View view, final int position, final long id) {
				Button btn1 = (Button) findViewById(R.id.coverflow_button1);
				Button btn2 = (Button) findViewById(R.id.coverflow_button2);
				TextView txnbmenu = (TextView) findViewById(R.id.coverflow_textView1);
				TextView txnbtotale = (TextView) findViewById(R.id.coverflow_textView0);
				TextView name = (TextView) findViewById(R.id.coverflow_textView11);
				Menu _m = null;
				try {
					_m = (Menu) view.getTag();
				} catch (Exception e) {
					_m = null;
				}
				if (_m == null) {
					return;
				}
				int _temp = 0;
				for (OrderDetail od : order.Details) {
					if (od.turn == UITable.this.turn) {
						if (od.Menu.Id.contentEquals(_m.Id)) {
							txnbmenu.setText(String.valueOf(od.nb));
							_temp++;
						}
					}
				}
				if (_temp == 0) {
					txnbmenu.setText(String.valueOf(0));
				}
				name.setText(_m.Name.toString());
				addButtonlistener.menu = _m;
				moinsButtonlistener.menu = _m;
				listAdapter.notifyDataSetChanged();
				gridAdapter.notifyDataSetChanged();
			}

			@Override
			public void onNothingSelected(final AdapterView<?> parent) {

			}
		});
	}

	public void onClickBackCoverFlow(View view) {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		LinearLayout coverflow = (LinearLayout) findViewById(R.id.table_coverflow);
		coverflow.setVisibility(View.GONE);
		LinearLayout uitable = (LinearLayout) findViewById(R.id.table_uitable);
		uitable.setVisibility(View.VISIBLE);
	}

	private int Dp2Px(Context context, float dp) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}

	private int Px2Dp(Context context, float px) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (px / scale + 0.5f);
	}

}
