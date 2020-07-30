package com.etkotsoftware.etkotforum;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    public Context context;
    public List<PostData> post_list;

    private FirebaseFirestore firebaseFirestore;

    public PostAdapter(List<PostData> post_list) {
        this.post_list = post_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
        firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        final String postID = post_list.get(position).PostId;

        String description = post_list.get(position).getDescription();
        holder.setDescription(description);

        String downloadThumbnailUrl = post_list.get(position).getThumbnail_url();
        String downloadImageUrl = post_list.get(position).getImage_url();
        holder.setImage(downloadImageUrl ,downloadThumbnailUrl);

        final String user_id = post_list.get(position).getUser_id();
        Boolean is_anonymous = post_list.get(position).getIs_anonymous();
        holder.setPostUsernamePicture(user_id, is_anonymous);

        Date timestamp = post_list.get(position).getTimestamp();
        holder.setPostDate(timestamp);

        firebaseFirestore.collection("Posts/" + postID + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                int count = 0;

                if (value == null) {

                }

                else if (!value.isEmpty()) {

                    count = value.size();
                    holder.postLikeCountTextView.setText("Likes: " + count);
                }
                else {

                    holder.postLikeCountTextView.setText("Likes: " + count);
                }
            }
        });

        firebaseFirestore.collection("Posts/" + postID + "/Comments").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                int count = 0;

                if (value == null) {
                    return;
                }
                else if (!value.isEmpty()) {

                    count = value.size();
                    holder.commentCount.setText("Comments: " + count);
                }
                else {

                    holder.commentCount.setText("Comments: " + count);
                }
            }
        });

        String current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseFirestore.collection("Posts/" + postID + "/Likes")
                .document(current_user_id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                if (value == null) {
                    return;
                }
                else if (value.exists()) {
                    holder.postLikeButtonImage.setImageDrawable(context.getDrawable(R.mipmap.ic_like_button_liked));
                } else {
                    holder.postLikeButtonImage.setImageDrawable(context.getDrawable(R.mipmap.ic_like_button_not_liked));
                }
            }
        });

        holder.postLikeButtonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                firebaseFirestore.collection("Posts/" + postID + "/Likes").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (!task.getResult().exists()) {

                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());

                            firebaseFirestore.collection("Posts/" + postID + "/Likes")
                                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(likesMap);
                        }
                        else {

                            firebaseFirestore.collection("Posts/" + postID + "/Likes")
                                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid()).delete();
                        }
                    }
                });
            }
        });

        holder.postCommentButtonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent commentIntent = new Intent(context, CommentActivity.class);
                commentIntent.putExtra("post_id", postID);
                context.startActivity(commentIntent);
            }
        });

        holder.deletePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String post_user_id = user_id;
                if (current_user_id.equals(post_user_id)) {
                    firebaseFirestore.collection("Posts/")
                            .document(postID).delete();
                    Toast.makeText(holder.mView.getContext(), "Post deleted. Please refresh to sort posts", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(holder.mView.getContext(), "Only admin/original poster can delete this", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return post_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        private TextView descriptionView;
        private TextView postUsernameView;
        private TextView postDateTextView;
        private TextView postLikeCountTextView;
        private TextView commentCount;

        private ImageView postItemImageView;
        private ImageView postProfileImageView;
        private ImageView postLikeButtonImage;
        private ImageView postCommentButtonImage;
        private Button deletePostButton;

        private FirebaseFirestore firebaseFirestore;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            postLikeButtonImage = (ImageView) mView.findViewById(R.id.likeButtonImageView);
            postLikeCountTextView = (TextView) mView.findViewById(R.id.likeCountTextView);
            postCommentButtonImage = (ImageView) mView.findViewById(R.id.postCommentButtonImageView);
            commentCount = (TextView) mView.findViewById(R.id.commentCountTextView);
            deletePostButton = (Button) mView.findViewById(R.id.deletePostButton);
        }

        public void setDescription(String description) {

            descriptionView = mView.findViewById(R.id.postDescriptionTextView);
            descriptionView.setText(description);
        }

        public void setImage(String downloadImageUri, String downloadThumbnailUri) {

            postItemImageView = mView.findViewById(R.id.postItemImageView);

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.ic_launcher_foreground);

            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloadImageUri)
                    .thumbnail(Glide.with(context).load(downloadThumbnailUri))
                    .into(postItemImageView);
        }

        public void setPostUsernamePicture(String user_id, final boolean is_anonymous) {

            postUsernameView = mView.findViewById(R.id.postUsernameTextView);
            firebaseFirestore = FirebaseFirestore.getInstance();

            final String[] username = new String[1];

            firebaseFirestore.collection("Users").document(user_id).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            if (task.isSuccessful()) {

                                if (task.getResult().exists()) {

                                    Uri imageURI = null;

                                    if (!is_anonymous) {
                                        username[0] = task.getResult().getString("username");
                                        String profile_image = task.getResult().getString("profile_image");
                                        imageURI = Uri.parse(profile_image);
                                    }
                                    else {
                                        username[0] = "Anonymous";
                                        imageURI = Uri.parse("https://firebasestorage.googleapis.com/v0/b/etkot-forum-f1c65.appspot.com/o/profile_images%2Fsolid-black.jpg?alt=media&token=a4b38035-0edc-4bb4-a5ec-dfce7e846e29");
                                    }
                                    postUsernameView.setText(username[0]);
                                    postProfileImageView = mView.findViewById(R.id.postUserCircleImageView);
                                    RequestOptions requestOptions = new RequestOptions();
                                    requestOptions.placeholder(R.drawable.ic_launcher_foreground);
                                    Glide.with(context).applyDefaultRequestOptions(requestOptions)
                                            .load(imageURI).into(postProfileImageView);
                                }
                            }
                        }
                    });
        }

        public void setPostDate(Date date) {

            postDateTextView = mView.findViewById(R.id.postDateTextView);
            postDateTextView.setText(date.toString().substring(0,19));
        }
    }
}
