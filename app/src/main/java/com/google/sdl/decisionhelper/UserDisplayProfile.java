package com.google.sdl.decisionhelper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by aditya on 13/10/17.
 */


public class UserDisplayProfile extends AppCompatActivity {

    ImageView imageview;
    TextView username;
    TextView authtype;
    UserObj mUser;

    private DatabaseReference mReference;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_display_profile);



        final Bundle bundle = getIntent().getExtras();
        Log.i("sssssss",bundle.getString("uid"));


        Query query = FirebaseDatabase.getInstance().getReference().child("users").orderByChild("uid").equalTo(bundle.getString("uid"));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null)
                {
                    for(DataSnapshot singlisnapshot:dataSnapshot.getChildren())
                        mUser=singlisnapshot.getValue(UserObj.class);
                    imageview=(ImageView)findViewById(R.id.userprofile_display_imageView);
                    username=(TextView)findViewById(R.id.userprofile_display_username);
                    authtype=(TextView)findViewById(R.id.userprofile_display_authtype);
                    if(mUser.profilepickUrl!=null)
                        Glide.with(imageview.getContext()).load(mUser.profilepickUrl).into(imageview);
                    username.setText(mUser.name);
                    authtype.setText(mUser.userAuthType);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onBackPressed() {
        finish();
    }
}
