package com.google.sdl.decisionhelper;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

/**
 * Created by aditya on 25/9/17.
 */

public class UserProfile extends AppCompatActivity {


    private static final int RC_PHOTO_PICKER =  2;

    private Toolbar toolbar;
    private EditText name;
    private ImageButton mPhotoPickerButton;
    private ImageView userprofilepic;
    private UserObj mUser;
    private EditText username;
    private String uid;
    private Uri selectImageUri;
    private ListView mlistView;
    private int flag1,flag2;


    //Firebase instance variables
    private DatabaseReference mDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mPhotosStorageReference;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        //basic defination
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        final ListView mlistView = (ListView) findViewById(com.google.sdl.decisionhelper.R.id.userprofile_grouplistView);


        userprofilepic=(ImageView)findViewById(R.id.userprofilepic);
        username=(EditText) findViewById(R.id.edit_username);


        final ArrayList<String> mGroupNames = new ArrayList<String>();
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(UserProfile.this, android.R.layout.simple_list_item_1, mGroupNames);
        mlistView.setAdapter(adapter);



        //firebase definitions
        mFirebaseStorage=FirebaseStorage.getInstance();
        mDatabaseReference=FirebaseDatabase.getInstance().getReference().child("users");
        mPhotosStorageReference= mFirebaseStorage.getReference().child("profile_pics");
        userprofilepic.setImageResource(R.drawable.defaultprofilepic);


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
                        Glide.with(userprofilepic.getContext()).load(mUser.getProfilepickUrl()).into(userprofilepic);
                    }
                    username.setText(mUser.getName());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("databseerr","The read failed: "+ databaseError.getMessage());
            }
        });

        mDatabaseReference=FirebaseDatabase.getInstance().getReference();
        mDatabaseReference.child("groups").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                GroupObj grp = dataSnapshot.getValue(GroupObj.class);
                boolean CheckforMember = grp.memberList.contains(FirebaseAuth.getInstance().getCurrentUser().getUid());
                if(CheckforMember == true) {  //if member uid exists in the members column of the group
                    mGroupNames.add(grp.gpName);//add to list
                    adapter.notifyDataSetChanged();
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

        flag1=0;
        flag2=0;
        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });

    }

    public void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);
     if(requestCode==RC_PHOTO_PICKER && resultCode==RESULT_OK)
        {
            selectImageUri=data.getData();
            flag1=1;
            StoreImageToFirebase();
        }
    }

    void StoreImageToFirebase(){
        //get reference to storage file at chat_photos/<FileName>
        StorageReference photoRef =mPhotosStorageReference.child(selectImageUri.getLastPathSegment());

        //upload file to firebase storage
        photoRef.putFile(selectImageUri)
                .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl=taskSnapshot.getDownloadUrl();
                        mUser.profilepickUrl=downloadUrl.toString();
                        Glide.with(userprofilepic.getContext()).load(mUser.getProfilepickUrl()).into(userprofilepic);
                        flag2=1;
                        flag1=0;

                    }
                });
    }



    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.google.sdl.decisionhelper.R.menu.menu_main5, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_profile:
                //sign out
                if (username.getText().toString().trim().equalsIgnoreCase("")) {
                    username.setError("This field can not be blank");
                }else {
                    if(flag1==0) {
                        updateProfile();
                        startActivity(new Intent(this, MainActivity.class));
                    }else
                        Toast.makeText(this, "Updationg account please wait and click again", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateProfile() {


        mUser.name = username.getText().toString();

        mDatabaseReference= FirebaseDatabase.getInstance().getReference().child("users");
        Query pendingTasks = mDatabaseReference.orderByChild("uid").equalTo(uid);
        pendingTasks.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot tasksSnapshot) {
                for (DataSnapshot snapshot: tasksSnapshot.getChildren()) {
                    snapshot.getRef().setValue(mUser);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("databseerr","The read failed: "+ databaseError.getMessage());
            }
        });
    }



    @Override
    public void onBackPressed() {
        if (username.getText().toString().trim().equalsIgnoreCase("")) {
            username.setError("This field can not be blank");
        }else {
            updateProfile();
            finish();

        }

    }

}
