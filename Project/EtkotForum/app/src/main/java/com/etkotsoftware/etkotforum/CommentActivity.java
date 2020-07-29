package com.etkotsoftware.etkotforum;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentActivity extends AppCompatActivity {

    private List<Comments> commentsList;
    private CommentsAdapter commentAdapter;

    private EditText comment_edit_text;
    private CheckBox is_anonymous_button;
    private String post_id;
    private String user_id;

    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Toolbar commentToolbar = (Toolbar) findViewById(R.id.comment_toolbar);
        setSupportActionBar(commentToolbar);
        getSupportActionBar().setTitle("Comments");

        post_id = getIntent().getStringExtra("post_id");

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        user_id = firebaseAuth.getCurrentUser().getUid();

        comment_edit_text = (EditText) findViewById(R.id.commentEditText);
        Button comment_post_button = (Button) findViewById(R.id.sendCommentButton);
        is_anonymous_button = (CheckBox) findViewById(R.id.isAnonymousCheckBox);
        RecyclerView comment_list = (RecyclerView) findViewById(R.id.commentRecyclerView);

        commentsList = new ArrayList<>();
        commentAdapter = new CommentsAdapter(commentsList);
        comment_list.setHasFixedSize(true);
        comment_list.setLayoutManager(new LinearLayoutManager(this));
        comment_list.setAdapter(commentAdapter);

        Query firstComments = firebaseFirestore.collection("Posts/" + post_id + "/Comments").orderBy("timestamp", Query.Direction.ASCENDING);
        firstComments.addSnapshotListener(CommentActivity.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                if (!value.isEmpty()) {

                    for (DocumentChange doc : value.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String commentId = doc.getDocument().getId();
                            Comments comments = doc.getDocument().toObject(Comments.class);
                            commentsList.add(comments);
                            commentAdapter.notifyDataSetChanged();

                        }
                    }
                }
            }
        });

        comment_post_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!comment_edit_text.getText().toString().trim().isEmpty()) {

                    Map<String, Object> commentsMap = new HashMap<>();
                    commentsMap.put("description", comment_edit_text.getText().toString().trim());
                    commentsMap.put("user_id", user_id);
                    commentsMap.put("is_anonymous", is_anonymous_button.isChecked());
                    commentsMap.put("timestamp", FieldValue.serverTimestamp());

                    firebaseFirestore.collection("Posts/" + post_id + "/Comments")
                            .add(commentsMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {

                            if (task.isSuccessful()) {

                                Toast.makeText(CommentActivity.this, "Post successful",
                                        Toast.LENGTH_LONG).show();
                                comment_edit_text.setText("");
                            }
                        }
                    });
                }
            }
        });
    }
}