 package com.example.peteryu.authclient;

import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.*;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

public class voice_recording extends AppCompatActivity {
    private Button record_voice;
    private Button submit;
    private MediaRecorder myAudioRecorder;
    private Socket ClientSocket;
    private PrintWriter writer;
    private BufferedReader reader;
    private BufferedInputStream bis;
    private OutputStream os;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_recording);
        Intent receiveIntent=getIntent();
        final String id=receiveIntent.getStringExtra("ID");
        //Log.i(null,id);
        record_voice=(Button)findViewById(R.id.button2);
        submit=(Button)findViewById(R.id.button3);
        submit.setEnabled(false);
        String[] permissions={android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.RECORD_AUDIO};
        ActivityCompat.requestPermissions(voice_recording.this, permissions,0);//set permission for voice recording
        final String outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/template.3gp";
        record_voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    record_voice.setEnabled(false);
                    if(record_voice.getText().equals("RECORD AGAIN")){
                        submit.setEnabled(false);
                    }
                    myAudioRecorder = new MediaRecorder();
                    myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                    myAudioRecorder.setOutputFile(outputFile);
                    myAudioRecorder.prepare();
                    myAudioRecorder.start();
                    Timer timer=new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {//set a timer to record voice, for a fixed time of 5s
                            myAudioRecorder.stop();
                            myAudioRecorder.release();
                            voice_recording.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    record_voice.setEnabled(true);
                                    record_voice.setText("RECORD AGAIN");
                                    submit.setEnabled(true);
                                }
                            });
                        }
                    },5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    record_voice.setEnabled(false);
                    submit.setEnabled(false);
                    ClientSocket=new Socket("192.168.1.212",8998);//(10.241.137.163 or 192.168.1.212)
                    // modify IP address whenever host IP is changed(PC connects to another network)
                    os = ClientSocket.getOutputStream();
                    writer=new PrintWriter(os,true);
                    writer.println(ClientSocket.getLocalAddress().toString()+
                            " "+Integer.toString(ClientSocket.getLocalPort())+"\nVoice Authentication\nid:"+id+"\n");
                    File file=new File(outputFile);
                    byte[] byteArray = new byte[(int) file.length()];
                    bis = new BufferedInputStream(new FileInputStream(file));
                    bis.read(byteArray, 0, byteArray.length);
                    os.write(byteArray, 0, byteArray.length);//send audio file to server
                    os.flush();
                    bis.close();
                    //os.close();//ClientSocket will be closed after os closed
                    /*if(ClientSocket.isClosed()){
                        Log.i(null,"closed\n");
                    }else{
                        Log.i(null,"open\n");
                    }*/
                    //writer.close();
                    ClientSocket.shutdownOutput();
                    if(file.delete()){
                        Toast toast = Toast.makeText(getApplicationContext(), "Voice submitted. Please don't leave until the authentication result is shown.", Toast.LENGTH_LONG);
                        toast.show();
                    }
                    reader=new BufferedReader(new InputStreamReader(ClientSocket.getInputStream()));
                    String authRes=reader.readLine();//receive voice authentication result
                    reader.close();
                    ClientSocket.close();
                    //Log.i(null,authRes);
                    Intent newActivity=new Intent(getApplicationContext(), com.example.peteryu.authclient.authRes.class);
                    newActivity.putExtra("result",authRes);
                    startActivity(newActivity);//jump to authRes Activity
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
}
