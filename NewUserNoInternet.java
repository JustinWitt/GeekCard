package com.boffinapes.geekcard;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class NewUserNoInternet extends Activity{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newusernointernet);
        Button newbut = (Button) findViewById(R.id.newnonetbutton);
        newbut.isClickable();
        newbut.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                finish();
            }
        });
    }
}
