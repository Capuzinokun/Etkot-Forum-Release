package com.etkotsoftware.etkotforum;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    public Context context;
    public List<PostData> post_list;

    public PostAdapter(List<PostData> post_list) {
        this.post_list = post_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String description = post_list.get(position).getDescription();
        holder.setDescription(description);

        String postImageUrl = post_list.get(position).getImage_url();
        holder.setPostImage(postImageUrl);

        String user_id = post_list.get(position).getUser_id();
        Boolean is_anonymous = post_list.get(position).getIs_anonymous();
        holder.setPostUsernamePicture(user_id, is_anonymous);

        Date timestamp = post_list.get(position).getTimestamp();
        holder.setPostDate(timestamp);
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
        private ImageView postImageView;
        private ImageView postProfileImageView;
        private FirebaseFirestore firebaseFirestore;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

        }

        public void setDescription(String description) {

            descriptionView = mView.findViewById(R.id.postDescriptionTextView);
            descriptionView.setText(description);
        }

        public void setPostImage(String downloadUri) {

            postImageView = mView.findViewById(R.id.postItemImageView);
            Glide.with(context).load(downloadUri).into(postImageView);
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
