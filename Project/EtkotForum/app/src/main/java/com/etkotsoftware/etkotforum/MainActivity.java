/**
 * This application depicts a forum/blog type messaging platform.
 *
 * The users can create accounts (with an email) and can also set up a
 * profile image and username.
 * They can then post posts which include an image and description.
 * Other users can like them and also comment in them.
 * All this can be done anonymous if wanted.
 * Only original poster or admin can delete posts.
 *
 * Program Author:
 * Name: Petrus Jussila
 */

package com.etkotsoftware.etkotforum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    private MainFragment mainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainFragment = new MainFragment();

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        FloatingActionButton addPostFloatingButton =
                (FloatingActionButton) findViewById(R.id.createPostFloatingButton);

        Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Etkot Forum");

        refreshPosts();

        addPostFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent createPostIntent =
                        new Intent(MainActivity.this, CreatePostActivity.class);
                startActivity(createPostIntent);
            }
        });
    }

    /// Brings the user to login screen.
    private void changeToLogin() {

        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Checks if user isn't logged in
        if (currentUser == null) {
            changeToLogin();
        } else {

            String user_id = mAuth.getCurrentUser().getUid();
            firebaseFirestore.collection("Users")
                    .document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {

                        // Checks whether or not the user has a username/prof.image.
                        if (!task.getResult().exists()) {

                            Intent setupIntent =
                                    new Intent(MainActivity.this, SetupActivity.class);
                            startActivity(setupIntent);
                            finish();
                        }
                    }
                }
            });
        }
    }

    // Creates main_menu.xml
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.refresh_posts:
                Intent mainIntent =
                        new Intent(MainActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
                refreshPosts();
                break;

            case R.id.sort_posts_button:
                // TODO
                break;

            case R.id.action_account_settings_button:
                Intent setupIntent =
                        new Intent(MainActivity.this, SetupActivity.class);
                startActivity(setupIntent);
                break;

            case R.id.action_logout_button:
                logOutAndExit();
                break;
    }
        return true;
    }

    // Empties the fragment and replaces with newly fetch information.
    private void refreshPosts() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame_container, mainFragment);
        fragmentTransaction.commit();
    }

    // Logs out the user and brings the user to login screen.
    private void logOutAndExit() {
        mAuth.signOut();
        changeToLogin();
    }
}