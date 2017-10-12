package com.google.sdl.decisionhelper;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import java.util.List;

/**
 * Created by aditya on 21/9/17.
 */
public class Chatting extends AppCompatActivity {

    //declaration of constants
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    private static final int RC_PHOTO_PICKER =  2;


    //other variable declaration
    private ListView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private ProgressBar mProgressBar;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;
    private UserObj mUser;
    private String Uid;
    int flag;

    //Firebase instance variables
    private DatabaseReference mMessagesDatabaseReference,mRefDatabase;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mChatPhotosStorageReference;

    private ChildEventListener mChildEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.google.sdl.decisionhelper.R.layout.chatting);

        final Bundle bundle = getIntent().getExtras();

        mUser=new UserObj();
        mFirebaseStorage=FirebaseStorage.getInstance();
        mChatPhotosStorageReference= mFirebaseStorage.getReference().child("chat_photos");
        mMessagesDatabaseReference= FirebaseDatabase.getInstance().getReference().child("chats").child(bundle.getString("QuestionKey"));

        Query query =  FirebaseDatabase.getInstance().getReference().child("users").orderByChild("uid").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singlisnapshot:dataSnapshot.getChildren())
                    mUser=singlisnapshot.getValue(UserObj.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        // Initialize references to views
        mProgressBar = (ProgressBar) findViewById(R.id.chatting_progressBar);
        mMessageListView = (ListView) findViewById(R.id.chatting_messageListView);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.chatting_photoPickerButton);
        mMessageEditText = (EditText) findViewById(R.id.chatting_messageEditText);
        mSendButton = (Button) findViewById(R.id.chatting_sendButton);

        // Initialize message ListView and its adapter
        final List<Message> Messages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(this, R.layout.item_message, Messages);
        mMessageListView.setAdapter(mMessageAdapter);

        attachDatabaseReadlistener();
        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);



        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Send messages on click

                // Clear input box
                Message msg = new Message(mMessageEditText.getText().toString(), mUser.name, null);
                mMessagesDatabaseReference.push().setValue(msg);
                mMessageEditText.setText("");
            }
        });

        mMessagesDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Message msg = dataSnapshot.getValue(Message.class);
                mMessageAdapter.add(msg);
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

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==RC_PHOTO_PICKER && resultCode==RESULT_OK)
        {
            flag=0;
            Uri selectImageUri=data.getData();

            //get reference to storage file at chat_photos/<FileName>
            StorageReference photoRef =mChatPhotosStorageReference.child(selectImageUri.getLastPathSegment());

            //upload file to firebase storage
            photoRef.putFile(selectImageUri)
                    .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri downloadUrl=taskSnapshot.getDownloadUrl();
                            Message msg=new Message(null,mUser.name,downloadUrl.toString());
                            mMessagesDatabaseReference.push().setValue(msg);
                            flag=1;
                        }
                    });
        }
    }

    private void onSignedInInitialize(String username) {
        attachDatabaseReadlistener();
        mUser.name=username;

    }

    private void attachDatabaseReadlistener(){
        if(mChildEventListener==null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Message msg = dataSnapshot.getValue(Message.class);
                    mMessageAdapter.add(msg);
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
        }

    }

    private void detchDatabasereadListener(){
        if(mChildEventListener!=null){
            mMessagesDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener=null;
        }

    }
    @Override
    public void onBackPressed() {
       finish();
    }




}
