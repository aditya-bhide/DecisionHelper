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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/**
 * Created by aditya on 27/9/17.
 */

public class Register extends AppCompatActivity implements View.OnClickListener{

    private static final int RC_PHOTO_PICKER =  2;

    private Toolbar toolbar;
    private ImageButton mPhotoPickerButton;
    private UserObj mUser;
    private ImageView mImageView;
    private Button register;
    private EditText username;
    private Uri selectImageUri;
    private Uri downloadUrl;
    private int flag,flag2;


    //firebase variable declaration
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mPhotosStorageReference;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);


        //basic definition
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mImageView=(ImageView)findViewById(R.id.register_image_view);
        register=(Button)findViewById(R.id.register_data);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.register_imagebutton);
        register.setOnClickListener(this);
        username=(EditText)findViewById(R.id.register_username);

        //firebase variable defination
        mFirebaseDatabase= FirebaseDatabase.getInstance();
        mDatabaseReference= mFirebaseDatabase.getReference().child("users");
        mFirebaseStorage=FirebaseStorage.getInstance();
        mPhotosStorageReference= mFirebaseStorage.getReference().child("profile_pics");

        mUser=new UserObj();
        mImageView.setImageResource(R.drawable.defaultprofilepic);



        String providertype="none";
        FirebaseUser user1 = FirebaseAuth.getInstance().getCurrentUser();
        for (UserInfo profile : user1.getProviderData()) {
           providertype=profile.getProviderId();
        };


        FirebaseUser user2 = FirebaseAuth.getInstance().getCurrentUser();
        if (user2 != null) {
            // Name, email address, and profile photo Url
            String uid = user2.getUid();
            mUser.setUid(uid);
            if(providertype.equals("google.com")) {
                String email = user2.getEmail();
                mUser.setUserAuthType(email);
                TextView temp = (TextView) findViewById(R.id.temp);
                temp.setText(email);
            }
            if(providertype.equals("phone")){

                    String phno=user2.getPhoneNumber();
                    mUser.setUserAuthType(phno);
                    TextView temp = (TextView) findViewById(R.id.temp);
                    temp.setText(phno);
            }
        }
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

    /*private void attachDatabaseReadlistener(){
        if(mChildEventListener==null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
                    mMessageAdapter.add(friendlyMessage);
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
            };
            mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
        }

    }*/


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
        getMenuInflater().inflate(com.google.sdl.decisionhelper.R.menu.menu_main7, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (username.getText().toString().trim().equalsIgnoreCase("")) {
            username.setError("This field can not be blank");
        }else {
            registerdata();
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.register_data:
                if (username.getText().toString().trim().equalsIgnoreCase("")) {
                    username.setError("This field can not be blank");
                }else {
                    if(flag==0) {
                        registerdata();
                        startActivity (new Intent(this, MainActivity.class));
                    }else
                        Toast.makeText(this, "Creating account please wait and click again", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void registerdata(){
        mUser.name = username.getText().toString();
        if(flag2==1)
            mUser.profilepickUrl=downloadUrl.toString();
        mDatabaseReference.push().setValue(mUser);

    }
}
