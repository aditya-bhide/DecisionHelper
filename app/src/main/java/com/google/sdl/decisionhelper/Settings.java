package com.google.sdl.decisionhelper;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;



/**
 * Created by aditya on 25/9/17.
 */

public class Settings extends AppCompatActivity implements View.OnClickListener{

    private Toolbar toolbar;
    private RelativeLayout linkToProfile;
    private ImageView ProfilePic;
    private String uid;


    //firebase
    private DatabaseReference mDatabaseReference;
    private UserObj mUser;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        //defining basic variables
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ProfilePic=(ImageView)findViewById(R.id.setting_profilepic);
        linkToProfile=(RelativeLayout) findViewById(R.id.linkToProfile);

        linkToProfile.setOnClickListener(this);

        FirebaseUser user2 = FirebaseAuth.getInstance().getCurrentUser();
        if (user2 != null) {
            uid = user2.getUid();
        }

        mUser=new UserObj();
        mDatabaseReference= FirebaseDatabase.getInstance().getReference().child("users");
        Query pendingTasks = mDatabaseReference.orderByChild("uid").equalTo(uid);
        pendingTasks.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot tasksSnapshot) {
                for (DataSnapshot snapshot: tasksSnapshot.getChildren()) {
                    mUser=snapshot.getValue(UserObj.class);
                    if(mUser.getProfilepickUrl()!=null)
                    {
                        Glide.with(ProfilePic.getContext()).load(mUser.getProfilepickUrl()).into(ProfilePic);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("databseerr","The read failed: "+ databaseError.getMessage());
            }
        });


    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main4, menu);
        return true;
    }
    @Override
    public void onBackPressed() {
       /* Intent intent_group = new Intent(Settings.this, MainActivity.class);
       startActivity(intent_group);*/
       finish();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.linkToProfile:
                startActivity(new Intent(this,UserProfile.class));
                break;
        }
    }
}
