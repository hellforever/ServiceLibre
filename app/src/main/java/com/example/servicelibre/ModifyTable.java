package com.example.servicelibre;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ModifyTable extends Activity {
	Intent intent;
	protected void onCreate(Bundle savedInstanceState) {
		intent = getIntent();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.modify_table);
	}
	
	public void OnRetourButtonClick(View view) {
      intent.setClass(ModifyTable.this, Config.class);
      startActivity(intent);
      finish();
	}
}
