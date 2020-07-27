package com.etkotsoftware.etkotforum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private String user_id;

    private CircleImageView userImage;

    private boolean imageChanged = false;

    private Uri imageURI = null;

    private EditText setupUsername;

    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        user_id = firebaseAuth.getCurrentUser().getUid();

        androidx.appcompat.widget.Toolbar setupToolbar = (Toolbar) findViewById(R.id.setup_toolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Account Settings");

        setupUsername = (EditText) findViewById(R.id.usernameEditText);
        Button confirmButton = (Button) findViewById(R.id.confirmButton);

        userImage = findViewById(R.id.userImageView);

        firebaseFirestore.collection("Users").document(user_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    if (task.getResult().exists()){

                        String username = task.getResult().getString("username");
                        String profile_image = task.getResult().getString("profile_image");

                        imageURI = Uri.parse(profile_image);

                        setupUsername.setText(username);

                        // Placeholder
                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.common_full_open_on_phone);

                        Glide.with(SetupActivity.this).load(profile_image).into(userImage);
                    }
                }
                else {
                    Toast.makeText(SetupActivity.this, "Error at retrieving data", Toast.LENGTH_LONG).show();
                }
            }
        });

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    if (ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                    || ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

                        askForPermission();
                    }
                    else {
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1,1)
                                .start(SetupActivity.this);

                    }
                }
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String username = setupUsername.getText().toString().trim();

                if (imageChanged) {

                    if (username.length() < 3) {
                        Toast.makeText(SetupActivity.this, "Username is too short", Toast.LENGTH_LONG).show();
                    } else if (imageURI == null) {
                        Toast.makeText(SetupActivity.this, "You need to select a picture", Toast.LENGTH_LONG).show();
                    } else {

                        user_id = firebaseAuth.getCurrentUser().getUid();
                        StorageReference image_path = storageReference.child("profile_images").child(user_id + ".png");

                        image_path.putFile(imageURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        storeIntoFirestore(uri, username);
                                    }
                                });
                            }
                        });
                    }
                }
                else {

                    storeIntoFirestore(null, username);
                }
            }
        });
    }

    private void storeIntoFirestore(Uri uri, String username) {

        if (username.length() < 3) {

            Toast.makeText(SetupActivity.this, "Username is too short", Toast.LENGTH_LONG).show();
            return;
        }

        Uri downloadUrl = uri;

        if (uri == null)  {

            downloadUrl = imageURI;
        }

        Map<String, String> userMap = new HashMap<>();
        userMap.put("username", username);
        userMap.put("profile_image", downloadUrl.toString());

        firebaseFirestore.collection("Users")
                .document(user_id).set(userMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()) {

                            Intent mainIntent = new Intent(SetupActivity.this,
                                    MainActivity.class);
                            startActivity(mainIntent);
                            finish();
                            Toast.makeText(SetupActivity.this, "Successful", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void askForPermission() {

        ActivityCompat.requestPermissions(SetupActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                imageURI = result.getUri();
                userImage.setImageURI(imageURI);

                imageChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(SetupActivity.this, "Error: " + error.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }
}