package com.example.servicelibre;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.arcsolu.sopda.biz.BizApp;
import com.arcsolu.sopda.biz.BizFactory;

public class Config extends Activity {

    BizApp bizApp;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config);
        bizApp = BizFactory.getBizApp(getFilesDir());
    }

    /**
     * 菜单设置
     *
     * @param view
     */
    public void onMenuButtonClick(View view) {
        if (bizApp.IsConnect(this)) {
            Intent intent = new Intent().setClass(Config.this, ModifyMenu.class);
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
    public void onSysButtonClick(View view) {
        Intent intent = new Intent().setClass(Config.this, SysmConf.class);
        startActivity(intent);
        finish();
    }

    /**
     * 返回退出
     *
     * @param view
     */
    public void onRetourButtonClick(View view) {
        Intent intent = new Intent().setClass(Config.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent().setClass(Config.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
