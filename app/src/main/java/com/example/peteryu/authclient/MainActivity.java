package com.example.peteryu.authclient;

import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private EditText ID;
    private EditText Password;
    private Button submit;
    private Socket clientSocket;
    private PrintWriter writer;
    private BufferedReader reader;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ID=(EditText)findViewById(R.id.editText);
        Password=(EditText)findViewById(R.id.editText2);
        submit=(Button)findViewById(R.id.button);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    clientSocket=new Socket("192.168.1.212",8998);//(10.241.137.163 or 192.168.1.212)
                                                                             // modify IP address whenever host IP is changed(PC connects to another network)
                    writer=new PrintWriter(clientSocket.getOutputStream(),true);
                    writer.println(clientSocket.getLocalAddress().toString()+
                            " "+Integer.toString(clientSocket.getLocalPort())+"\nPassword Authentication\nid:"
                            +ID.getText()+"\npwd:"+Password.getText()+"\n");//send id and password to server
                    reader=new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));//get password authentication result from server
                    String message=reader.readLine();
                    Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
                    toast.show();
                    writer.close();
                    reader.close();
                    clientSocket.close();
                    if(message.startsWith("Password correct")){//if password authentication is passed, switch to voice_recording Activity
                        Intent newActivity=new Intent(getApplicationContext(),voice_recording.class);
                        newActivity.putExtra("ID",ID.getText().toString());
                        startActivity(newActivity);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
}
