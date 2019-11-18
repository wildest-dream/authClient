package com.example.peteryu.authclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

public class authRes extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_res);
        EditText authMsg=(EditText)findViewById(R.id.editText3);
        Intent receiveIntent=getIntent();
        String res=receiveIntent.getStringExtra("result");
        authMsg.setText(res);//display authentication result
    }
}
