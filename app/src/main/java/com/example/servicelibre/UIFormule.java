package com.example.servicelibre;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.arcsolu.sopda.biz.BizApp;
import com.arcsolu.sopda.biz.BizFloor;
import com.arcsolu.sopda.biz.BizMenu;
import com.arcsolu.sopda.biz.BizOrder;
import com.arcsolu.sopda.biz.BizPrinter;
import com.arcsolu.sopda.biz.BizTable;
import com.arcsolu.sopda.entity.Formule;
import com.arcsolu.sopda.entity.Order;
import com.arcsolu.sopda.entity.Parametres;
import com.arcsolu.sopda.entity.Parametres.ParaKey;
import com.arcsolu.sopda.entity.Table;
import com.arcsolu.sopda.entity.User;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class UIFormule extends Activity {

	BizApp bizApp;
	BizOrder bizOrder;
	BizFloor bizFloor;
	BizMenu bizMenu;
	BizTable bizTable;
	BizPrinter bizPrinter;
	Intent intent;
	Bundle bundle;

	ListAdapter listAdapter;
	ListView listView;
	List<Formule> listFormule;
	Map<Formule, Integer> map;
	ProgressDialog progressDialog;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.formule);
		intent = getIntent();
		bundle = intent.getExtras();
		bizMenu = (BizMenu) bundle.getSerializable("BIZMENU");
		bizOrder = (BizOrder) bundle.getSerializable("BIZORDER");
		bizApp = (BizApp) bundle.getSerializable("BIZAPP");

		map = new HashMap<Formule, Integer>();

		try {
			listFormule = bizMenu.SelectAvailableFormule();
		} catch (Exception e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("SelectAvailableFormule Exception").show();
		}

		if (listFormule == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("SelectAvailableFormule Exception").show();
			return;
		}
		ListAdapter listAdapter = new ListAdapter(this, R.layout.formule_list);
		ListView listView = (ListView) findViewById(R.id.formuleList);
		listView.setAdapter(listAdapter);

	}

	public class ListAdapter extends ArrayAdapter<Object> {

		public ListAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
			// TODO Auto-generated constructor stub
		}

		public int getCount() {
			return listFormule.size();
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			Holder holder;
			if (convertView == null) {
				holder = new Holder(listFormule.get(position), convertView);
				convertView = LayoutInflater.from(UIFormule.this).inflate(
						R.layout.formule_list, null);
				holder.name = (TextView) convertView
						.findViewById(R.id.formule_list_textView1);
				holder.price = (TextView) convertView
						.findViewById(R.id.formule_list_textView2);
				holder.moins = (Button) convertView
						.findViewById(R.id.formule_list_button1);
				holder.num = (TextView) convertView
						.findViewById(R.id.formule_list_textView3);
				holder.plus = (Button) convertView
						.findViewById(R.id.formule_list_button2);
				convertView.setTag(holder);
				holder.name.setTextSize(20);
				holder.price.setTextSize(20);
				holder.num.setTextSize(20);
				holder.moins
						.setOnClickListener(new MoinsButtonListener(holder));
				holder.plus.setOnClickListener(new AddButtonListener(holder));
				holder.name.setText(listFormule.get(position).Name);
				holder.price
						.setText(String.valueOf(listFormule.get(position).Price));
				holder.num.setText(String.valueOf(holder.nombre));

			} else {
				holder = (Holder) convertView.getTag();
			}
			holder.view = convertView;
			return convertView;

		}

	}

	class AddButtonListener implements OnClickListener {
		Holder holder;

		AddButtonListener(Holder h) {
			holder = h;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			holder.nombre++;
			map.put(holder.formule, holder.nombre);
			holder.num.setText(String.valueOf(holder.nombre));
			holder.view.setBackgroundColor(Color.YELLOW);
			holder.name.setTextColor(Color.BLACK);
			holder.price.setTextColor(Color.BLACK);
			holder.num.setTextColor(Color.BLACK);
		}

	}

	class MoinsButtonListener implements OnClickListener {
		Holder holder;

		MoinsButtonListener(Holder h) {
			holder = h;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (holder.nombre > 0) {
				holder.nombre--;
				map.put(holder.formule, holder.nombre);
				holder.num.setText(String.valueOf(holder.nombre));
				if (holder.nombre == 0) {
					map.remove(holder.formule);
					holder.view.setBackground(null);
					holder.name.setTextColor(Color.WHITE);
					holder.price.setTextColor(Color.WHITE);
					holder.num.setTextColor(Color.WHITE);
				}
			}
		}
	}

	/**消息响应
	 * @param indice
	 */
	private void sendMsg(int indice) {
		Message msg = new Message();
		msg.what = indice;
		handler.sendMessage(msg);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// 定义一个Handler，用于处理下载线程与UI间通讯
			if (!Thread.currentThread().isInterrupted()) {
				switch (msg.what) {
				case 0:
					AlertDialog.Builder builder = new AlertDialog.Builder(
							UIFormule.this);
					builder.setTitle("Please register this software").show();
				}
			}
			super.handleMessage(msg);
		}
	};

	/**跳转下一界面
	 * @param v
	 */
	public void onClickOkButton(View v) {
		
		boolean isMatch = false;
		
		try{
		isMatch = bizApp.IsMactchedSerieNumber(bizApp.getDeviceId());
		}catch(Exception e){
			isMatch = false;
		}
		
		if (map.size() > 0 && isMatch) {

			progressDialog = new ProgressDialog(UIFormule.this);
			final UIOnProgressCaller caller = new UIOnProgressCaller();
			caller.SetProgressDlg(progressDialog);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMessage("Loading...");
			progressDialog.setCancelable(false);
			progressDialog.show();
			Thread t0 = new Thread() {
				public void run() {
					caller.OnProgress(0, 20);
					Order order = null;
					try {
						caller.OnProgress(2, 20);
						Table table = (Table) bundle.getSerializable("TABLE");
						User user = (User) bundle.getSerializable("USER");
						int nb = (Integer) bundle.getSerializable("NB");
						caller.OnProgress(3, 20);
						order = bizOrder.CreateOrder(user, table, nb, map);
						caller.OnProgress(11, 20);
					} catch (Exception e) {
						e.printStackTrace();
						AlertDialog.Builder builder = new AlertDialog.Builder(
								UIFormule.this);
						builder.setMessage("Connect error").show();
						return;
					}

					bundle.putSerializable("ORDER", order);
					intent.putExtras(bundle);
					caller.OnProgress(12, 20);
					intent.setClass(UIFormule.this, UITable.class);
					caller.OnProgress(14, 20);
					startActivity(intent);
					caller.OnProgress(20, 20);
					caller.OnFinished();
					finish();

				}
			};
			t0.start();
		}
		
		if(!isMatch){
			AlertDialog.Builder builder = new AlertDialog.Builder(
					UIFormule.this);
			builder.setTitle("Please register this software")
					.show();
		}

	}

	/**返回
	 * @param v
	 */
	public void onClickCancerButton(View v) {
		String floorview = bizApp.GetParam(Parametres.ParaKey.MAPMODE);
		if (floorview == null) {
			floorview = "list";
		}
		if (floorview.contentEquals("map")) {
			intent.setClass(UIFormule.this, UIFloor.class);
		} else {
			intent.setClass(UIFormule.this, UIFloorMap.class);
		}
		startActivity(intent);
		finish();
	}

	@Override
	public void onBackPressed() {
		String floorview = "list";
		try {
			floorview = bizApp.GetParam(Parametres.ParaKey.MAPMODE);
		} catch (Exception e) {
			floorview = "list";
		}
		if (floorview.contentEquals("map")) {
			intent.setClass(UIFormule.this, UIFloor.class);
		} else {
			intent.setClass(UIFormule.this, UIFloorMap.class);
		}
		startActivity(intent);
		finish();
	}

	class Holder {
		Formule formule;
		int nombre;
		TextView name;
		TextView price;
		Button moins;
		Button plus;
		TextView num;
		View view;

		public Holder(Formule formule, View v) {
			this.formule = formule;
			nombre = 0;
			view = v;
		}

	}

}
