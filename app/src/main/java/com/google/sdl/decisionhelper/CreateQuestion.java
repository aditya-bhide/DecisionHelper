package com.google.sdl.decisionhelper;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by aditya on 21/9/17.
 */
public class CreateQuestion extends AppCompatActivity implements View.OnClickListener{

    //other variable declaration

    private Toolbar toolbar;
    private String uid;

    QuestionObj q1;

    private Button sendToGroup;
    private EditText question;


    //Firebase instance variables
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.google.sdl.decisionhelper.R.layout.create_question);

        final Bundle key_bun = getIntent().getExtras();


        q1=new QuestionObj();

        toolbar = (Toolbar) findViewById(com.google.sdl.decisionhelper.R.id.toolbar);
        setSupportActionBar(toolbar);

        sendToGroup=(Button)findViewById(R.id.send_to_group);
        question=(EditText) findViewById(R.id.get_question);

        //firebase definitions
        mFirebaseDatabase=FirebaseDatabase.getInstance();

        //on clicks
        sendToGroup.setOnClickListener(this);

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.google.sdl.decisionhelper.R.menu.menu_main3, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.send_to_group:
                if(question.getText().toString().trim().equalsIgnoreCase(""))
                    question.setError("This field can not be blank");
                else {
                    sendQuestion();
                    /*Bundle bundle = getIntent().getExtras();
                    Intent intent=new Intent(this, Group.class);
                    intent.putExtra("GroupKey",bundle.getString("GroupKey"));
                    startActivity(intent);*/
                    finish();
                }
                break;
        }
    }

    private void sendQuestion() {

        final Bundle key_bun = getIntent().getExtras();
        q1.setQuestion(question.getText().toString());
        q1.setGpid(key_bun.getString("GroupKey"));
        mDatabaseReference= mFirebaseDatabase.getReference().child("questions");
        mDatabaseReference.push().setValue(q1);
    }


}
