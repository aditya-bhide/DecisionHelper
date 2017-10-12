package com.google.sdl.decisionhelper;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by aditya on 21/9/17.
 */
public class Question extends AppCompatActivity {
    private Button comment;
    private DatabaseReference mQuesRef;
    private DatabaseReference mRandom;
    private RadioGroup rdgroup;
    private RadioButton rdbtn;
    private QuestionObj question;
    TextView question_text;
    TextView yescount;
    TextView nocount;
    String asach;

    //firebase
    private DatabaseReference mDatabaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.question);

        mQuesRef= FirebaseDatabase.getInstance().getReference();
        // This will get the radiogroup
        //RadioGroup rGroup = (RadioGroup)findViewById(R.id.question_radioGroup);
        // This will get the radiobutton in the radiogroup that is checked
       // RadioButton checkedRadioButton = (RadioButton)rGroup.findViewById(rGroup.getCheckedRadioButtonId());
        final Bundle bundle = getIntent().getExtras();
        final FirebaseUser user_this = FirebaseAuth.getInstance().getCurrentUser();

        //for actually retrieving the question
        mQuesRef.child("questions").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                QuestionObj quest = dataSnapshot.getValue(QuestionObj.class);
                boolean CheckforQuestion = dataSnapshot.getKey().matches(bundle.getString("QuestionKey"));

                if(CheckforQuestion == true) {  //if member uid exists in the members column of the group
                    //text value becomes that question

                    question_text = (TextView) findViewById(R.id.question_question);
                    yescount = (TextView) findViewById(R.id.question_yes_count);
                    nocount = (TextView) findViewById(R.id.question_no_count);

                    question_text.setText(quest.getQuestion().toString());
                    yescount.setText(String.valueOf(quest.getYes()));
                    nocount.setText(String.valueOf(quest.getNo()));
                    setOuestionObj(quest);
                    boolean Member_in_yes = quest.YesUserList.contains(user_this.getUid());
                    boolean Member_in_no = quest.NoUserList.contains(user_this.getUid());
                    if(Member_in_yes) {
                        rdbtn=(RadioButton)findViewById(R.id.radioButton1);
                        rdbtn.setChecked(true);
                        rdbtn=(RadioButton)findViewById(R.id.radioButton2);
                        rdbtn.setChecked(false);

                    }
                    else if(Member_in_no)
                    {
                        rdbtn=(RadioButton)findViewById(R.id.radioButton2);
                        rdbtn.setChecked(true);
                        rdbtn=(RadioButton)findViewById(R.id.radioButton1);
                        rdbtn.setChecked(false);
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        setContentView(com.google.sdl.decisionhelper.R.layout.question);
        rdgroup=(RadioGroup)findViewById(R.id.question_radioGroup);
        comment=(Button) findViewById(com.google.sdl.decisionhelper.R.id.comment);
        comment.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                int selectedId=rdgroup.getCheckedRadioButtonId();
                rdbtn=(RadioButton)findViewById(selectedId);
                if(rdbtn==null)
                    Toast.makeText(Question.this,"PLeaseEnter your choice",Toast.LENGTH_SHORT).show();
                else {
                    if (rdbtn.getText().equals("Yes")) {
                        Toast.makeText(Question.this,"You have selected yes", Toast.LENGTH_SHORT).show();
                        ManageDatabase("Yes");
                    }
                    else if (rdbtn.getText().equals("No")) {
                        Toast.makeText(Question.this, "You have selected no", Toast.LENGTH_SHORT).show();
                        ManageDatabase("No");
                    }
                    ;
                    Intent intent_group = new Intent(Question.this, Chatting.class);
                    intent_group.putExtra("QuestionKey",bundle.getString("QuestionKey"));
                    startActivity(intent_group);
                }
            }
        });
    }
    void ManageDatabase(String selected)
    {
        boolean type1;
        boolean type2;
        boolean type3;
        final Bundle bundle = getIntent().getExtras();

        final FirebaseUser user_this = FirebaseAuth.getInstance().getCurrentUser();


        if(selected.equals("Yes")){
            mRandom= FirebaseDatabase.getInstance().getReference();
            mRandom.child("questions").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    QuestionObj quest = dataSnapshot.getValue(QuestionObj.class);
                    if(dataSnapshot.getKey().equals(bundle.getString("QuestionKey")))
                    {
                        boolean Member_in_yes = quest.YesUserList.contains(user_this.getUid());
                        boolean Member_in_no = quest.NoUserList.contains(user_this.getUid());
                        if(Member_in_no==false && Member_in_yes==false) {
                            quest.yes++;
                            quest.YesUserList.add(user_this.getUid());
                           /* yescount.setText(String.valueOf(quest.getYes()));
                            nocount.setText(String.valueOf(quest.getNo()));*/
                        }
                        if(Member_in_yes==true && Member_in_no==false)
                        {

                        }
                        if(Member_in_no==true && Member_in_yes==false)
                        {
                            quest.no--;
                            quest.yes++;
                            quest.NoUserList.remove(user_this.getUid());
                            quest.YesUserList.add(user_this.getUid());
                            /*yescount.setText(String.valueOf(quest.getYes()));
                            nocount.setText(String.valueOf(quest.getNo()));*/
                        }
                        question_text = (TextView) findViewById(R.id.question_question);
                        yescount = (TextView) findViewById(R.id.question_yes_count);
                        nocount = (TextView) findViewById(R.id.question_no_count);

                        question_text.setText(quest.getQuestion().toString());
                        yescount.setText(String.valueOf(quest.getYes()));
                        nocount.setText(String.valueOf(quest.getNo()));
                        dataSnapshot.getRef().setValue(quest);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }else if(selected.equals("No")){

            mRandom= FirebaseDatabase.getInstance().getReference();
            mRandom.child("questions").addChildEventListener(new ChildEventListener() {

                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    QuestionObj quest = dataSnapshot.getValue(QuestionObj.class);

                    if(dataSnapshot.getKey().equals(bundle.getString("QuestionKey")))
                    {
                        boolean Member_in_yes = quest.YesUserList.contains(user_this.getUid());
                        boolean Member_in_no = quest.NoUserList.contains(user_this.getUid());
                        if(Member_in_no==false && Member_in_yes==false) {
                            quest.no++;
                            quest.NoUserList.add(user_this.getUid());
                            /*yescount.setText(String.valueOf(quest.getYes()));
                            nocount.setText(String.valueOf(quest.getNo()));*/

                        }
                        if(Member_in_no==true  && Member_in_yes==false)
                        {

                        }
                        if(Member_in_yes==true && Member_in_no==false)
                        {
                            quest.yes--;
                            quest.no++;
                            quest.YesUserList.remove(user_this.getUid());
                            quest.NoUserList.add(user_this.getUid());
                            /*yescount.setText(String.valueOf(quest.getYes()));
                            nocount.setText(String.valueOf(quest.getNo()));*/
                        }
                        question_text = (TextView) findViewById(R.id.question_question);
                        yescount = (TextView) findViewById(R.id.question_yes_count);
                        nocount = (TextView) findViewById(R.id.question_no_count);

                        question_text.setText(quest.getQuestion().toString());
                        yescount.setText(String.valueOf(quest.getYes()));
                        nocount.setText(String.valueOf(quest.getNo()));
                        dataSnapshot.getRef().setValue(quest);

                    }

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }
    @Override
    public void onBackPressed() {
        finish();
    }

    void setOuestionObj(QuestionObj obj){
        question=obj;
    }


}
