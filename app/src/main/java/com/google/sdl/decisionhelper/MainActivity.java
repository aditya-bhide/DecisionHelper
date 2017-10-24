package com.google.sdl.decisionhelper;
import android.app.NotificationManager;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class MainActivity extends AppCompatActivity {


    //constant declarations
    public static final int RC_SIGN_IN = 1;


    //other variable declaration
//    private MaterialSearchView searchView;
    private Toolbar toolbar;
    private UserObj mUser;
    private GroupObj mGroupObj;
    private QuestionObj question;


    //firebase variable declaration
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;

    private ChildEventListener mChildEventListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;


    //for Displaying Groups
     private DatabaseReference mRefForGroups;
     private ListView mlistView;
    final ArrayList<String> mGroupNames = new ArrayList<String>();
    final ArrayList<String> mGroupKeys = new ArrayList<String>();
    ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.google.sdl.decisionhelper.R.layout.activity_main);
        //firebase variable defination
        mFirebaseDatabase=FirebaseDatabase.getInstance();
        mDatabaseReference= mFirebaseDatabase.getReference().child("group");
        mFirebaseAuth=FirebaseAuth.getInstance();

        //for initialising list
        adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, mGroupNames);
        final ListView mlistView = (ListView) findViewById(com.google.sdl.decisionhelper.R.id.listView);

        //for initialising adapter



        mlistView.setAdapter(adapter);

        mRefForGroups = FirebaseDatabase.getInstance().getReference();


        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent_group = new Intent(MainActivity.this, Group.class);
                intent_group.putExtra("GroupKey", mGroupKeys.get(position).toString());
                intent_group.putExtra("GroupName", mlistView.getItemAtPosition(position).toString());
                mGroupKeys.clear();
                mGroupNames.clear();
                adapter.clear();//he send karta sadhya tari name send kelay group cha
                startActivity(intent_group);
            }
        });

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=firebaseAuth.getCurrentUser();

                if(user!=null){
                    //user is signed in
                    onSignedInInitialize();
                }
                else {
                    //user is signed out
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                   .setAvailableProviders(
                                            Arrays.asList(
                                                    new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build(),
                                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };


        //other variable definations
        toolbar = (Toolbar) findViewById(com.google.sdl.decisionhelper.R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Decision Helper");
//        searchView = (MaterialSearchView) findViewById(com.google.sdl.decisionhelper.R.id.search_view);


        //search
//        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                //Do some magic
//                return false;
//            }
//        });
//        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
//            @Override
//            public void onSearchViewShown() {
//                //Do some magic
//            }
//            @Override
//            public void onSearchViewClosed() {
//                //Do some magic
//            }
//        });

        //on clicks


    }

    private void onSignedOutCleanup() {
        destoryArrays();
        detachDatabasereadListener();

    }

    private void onSignedInInitialize() {
        destoryArrays();
        attachDatabaseReadListener();
    }

    private void attachDatabaseReadListener(){
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    GroupObj grp = dataSnapshot.getValue(GroupObj.class);
                    boolean CheckforMember = grp.memberList.contains(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    if(CheckforMember == true) {  //if member uid exists in the members column of the group
                        mGroupNames.add(grp.gpName);//add to list
                        adapter.notifyDataSetChanged();
                        mGroupKeys.add(dataSnapshot.getKey());
                        /*android.support.v4.app.NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this)
                                .setSmallIcon(R.drawable.defaultgroupicon)
                                .setContentTitle("Hello")
                                .setContentText("You have been added to a new group "+grp.gpName);
                        NotificationManager notificationmgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                        notificationmgr.notify(001, mBuilder.build());*/
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
            };
            mRefForGroups.child("groups").addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabasereadListener(){
        if(mChildEventListener!=null){
            mRefForGroups.removeEventListener(mChildEventListener);
            mChildEventListener=null;
        }

    }

    public void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==RC_SIGN_IN)
        {
            if(resultCode==RESULT_OK)
            {
                mDatabaseReference= mFirebaseDatabase.getReference().child("users");
                Query query = mDatabaseReference.orderByChild("uid").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override


                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue()==null)
                        {
                            gotoRegister();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                /*Toast.makeText(this, "Signed in", Toast.LENGTH_SHORT).show();*/
            }else if(resultCode ==RESULT_CANCELED)
            {
                Toast.makeText(this, "Signed in canceled ", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

    }

    private void gotoRegister() {
        startActivity(new Intent(this,Register.class));
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        destoryArrays();
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                //sign out
                onSignedOutCleanup();
                AuthUI.getInstance().signOut(this);
                return true;
            case R.id.settings_menu:
                startActivity(new Intent(this,Settings.class));
                return true;
            case R.id.new_group_menu:
                startActivity(new Intent(this,CreateGroup.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.google.sdl.decisionhelper.R.menu.menu_main, menu);
        //MenuItem item = menu.findItem(com.google.sdl.decisionhelper.R.id.action_search);
  //      searchView.setMenuItem(item);
        return true;
    }

    @Override
    public void onBackPressed() {
       // Toast.makeText(this, "Creating account please wait and click again", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
       protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        detachDatabasereadListener();
    }


    protected void onResume(){
        super.onResume();
        destoryArrays();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }
    private void destoryArrays(){
        mGroupKeys.clear();
        mGroupNames.clear();
        adapter.clear();
    }

}
