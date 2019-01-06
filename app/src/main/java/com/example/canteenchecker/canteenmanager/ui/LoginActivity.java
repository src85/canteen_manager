package com.example.canteenchecker.canteenmanager.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.canteenchecker.canteenmanager.CanteenManagerApplication;
import com.example.canteenchecker.canteenmanager.R;
import com.example.canteenchecker.canteenmanager.proxy.ServiceProxy;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.toString();

    public static Intent createIntent(Context context) {
        return new Intent(context, LoginActivity.class);
    }

    private EditText edtUserName;
    private EditText edtPassWord;
    private Button btnLogIn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtUserName = findViewById(R.id.edtUserName);
        edtPassWord = findViewById(R.id.edtPassWord);
        btnLogIn = findViewById(R.id.btnLogIn);

        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void login() {
        setUIEnabled(false);
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... strings) {
                try {
                    return new ServiceProxy().authenticate(strings[0], strings[1]);
                } catch (IOException e) {
                    Log.e(TAG, getString(R.string.msg_LoginFailed), e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String s) {
                if (s != null) {
                    //authentication finished with success ...
                    CanteenManagerApplication.getInstance().setAuthenticationToken(s);
                    //startActivity(CanteenActivity.createIntent(LoginActivity.this));
                    startActivity(MainActivity.createIntent(LoginActivity.this));
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    // or not ...
                    edtPassWord.setText(null);
                    Toast.makeText(LoginActivity.this, R.string.msg_LoginFailed, Toast.LENGTH_LONG).show();
                    setUIEnabled(true);
                }
            }
        }.execute(edtUserName.getText().toString(), edtPassWord.getText().toString());
    }

    private void setUIEnabled(boolean enabled) {
        edtUserName.setEnabled(enabled);
        edtPassWord.setEnabled(enabled);
        btnLogIn.setEnabled(enabled);
    }
}
