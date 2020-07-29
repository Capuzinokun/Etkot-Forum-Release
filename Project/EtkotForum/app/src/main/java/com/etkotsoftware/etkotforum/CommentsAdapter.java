package com.etkotsoftware.etkotforum;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder>{

    public List<Comments> commentsList;
    public Context context;
    private FirebaseFirestore firebaseFirestore;

    public CommentsAdapter(List<Comments> commentsList) {

        this.commentsList = commentsList;
    }

    @Override
    public CommentsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();

        return new CommentsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CommentsAdapter.ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        String comment_description = commentsList.get(position).getDescription();
        holder.setCommentDescription(comment_description);

        final String user_id = commentsList.get(position).getUser_id();
        Boolean is_anonymous = commentsList.get(position).getIs_anonymous();
        holder.setCommendUsernamePicture(user_id, is_anonymous);

        Date timestamp = commentsList.get(position).getTimestamp();
        holder.setCommentDate(timestamp);
    }

    @Override
    public int getItemCount() {

        if (commentsList != null) {

            return commentsList.size();
        }
        else {
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setCommentDescription(String description) {

            TextView comment_description = mView.findViewById(R.id.commentDescriptionTextView);
            comment_description.setText(description);
        }

        public void setCommentDate(Date timestamp) {
            TextView commentDateTextView = mView.findViewById(R.id.commentDateTextView);
            try {
                commentDateTextView.setText(timestamp.toString().substring(0, 19));
            }
            catch (Exception e) {
                commentDateTextView.setText("Failed to fetch date/time");
            }
        }

        public void setCommendUsernamePicture(String user_id, final Boolean is_anonymous) {

            final TextView commentUsernameView = (TextView) mView
                    .findViewById(R.id.commentUsernameTextView);
            final CircleImageView commentImageView = (CircleImageView) mView
                    .findViewById(R.id.commentUserCircleImageView);
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

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
                                    commentUsernameView.setText(username[0]);
                                    RequestOptions requestOptions = new RequestOptions();
                                    requestOptions.placeholder(R.drawable.ic_launcher_foreground);
                                    Glide.with(context).applyDefaultRequestOptions(requestOptions)
                                            .load(imageURI).into(commentImageView);
                                }
                            }
                        }
                    });
        }


    }
}
