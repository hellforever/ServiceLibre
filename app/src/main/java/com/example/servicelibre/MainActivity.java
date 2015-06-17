package com.example.servicelibre;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsolu.sopda.biz.BizApp;
import com.arcsolu.sopda.biz.BizFactory;
import com.arcsolu.sopda.entity.Order;
import com.arcsolu.sopda.entity.Parametres;
import com.arcsolu.sopda.entity.User;

import java.util.Locale;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    String password = "";
    Order thisOrder = null;
    String floorview;
    boolean isActive = false;
    Dialog dialogActiver;
    Handler handler;

    private void initLanguage(String localName) {
        Locale locale = null;
        Locale[] locales = Locale.getAvailableLocales();
        for (Locale thisLocale : locales) {
            if (thisLocale.toString().equalsIgnoreCase(localName)) {
                locale = thisLocale;
                break;
            }
        }
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void initBackground(String pic) {
        ImageButton flag1 = (ImageButton) findViewById(R.id.login_imageButton1);
        ImageButton flag2 = (ImageButton) findViewById(R.id.login_imageButton2);
        ImageButton flag3 = (ImageButton) findViewById(R.id.login_imageButton3);
        ImageButton flag4 = (ImageButton) findViewById(R.id.login_imageButton4);
        flag1.setOnClickListener(new LocaleSetClickListener("en"));
        flag2.setOnClickListener(new LocaleSetClickListener("zh_CN"));
        flag3.setOnClickListener(new LocaleSetClickListener("fr_FR"));
        flag4.setOnClickListener(new LocaleSetClickListener("de"));

        int bbg = R.drawable.map1;
        if (pic.contentEquals("map1")) {
            bbg = R.drawable.map1;
        } else if (pic.contentEquals("map2")) {
            bbg = R.drawable.map2;
        } else if (pic.contentEquals("map3")) {
            bbg = R.drawable.map3;
        } else if (pic.contentEquals("map4")) {
            bbg = R.drawable.map4;
        }
        View bg = findViewById(R.id.LinearLayout1);
        bg.setBackgroundDrawable(getResources().getDrawable(bbg));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getSharedPreferences("com",
                Context.MODE_PRIVATE);
        String localName = sharedPreferences.getString("locale", "fr");
        String pic = sharedPreferences.getString("map", "map1");
        initLanguage(localName);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initBackground(pic);


        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // 定义一个Handler，用于处理下载线程与UI间通讯
                if (!Thread.currentThread().isInterrupted()) {
                    switch (msg.what) {
                        case 0:
                            boolean network = false;
                            BizApp bizApp = BizFactory.getBizApp(getFilesDir());
                            try {
                                String address = bizApp
                                        .GetParam(Parametres.ParaKey.ADDRESS);
                                String database = bizApp
                                        .GetParam(Parametres.ParaKey.DATABASE);
                                String usename = bizApp
                                        .GetParam(Parametres.ParaKey.MASTER);
                                String password = bizApp
                                        .GetParam(Parametres.ParaKey.DB_PASSWORD);
                                network = bizApp.GetFBDB(address, database,
                                        usename, password);
                                floorview = bizApp
                                        .GetParam(Parametres.ParaKey.MAPMODE);
                            } catch (Exception e) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(
                                        MainActivity.this);
                                builder.setTitle(
                                        getText(R.string.connect_exception))
                                        .show();
                            }

                            if (!network) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(
                                        MainActivity.this);
                                builder.setTitle(getText(R.string.connect_fail))
                                        .show();
                            } else {

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

                                } else {
                                    Intent intent = new Intent();
                                    intent.setClass(MainActivity.this, UITable.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                            break;
                        default:

                    }
                }

                super.handleMessage(msg);
            }
        };

        if (isActive) {
            this.sendMsg(0);
        }

    }

    public void onNumButtonClick(View view) {
        Button btn = (Button) view;
        TextView edt = (TextView) findViewById(R.id.login_TextView1);
        String affichage = "";

        password = password + btn.getTag().toString();
        for (int i = 0; i < password.length(); i++) {
            affichage += "*";
        }
        edt.setText(affichage);
    }

    public void onSupButtonClick(View view) {

        TextView edt = (TextView) findViewById(R.id.login_TextView1);
        String temp = edt.getText().toString();
        if (temp.length() == 0) {
            return;
        }
        password = password.substring(0, temp.length() - 1);
        temp = temp.substring(0, temp.length() - 1);
        edt.setText(temp);

    }

    public void onOkButtonClick(View view) {
        User user = null;
        try {
            user = BizFactory.getBizApp(getFilesDir()).Login(password);
        } catch (Exception e) {
            new AlertDialog.Builder(this).setTitle("No networks, please configure the parameters")
                    .show();
            return;
        }

        TextView edt = (TextView) findViewById(R.id.login_TextView1);
        if (user == null) {
            password = "";
            edt.setText(password);
        } else {
            Intent intent = new Intent();
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

    public void onConfigButtonClick(View view) {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, AdminConfig.class);
        startActivity(intent);
        finish();
    }

    class LocaleSetClickListener implements OnClickListener {
        private Locale locale = Locale.SIMPLIFIED_CHINESE;

        public LocaleSetClickListener(String locale) {
            Locale[] locales = Locale.getAvailableLocales();
            for (Locale l : locales) {
                if (l.toString().equalsIgnoreCase(locale)) {
                    this.locale = l;
                    break;
                }
            }
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(MainActivity.this, "" + locale, Toast.LENGTH_SHORT).show();
            SharedPreferences sharedPreferences = getSharedPreferences("com", Context.MODE_PRIVATE);
            sharedPreferences.edit().putString("locale", locale.toString()).apply();
            startActivity(new Intent().setClass(MainActivity.this, MainActivity.class));
            finish();
        }
    }

    private void sendMsg(int indice) {
        Message msg = new Message();
        msg.what = indice;
        handler.sendMessage(msg);
    }

}
