package com.mjniuz.bido.bidoblinddescriptor;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;

public class RedirectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_redirect);


        Bundle b = getIntent().getExtras();
        int value = 1000; // or other values
        if(b != null)
            value = b.getInt("wait");

        playNotify("beep-error.wav");

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        Intent redirect = new Intent(RedirectActivity.this, PlayActivity.class);

                        Bundle b = new Bundle();
                        b.putInt("redirected", 0); //Your id
                        redirect.putExtras(b); //Put your id to your next Intent
                        RedirectActivity.this.startActivity(redirect);
                        finish();
                    }
                },
                value);
    }


    public void playNotify(String soundName){
        AssetFileDescriptor afd = null;
        try {
            afd = getAssets().openFd(soundName);
            final MediaPlayer player = new MediaPlayer();
            player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
            player.prepare();
            player.start();

            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            player.stop();
                        }
                    },
                    2000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
