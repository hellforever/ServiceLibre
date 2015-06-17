package com.example.servicelibre;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.arcsolu.sopda.biz.BizApp;
import com.arcsolu.sopda.biz.BizFactory;

public class AdminConfig extends Activity {

    Intent intent;
    String password = "";
    BizApp bizApp;
    private final int passwordLength = 6;

    protected void onCreate(Bundle savedInstanceState) {
        intent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_config);
        bizApp = BizFactory.getBizApp(getFilesDir());
    }

    /**
     * 密码按键响应
     *
     * @param view
     */
    public void onNumButtonClick(View view) {

        Button btn = (Button) view;
        TextView edt = (TextView) findViewById(R.id.config_TextView1);
        String affichage = "";

        password += btn.getText().toString();
        for (int i = 0; i < password.length(); i++) {
            affichage += "*";
        }
        edt.setText(affichage);

        boolean isGoodPassWord = false;

        try {
            isGoodPassWord = bizApp.AdminLogin(password);
        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Adminlogin Exception").show();
        }
        if (isGoodPassWord) {
            intent.setClass(AdminConfig.this, Config.class);
            startActivity(intent);
            finish();
        } else if (password.length() == passwordLength) {
            password = "";
            edt.setText("");

        }

    }

    @Override
    public void onBackPressed() {
        intent.setClass(AdminConfig.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
