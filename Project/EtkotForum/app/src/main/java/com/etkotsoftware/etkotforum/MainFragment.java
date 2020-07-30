package com.etkotsoftware.etkotforum;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {

    private Boolean sort_by_liked = false;

    private RecyclerView post_view_recycler;
    private PostAdapter postAdapter;

    private List<PostData> post_list;
    private Boolean isPrimaryLoad = true;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private DocumentSnapshot lastPost;

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        firebaseAuth = FirebaseAuth.getInstance();

        post_list = new ArrayList<>();
        post_view_recycler = (RecyclerView) view.findViewById(R.id.post_view_recycler);
        postAdapter = new PostAdapter(post_list);
        post_view_recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        post_view_recycler.setAdapter(postAdapter);

        if (firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            post_view_recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean is_bottom = !recyclerView.canScrollVertically(1);

                    if (is_bottom) {
                        if (sort_by_liked) {
                            nextPostsByLiked();
                        }
                        else {
                            nextPosts();
                        }
                    }
                }
            });

            if (sort_by_liked) {
                nextPostsByLiked();
            }
            else {
                Query firstPosts = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(4);
                firstPosts.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                        if (value == null) {
                            return;
                        }

                        if (isPrimaryLoad) {

                            lastPost = value.getDocuments()
                                    .get(value.size() - 1);
                        }

                        for (DocumentChange doc : value.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String postId = doc.getDocument().getId();
                                PostData postData = doc.getDocument()
                                        .toObject(PostData.class).withId(postId);

                                if (isPrimaryLoad) {

                                    post_list.add(postData);
                                } else {

                                    post_list.add(0, postData);
                                }
                                postAdapter.notifyDataSetChanged();
                            }
                        }

                        isPrimaryLoad = false;
                    }
                });
            }
        }

        // Inflate the layout for this fragment
        return view;
    }

    public void nextPosts() {

        Query nextPosts = firebaseFirestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING).limit(4)
                .startAfter(lastPost)
                .limit(4);

        nextPosts.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                if (value == null) {
                    return;
                }

                else if (!value.isEmpty()) {

                    Toast.makeText(getContext(), "Loading more posts...", Toast.LENGTH_SHORT).show();

                    lastPost = value.getDocuments()
                            .get(value.size() - 1);

                    for (DocumentChange doc : value.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String postId = doc.getDocument().getId();

                            PostData postData = doc.getDocument()
                                    .toObject(PostData.class).withId(postId);
                            post_list.add(postData);

                            postAdapter.notifyDataSetChanged();
                        }
                    }
                }
                else {
                    Toast.makeText(getContext(), "Reached bottom!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void nextPostsByLiked() {
        // TODO: Implement
    }
}