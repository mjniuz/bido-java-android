package com.mjniuz.dipo.dipoblinddescriptor;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

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

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        Intent redirect = new Intent(RedirectActivity.this, MainActivity.class);

                        Bundle b = new Bundle();
                        b.putInt("redirected", 270); //Your id
                        redirect.putExtras(b); //Put your id to your next Intent
                        startActivity(redirect);
                        finish();
                    }
                },
                value);
    }

}
