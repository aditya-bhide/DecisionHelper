package com.google.sdl.decisionhelper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by aditya on 13/10/17.
 */

public class UserDisplayProfile extends AppCompatActivity {

    ImageView imageview;
    TextView username;
    TextView authtype;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        imageview=(ImageView)findViewById(R.id.userprofile_display_imageView);
        username=(TextView)findViewById(R.id.userprofile_display_username);
        authtype=(TextView)findViewById(R.id.userprofile_display_authtype);

        final Bundle bundle = getIntent().getExtras();



    }
}
