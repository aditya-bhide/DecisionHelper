package com.google.sdl.decisionhelper;
import android.content.Intent;
import android.icu.text.LocaleDisplayNames;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.firebase.ui.auth.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Created by aditya on 20/9/17.
 */
public class Group extends AppCompatActivity{

    //declaring variables
    private Toolbar toolbar;

    private UserObj mUser;
    final ArrayList<String> mQuestion = new ArrayList<String>();
    final ArrayList<String> mQuestionKey = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    private DatabaseReference mRefForQuestions;
    private ChildEventListener mChildEventListener;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.google.sdl.decisionhelper.R.layout.group);

        final ListView questionlist = (ListView) findViewById(R.id.group_questionList);
        //for initialising adapter
        mRefForQuestions=FirebaseDatabase.getInstance().getReference();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mQuestion);
        questionlist.setAdapter(adapter);


        //defining basic variables
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final Bundle bundle = getIntent().getExtras();

        if(bundle!= null)
        {
            getSupportActionBar().setTitle(bundle.getString("GroupName"));
        }

        mUser=new UserObj();


        //firebase get the bloody user IMP
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {
                mUser.uid = profile.getUid();
            };
        }
        attachDatabaseReadListener();
        Log.i("yyyy","yyyy");

        questionlist.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent_group = new Intent(Group.this, Question.class);
                intent_group.putExtra("QuestionKey", mQuestionKey.get(position));
                destoryArrays();
                startActivity(intent_group);
            }
        });

    }

    private void attachDatabaseReadListener(){
    if(mChildEventListener==null) {
        mRefForQuestions.child("questions").addChildEventListener(mChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.i("zzzzz", "zzzzz");
            final Bundle bundle = getIntent().getExtras();
            QuestionObj quest = dataSnapshot.getValue(QuestionObj.class);
            Log.i("zzzzz", quest.getGpid());
            if (quest.gpid.matches(bundle.getString("GroupKey"))) {  //if member uid exists in the members column of the group
                mQuestion.add(quest.getQuestion());//add to list
                mQuestionKey.add(dataSnapshot.getKey());
                adapter.notifyDataSetChanged();
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


    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main2, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case com.google.sdl.decisionhelper.R.id.ask_question_menu:
                Bundle bundle = getIntent().getExtras();
                Intent intent=new Intent(this,CreateQuestion.class);
                intent.putExtra("GroupKey",bundle.getString("GroupKey"));
                startActivity(intent);
                return true;
            case R.id.group_profile_menu:
                Bundle bundle2 = getIntent().getExtras();
                Intent intent2=new Intent(this,GroupProfile.class);
                intent2.putExtra("GroupKey",bundle2.getString("GroupKey"));
                intent2.putExtra("GroupName",bundle2.getString("GroupName"));
                startActivity(intent2);
                return true;
            case R.id.add_participants_menu:
                Bundle bundle3 = getIntent().getExtras();
                Intent intent3=new Intent(this,AddParticipants.class);
                intent3.putExtra("GroupKey",bundle3.getString("GroupKey"));
                intent3.putExtra("GroupName",bundle3.getString("GroupName"));
                startActivity(intent3);
                return true;
            case R.id.exit_qroup_menu:
                ExitGroup();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mChildEventListener==null) {
            attachDatabaseReadListener();
        }
        detachDatabasereadListener();
    }

    @Override
    protected void onResume(){
        super.onResume();
        destoryArrays();
        attachDatabaseReadListener();

    }

    private void detachDatabasereadListener(){
        if(mChildEventListener!=null){

            mRefForQuestions.removeEventListener(mChildEventListener);
            mChildEventListener=null;
        }

    }

    private void destoryArrays(){
        mQuestion.clear();
        mQuestionKey.clear();
        adapter.clear();
    }

    void ExitGroup(){

        final Bundle bundle = getIntent().getExtras();
        FirebaseDatabase.getInstance().getReference().child("questions").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                QuestionObj quest = dataSnapshot.getValue(QuestionObj.class);
                if(quest.gpid.equals(bundle.getString("GroupKey"))) {
                    boolean CheckforMember = quest.YesUserList.contains(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    if (CheckforMember == true) {  //if member uid exists in the members column of question
                        quest.YesUserList.remove(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        quest.yes--;
                        dataSnapshot.getRef().setValue(quest);

                    }
                    CheckforMember = quest.NoUserList.contains(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    if (CheckforMember == true) {  //if member uid exists in the members column of question
                        quest.NoUserList.remove(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        quest.no--;
                        dataSnapshot.getRef().setValue(quest);
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

        FirebaseDatabase.getInstance().getReference().child("users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                UserObj user = dataSnapshot.getValue(UserObj.class);
                if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(user.uid)) {
                    boolean CheckforMember = user.groupid.contains(bundle.getString("GroupKey"));
                    if (CheckforMember == true) {  //if member uid exists in the members column of the group
                        user.groupid.remove(bundle.getString("GroupKey"));
                        dataSnapshot.getRef().setValue(user);
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


        FirebaseDatabase.getInstance().getReference().child("groups").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                GroupObj grp = dataSnapshot.getValue(GroupObj.class);
                if(dataSnapshot.getKey().equals(bundle.getString("GroupKey"))){
                  boolean CheckforMember = grp.memberList.contains(FirebaseAuth.getInstance().getCurrentUser().getUid());
                  if(CheckforMember == true) {  //if member uid exists in the members column of the group
                      grp.memberList.remove(FirebaseAuth.getInstance().getCurrentUser().getUid());
                     dataSnapshot.getRef().setValue(grp);
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

    }
}
