package com.example.servicelibre;

import com.arcsolu.sopda.biz.BizApp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Config extends Activity {

	Intent intent;
	Bundle bundle;
	BizApp bizApp;

	protected void onCreate(Bundle savedInstanceState) {
		intent = getIntent();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.config);
		bundle = intent.getExtras();
		bizApp = (BizApp) bundle.getSerializable("BIZAPP");
	}

	/**
	 * 菜单设置
	 * 
	 * @param view
	 */
	public void OnMenuButtonClick(View view) {
		if (bizApp.IsConnect(this)) {
			intent.setClass(Config.this, ModifyMenu.class);
			startActivity(intent);
			finish();
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("No networks, please configure the parameters")
					.show();
			return;
		}
	}

	/**
	 * 系统设置
	 * 
	 * @param view
	 */
	public void OnSysButtonClick(View view) {
		intent.setClass(Config.this, Sysm_conf.class);
		startActivity(intent);
		finish();
	}

	/**
	 * 返回退出
	 * 
	 * @param view
	 */
	public void OnRetourButtonClick(View view) {
		intent.setClass(Config.this, MainActivity.class);
		startActivity(intent);
		finish();
	}

	@Override
	public void onBackPressed() {
		intent.setClass(Config.this, MainActivity.class);
		startActivity(intent);
		finish();
	}

}
