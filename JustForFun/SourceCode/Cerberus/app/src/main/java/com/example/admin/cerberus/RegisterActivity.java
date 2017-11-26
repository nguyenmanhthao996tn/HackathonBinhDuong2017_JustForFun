package com.example.admin.cerberus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {
    public static final String USERNAME_EXTRA = "USERNAME_EXTRA";

    SharedPreferences sharedPref;

    Button bntRegister;
    EditText etName, etAge, etUsername, etPassword, etConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registers);

        sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        etName = (EditText) findViewById(R.id.etName);
        etAge = (EditText) findViewById(R.id.etAge);
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etConfirmPassword = (EditText) findViewById(R.id.etConfirmPassword);

        bntRegister = (Button) findViewById(R.id.bntRegister);

        bntRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username, password, confirmPassword, name;
                int age = 0;

                username = etUsername.getText().toString();
                name = etName.getText().toString();
                if (!(etAge.getText().toString().isEmpty())) {
                    age = Integer.parseInt(etAge.getText().toString());
                }

                password = etPassword.getText().toString();
                confirmPassword = etConfirmPassword.getText().toString();


                if (createNewUser(username, password, confirmPassword, name, age)) {
                    Intent registerIntent = new Intent(RegisterActivity.this, MapsActivity.class);
                    registerIntent.putExtra(USERNAME_EXTRA, username);
                    RegisterActivity.this.startActivity(registerIntent);
                }
            }
        });
    }


    private boolean createNewUser(String Username, String Password, String ConfirmPassword, String Name, int Age) {
        if(Username.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Enter your Username", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (Name.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Enter your Name", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (Age <= 0) {
            Toast.makeText(getApplicationContext(), "Enter your Age", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (Password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Enter your Password", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (ConfirmPassword.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Enter your Confirm password", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (!(Password.equals(ConfirmPassword)))    {
            Toast.makeText(getApplicationContext(), "Passwords must match ", Toast.LENGTH_SHORT).show();
            return false;
        }

        String app_account_data = sharedPref.getString(getString(R.string.saved_data), "{}");
        try {
            JSONObject savedDataJSONObject = new JSONObject(app_account_data);
            JSONArray accountArray = savedDataJSONObject.getJSONArray("Accounts");

            boolean accountExistedFlag = false;
            for (int i = 0; i < accountArray.length(); i++) {
                JSONObject obj = accountArray.getJSONObject(i);
                String username = obj.getString("username");
                if (Username.equals(username)) {
                    String password = obj.getString("password");
                    if (Password.equals(password)) {
                        accountExistedFlag = true;
                        break;
                    }
                }
            }

            if (accountExistedFlag) {
                return false;
            } else {
                JSONObject newAccount = new JSONObject();
                newAccount.put("username", Username);
                newAccount.put("password", Password);
                newAccount.put("name", Name);
                newAccount.put("age", Age);
                accountArray.put(newAccount);

                savedDataJSONObject.remove("Accounts");
                savedDataJSONObject.put("Accounts", accountArray);

                savedDataJSONObject.remove("LastLogin");
                savedDataJSONObject.put("LastLogin", Username);

                String save_data = savedDataJSONObject.toString();
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.clear();
                editor.putString(getString(R.string.saved_data), save_data);
                editor.commit();

                return true;
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
}
