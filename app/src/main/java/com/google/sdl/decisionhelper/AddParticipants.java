package com.google.sdl.decisionhelper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by aditya on 2/10/17.
 */

public class AddParticipants extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText email,phnumber;
    private Button emailbtn,phnumberbtn;
    private DatabaseReference mDatabaseReference;
    private UserObj mUser;
    private String type;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.google.sdl.decisionhelper.R.layout.add_participants);

        //basic defination
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        email=(EditText)findViewById(R.id.email_edittext);
        phnumber=(EditText)findViewById(R.id.phnumber_edittext);
        emailbtn=(Button)findViewById(R.id.email_button);
        phnumberbtn=(Button)findViewById(R.id.phnumber_button);

        mUser=new UserObj();

        final Bundle bundle = getIntent().getExtras();
        if(bundle!= null)
        {
            getSupportActionBar().setTitle(bundle.getString("GroupName"));
        }

/*
       Log.i("khitari",mDatabaseReference.child("groups").getKey();
*/


        emailbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(email.getText().toString().equals(""))
                    Toast.makeText(AddParticipants.this,"Enter participant first",Toast.LENGTH_SHORT).show();
                else
                {
                    type=email.getText().toString();
                    checkmember(type);
                    email.setText("");
                }
            }
        });

        phnumberbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(phnumber.getText().toString().equals(""))
                    Toast.makeText(AddParticipants.this,"Enter participant first",Toast.LENGTH_SHORT).show();
                else
                {
                    type=phnumber.getText().toString();
                    checkmember(type);
                    phnumber.setText("");
                }
            }
        });


    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.google.sdl.decisionhelper.R.menu.menu_main9, menu);
        return true;
    }

    void checkmember(String usertype)
    {
        mDatabaseReference= FirebaseDatabase.getInstance().getReference().child("users");
        Query query = mDatabaseReference.orderByChild("userAuthType").equalTo(usertype);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()==null)
                {
                    Toast.makeText(AddParticipants.this,"User is not in the database",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    for(DataSnapshot singlisnapshot:dataSnapshot.getChildren())
                        mUser=singlisnapshot.getValue(UserObj.class);

                    int flag=0;
                    final Bundle bundle = getIntent().getExtras();
                    for(String a:mUser.groupid)
                    {
                        if(bundle.getString("GroupKey").equals(a)){
                            flag=1;
                        }
                    }
                    if(flag==1)
                    {
                        Toast.makeText(AddParticipants.this,"Member is already in the group",Toast.LENGTH_SHORT).show();
                    }else
                    {
                        mUser.groupid.add(bundle.getString("GroupKey"));
                        for(DataSnapshot singlisnapshot:dataSnapshot.getChildren())
                            singlisnapshot.getRef().setValue(mUser);

                        mDatabaseReference=FirebaseDatabase.getInstance().getReference();
                        mDatabaseReference.child("groups").addChildEventListener(new ChildEventListener() {

                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                               if(dataSnapshot.getKey().equals(bundle.getString("GroupKey")))
                                {
                                    GroupObj grp = dataSnapshot.getValue(GroupObj.class);
                                    grp.memberList.add(mUser.getUid());
                                    dataSnapshot.getRef().setValue(grp);
                                }
                            }
                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {}
                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                            @Override
                            public void onCancelled(DatabaseError databaseError) {}
                        });

                        Toast.makeText(AddParticipants.this,"Member Succefully Added",Toast.LENGTH_SHORT).show();
                    }
                }
            }

            public void onBackPressed() {
        /*if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }*/
                finish();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }





}
