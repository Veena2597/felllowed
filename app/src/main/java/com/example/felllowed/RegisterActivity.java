package com.example.felllowed;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/** Register activity handles :
 * 1. Checking if user already exists and redirects to MainActivity
 * 2. Create user according to rules set in button.onClickListener
 *    and creates a child under "Users" document in Firebase
 * 3. Handle forgotten password case
 * 4. Redirect to Login activity on login.onClickListener
 */

public class RegisterActivity extends AppCompatActivity {
    public static final String TAG = "TAG";
    public static final int PICK_IMAGE_REQUEST = 1000;
    static member user;
    EditText mUserName,mEmail,mPassword,mPhone;
    Button mRegisterBtn;
    TextView mLoginBtn;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    ImageView mProfile;
    String userID;
    StorageReference storageReference;
    FirebaseDatabase database;
    DatabaseReference databaseReference;
    DatabaseReference profileReference;
    Uri profileFile;
    String profileref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //getSupportActionBar().hide();

        mUserName   = findViewById(R.id.fullName);
        mEmail      = findViewById(R.id.Email);
        mPassword   = findViewById(R.id.password);
        mPhone      = findViewById(R.id.phone);
        mRegisterBtn= findViewById(R.id.registerBtn);
        mLoginBtn   = findViewById(R.id.createText);
        mProfile = findViewById(R.id.profile);

        fAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);


        // Write a message to the database
        storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://fellowed-a5hvee.appspot.com");
        database = FirebaseDatabase.getInstance();


        //Check if user already exists and redirects if yes
        /*
        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(),ForumActivity.class));
            finish();
        }*/

        mProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                Log.e("RA", "hello1");
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
                //finish();
            }
        });

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                final String userName = mUserName.getText().toString();
                final String phone    = mPhone.getText().toString();

                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email is Required.");
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    mPassword.setError("Password is Required.");
                    return;
                }

                if(password.length() < 6){
                    mPassword.setError("Oops! Password less than 6 Characters");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                // register the user in firebase
                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser fuser = fAuth.getCurrentUser();
                            fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(RegisterActivity.this, "Verification Email Has been Sent.", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: Email not sent " + e.getMessage());
                                }
                            });

                            userID = fAuth.getCurrentUser().getUid();
                            databaseReference = database.getReference("Users").child(userID);
                            profileReference = database.getReference("Profiles").child(userID);
                            uploadProfile(profileFile);

                            Toast.makeText(RegisterActivity.this, "User Created.", Toast.LENGTH_SHORT).show();

                            user = new member(userName,email,phone);
                            databaseReference.setValue(user);
                            profileReference.setValue(profileref);

                            DatabaseReference rewardsData = database.getReference("Rewards").child(userID);
                            rewardsData.setValue(0);
                            startActivity(new Intent(getApplicationContext(),ForumActivity.class));

                        }else {
                            Toast.makeText(RegisterActivity.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
            }

        });
    }

    private void uploadProfile(Uri profileImage){
        if(profileImage != null){
            Log.e("FA", String.valueOf(profileImage));
            StorageReference fileReference = storageReference.child(userID+ getFileExtension(profileImage));

            profileref = userID+'.'+ getFileExtension(profileImage);
            Log.e("RA",userID+ getFileExtension(profileImage));
            fileReference.putFile(profileImage);

            fileReference.putFile(profileImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //Picasso.
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "profile not uploaded");
                        }
                    });
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            profileFile = data.getData();
            mProfile.setImageURI(profileFile);
        }
    }
}

