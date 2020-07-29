package com.etkotsoftware.etkotforum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class CreatePostActivity extends AppCompatActivity {

    private Toolbar createPostToolbar;
    private ImageView createPostImage;
    private EditText createPostText;
    private Button createPostButton;
    private CheckBox createPostAnonymously;

    private Uri createPostUri = null;
    private String thumbnailUri = null;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String current_user_id;
    private Bitmap compressedImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        createPostToolbar = (Toolbar) findViewById(R.id.create_post_toolbar);
        setSupportActionBar(createPostToolbar);
        getSupportActionBar().setTitle("Create A New Post");

        createPostImage = (ImageView) findViewById(R.id.createPostImageView);
        createPostText = (EditText) findViewById(R.id.createPostTextMultiLine);
        createPostButton = (Button) findViewById(R.id.createPostButton);
        createPostAnonymously = (CheckBox) findViewById(R.id.createPostAnonymouslyCheckBox);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        current_user_id = firebaseAuth.getCurrentUser().getUid();

        createPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .start(CreatePostActivity.this);
            }
        });

        createPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String description = createPostText.getText().toString();

                if (!description.trim().isEmpty() && createPostUri != null) {

                    File createImageFile = new File(createPostUri.getPath());

                    try {

                        compressedImageBitmap = new Compressor(CreatePostActivity.this)
                                .setMaxHeight(100)
                                .setMaxWidth(100)
                                .setQuality(2)
                                .compressToBitmap(createImageFile);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    final String randomPath = generateRandomString();

                    StorageReference path = storageReference.child("post_images").child(randomPath + ".jpg");
                    path.putFile(createPostUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                            taskSnapshot.getMetadata().getReference().getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                    byte[] ThumbnailData = baos.toByteArray();
                                    UploadTask uploadThumbnail = storageReference
                                            .child("post_images/thumbnails")
                                            .child(randomPath + ".jpg").putBytes(ThumbnailData);

                                    uploadThumbnail.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot1) {
                                            taskSnapshot1.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri1) {

                                                    thumbnailUri = uri1.toString();
                                                }
                                            });

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            // TODO: Error handling

                                        }
                                    });

                                    thumbnailUri = uri.toString();
                                    String downloadUri = uri.toString();

                                    Map<String, Object> postMap = new HashMap<>();
                                    postMap.put("image_url", downloadUri);
                                    postMap.put("thumbnail_url", thumbnailUri);
                                    postMap.put("description", description);
                                    postMap.put("user_id", current_user_id);
                                    postMap.put("timestamp", FieldValue.serverTimestamp());
                                    postMap.put("is_anonymous", createPostAnonymously.isChecked());

                                    firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {

                                            if (task.isSuccessful()) {

                                                Toast.makeText(CreatePostActivity.this, "Post was created", Toast.LENGTH_LONG).show();
                                                Intent mainIntent = new Intent(CreatePostActivity.this,
                                                        MainActivity.class);
                                                startActivity(mainIntent);
                                                finish();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                createPostUri = result.getUri();
                createPostImage.setImageURI(createPostUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(CreatePostActivity.this, "Error: " + error.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private static String generateRandomString() {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        return uuid;
    }
}