package com.example.servicelibre;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.arcsolu.sopda.biz.BizApp;
import com.arcsolu.sopda.biz.BizFloor;
import com.arcsolu.sopda.biz.BizMenu;
import com.arcsolu.sopda.biz.BizOrder;
import com.arcsolu.sopda.biz.BizPrinter;
import com.arcsolu.sopda.biz.BizTable;
import com.arcsolu.sopda.entity.Floor;
import com.arcsolu.sopda.entity.Order;
import com.arcsolu.sopda.entity.Table;
import com.arcsolu.sopda.entity.User;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class UIFloor extends Activity {
	int nbPersonne = 2;
	Intent intent;
	Bundle bundle;
	Floor floor;
	Table table;

	BizApp bizApp;
	BizOrder bizOrder;
	BizFloor bizFloor;
	BizMenu bizMenu;
	BizTable bizTable;
	BizPrinter bizPrinter;
	Map<Table, View> map;
	Dialog dialog;
	EditText nbPeople;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.floor);
		map = new HashMap<Table, View>();
		intent = getIntent();
		bundle = intent.getExtras();
		bizApp = (BizApp) bundle.getSerializable("BIZAPP");
		bizFloor = (BizFloor) bundle.getSerializable("BIZFLOOR");
		try {
			bizApp.DownloadFloors();
		} catch (Exception e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("DownloadFloor Exception").show();
		}
		List<Floor> listfloor = null;
		try {
			listfloor = bizFloor.Select();
		} catch (Exception e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("SelectFloor Exception").show();
		}

		if (listfloor == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("SelectFloor Exception").show();
			return;
		}
		LinearLayout layout = (LinearLayout) findViewById(R.id.floor_num);
		for (Floor floor : listfloor) {
			Button btn = new Button(this);
			btn.setText(floor.Floor);
			btn.setOnClickListener(new OnClickFloorListener(floor));
			btn.setWidth(Dp2Px(this, 90));
			btn.setHeight(Dp2Px(this, 100));
			layout.addView(btn);
		}
		floorView(listfloor.get(0));

	}

	class OnClickFloorListener implements OnClickListener {
		Floor thisfloor;

		OnClickFloorListener(Floor floor) {
			thisfloor = floor;
		}

		@Override
		public void onClick(View arg0) {
			UIFloor.this.table = null;
			floorView(thisfloor);
		}

	}

	/**
	 * 显示楼层
	 * 
	 * @param floor
	 */
	public void floorView(Floor floor) {
		this.floor = floor;
		map.clear();
		RelativeLayout layout = (RelativeLayout) this
				.findViewById(R.id.floor_absolute);
		layout.removeAllViews();
		try {
			Bitmap bitmap = BitmapFactory.decodeByteArray(floor.Img, 0,
					floor.Img.length);
			Drawable drawable = new BitmapDrawable(getResources(), bitmap);
			layout.setBackground(drawable);
		} catch (Exception e) {
		}
		TextView[] tables = new TextView[floor.Tables.size()];
		for (int i = 0; i < floor.Tables.size(); i++) {
			tables[i] = new TextView(this);

			tables[i].setBackgroundColor(Color.WHITE);

			tables[i].setTextColor(Color.RED);
			tables[i].setText(floor.Tables.get(i).Number);
			LayoutParams params = new LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);

			params.setMargins(floor.Tables.get(i).Point_x,
					floor.Tables.get(i).Point_y, 0, 0);
			map.put(floor.Tables.get(i), tables[i]);
			tables[i].setTextSize(20);
			tables[i].setWidth(floor.Tables.get(i).TableWidth);
			tables[i].setHeight(floor.Tables.get(i).TableHeight);
			layout.addView(tables[i], params);
			if (floor.Tables.get(i).State == Table.TableState.tsEmpty) {
				tables[i].setClickable(true);
				tables[i].setOnClickListener(new MyListenner());
			} else {

				tables[i].setBackgroundColor(Color.YELLOW);
			}

		}

	}

	class MyListenner implements OnClickListener {
		public void onClick(View arg0) {
			TextView ttn = (TextView) arg0;

			for (Table thistable : floor.Tables) {
				if (map.get(thistable).equals(arg0)) {
					table = thistable;
					ttn.setBackgroundColor(Color.GREEN);
					AlertDialog.Builder builder = new AlertDialog.Builder(
							UIFloor.this);
					builder.setTitle(R.string.number_of_people);
					nbPeople = new EditText(UIFloor.this);
					nbPeople.setText("2");
					nbPeople.setInputType(InputType.TYPE_NUMBER_VARIATION_NORMAL);
					builder.setView(nbPeople);
					builder.setPositiveButton(R.string.OK,
							new OnClickTableListener());
					builder.setNegativeButton(R.string.cancel, null);
					dialog = builder.show();

				} else if (thistable.State != Table.TableState.tsEmpty) {
					map.get(thistable).setBackgroundColor(Color.YELLOW);

				} else {
					map.get(thistable).setBackgroundColor(Color.WHITE);

				}
			}
		}
	}

	class OnClickTableListener implements DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			Integer nb = null;

			if (table != null && UIFloor.this.nbPersonne > 0) {
				try {
					UIFloor.this.nbPersonne = Integer.parseInt(nbPeople
							.getText().toString());
					nb = UIFloor.this.nbPersonne;
				} catch (Exception e) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							UIFloor.this);
					builder.setTitle(R.string.number_of_people).show();
					return;
				}

				bundle.putSerializable("NB", nb);
				bundle.putSerializable("TABLE", UIFloor.this.table);
				intent.putExtras(bundle);
				dialog.dismiss();
				intent.setClass(UIFloor.this, UIFormule.class);
				startActivity(intent);
				finish();
			}

		}
	}

	private int Dp2Px(Context context, float dp) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}

	private int Px2Dp(Context context, float px) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (px / scale + 0.5f);
	}

	@Override
	public void onBackPressed() {
		intent.setClass(UIFloor.this, MainActivity.class);
		startActivity(intent);
		finish();

	}

	public void OnCancerButtonClick(View view) {
		intent.setClass(UIFloor.this, MainActivity.class);
		startActivity(intent);
		finish();
	}

}
