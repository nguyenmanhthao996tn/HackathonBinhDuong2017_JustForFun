package com.example.admin.cerberus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ShowParkingMap extends AppCompatActivity {
    JSONArray array;

    ImageView[] btn = new ImageView[25];
    boolean[] btnStatus = {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_parking_map);

        Intent intent = getIntent();
        String data = intent.getStringExtra("data");
        try {
            array = new JSONArray(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        configButton();

        // Get the Intent that started this activity and extract the string

        // Capture the layout's TextView and set the string as its text
        TextView textView = (TextView) findViewById(R.id.textView2);
        textView.setText("Parking Map");

        //analyze data
        String[] parkData = stringOfJsonToStringArray(data);
        if(parkData != null) {
            //updateParkStatus(parkData);
        }
    }

    public void afterward() {
        finish();
    }

    public String[] stringOfJsonToStringArray(String message) {
        String[] ret = null;

        try {
            JSONArray jsonarray = new JSONArray(message);
            ret = new String[jsonarray.length()];

            for (int i = 0; i < jsonarray.length(); i++) {
                JSONObject jsonobject = jsonarray.getJSONObject(i);
                ret[i] = jsonobject.getString("status");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    public void updateParkStatus(String[] parkStatus) {
        for (int i = 0; i < parkStatus.length; i++) {
            switch (parkStatus[i]) {
                case "empty":
                    btn[i].setImageResource(R.drawable.g);
                    btnStatus[i] = false;

                    break;
                case "booked":
//                    btn[i].setImageResource(R.drawable.y);
                    btn[i].setImageResource(R.drawable.o);
                    btn[i].setClickable(false);

                    break;
                case "pending":
                    btn[i].setImageResource(R.drawable.b);
                    btnStatus[i] = true;
                    btn[i].setClickable(false);

                    break;
                case "parked":
                    btn[i].setImageResource(R.drawable.o);
                    btn[i].setClickable(false);

                    break;
            }
        }
    }

    public void configButton() {
        btn[0] = (ImageView) findViewById(R.id.btn0);
        btn[1] = (ImageView) findViewById(R.id.btn1);
        btn[2] = (ImageView) findViewById(R.id.btn2);
        btn[3] = (ImageView) findViewById(R.id.btn3);
        btn[4] = (ImageView) findViewById(R.id.btn4);
        btn[5] = (ImageView) findViewById(R.id.btn5);
        btn[6] = (ImageView) findViewById(R.id.btn6);
        btn[7] = (ImageView) findViewById(R.id.btn7);
        btn[8] = (ImageView) findViewById(R.id.btn8);
        btn[9] = (ImageView) findViewById(R.id.btn9);
        btn[10] = (ImageView) findViewById(R.id.btn10);
        btn[11] = (ImageView) findViewById(R.id.btn11);
        btn[12] = (ImageView) findViewById(R.id.btn12);
        btn[13] = (ImageView) findViewById(R.id.btn13);
        btn[14] = (ImageView) findViewById(R.id.btn14);
        btn[15] = (ImageView) findViewById(R.id.btn15);
        btn[16] = (ImageView) findViewById(R.id.btn16);
        btn[17] = (ImageView) findViewById(R.id.btn17);
        btn[18] = (ImageView) findViewById(R.id.btn18);
        btn[19] = (ImageView) findViewById(R.id.btn19);
        btn[20] = (ImageView) findViewById(R.id.btn20);
        btn[21] = (ImageView) findViewById(R.id.btn21);
        btn[22] = (ImageView) findViewById(R.id.btn22);
        btn[23] = (ImageView) findViewById(R.id.btn23);
        btn[24] = (ImageView) findViewById(R.id.btn24);
    }

    public void disableAllButton() {
        for (int i = 0; i < btn.length; i++) {
            btn[i].setClickable(false);
        }
    }

    public void btn0(View view) {
        if (!btnStatus[0])
            btn[0].setImageResource(R.drawable.b);
        else
            btn[0].setImageResource(R.drawable.g);
        btnStatus[0] = !btnStatus[0];

        disableAllButton();
    }

    public void btn1(View view) {
        if (!btnStatus[1])
            btn[1].setImageResource(R.drawable.b);
        else
            btn[1].setImageResource(R.drawable.g);
        btnStatus[1] = !btnStatus[1];

        disableAllButton();
    }

    public void btn2(View view) {
        if (!btnStatus[2])
            btn[2].setImageResource(R.drawable.b);
        else
            btn[2].setImageResource(R.drawable.g);
        btnStatus[2] = !btnStatus[2];

        disableAllButton();
    }

    public void btn3(View view) {
        if (!btnStatus[3])
            btn[3].setImageResource(R.drawable.b);
        else
            btn[3].setImageResource(R.drawable.g);
        btnStatus[3] = !btnStatus[3];

        disableAllButton();
    }

    public void btn4(View view) {
        if (!btnStatus[4])
            btn[4].setImageResource(R.drawable.b);
        else
            btn[4].setImageResource(R.drawable.g);
        btnStatus[4] = !btnStatus[4];

        disableAllButton();
    }

    public void btn5(View view) {
        if (!btnStatus[5])
            btn[5].setImageResource(R.drawable.b);
        else
            btn[5].setImageResource(R.drawable.g);
        btnStatus[5] = !btnStatus[5];

        disableAllButton();
    }

    public void btn6(View view) {
        if (!btnStatus[6])
            btn[6].setImageResource(R.drawable.b);
        else
            btn[6].setImageResource(R.drawable.g);
        btnStatus[6] = !btnStatus[6];

        disableAllButton();
    }

    public void btn7(View view) {
        if (!btnStatus[7])
            btn[7].setImageResource(R.drawable.b);
        else
            btn[7].setImageResource(R.drawable.g);
        btnStatus[7] = !btnStatus[7];

        disableAllButton();
    }

    public void btn8(View view) {
        if (!btnStatus[8])
            btn[8].setImageResource(R.drawable.b);
        else
            btn[8].setImageResource(R.drawable.g);
        btnStatus[8] = !btnStatus[8];

        disableAllButton();
    }

    public void btn9(View view) {
        if (!btnStatus[9])
            btn[9].setImageResource(R.drawable.b);
        else
            btn[9].setImageResource(R.drawable.g);
        btnStatus[9] = !btnStatus[9];

        disableAllButton();
    }

    public void btn10(View view) {
        if (!btnStatus[10])
            btn[10].setImageResource(R.drawable.b);
        else
            btn[10].setImageResource(R.drawable.g);
        btnStatus[10] = !btnStatus[10];

        disableAllButton();
    }

    public void btn11(View view) {
        if (!btnStatus[11])
            btn[11].setImageResource(R.drawable.b);
        else
            btn[11].setImageResource(R.drawable.g);
        btnStatus[11] = !btnStatus[11];

        disableAllButton();
    }

    public void btn12(View view) {
        if (!btnStatus[12])
            btn[12].setImageResource(R.drawable.b);
        else
            btn[12].setImageResource(R.drawable.g);
        btnStatus[12] = !btnStatus[12];

        disableAllButton();
    }

    public void btn13(View view) {
        if (!btnStatus[13])
            btn[13].setImageResource(R.drawable.b);
        else
            btn[13].setImageResource(R.drawable.g);
        btnStatus[13] = !btnStatus[13];

        disableAllButton();
    }

    public void btn14(View view) {
        if (!btnStatus[14])
            btn[14].setImageResource(R.drawable.b);
        else
            btn[14].setImageResource(R.drawable.g);
        btnStatus[14] = !btnStatus[14];

        disableAllButton();
    }

    public void btn15(View view) {
        if (!btnStatus[15])
            btn[15].setImageResource(R.drawable.b);
        else
            btn[15].setImageResource(R.drawable.g);
        btnStatus[15] = !btnStatus[15];

        disableAllButton();
    }

    public void btn16(View view) {
        if (!btnStatus[16])
            btn[16].setImageResource(R.drawable.b);
        else
            btn[16].setImageResource(R.drawable.g);
        btnStatus[16] = !btnStatus[16];

        disableAllButton();
    }

    public void btn17(View view) {
        if (!btnStatus[17])
            btn[17].setImageResource(R.drawable.b);
        else
            btn[17].setImageResource(R.drawable.g);
        btnStatus[17] = !btnStatus[17];

        disableAllButton();
    }

    public void btn18(View view) {
        if (!btnStatus[18])
            btn[18].setImageResource(R.drawable.b);
        else
            btn[18].setImageResource(R.drawable.g);
        btnStatus[18] = !btnStatus[18];

        disableAllButton();
    }

    public void btn19(View view) {
        if (!btnStatus[19])
            btn[19].setImageResource(R.drawable.b);
        else
            btn[19].setImageResource(R.drawable.g);
        btnStatus[19] = !btnStatus[19];

        disableAllButton();
    }

    public void btn20(View view) {
        if (!btnStatus[20])
            btn[20].setImageResource(R.drawable.b);
        else
            btn[20].setImageResource(R.drawable.g);
        btnStatus[20] = !btnStatus[20];

        disableAllButton();
    }

    public void btn21(View view) {
        if (!btnStatus[21])
            btn[21].setImageResource(R.drawable.b);
        else
            btn[21].setImageResource(R.drawable.g);
        btnStatus[21] = !btnStatus[21];

        disableAllButton();
    }

    public void btn22(View view) {
        if (!btnStatus[22])
            btn[22].setImageResource(R.drawable.b);
        else
            btn[22].setImageResource(R.drawable.g);
        btnStatus[22] = !btnStatus[22];

        disableAllButton();
    }

    public void btn23(View view) {
        if (!btnStatus[23])
            btn[23].setImageResource(R.drawable.b);
        else
            btn[23].setImageResource(R.drawable.g);
        btnStatus[23] = !btnStatus[23];

        disableAllButton();
    }

    public void btn24(View view) {
        if (!btnStatus[24])
            btn[24].setImageResource(R.drawable.b);
        else
            btn[24].setImageResource(R.drawable.g);
        btnStatus[24] = !btnStatus[24];

        disableAllButton();
    }
}
