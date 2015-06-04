package com.example.servicelibre;

import java.io.ByteArrayOutputStream;
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
import com.arcsolu.sopda.entity.Menu;
import com.arcsolu.sopda.entity.Order;
import com.arcsolu.sopda.entity.OrderDetail;
import com.arcsolu.sopda.entity.Table;
import com.arcsolu.sopda.entity.User;
import com.example.servicelibre.UIFloor.MyListenner;
import com.example.servicelibre.UITable.Holder2;
import com.example.servicelibre.UITable.MenuAddListener;
import com.example.servicelibre.UITable.MenuSupListener;
import com.example.servicelibre.UITable.OnClickImageButton;

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
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class UIFloorMap extends Activity {
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
	GridView gridView;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.floor_map);
		byte[] img;

		map = new HashMap<Table, View>();
		intent = getIntent();
		bundle = intent.getExtras();
		bizApp = (BizApp) bundle.getSerializable("BIZAPP");
		bizFloor = (BizFloor) bundle.getSerializable("BIZFLOOR");
		bizTable = (BizTable) bundle.getSerializable("BIZTABLE");
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
		LinearLayout layout = (LinearLayout) findViewById(R.id.floormap_num);
		for (Floor floor : listfloor) {
			Button btn = new Button(this);
			btn.setBackground(this.getResources().getDrawable(
					R.drawable.floorbtn));
			btn.setText(floor.Floor);
			btn.setOnClickListener(new OnClickFloorListener(floor));
			btn.setWidth(Dp2Px(this, 90));
			btn.setHeight(Dp2Px(this, 100));
			layout.addView(btn);
		}
		try {
			img = bizApp.getBackground();
			Bitmap bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
			Drawable drawable = new BitmapDrawable(getResources(), bitmap);
			View v = findViewById(R.id.floormap);
			v.setBackground(drawable);
		} catch (Exception e) {
			e.printStackTrace();
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
			UIFloorMap.this.table = null;
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
		gridView = (GridView) this.findViewById(R.id.floormap_gridview);
		GridAdapter gridAdapter = new GridAdapter(this, R.id.floormap_gridview);
		gridView.setStretchMode(3);
		gridView.setAdapter(gridAdapter);
		gridAdapter.notifyDataSetChanged();

	}

	public class GridAdapter extends ArrayAdapter<Object> {
		private Context mContext;
		int mTextViewResourceID = 0;

		public GridAdapter(Context c, int textViewResourceId) {
			super(c, textViewResourceId);
			mContext = c;
			mTextViewResourceID = textViewResourceId;
		}

		public int getCount() {
			return floor.Tables.size();
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			TextView table = null;

			if (convertView == null) {
				table = new TextView(UIFloorMap.this);
				table.setTextColor(Color.BLACK);
				table.setText(floor.Tables.get(position).Number);

			} else {
				table = (TextView) convertView;
				table.setBackground(UIFloorMap.this.getResources().getDrawable(
						R.drawable.table_click));
				table.setTextColor(Color.BLACK);
				table.setText(floor.Tables.get(position).Number);
			}

			table.setTag(floor.Tables.get(position));
			table.setGravity(Gravity.CENTER);
			table.setTextSize(20);

			if (floor.Tables.get(position).State == Table.TableState.tsEmpty) {
				table.setBackground(UIFloorMap.this.getResources().getDrawable(
						R.drawable.tablebtn));
				table.setClickable(true);
				table.setOnClickListener(new MyListenner());
			} else {

				table.setBackground(UIFloorMap.this.getResources().getDrawable(
						R.drawable.table_click));
			}
			table.setTextSize(40);
			table.setWidth(UIFloorMap.this.Dp2Px(UIFloorMap.this, 100));
			table.setHeight(UIFloorMap.this.Dp2Px(UIFloorMap.this, 100));
			return table;
		}

	}

	class MyListenner implements OnClickListener {
		public void onClick(View arg0) {
			TextView ttn = (TextView) arg0;

			for (int i = 0; i < gridView.getChildCount(); i++) {
				Table _table = (Table) gridView.getChildAt(i).getTag();
				if (_table.State != Table.TableState.tsEmpty) {
					gridView.getChildAt(i).setBackground(
							UIFloorMap.this.getResources().getDrawable(
									R.drawable.table_click));
				} else {
					gridView.getChildAt(i).setBackground(
							UIFloorMap.this.getResources().getDrawable(
									R.drawable.table));
				}
			}
			table = (Table) arg0.getTag();
			// ttn.setBackground(Color.GREEN);
			AlertDialog.Builder builder = new AlertDialog.Builder(
					UIFloorMap.this);
			builder.setTitle(R.string.number_of_people);
			nbPeople = new EditText(UIFloorMap.this);
			nbPeople.setText("2");
			nbPeople.setInputType(InputType.TYPE_CLASS_NUMBER);
			builder.setView(nbPeople);
			nbPeople.selectAll();
			builder.setPositiveButton(R.string.OK, new OnClickTableListener());
			builder.setNegativeButton(R.string.cancel, null);
			dialog = builder.show();
		}
	}

	class OnClickTableListener implements DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			Integer nb = null;

			if (table != null && UIFloorMap.this.nbPersonne > 0) {
				try {
					UIFloorMap.this.nbPersonne = Integer.parseInt(nbPeople
							.getText().toString());
					nb = UIFloorMap.this.nbPersonne;
				} catch (Exception e) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							UIFloorMap.this);
					builder.setTitle(R.string.number_of_people).show();
					return;
				}

				bundle.putSerializable("NB", nb);
				bundle.putSerializable("TABLE", UIFloorMap.this.table);
				intent.putExtras(bundle);
				dialog.dismiss();
				intent.setClass(UIFloorMap.this, UIFormule.class);
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
		intent.setClass(UIFloorMap.this, MainActivity.class);
		startActivity(intent);
		finish();

	}

	public void OnCancerButtonClick(View view) {
		intent.setClass(UIFloorMap.this, MainActivity.class);
		startActivity(intent);
		finish();
	}

	/**
	 * 增加桌子
	 * 
	 * @param v
	 */
	public void OnClickAddTableClick(View v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(UIFloorMap.this);
		builder.setTitle(R.string.add_table);
		final EditText tableName = new EditText(UIFloorMap.this);
		tableName
				.setFilters(new InputFilter[] { new InputFilter.LengthFilter(4) });
		builder.setView(tableName);
		builder.setPositiveButton(R.string.OK,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String name = tableName.getText().toString();

						name = name.replace("\n", "");
						name = name.replace("\r", "");
						name = name.trim();

						Table table = null;
						try {
							table = bizTable.CreateTable(floor, name);

							if (!bizTable.InsertTable(table)) {
								throw new Exception();
							}
						} catch (Exception e) {
							e.printStackTrace();
							floor.Tables.remove(table);
							AlertDialog.Builder builder = new AlertDialog.Builder(
									UIFloorMap.this);
							builder.setMessage("Creat table fail, try again")
									.show();

						}
						floorView(floor);
					}
				});
		builder.setNegativeButton(R.string.cancel, null);
		dialog = builder.show();
	}

	/**
	 * 修改背景
	 * 
	 * @param v
	 */
	public void OnClickChangeBackGroundClick(View v) {
		try {
			Intent intent = new Intent("org.openintents.action.PICK_FILE");
			intent.putExtra("org.openintents.extra.TITLE", getResources()
					.getText(R.string.select_pic));
			startActivityForResult(intent, 1);
		} catch (Exception e) {

		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {

			if (resultCode == Activity.RESULT_OK) {

				Uri uri = data.getData();
				final Bitmap bmp = BitmapFactory.decodeFile(uri.getPath());
				new Thread() {
					public void run() {
						byte[] image;
						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
						image = stream.toByteArray();
						try {
							bizApp.setBackground(image);
						} catch (Exception e) {
							AlertDialog.Builder builder = new AlertDialog.Builder(
									UIFloorMap.this);
							builder.setTitle("Please install OI File Manager")
									.show();
							return;
						}
					}
				}.start();

				try {

					AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
					builder2.setMessage(
							getResources().getString(R.string.success)).show();
					Drawable drawable = new BitmapDrawable(getResources(), bmp);
					View v = findViewById(R.id.floormap);
					v.setBackground(drawable);

				} catch (Exception e) {
					AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
					builder2.setMessage("Error").show();
				}
			}
		}
	}

}
