package com.google.sdl.decisionhelper;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/**
 * Created by aditya on 26/9/17.
 */

public class CreateGroup extends AppCompatActivity implements View.OnClickListener{

    private static final int RC_PHOTO_PICKER =  2;

    private Toolbar toolbar;
    private ImageButton mPhotoPickerButton;
    private EditText GroupName;
    private Button createGroup;
    private GroupObj mGroup;
    private ImageView mImageView;
    private Uri selectImageUri;
    private Uri downloadUrl;
    private UserObj mUser;
    private  String uid;
    private int flag,flag2;

    //firebase variable declaration
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mPhotosStorageReference;




    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_group);


        toolbar = (Toolbar) findViewById(com.google.sdl.decisionhelper.R.id.toolbar);
        setSupportActionBar(toolbar);
        createGroup=(Button)findViewById(R.id.create_group_button);
        GroupName=(EditText)findViewById(R.id.group_profile_name);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.create_group_photoPickerButton);
        mImageView=(ImageView)findViewById(R.id.create_group_imageView);


        createGroup.setOnClickListener(this);

        //firebase variable defination
        mFirebaseDatabase= FirebaseDatabase.getInstance();
        mFirebaseStorage=FirebaseStorage.getInstance();
        mPhotosStorageReference= mFirebaseStorage.getReference().child("group_icons");

        mGroup=new GroupObj();
        mUser=new UserObj();

        flag=0;
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
            flag=1;

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
                        flag=0;
                        flag2=1;
                    }
                });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.google.sdl.decisionhelper.R.menu.menu_main6, menu);
        return true;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.create_group_button:
                if (GroupName.getText().toString().trim().equalsIgnoreCase("")) {
                    GroupName.setError("This field can not be blank");
                }else {
                    if(flag==0){
                        CreateGroupData();
                        finish();
                    }
                    else
                        Toast.makeText(this, "Creating group please wait and click again", Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

    private void CreateGroupData() {
        FirebaseUser user2 = FirebaseAuth.getInstance().getCurrentUser();
        if (user2 != null) {
            uid = user2.getUid();
        }
        Log.i("uid",uid);
        if(flag2==1)
            mGroup.setGpProfilePic(downloadUrl.toString());
        mGroup.setGpName(GroupName.getText().toString());
        mGroup.memberList.add(uid);

        mDatabaseReference= mFirebaseDatabase.getReference();
        final String groupKey= mDatabaseReference.child("groups").push().getKey();
        mFirebaseDatabase.getReference().child("groups").child(groupKey).setValue(mGroup);

        mDatabaseReference= mFirebaseDatabase.getReference().child("users");
        Query pendingTasks = mDatabaseReference.orderByChild("uid").equalTo(uid);
        pendingTasks.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot tasksSnapshot) {
                for (DataSnapshot snapshot: tasksSnapshot.getChildren()) {
                    mUser=snapshot.getValue(UserObj.class);
                }
                mUser.groupid.add(groupKey);
                for (DataSnapshot snapshot: tasksSnapshot.getChildren()) {
                    snapshot.getRef().setValue(mUser);
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("databseerr","The read failed: "+ databaseError.getMessage());
            }
        });
    }
    @Override
    public void onBackPressed() {
        /*if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }*/
        finish();
    }
}
