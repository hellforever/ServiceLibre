package com.example.servicelibre;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.arcsolu.sopda.biz.BizApp;
import com.arcsolu.sopda.biz.BizFactory;
import com.arcsolu.sopda.biz.BizMenu;
import com.arcsolu.sopda.biz.BizPrinter;
import com.arcsolu.sopda.entity.Formule;
import com.arcsolu.sopda.entity.Parametres;
import com.arcsolu.sopda.entity.Printer;
import com.arcsolu.sopda.entity.Script;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
@SuppressLint("NewApi")
public class SysmConf extends Activity {
    Intent intent;
    BizApp bizApp;
    BizMenu bizMenu;
    BizPrinter bizPrinter;

    List<Printer> testList;
    List<Printer> listPrinter;
    List<Script> listScript;
    Map<Printer, View> map;
    Map<View, Formule> mapFormule;
    Resources res;
    Dialog dlg1;

    protected void onCreate(Bundle savedInstanceState) {
        intent = getIntent();
        map = new HashMap<Printer, View>();
        mapFormule = new HashMap<View, Formule>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sys_conf);

        TextView tx1 = (TextView) findViewById(R.id.sysconfig_textView2);
        TextView tx2 = (TextView) findViewById(R.id.sysconfig_textView3);

        tx1.setText(tx1.getText() + "/min");
        tx2.setText(tx2.getText() + "/min");

        testList = new ArrayList<Printer>();
        bizApp = BizFactory.getBizApp(getFilesDir());
        bizMenu = BizFactory.getBizMenu();
        bizPrinter = BizFactory.getBizPrinter();
        res = getResources();

        boolean network = false;
        String address = null;
        String database = null;
        String usename = null;
        String password = null;
        try {
            address = bizApp.GetParam(Parametres.ParaKey.ADDRESS);
            database = bizApp.GetParam(Parametres.ParaKey.DATABASE);
            usename = bizApp.GetParam(Parametres.ParaKey.MASTER);
            password = bizApp.GetParam(Parametres.ParaKey.DB_PASSWORD);
            network = bizApp.GetFBDB(address, database, usename, password);
        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Can't connect to database").show();
        }

        if (network) {
            EditText edt1 = (EditText) findViewById(R.id.sysconfig_EditText1);
            EditText edt2 = (EditText) findViewById(R.id.sysconfig_EditText2);
            EditText edt3 = (EditText) findViewById(R.id.sysconfig_EditText3);
            EditText edt4 = (EditText) findViewById(R.id.sysconfig_EditText4);
            EditText edt5 = (EditText) findViewById(R.id.sysconfig_editText5);
            EditText edt6 = (EditText) findViewById(R.id.sysconfig_editText6);
            EditText edt7 = (EditText) findViewById(R.id.sysconfig_editText7);
            EditText edt17 = (EditText) findViewById(R.id.sysconfig_editText17);

            try {
                edt1.setText(address);
                edt2.setText(database);
                edt3.setText(usename);
                edt4.setText(password);
                bizApp.DownloadParams();
                bizApp.DownloadPrinters();
                bizApp.DownloadFormules();
                List<String> listFormuleAva = bizMenu.getAvailableFormules();
                List<Formule> listFormule = bizMenu.SelectFormule();
                List<Formule> listFormuleCache = bizMenu
                        .SelectAvailableFormule();
                listFormuleCache.clear();
                for (Formule formul : listFormule) {
                    if (listFormuleAva.contains(formul.Id)) {
                        bizMenu.setFormule(formul);
                    }
                }
                listFormuleCache = bizMenu.SelectAvailableFormule();

                bizMenu.SaveAvailableFormules(listFormuleCache);

                String t1 = bizApp.GetParam(Parametres.ParaKey.MAXTIME);
                int tt1 = Integer.parseInt(t1) / 60;
                String t2 = bizApp.GetParam(Parametres.ParaKey.TIME_OF_TURN);
                int tt2 = Integer.parseInt(t2) / 60;
                edt5.setText(String.valueOf(tt1));
                edt6.setText(String.valueOf(tt2));
                edt7.setText(bizApp
                        .GetParam(Parametres.ParaKey.COMMANDE_PAR_PERSON));
                edt17.setText(bizApp.GetParam(Parametres.ParaKey.TURN));
            } catch (Exception e) {
                e.printStackTrace();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Parametre Exception").show();
                return;
            }

            LinearLayout listview = (LinearLayout) findViewById(R.id.sys_config_printerview);

            try {
                listPrinter = bizPrinter.Select();
                listScript = bizPrinter.SelectScript();
                for (Printer printer : listPrinter) {
                    TextView tx = new TextView(this);
                    tx.setBackgroundColor(Color.BLACK);
                    tx.setHeight(50);
                    tx.setTextSize(20);
                    tx.setText(printer.Name);
                    map.put(printer, tx);
                    listview.addView(tx);
                    tx.setOnClickListener(new OnClickListener() {

                        boolean selected = false;

                        @Override
                        public void onClick(View arg0) {
                            // TODO Auto-generated method stub
                            if (!selected) {
                                selected = true;
                                arg0.setBackgroundColor(Color.YELLOW);
                                for (Printer pr : listPrinter) {
                                    if (map.get(pr).equals(arg0)) {
                                        testList.add(pr);
                                        break;
                                    }
                                }
                            } else {
                                selected = false;
                                arg0.setBackgroundColor(Color.BLACK);
                                for (Printer pr : listPrinter) {
                                    if (map.get(pr).equals(arg0)) {
                                        testList.remove(pr);
                                        break;
                                    }
                                }
                            }
                        }
                    });
                    tx.setLongClickable(true);
                    tx.setOnLongClickListener(new LongPressedPrinterListener(
                            printer));
                }

            } catch (Exception e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Printer Exception").show();
            }
        }
    }

    /**
     * 确认配置
     *
     * @param view
     */
    public void onClickOkButton(View view) {
        EditText edt5 = (EditText) findViewById(R.id.sysconfig_editText5);
        EditText edt6 = (EditText) findViewById(R.id.sysconfig_editText6);
        EditText edt7 = (EditText) findViewById(R.id.sysconfig_editText7);
        EditText edt8 = (EditText) findViewById(R.id.sysconfig_editText17);
        String totaltime = edt5.getText().toString();
        String turntime = edt6.getText().toString();
        String orderlimit = edt7.getText().toString();
        String turn = edt8.getText().toString();

        try {
            Integer.parseInt(totaltime);
            Integer.parseInt(turntime);
            Integer.parseInt(orderlimit);
            Integer.parseInt(turn);

        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Error input");
            builder.show();
            return;
        }
        int t1 = Integer.parseInt(totaltime) * 60;
        int t2 = Integer.parseInt(turntime) * 60;
        try {
            bizApp.SetParam(Parametres.ParaKey.MAXTIME, String.valueOf(t1));
            bizApp.SetParam(Parametres.ParaKey.TIME_OF_TURN, String.valueOf(t2));
            bizApp.SetParam(Parametres.ParaKey.COMMANDE_PAR_PERSON, orderlimit);
            bizApp.SetParam(Parametres.ParaKey.TURN, turn);
            bizApp.UpdateParams(Parametres.ParaKey.MAXTIME);
            bizApp.UpdateParams(Parametres.ParaKey.TIME_OF_TURN);
            bizApp.UpdateParams(Parametres.ParaKey.COMMANDE_PAR_PERSON);
            bizApp.UpdateParams(Parametres.ParaKey.PRINTER_DF_ID);
            bizApp.UpdateParams(Parametres.ParaKey.TURN);

        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Save parameter Exception").show();
            return;
        }
        intent.setClass(SysmConf.this, Config.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        intent.setClass(SysmConf.this, Config.class);
        startActivity(intent);
        finish();
    }

    /**
     * 连接服务器
     *
     * @param view
     */
    public void onClickTestButton(View view) {
        EditText edt1 = (EditText) findViewById(R.id.sysconfig_EditText1);
        EditText edt2 = (EditText) findViewById(R.id.sysconfig_EditText2);
        EditText edt3 = (EditText) findViewById(R.id.sysconfig_EditText3);
        EditText edt4 = (EditText) findViewById(R.id.sysconfig_EditText4);
        String adress = edt1.getText().toString();
        String database = edt2.getText().toString();
        String usename = edt3.getText().toString();
        String password = edt4.getText().toString();

        boolean isConnect = false;
        try {
            isConnect = bizApp.GetFBDB(adress, database, usename, password);
        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Can't connect to server").show();
            return;
        }

        if (!isConnect) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Can't connect to database ");
            builder.show();
        } else {
            try {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Success");
                builder.show();
                bizApp.DownloadParams();
                bizApp.DownloadPrinters();
                bizApp.DownloadFormules();
                List<String> listFormuleAva = bizMenu.getAvailableFormules();
                List<Formule> listFormule = bizMenu.SelectFormule();
                List<Formule> listttyy = bizMenu.SelectAvailableFormule();
                listttyy.clear();
                for (Formule formul : listFormule) {
                    if (listFormuleAva.contains(formul.Id)) {
                        bizMenu.setFormule(formul);
                    }
                }

                bizApp.SetParam(Parametres.ParaKey.ADDRESS, adress);
                bizApp.SetParam(Parametres.ParaKey.DATABASE, database);
                bizApp.SetParam(Parametres.ParaKey.MASTER, usename);
                bizApp.SetParam(Parametres.ParaKey.DB_PASSWORD, password);

                String orderlimit = bizApp
                        .GetParam(Parametres.ParaKey.COMMANDE_PAR_PERSON);
                String turn = bizApp.GetParam(Parametres.ParaKey.TURN);
                EditText edt5 = (EditText) findViewById(R.id.sysconfig_editText5);
                EditText edt6 = (EditText) findViewById(R.id.sysconfig_editText6);
                EditText edt7 = (EditText) findViewById(R.id.sysconfig_editText7);
                EditText edt17 = (EditText) findViewById(R.id.sysconfig_editText17);
                String t1 = bizApp.GetParam(Parametres.ParaKey.MAXTIME);
                int tt1 = Integer.parseInt(t1) / 60;
                String t2 = bizApp.GetParam(Parametres.ParaKey.TIME_OF_TURN);
                int tt2 = Integer.parseInt(t2) / 60;
                edt5.setText(String.valueOf(tt1));
                edt6.setText(String.valueOf(tt2));
                edt7.setText(orderlimit);
                edt17.setText(turn);
                listPrinter = bizPrinter.Select();
                this.listScript = bizPrinter.SelectScript();
            } catch (Exception e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Parametre and download printer Exception")
                        .show();
                return;
            }

            LinearLayout listview = (LinearLayout) findViewById(R.id.sys_config_printerview);
            listview.removeAllViews();
            for (Printer printer : listPrinter) {
                TextView tx = new TextView(this);
                tx.setBackgroundColor(Color.BLACK);
                tx.setHeight(80);
                tx.setTextSize(20);
                tx.setText(printer.Name);
                map.put(printer, tx);
                listview.addView(tx);
                tx.setOnClickListener(new OnClickListener() {
                    boolean selected = false;

                    @Override
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        if (!selected) {
                            selected = true;
                            arg0.setBackgroundColor(Color.YELLOW);
                            for (Printer pr : listPrinter) {
                                if (map.get(pr).equals(arg0)) {
                                    testList.add(pr);
                                    break;
                                }
                            }
                        } else {
                            selected = false;
                            arg0.setBackgroundColor(Color.BLACK);
                            for (Printer pr : listPrinter) {
                                if (map.get(pr).equals(arg0)) {
                                    testList.remove(pr);
                                    break;
                                }
                            }
                        }
                    }
                });
                tx.setLongClickable(true);
                tx.setOnLongClickListener(new LongPressedPrinterListener(
                        printer));
            }

        }
    }

    /**
     * 修改套餐
     *
     * @param view
     */
    public void onClickModifyFormuleButton(View view) {
        List<Formule> listFormule;
        List<String> listFormuleAva;

        try {
            listFormule = bizMenu.SelectFormule();
            listFormuleAva = bizMenu.getAvailableFormules();

        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Download Formule Exception").show();
            return;
        }

        if (listFormule == null || listFormuleAva == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Download Formule Exception").show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ScrollView sc = new ScrollView(SysmConf.this);
        LinearLayout layout = new LinearLayout(SysmConf.this);
        sc.addView(layout);
        layout.setOrientation(LinearLayout.VERTICAL);
        mapFormule.clear();
        for (Formule formule : listFormule) {
            CheckBox checkbox = new CheckBox(this);
            checkbox.setText(formule.Name + "        " + formule.Price);
            if (listFormuleAva.contains(formule.Id)) {
                checkbox.setChecked(true);
            } else {
                checkbox.setChecked(false);
            }
            layout.addView(checkbox);
            mapFormule.put(checkbox, formule);
        }

        builder.setView(sc);

        builder.setPositiveButton(res.getText(R.string.OK),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        List<Formule> listf = bizMenu.SelectAvailableFormule();
                        listf.clear();
                        for (View view : mapFormule.keySet()) {
                            CheckBox ch = (CheckBox) view;
                            if (!ch.isChecked()) {
                                bizMenu.removeFormule(mapFormule.get(view));
                            } else {
                                bizMenu.setFormule(mapFormule.get(view));
                            }
                        }
                        listf = bizMenu.SelectAvailableFormule();
                        bizMenu.SaveAvailableFormules(listf);
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

    /**
     * 长按打印机设置表单
     *
     * @author Zhu Zijian
     */
    class LongPressedPrinterListener implements OnLongClickListener {
        Printer printer;
        Map<View, Script> mapScripts;
        CheckBox defaultPrinter;

        LongPressedPrinterListener(Printer printer) {
            this.printer = printer;
        }

        @Override
        public boolean onLongClick(View arg0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    SysmConf.this);
            LinearLayout layout = new LinearLayout(SysmConf.this);
            layout.setOrientation(LinearLayout.VERTICAL);
            mapScripts = new HashMap<View, Script>();

            defaultPrinter = new CheckBox(SysmConf.this);
            String defa = new String();
            try {
                defa = bizApp.GetParam(Parametres.ParaKey.PRINTER_DF_ID);
            } catch (Exception e) {
                AlertDialog.Builder builder2 = new AlertDialog.Builder(
                        SysmConf.this);
                builder2.setMessage("get default printer Exception").show();
                return false;
            }

            if (defa.contentEquals(printer.Id)) {
                defaultPrinter.setChecked(true);
            } else {
                defaultPrinter.setChecked(false);
            }
            defaultPrinter.setText("Default Printer");
            layout.addView(defaultPrinter);

            if (listScript == null) {
                listScript = new ArrayList<Script>();
            }
            for (Script script : listScript) {
                CheckBox box = new CheckBox(SysmConf.this);
                box.setText(script.Description);
                if (printer.Scripts.contains(script)) {
                    box.setChecked(true);
                } else {
                    box.setChecked(false);
                }
                mapScripts.put(box, script);
                layout.addView(box);
            }

            builder.setView(layout);

            builder.setPositiveButton(res.getText(R.string.OK),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            printer.Scripts.clear();
                            for (View v : mapScripts.keySet()) {
                                CheckBox ch = (CheckBox) v;
                                if (ch.isChecked()) {
                                    printer.Scripts.add(mapScripts.get(v));
                                } else {
                                    printer.Scripts.remove(mapScripts.get(v));
                                }
                            }
                            if (defaultPrinter.isChecked()) {
                                bizApp.SetParam(
                                        Parametres.ParaKey.PRINTER_DF_ID,
                                        printer.Id);
                            }
                            bizPrinter.InsertPrinter(printer);
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

            return true;
        }
    }

    /**
     * 单机打印机选中
     *
     * @param view
     */
    public void onClickPrinterButton(View view) {
        for (Printer pr : testList) {
            if (!bizPrinter.Test(pr)) {
                AlertDialog.Builder builder2 = new AlertDialog.Builder(
                        SysmConf.this);
                builder2.setMessage(pr.Name + " erreur ,Code:10001").show();
            }
        }
    }

    public void onClickModifyPasswordButton(View view) {
        final View layout = LayoutInflater.from(SysmConf.this).inflate(
                R.layout.modify_password, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(res.getText(R.string.modify_password)).setView(layout);
        builder.setPositiveButton(res.getText(R.string.OK),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // TODO Auto-generated method stub
                        String s1 = ((EditText) layout
                                .findViewById(R.id.modify_password_editText1))
                                .getText().toString();
                        String s2 = ((EditText) layout
                                .findViewById(R.id.modify_password_editText2))
                                .getText().toString();
                        String s3 = ((EditText) layout
                                .findViewById(R.id.modify_password_editText3))
                                .getText().toString();
                        boolean isgoodpass = false;
                        try {
                            isgoodpass = bizApp.AdminLogin(s1);
                            Integer.parseInt(s1);
                            Integer.parseInt(s2);
                            Integer.parseInt(s3);
                        } catch (Exception e) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(
                                    SysmConf.this);
                            builder.setTitle(
                                    res.getText(R.string.connect_exception))
                                    .show();
                            return;
                        }
                        if (!isgoodpass) {
                            new AlertDialog.Builder(SysmConf.this).setMessage(
                                    res.getText(R.string.wrong_password))
                                    .show();
                            ((EditText) layout
                                    .findViewById(R.id.modify_password_editText1))
                                    .setText("");
                            ((EditText) layout
                                    .findViewById(R.id.modify_password_editText2))
                                    .setText("");
                            ((EditText) layout
                                    .findViewById(R.id.modify_password_editText3))
                                    .setText("");
                        } else if (s2.length() != 6) {
                            new AlertDialog.Builder(SysmConf.this).setMessage(
                                    res.getText(R.string.wrong_length)).show();
                            ((EditText) layout
                                    .findViewById(R.id.modify_password_editText1))
                                    .setText("");
                            ((EditText) layout
                                    .findViewById(R.id.modify_password_editText2))
                                    .setText("");
                            ((EditText) layout
                                    .findViewById(R.id.modify_password_editText3))
                                    .setText("");
                        } else if (!s3.contentEquals(s2)) {
                            new AlertDialog.Builder(SysmConf.this).setMessage(
                                    res.getText(R.string.match_error)).show();
                            ((EditText) layout
                                    .findViewById(R.id.modify_password_editText1))
                                    .setText("");
                            ((EditText) layout
                                    .findViewById(R.id.modify_password_editText2))
                                    .setText("");
                            ((EditText) layout
                                    .findViewById(R.id.modify_password_editText3))
                                    .setText("");
                        } else {
                            bizApp.SetAdminPassword(s3);
                            bizApp.UpdateParams(Parametres.ParaKey.PASSWORD);
                            new AlertDialog.Builder(SysmConf.this).setMessage(
                                    "Success").show();
                        }
                    }
                });

        builder.setNegativeButton(res.getText(R.string.cancel), null);
        builder.show();
    }

    public void onClickCreatButton(View view) {

    }

    /**
     * 查看设备ID
     *
     * @param view
     */
    public void onClickRegisterButton(View view) {
        AlertDialog.Builder builder2 = new AlertDialog.Builder(SysmConf.this);
        builder2.setMessage(bizApp.getDeviceId()).show();
    }

    public void onClickModifyLogoButton(View view) {
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
                byte[] image;
                Uri uri = data.getData();
                Bitmap bmp = BitmapFactory.decodeFile(uri.getPath());

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                image = stream.toByteArray();
                try {
                    if (!bizApp.setLogo(image)) {
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(
                                SysmConf.this);
                        builder2.setMessage("Fail").show();
                    } else {
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(
                                SysmConf.this);
                        builder2.setMessage("Success").show();
                    }
                } catch (Exception e) {
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(
                            SysmConf.this);
                    builder2.setMessage("Error").show();
                }
            }
        }
    }

    /**
     * 修改楼层显示方式
     *
     * @param view
     */
    public void onClickModifyFloorView(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        RadioGroup radioGroup = new RadioGroup(this);
        final RadioButton btn1 = new RadioButton(this);
        final RadioButton btn2 = new RadioButton(this);
        btn1.setText(R.string.map_mode);
        btn2.setText(R.string.list_mode);
        builder.setTitle(R.string.modify_floorview);
        radioGroup.addView(btn1);
        radioGroup.addView(btn2);
        String floorView = bizApp.GetParam(Parametres.ParaKey.MAPMODE);
        try {
            if (floorView.contentEquals("map")) {
                btn1.setChecked(true);
                btn2.setChecked(false);
            } else {
                btn1.setChecked(false);
                btn2.setChecked(true);
            }
        } catch (Exception e) {
            btn1.setChecked(false);
            btn2.setChecked(false);
        }
        builder.setView(radioGroup);
        builder.setPositiveButton(R.string.OK,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        if (btn1.isChecked()) {
                            bizApp.SetParam(Parametres.ParaKey.MAPMODE, "map");
                            bizApp.UpdateParams(Parametres.ParaKey.MAPMODE);
                        } else if (btn2.isChecked()) {
                            bizApp.SetParam(Parametres.ParaKey.MAPMODE, "list");
                            bizApp.UpdateParams(Parametres.ParaKey.MAPMODE);
                        }
                    }
                });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    @SuppressLint("NewApi")
    public void onClickChangeBackground(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(res.getString(R.string.change_background));

        SharedPreferences sharedPreferences = this.getSharedPreferences("com",
                Context.MODE_PRIVATE);
        String pic = sharedPreferences.getString("pic", "map1");

        LinearLayout ln = new LinearLayout(this);
        ln.setOrientation(LinearLayout.VERTICAL);
        LinearLayout ln1 = new LinearLayout(this);
        LinearLayout ln2 = new LinearLayout(this);
        ln1.setOrientation(LinearLayout.HORIZONTAL);
        ln2.setOrientation(LinearLayout.HORIZONTAL);
        ln.addView(ln1);
        ln.addView(ln2);
        ImageButton img1 = new ImageButton(this);
        ImageButton img2 = new ImageButton(this);
        ImageButton img3 = new ImageButton(this);
        ImageButton img4 = new ImageButton(this);
        img1.setBackground(res.getDrawable(R.drawable.map1));
        img2.setBackground(res.getDrawable(R.drawable.map2));
        img3.setBackground(res.getDrawable(R.drawable.map3));
        img4.setBackground(res.getDrawable(R.drawable.map4));
        img1.setTag("map1");
        img2.setTag("map2");
        img3.setTag("map3");
        img4.setTag("map4");
        img1.setOnClickListener(new OnClickImg());
        img2.setOnClickListener(new OnClickImg());
        img3.setOnClickListener(new OnClickImg());
        img4.setOnClickListener(new OnClickImg());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(200,
                200);
        params.setMargins(10, 10, 10, 10);
        ln1.addView(img1, params);
        ln1.addView(img2, params);
        ln2.addView(img3, params);
        ln2.addView(img4, params);
        if (pic.contains("map1")) {
            img1.setSelected(true);
        } else if (pic.contains("map2")) {
            img2.setSelected(true);
        } else if (pic.contains("map3")) {
            img3.setSelected(true);
        } else if (pic.contains("map4")) {
            img4.setSelected(true);
        }
        builder.setView(ln);
        dlg1 = builder.show();

    }

    class OnClickImg implements OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            SharedPreferences sharedPreferences = SysmConf.this
                    .getSharedPreferences("com", Context.MODE_PRIVATE);
            sharedPreferences.edit().putString("map", (String) v.getTag())
                    .commit();
            dlg1.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    SysmConf.this);
            builder.setTitle(res.getString(R.string.success)).show();
        }

    }

}
