package com.example.admin.cerberus;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 1;
    Button bntLogin, bntRegister;
    EditText etUsername, etPassword;
    CheckBox chkRemember;

    SharedPreferences sharedPref;

    public class Account {
        public String user, pass;
    }

    List<Account> acountList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent startBluethoothServiceIntent = new Intent(this, WSManagerService.class);
        startService(startBluethoothServiceIntent);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        chkRemember = (CheckBox) findViewById(R.id.chkRemember);
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        bntLogin = (Button) findViewById(R.id.bntLogin);
        bntRegister = (Button) findViewById(R.id.bntRegister);

        bntRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                LoginActivity.this.startActivity(registerIntent);
            }
        });

        checkPermissions();
    }

    @Override
    protected void onStart() {
        super.onStart();

        updateLastLoginAndRememberAccount();
    }

    public void bntLogin(View view) {
        String username, password;
        username = etUsername.getText().toString();
        password = etPassword.getText().toString();

        if (checkLogin(username, password)) {
            Intent loginIntent = new Intent(LoginActivity.this, MapsActivity.class);
            loginIntent.putExtra(RegisterActivity.USERNAME_EXTRA, username);
            LoginActivity.this.startActivity(loginIntent);
        } else {
            Toast.makeText(getApplicationContext(), "Invalid Username or Password", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkLogin(String Username, String Password) {
        String app_account_data = sharedPref.getString(getString(R.string.saved_data), "{}");
        try {
            JSONObject savedDataJSONObject = new JSONObject(app_account_data);
            JSONArray accountArray = savedDataJSONObject.getJSONArray("Accounts");
            for (int i = 0; i < accountArray.length(); i++) {
                JSONObject obj = accountArray.getJSONObject(i);
                String username = obj.getString("username");
                if (Username.equals(username)) {
                    String password = obj.getString("password");
                    if (Password.equals(password)) {
                        // Update last login and remember state
                        JSONObject newObject = new JSONObject();
                        newObject.put("LastLogin", Username);
                        newObject.put("Remember", chkRemember.isChecked());
                        newObject.put("Accounts", accountArray);

                        String save_data = newObject.toString();

                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.clear();
                        editor.putString(getString(R.string.saved_data), save_data);
                        editor.commit();

                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } catch (JSONException e) {
            // Reset all data
            JSONObject obj = new JSONObject();
            try {
                obj.put("LastLogin", "");
                obj.put("Remember", false);
                JSONArray accountArray = new JSONArray(); // Empty
                obj.put("Accounts", accountArray);

                String save_data = obj.toString();

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.clear();
                editor.putString(getString(R.string.saved_data), save_data);
                editor.commit();
            } catch (JSONException e1) {
                e1.printStackTrace();
            }

            e.printStackTrace();
        }

        return false;
    }

    private void checkPermissions() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_WIFI_STATE
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, REQUEST_PERMISSIONS);
        }
    }

    private void updateLastLoginAndRememberAccount() {
        String app_account_data = sharedPref.getString(getString(R.string.saved_data), "{}");
        try {
            JSONObject savedDataJSONObject = new JSONObject(app_account_data);
            JSONArray accountArray = savedDataJSONObject.getJSONArray("Accounts");

            String Username = savedDataJSONObject.getString("LastLogin");
            etUsername.setText(Username);
            boolean remember = savedDataJSONObject.getBoolean("Remember");
            chkRemember.setChecked(remember);
            if (remember) {
                for (int i = 0; i < accountArray.length(); i++) {
                    JSONObject obj = accountArray.getJSONObject(i);
                    String username = obj.getString("username");
                    if (Username.equals(username)) {
                        String password = obj.getString("password");
                        etPassword.setText(password);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
