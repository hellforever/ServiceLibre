package com.example.servicelibre;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.arcsolu.sopda.biz.BizApp;
import com.arcsolu.sopda.biz.BizMenu;
import com.arcsolu.sopda.biz.BizPrinter;
import com.arcsolu.sopda.entity.Menu;
import com.arcsolu.sopda.entity.Printer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class ModifyMenu extends Activity {
	Intent intent;
	ListView listView;
	BizMenu bizMenu;
	BizApp bizApp;
	BizPrinter bizPrinter;
	List<Menu> listMenu;
	Menu thismenu = null;
	ListAdapter listAdapter;

	List<Printer> printers;
	Map<View, Printer> printermap;
	boolean isShown = true;
	byte[] image;
	PopupWindow popView;
	List<String> catas;
	MenuComparator comp;
	BitmapFactory.Options opts;
	Resources res;
	Dialog dialog2;
	ProgressDialog progressDialog;

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.modify_menu);

		opts = new BitmapFactory.Options();
		opts.inSampleSize = 4;

		intent = getIntent();
		bizMenu = (BizMenu) intent.getExtras().getSerializable("BIZMENU");
		bizApp = (BizApp) intent.getExtras().getSerializable("BIZAPP");
		res = this.getResources();
		try {
			bizApp.DownloadPrinters();
		} catch (Exception e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("DownloadPrinter Exception").show();
		}
		bizPrinter = (BizPrinter) intent.getExtras().getSerializable(
				"BIZPRINTER");
		try {
			printers = bizPrinter.Select();
		} catch (Exception e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("selectprinter Exception").show();
		}
		catas = new ArrayList<String>();
		LinearLayout printerView = (LinearLayout) findViewById(R.id.modimenu_printer);
		listView = (ListView) findViewById(R.id.modimenu_view);

		try {
			listMenu = bizMenu.Select();
		} catch (Exception e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("SelectMenu Exception").show();
		}
		for (Menu menu : listMenu) {
			if (!catas.contains(menu.Catalog)) {
				catas.add(menu.Catalog);
			}
		}
		comp = new MenuComparator();
		Collections.sort(listMenu, comp);
		printermap = new HashMap<View, Printer>();

		EditText yut = (EditText) findViewById(R.id.modimenu_editText3);
		yut.setText(String.valueOf(listMenu.size()));

		listAdapter = new ListAdapter(this, R.layout.modify_menulist, listMenu);
		listView.setAdapter(listAdapter);

		try {

			for (Printer printer : printers) {
				CheckBox ch = new CheckBox(this);
				ch.setText(printer.Name);
				printerView.addView(ch);
				printermap.put(ch, printer);
			}
		} catch (Exception e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Printer Exception").show();
		}

	}

	public void OnRetourButtonClick(View view) {
		intent.setClass(ModifyMenu.this, Config.class);
		startActivity(intent);
		finish();
	}

	@Override
	public void onBackPressed() {
		intent.setClass(ModifyMenu.this, Config.class);
		startActivity(intent);
		finish();
	}

	/**菜单显示
	 * @author Zhu ZIJIAN
	 *
	 */
	public class ListAdapter extends ArrayAdapter<Object> {
		int mTextViewResourceID = 0;
		private Context mContext;
		List<Menu> list;

		public ListAdapter(Context context, int textViewResourceId,
				List<Menu> list) {
			super(context, textViewResourceId);
			mTextViewResourceID = textViewResourceId;
			mContext = context;
			this.list = list;
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
						.findViewById(R.id.modify_menulist_imageView1);
				holder.text = (TextView) convertView
						.findViewById(R.id.modify_menulist_textView2);
				holder.fam = (TextView) convertView
						.findViewById(R.id.modify_menulist_textView1);

				if (list.get(position) != null) {

					if (list.get(position).Pic != null) {
						Bitmap bitmap = BitmapFactory.decodeByteArray(
								list.get(position).Pic, 0,
								list.get(position).Pic.length, opts);
						holder.image.setImageBitmap(bitmap);
					}
				}
				if (!list.get(position).IsVailable
						|| list.get(position).Printers.isEmpty()) {
					holder.fam.setTextColor(Color.DKGRAY);
					holder.text.setTextColor(Color.DKGRAY);
				} else {
					holder.fam.setTextColor(Color.WHITE);
					holder.text.setTextColor(Color.WHITE);
				}
				holder.fam.setText(list.get(position).Catalog);
				if (list.get(position).Display != null) {
					holder.text.setText(list.get(position).Name + "\n"
							+ list.get(position).Display);
				} else {
					holder.text.setText(list.get(position).Name);
				}
				holder.menu = list.get(position);
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
				if (list.get(position) != null) {

					if (list.get(position).Pic != null) {
						Bitmap bitmap = BitmapFactory.decodeByteArray(
								list.get(position).Pic, 0,
								list.get(position).Pic.length, opts);
						holder.image.setImageBitmap(bitmap);
					}
				}
				if (!list.get(position).IsVailable
						|| list.get(position).Printers.isEmpty()) {
					holder.fam.setTextColor(Color.DKGRAY);
					holder.text.setTextColor(Color.DKGRAY);
				} else {
					holder.fam.setTextColor(Color.WHITE);
					holder.text.setTextColor(Color.WHITE);
				}
				holder.fam.setText(list.get(position).Catalog);
				if (list.get(position).Display != null) {
					holder.text.setText(list.get(position).Name + "\n"
							+ list.get(position).Display);
				} else {
					holder.text.setText(list.get(position).Name);
				}
				holder.menu = list.get(position);
			}

			if (thismenu != null && list.get(position).equals(thismenu)) {
				convertView.setBackgroundColor(Color.YELLOW);
			} else {
				convertView.setBackgroundColor(Color.BLACK);
			}

			return convertView;
		}
	}

	class Holder {
		Menu menu;
		ImageView image;
		TextView text;
		TextView fam;
	}

	/**添加菜单按钮
	 * @param view
	 */
	public void onClickAddButton(View view) {
		Menu menu = bizMenu.GetMenu();
		EditText cata = (EditText) findViewById(R.id.modimenu_editText0);
		EditText name = (EditText) findViewById(R.id.modimenu_editText1);
		EditText description = (EditText) findViewById(R.id.modimenu_editText2);
		EditText limit = (EditText) findViewById(R.id.modimenu_editText3);
		CheckBox box1 = (CheckBox) findViewById(R.id.modimenu_checkBox1);
		CheckBox box2 = (CheckBox) findViewById(R.id.modimenu_checkBox2);
		CheckBox box3 = (CheckBox) findViewById(R.id.modimenu_checkBox3);
		CheckBox bbox = (CheckBox) findViewById(R.id.modimenu_checkava);
		menu.IsVailable = bbox.isChecked();
		menu.Display = description.getText().toString();
		menu.Name = name.getText().toString();
		menu.Chef = box3.isChecked();
		menu.Top5 = box1.isChecked();
		menu.NEW = box2.isChecked();
		menu.Catalog = cata.getText().toString();
		if (!catas.contains(menu.Catalog)) {
			catas.add(menu.Catalog);
		}
		try {
			menu.Limit = Integer.parseInt(limit.getText().toString());
		} catch (Exception e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(
					"Please input a number in the blank of limit in turn")
					.show();
			return;
		}
		menu.Pic = image;
		menu.Printers.clear();
		for (View v : printermap.keySet()) {
			CheckBox vv = (CheckBox) v;
			if (vv.isChecked()) {
				menu.Printers.add(printermap.get(v));
			}
		}

		listMenu.add(menu);
		try {
			bizMenu.Save(listMenu);
			listMenu = bizMenu.Select();
		} catch (Exception e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Save and select menu Exception").show();
		}
		Collections.sort(listMenu, comp);
		listAdapter.list = listMenu;
		listAdapter.notifyDataSetChanged();
		listView.setSelection(listMenu.size());
		thismenu = null;
	}

	/**选中菜单按钮
	 * @param view
	 */
	public void onClickMenuButton(View view) {
		for (int i = 0; i < listView.getChildCount(); i++) {
			listView.getChildAt(i).setBackgroundColor(Color.BLACK);
		}

		thismenu = ((Holder) view.getTag()).menu;
		image = thismenu.Pic;
		view.setBackgroundColor(Color.YELLOW);

		if (thismenu == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Menu null").show();
			return;
		}

		EditText cata = (EditText) findViewById(R.id.modimenu_editText0);
		EditText name = (EditText) findViewById(R.id.modimenu_editText1);
		EditText description = (EditText) findViewById(R.id.modimenu_editText2);
		EditText limit = (EditText) findViewById(R.id.modimenu_editText3);
		CheckBox box1 = (CheckBox) findViewById(R.id.modimenu_checkBox1);
		CheckBox box2 = (CheckBox) findViewById(R.id.modimenu_checkBox2);
		CheckBox box3 = (CheckBox) findViewById(R.id.modimenu_checkBox3);
		CheckBox bbox = (CheckBox) findViewById(R.id.modimenu_checkava);
		ImageView imageView = (ImageView) findViewById(R.id.modimenu_ImageView);
		name.setText(thismenu.Name);
		description.setText(thismenu.Display);
		cata.setText(thismenu.Catalog);
		limit.setText(String.valueOf(thismenu.Limit));
		box1.setChecked(thismenu.Top5);
		box2.setChecked(thismenu.NEW);
		box3.setChecked(thismenu.Chef);
		bbox.setChecked(thismenu.IsVailable);

		if (thismenu.Pic != null) {
			image = thismenu.Pic;
			Bitmap bitmap = BitmapFactory.decodeByteArray(thismenu.Pic, 0,
					thismenu.Pic.length);
			imageView.setImageBitmap(bitmap);
		}

		for (View v : printermap.keySet()) {
			CheckBox ch = (CheckBox) v;
			if (thismenu.Printers.contains(printermap.get(v))) {
				ch.setChecked(true);
			} else {
				ch.setChecked(false);
			}
		}

	}

	/**点击修改菜单按钮
	 * @param view
	 */
	public void onClickEditButton(View view) {
		if (thismenu != null) {
			Menu menu = thismenu;
			EditText cata = (EditText) findViewById(R.id.modimenu_editText0);
			EditText name = (EditText) findViewById(R.id.modimenu_editText1);
			EditText description = (EditText) findViewById(R.id.modimenu_editText2);
			EditText limit = (EditText) findViewById(R.id.modimenu_editText3);
			CheckBox box1 = (CheckBox) findViewById(R.id.modimenu_checkBox1);
			CheckBox box2 = (CheckBox) findViewById(R.id.modimenu_checkBox2);
			CheckBox box3 = (CheckBox) findViewById(R.id.modimenu_checkBox3);
			CheckBox bbox = (CheckBox) findViewById(R.id.modimenu_checkava);
			menu.IsVailable = bbox.isChecked();
			menu.Display = description.getText().toString();
			menu.Name = name.getText().toString();
			menu.Chef = box3.isChecked();
			menu.Top5 = box1.isChecked();
			menu.NEW = box2.isChecked();
			menu.Pic = image;
			menu.Catalog = cata.getText().toString();
			if (!catas.contains(menu.Catalog)) {
				catas.add(menu.Catalog);
			}
			try {
				menu.Limit = Integer.parseInt(limit.getText().toString());
			} catch (Exception e) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(
						"Please input a number in the blank of limit in turn")
						.show();
				return;
			}
			menu.Printers.clear();
			for (View v : printermap.keySet()) {
				CheckBox vv = (CheckBox) v;
				if (vv.isChecked()) {
					menu.Printers.add(printermap.get(v));
				} else {
					menu.Printers.remove(printermap.get(v));
				}
			}
			try {
				bizMenu.Save(listMenu);

				listMenu = bizMenu.Select();
			} catch (Exception e) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Save and select menu Exception").show();
			}
			Collections.sort(listMenu, comp);
			listAdapter.list = listMenu;
			listAdapter.notifyDataSetChanged();
			thismenu = null;
		}
	}

	/**菜单上移按钮
	 * @param view
	 */
	public void onClickMoveUpButton(View view) {
		if (thismenu != null) {
			Menu menu = thismenu;

			int _p1 = listMenu.indexOf(menu);

			if (_p1 > 0) {
				int _p2 = _p1 - 1;
				int temp;
				temp = listMenu.get(_p1).Index;
				listMenu.get(_p1).Index = listMenu.get(_p2).Index;
				listMenu.get(_p2).Index = temp;
				Collections.sort(listMenu, comp);
				listAdapter.list = listMenu;
				listAdapter.notifyDataSetChanged();
				listView.setSelection(_p2);

			}
		}

	}

	/**点击菜单下移按钮
	 * @param view
	 */
	public void onClickMoveDownButton(View view) {
		if (thismenu != null) {
			Menu menu = thismenu;
			int _p1 = listMenu.indexOf(menu);
			if (_p1 < listMenu.size() - 1) {
				int _p2 = _p1 + 1;
				int temp;
				temp = listMenu.get(_p1).Index;
				listMenu.get(_p1).Index = listMenu.get(_p2).Index;
				listMenu.get(_p2).Index = temp;
				Collections.sort(listMenu, comp);
				listAdapter.list = listMenu;
				listAdapter.notifyDataSetChanged();
				listView.setSelection(_p2);
			}
		}
	}

	/**点击垃圾箱按钮
	 * @param view
	 */
	public void onClickDustbinButton(View view) {
		Button btn = (Button) view;
		Button btn2 = (Button) findViewById(R.id.modimenu_buttonDelete);
		thismenu = null;
		if (isShown) {
			btn.setText(res.getText(R.string.menu));
			btn2.setText(res.getText(R.string.undelete));
			try {
				listMenu = bizMenu.SelectDeletedMenus();
			} catch (Exception e) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Select deleted menus Exception").show();
			}
			Collections.sort(listMenu, comp);
			listAdapter.list = listMenu;
			listAdapter.notifyDataSetChanged();
			isShown = false;
			Button _add = (Button) findViewById(R.id.modimenu_buttonAdd);
			Button _change = (Button) findViewById(R.id.modimenu_buttonChange);
			Button _up = (Button) findViewById(R.id.modimenu_buttonUp);
			Button _down = (Button) findViewById(R.id.modimenu_buttonDown);
			_change.setClickable(false);
			_up.setClickable(false);
			_down.setClickable(false);
			_add.setClickable(false);
		} else {
			btn.setText(res.getText(R.string.recycle));
			btn2.setText(res.getText(R.string.delete));
			try {
				listMenu = bizMenu.Select();
			} catch (Exception e) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Select menus Exception").show();
			}
			Collections.sort(listMenu, comp);
			listAdapter.list = listMenu;
			listAdapter.notifyDataSetChanged();
			isShown = true;
			Button _add = (Button) findViewById(R.id.modimenu_buttonAdd);
			Button _change = (Button) findViewById(R.id.modimenu_buttonChange);
			Button _up = (Button) findViewById(R.id.modimenu_buttonUp);
			Button _down = (Button) findViewById(R.id.modimenu_buttonDown);
			_change.setClickable(true);
			_up.setClickable(true);
			_down.setClickable(true);
			_add.setClickable(true);
		}
	}

	/**点击删除菜单按钮
	 * @param view
	 */
	public void onClickDeleteButton(View view) {
		if (isShown) {
			if (thismenu != null) {
				int _t = listMenu.indexOf(thismenu);
				try {
					bizMenu.Delete(thismenu);
				} catch (Exception e) {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setMessage("Delete menus Exception").show();
				}
				listMenu = bizMenu.Select();
				Collections.sort(listMenu, comp);
				bizMenu.Save(listMenu);
				try {
					listAdapter.list = listMenu;
				} catch (Exception e) {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setMessage("Save menus Exception").show();
				}
				listAdapter.notifyDataSetChanged();
				listView.setSelection(_t);
				thismenu = null;

			}
		}

		else {
			if (thismenu != null) {
				int _t = listMenu.indexOf(thismenu);
				try {
					bizMenu.Undelete(thismenu);
					listMenu = bizMenu.SelectDeletedMenus();
				} catch (Exception e) {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setMessage("Undelete Exception").show();
				}
				Collections.sort(listMenu, comp);
				List<Menu> _listMenu = bizMenu.Select();
				bizMenu.Save(_listMenu);
				listAdapter.list = listMenu;
				listAdapter.notifyDataSetChanged();
				listView.setSelection(_t);
				thismenu = null;

			}
		}
	}

	/**点击下载按钮
	 * @param view
	 */
	public void onClickDownloadButton(View view) {

		progressDialog = new ProgressDialog(ModifyMenu.this);
		final UIOnProgressCaller caller = new UIOnProgressCaller();
		caller.SetProgressDlg(progressDialog);

		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMessage("Loading...");
		progressDialog.setCancelable(false);
		progressDialog.show();

		Thread t0 = new Thread() {
			public void run() {
				boolean isDownload = false;
				try {
					bizApp.SetOnProgressCaller(caller);
					isDownload = bizApp.DownloadMenus();

				} catch (Exception e) {
					sendMsg(2);
					return;

				}
				bizApp.SetOnProgressCaller(null);
				if (!isDownload) {
					sendMsg(2);
					return;
				} else {
					sendMsg(0);
				}

			}

		};
		t0.start();
	}

	/**点击上传按钮
	 * @param view
	 */
	public void onClickUpdateButton(View view) {
		try {
			bizMenu.Save(listMenu);
		} catch (Exception e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Save menu Exception").show();
		}

		progressDialog = new ProgressDialog(ModifyMenu.this);
		final UIOnProgressCaller caller = new UIOnProgressCaller();
		caller.SetProgressDlg(progressDialog);

		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMessage("Loading...");
		progressDialog.setCancelable(false);
		progressDialog.show();

		Thread t1 = new Thread() {
			public void run() {

				boolean isUpdate = false;
				try {
					bizApp.SetOnProgressCaller(caller);
					isUpdate = bizApp.Update();
				} catch (Exception e) {
					sendMsg(2);
					return;
				}
				if (!isUpdate) {
					sendMsg(2);
					return;

				} else {
					sendMsg(1);
				}
			}
		};
		t1.start();

	}

	/**更换菜单图片按钮
	 * @param view
	 */
	public void onClickImage(View view) {
		try {
			Intent intent = new Intent("org.openintents.action.PICK_FILE");
			intent.putExtra("org.openintents.extra.TITLE",
					res.getText(R.string.select_pic));
			startActivityForResult(intent, 1);
		} catch (Exception e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("No networks, please configure the parameters")
					.show();
			return;
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if (resultCode == Activity.RESULT_OK) {
				Uri uri = data.getData();

				ImageView imageView = (ImageView) findViewById(R.id.modimenu_ImageView);
				Bitmap bmp = BitmapFactory.decodeFile(uri.getPath());
				imageView.setImageBitmap(bmp);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
				this.image = stream.toByteArray();
			}
		}
	}

	/**Catalog 记忆下拉菜单
	 * @param view
	 */
	public void onClickDropButton(View view) {
		if (catas.size() > 0) {
			DropAdapter adt = new DropAdapter(this,
					android.R.layout.simple_spinner_dropdown_item, catas);
			ListView listView = new ListView(this);
			listView.setAdapter(adt);
			EditText ca = (EditText) findViewById(R.id.modimenu_editText0);
			popView = new PopupWindow(listView, ca.getWidth(),
					ViewGroup.LayoutParams.WRAP_CONTENT, true);
			popView.setFocusable(true);
			popView.setOutsideTouchable(true);
			popView.setBackgroundDrawable(getResources().getDrawable(
					android.R.color.white));

			if (!popView.isShowing()) {
				popView.showAsDropDown(ca);
			} else {
				popView.dismiss();
			}
		}
	}

	/**
	 * @author ZHU Zijian
	 *
	 */
	class DropAdapter extends ArrayAdapter<String> {
		List<String> objects;

		public DropAdapter(Context context, int textViewResourceId,
				List<String> objects) {

			super(context, textViewResourceId, objects);
			this.objects = objects;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			convertView = super.getView(position, convertView, parent);
			convertView.setClickable(true);
			convertView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					EditText edt = (EditText) findViewById(R.id.modimenu_editText0);
					edt.setText(((TextView) v).getText().toString());
					popView.dismiss();
				}
			});
			return convertView;
		}

	}

	public class MenuComparator implements Comparator<Menu> {

		@Override
		public int compare(Menu o1, Menu o2) {
			if (o1.Index > o2.Index)
				return 1;
			else if (o1.Index == o2.Index)
				return 0;
			else
				return -1;

		}
	}

	/**
	 * 下载的线程
	 */
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// 定义一个Handler，用于处理下载线程与UI间通讯
			if (!Thread.currentThread().isInterrupted()) {
				switch (msg.what) {
				case 0:
					try {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								ModifyMenu.this);
						builder.setMessage(
								ModifyMenu.this.getResources().getString(
										R.string.success)).show();
						ModifyMenu.this.listMenu = bizMenu.Select();
						Collections.sort(listMenu, comp);
						ModifyMenu.this.listAdapter.list = listMenu;
						ModifyMenu.this.listAdapter.notifyDataSetChanged();
					} catch (Exception e) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								ModifyMenu.this);
						builder.setMessage("Select Menu Exception").show();

					}
					break;

				case 1:
					try {
						listMenu = bizMenu.Select();
					} catch (Exception e) {
						sendMsg(2);
						return;
					}
					AlertDialog.Builder builder2 = new AlertDialog.Builder(
							ModifyMenu.this);
					builder2.setMessage(ModifyMenu.this.getResources().getString(R.string.success));
					builder2.show();

					Collections.sort(listMenu, comp);
					listAdapter.list = listMenu;
					listAdapter.notifyDataSetChanged();
					break;

				case -1:
					String error = msg.getData().getString("error");
					Toast.makeText(ModifyMenu.this, error, 1).show();
					break;
				case 2:
					AlertDialog.Builder builder = new AlertDialog.Builder(
							ModifyMenu.this);
					builder.setMessage("Download Menu Exception").show();
					break;
				}
			}
			super.handleMessage(msg);
		}
	};

	private void sendMsg(int indice) {
		Message msg = new Message();
		msg.what = indice;
		handler.sendMessage(msg);
	}
}
