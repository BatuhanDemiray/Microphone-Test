package com.example.microphonetest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button btnRecord, btnRecordStop, btnStart, btnStop;
    ProgressBar progressBar;
    ImageView btnInfo;
    String pathSave = "";
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    CountDownTimer countDownTimer;
    Context context = this;

    final int PERMISSION_CODE = 1000;
    final int TIME_LIMIT = 15000;
    final int COUNTDOWN_INTERVAL = 1000;
    private int progressStatus =  0;
    long duration;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen
        setContentView(R.layout.record);


        //Request Runtime Permission
        if (!checkPermissionFromDevice())
            requestPermissions();


        btnRecord = findViewById(R.id.btnRecord);
        btnRecordStop = findViewById(R.id.btnRecordStop);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnInfo = findViewById(R.id.btnInfo);
        progressBar = findViewById(R.id.progressBar);

        //Opening Screen - Button Status
        btnRecord.setEnabled(true);
        btnRecordStop.setEnabled(false);
        btnStart.setEnabled(false);
        btnStop.setEnabled(false);

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnRecord.setEnabled(false);
                btnRecordStop.setEnabled(true);
                btnStart.setEnabled(false);
                btnStop.setEnabled(false);

                if (checkPermissionFromDevice()){
                    pathSave = Environment.getExternalStorageDirectory()
                            .getAbsolutePath()+"/"
                            + UUID.randomUUID().toString()+"_audio_record.3gp";
                    setupMediaRecorder();
                    try{
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                        Toast.makeText(context, "Recording...", Toast.LENGTH_SHORT).show();
                        ///////////////////////////////////////////////////////
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.setProgress(progressStatus);
                        countDownTimer = new CountDownTimer(TIME_LIMIT, COUNTDOWN_INTERVAL) {
                            @Override
                            public void onTick(long l) {
                                progressStatus++;
                                progressBar.setProgress(progressStatus * 100 / (15000 / 1000));
                                //////////////////////////////////////////
                                btnRecordStop.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        progressBar.setVisibility(View.GONE);
                                        progressBar.setProgress(0);
                                        progressStatus = 0;
                                        mediaRecorder.stop();
                                        btnRecordStop.setEnabled(false);
                                        btnRecord.setEnabled(true);
                                        btnStart.setEnabled(true);
                                        btnStop.setEnabled(false);
                                        countDownTimer.cancel();
                                        Toast.makeText(context, "Record Stopped...", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                /////////////////////////////////////////
                            }

                            @Override
                            public void onFinish() {
                                mediaRecorder.stop();
                                btnRecord.setEnabled(true);
                                btnRecordStop.setEnabled(false);
                                btnStart.setEnabled(true);
                                btnStop.setEnabled(false);
                                progressBar.setVisibility(View.GONE);
                                progressStatus = 0;
                                Toast.makeText(context, "Record Stopped...", Toast.LENGTH_SHORT).show();
                            }
                        }.start();
                        ///////////////////////////////////////////////////////
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                }
                else {
                    requestPermissions();
                }
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
                btnRecord.setEnabled(false);
                btnRecordStop.setEnabled(false);

                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(pathSave);
                    mediaPlayer.prepare();
                }
                catch (IOException e){
                    e.printStackTrace();
                }
                duration = mediaPlayer.getDuration();
                mediaPlayer.start();
                countDownTimer = new CountDownTimer(duration, COUNTDOWN_INTERVAL) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        //mediaPlayer.start();
                        Toast.makeText(context, "Playing...", Toast.LENGTH_SHORT).show();
                        duration = duration - COUNTDOWN_INTERVAL;
                        if (duration <= 0){
                            btnStart.setEnabled(true);
                            btnStop.setEnabled(false);
                            btnRecord.setEnabled(true);
                            btnRecordStop.setEnabled(false);
                            stopPlaying();
                            countDownTimer.cancel();
                        }

                        btnStop.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                btnStart.setEnabled(true);
                                btnStop.setEnabled(false);
                                btnRecord.setEnabled(true);
                                btnRecordStop.setEnabled(false);
                                setupMediaRecorder();
                                stopPlaying();
                                countDownTimer.cancel();
                            }
                        });
                    }

                    @Override
                    public void onFinish() {
                        btnStart.setEnabled(true);
                        btnStop.setEnabled(false);
                        btnRecord.setEnabled(true);
                        btnRecordStop.setEnabled(false);
                        stopPlaying();
                    }
                }.start();
            }
        });

        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder al = new AlertDialog.Builder(context);
                al.setPositiveButton("Tamam",null)
                        .setTitle("App Info - Uygulama Bilgileri")
                        .setMessage("Uygulamada alacağınız ses kayıtları 15 saniye ile sınırlıdır\n")
                        .setCancelable(false)
                        .show();
            }
        });
    }

    protected void stopPlaying(){
        // If media player is not null then try to stop it
        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            Toast.makeText(context,"Stopped...",Toast.LENGTH_SHORT).show();
        }
    }

    private void setupMediaRecorder(){
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(pathSave);
    }

    private void requestPermissions(){
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        },PERMISSION_CODE);
    }

    private boolean checkPermissionFromDevice(){
        int write_external_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read_external_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return write_external_storage_result == PackageManager.PERMISSION_GRANTED &&
                read_external_storage_result == PackageManager.PERMISSION_GRANTED &&
                record_audio_result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
