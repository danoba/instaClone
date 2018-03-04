package com.rapidapps.instagram.instagram.instagrampostinfo;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rapidapps.instagram.instagram.R;
import com.rapidapps.instagram.instagram.instagramposthome.InstagramHome;
import com.rapidapps.instagram.instagram.profileuser.UserProfileActivity;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class UserPostSend extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 3;
    private static final int SELECT_FILE = 2;

    //IMAGE HOLD URI
    Uri imageHoldUri = null;

    //PROGRESS DIALOG
    ProgressDialog mProgress;

    //IMAGE FIELD
    ImageView postImage;

    //CAPTION EDIT TEXT
    EditText captionEditText;

    //ADD BUTTON
    Button addPostButton;

    //FIREBASE INSTANCES
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthStateListener;

    DatabaseReference mPostDatabaseRef;
    DatabaseReference mUserDatabaseRef;
    StorageReference mStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_post_send);

        //INITIALIZE VIEW FIELDS
        postImage = (ImageView) findViewById( R.id.add_postuser_image );
        captionEditText = (EditText) findViewById( R.id.userPostCaption );
        addPostButton = (Button) findViewById( R.id.userPostAdd );

        mProgress = new ProgressDialog( this );

        //INITIALIZE FREBASE INSTANCES
        mAuth = FirebaseAuth.getInstance();

        mPostDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Post");
        mUserDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child( mAuth.getCurrentUser().getUid() );
        mStorageReference = FirebaseStorage.getInstance().getReference();

        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profilePicSelection();
            }
        });

        addPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               addPost();
            }
        });
    }

    private void addPost()
    {

        if( !TextUtils.isEmpty( captionEditText.getText().toString().trim() ) && imageHoldUri != null )
        {

            final String captionTextValue = captionEditText.getText().toString().trim();

            mProgress.setTitle( "Adding Post" );
            mProgress.setMessage( "Please wait ..." );
            mProgress.show();

            StorageReference postImageStorage = mStorageReference.child( "Post_Images" ).child( imageHoldUri.getLastPathSegment() );
            postImageStorage.putFile( imageHoldUri ).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    final Uri downloadableImageUri = taskSnapshot.getDownloadUrl();
                    final DatabaseReference mPostSpecificDatabaseRef = mPostDatabaseRef.push();

                    mUserDatabaseRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            mPostSpecificDatabaseRef.child( "caption_text" ).setValue( captionTextValue );
                            mPostSpecificDatabaseRef.child( "image" ).setValue( downloadableImageUri.toString() );
                            mPostSpecificDatabaseRef.child( "userid" ).setValue( mAuth.getCurrentUser().getUid() );
                            mPostSpecificDatabaseRef.child( "username" ).setValue( dataSnapshot.child( "username" ).getValue() ).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    mProgress.dismiss();

                                    if( task.isSuccessful() )
                                    {
                                        Toast.makeText(  UserPostSend.this, "Post Added Successfully", Toast.LENGTH_SHORT );
                                        startActivity(new Intent(UserPostSend.this, InstagramHome.class));

                                    }else
                                    {
                                        Toast.makeText(  UserPostSend.this, "Filed To Add Post", Toast.LENGTH_SHORT );
                                    }

                                }
                            });

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            });
        }

    }

    private void profilePicSelection() {


        //DISPLAY DIALOG TO CHOOSE CAMERA OR GALLERY

        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(UserPostSend.this);
        builder.setTitle("Add Photo!");

        //SET ITEMS AND THERE LISTENERS
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals("Take Photo")) {
                    cameraIntent();
                } else if (items[item].equals("Choose from Library")) {
                    galleryIntent();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();

    }

    private void cameraIntent() {

        //CHOOSE CAMERA
        Log.d("gola", "entered here");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void galleryIntent() {

        //CHOOSE IMAGE FROM GALLERY
        Log.d("gola", "entered here");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        //SAVE URI FROM GALLERY
        if(requestCode == SELECT_FILE && resultCode == RESULT_OK)
        {
            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }else if ( requestCode == REQUEST_CAMERA && resultCode == RESULT_OK ){
            //SAVE URI FROM CAMERA

            Uri imageUri = data.getData();

            Log.d( "Image Uri Data log: " , imageUri.toString());

            if( imageUri != null ) {
                CropImage.activity(imageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(this);
            }

        }

        //image crop library code
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageHoldUri = result.getUri();

                postImage.setImageURI(imageHoldUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }
}
