package com.google.sdl.decisionhelper;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

/**
 * Created by aditya on 22/9/17.
 */

public class GroupProfile extends AppCompatActivity {

    private static final int RC_PHOTO_PICKER =  2;

    private Toolbar toolbar;
    private ImageButton mPhotoPickerButton;
    private EditText GroupName;
    private GroupObj mGroup;
    private ImageView mImageView;
    private Uri selectImageUri;
    private Uri downloadUrl;
    private String uid;
    private ListView mlistview;
    int flag1,flag2;
    final ArrayList<String> mMemberList = new ArrayList<String>();


    //firebase variable declaration
    private DatabaseReference mDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mPhotosStorageReference;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_profile);



        toolbar = (Toolbar) findViewById(com.google.sdl.decisionhelper.R.id.toolbar);
        setSupportActionBar(toolbar);
        GroupName=(EditText)findViewById(R.id.group_profile_name);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.group_profile_photoPickerButton);
        mImageView=(ImageView)findViewById(R.id.group_profile_imageview);
        mImageView.setImageResource(R.drawable.defaultgroupicon);
        mlistview=(ListView)findViewById(R.id.group_profile_listview);

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(GroupProfile.this, android.R.layout.simple_list_item_1, mMemberList);
        mlistview.setAdapter(adapter);

        //firebase variable defination
        mFirebaseStorage=FirebaseStorage.getInstance();
        mPhotosStorageReference= mFirebaseStorage.getReference().child("group_icons");

        final Bundle bundle = getIntent().getExtras();
        mDatabaseReference=FirebaseDatabase.getInstance().getReference();
        mDatabaseReference.child("groups").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if(dataSnapshot.getKey().equals(bundle.getString("GroupKey")))
                {
                    GroupObj grp = dataSnapshot.getValue(GroupObj.class);
                    if(grp.getGpProfilePic()!=null)
                    {
                        Glide.with(mImageView.getContext()).load(grp.getGpProfilePic()).into(mImageView);
                    }
                    Glide.with(mImageView.getContext()).load(grp.getGpProfilePic()).into(mImageView);
                    GroupName.setText(grp.getGpName());
                    showList(grp.memberList);
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

        flag1=0;flag2=0;
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

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main8, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_group:
                //sign out
                if (GroupName.getText().toString().trim().equalsIgnoreCase("")) {
                    GroupName.setError("This field can not be blank");
                }else {
                    if(flag1==0){
                    updateGroupProfile();
                    finish();
                    }else{
                        Toast.makeText(this, "Updationg group please wait and click again", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
                        downloadUrl=taskSnapshot.getDownloadUrl();
                        Glide.with(mImageView.getContext()).load(downloadUrl.toString()).into(mImageView);
                        flag1=0;
                        flag2=1;
                    }
                });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void updateGroupProfile() {

        final Bundle bundle = getIntent().getExtras();
        mDatabaseReference=FirebaseDatabase.getInstance().getReference();
        mDatabaseReference.child("groups").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if(dataSnapshot.getKey().equals(bundle.getString("GroupKey")))
                {
                    GroupObj grp = dataSnapshot.getValue(GroupObj.class);
                    grp.setGpName(GroupName.getText().toString());
                    if(flag2==1)
                        grp.setGpProfilePic(downloadUrl.toString());
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

    }
    void showList(final ArrayList<String> memberlist){
        mDatabaseReference=FirebaseDatabase.getInstance().getReference();
        mDatabaseReference.child("users").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                UserObj user = dataSnapshot.getValue(UserObj.class);
                int flag=0;
                for(String a:memberlist){
                    if(a.equals(user.getUid())){
                        mMemberList.add(user.getName());
                    }
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

    }



}
