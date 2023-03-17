package com.example.socialapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Map;


public class ProfileFragment extends Fragment {
    NavController navController;   // <-----------------
    ImageView photoImageView;
    TextView displayNameTextView, emailTextView, authorTextView, likesTextView;
    public AppViewModel appViewModel;
    String uid;
    String nombre;
    int count;
    Query query;

    public ProfileFragment() {}

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);  // <-----------------
        photoImageView = view.findViewById(R.id.profile_image);
        displayNameTextView = view.findViewById(R.id.profile_name);
        emailTextView = view.findViewById(R.id.profile_email);
        likesTextView = view.findViewById(R.id.profile_likes);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null){
            displayNameTextView.setText(user.getDisplayName());
            emailTextView.setText(user.getEmail());
            Glide.with(requireView())
                    .load(user.getPhotoUrl())
                    .transform(new CircleCrop())
                    .into(photoImageView);
            uid = user.getUid();
        }

        if(user.getPhotoUrl() == null){
            Glide.with(requireView())
                    .load(R.drawable.profile)
                    .transform(new CircleCrop())
                    .into(photoImageView);

            String email = emailTextView.getText().toString();
            int indexArroba = email.indexOf("@");
            nombre = email.substring(0, indexArroba);
            displayNameTextView.setText(nombre);
            uid = user.getUid();
        }

        RecyclerView postsProfilesRecyclerView = view.findViewById(R.id.postsProfilesRecyclerView);

        query = FirebaseFirestore.getInstance().collection("posts").whereEqualTo("uid",uid).limit(50).orderBy("date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .setLifecycleOwner(this)
                .build();

        postsProfilesRecyclerView.setAdapter(new ProfileFragment.PostsAdapter(options));

        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    count = 0;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if(document.get("likes")==null){
                            break;
                        }
                        else {
                            Map<String, Boolean> likes = (Map<String, Boolean>) document.get("likes");
                            count += likes.size();
                        }
                    }
                    likesTextView.setText(count+"");

                }
            }
        });
    }

    class PostsAdapter extends FirestoreRecyclerAdapter<Post, PostsAdapter.PostViewHolder> {
        public PostsAdapter(@NonNull FirestoreRecyclerOptions<Post> options) {super(options);}
        @NonNull
        @Override
        public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PostViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_post_profile, parent, false));
        }

        @Override
        protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull final Post post) {
            if(post.author == null){
                holder.authorTextView.setText(nombre);
                Glide.with(requireView())
                        .load(R.drawable.profile)
                        .transform(new CircleCrop())
                        .into(holder.authorPhotoImageView);
            }

            else {
                Glide.with(getContext()).load(post.authorPhotoUrl).circleCrop().into(holder.authorPhotoImageView);
                holder.authorTextView.setText(post.author);
            }

            holder.contentTextView.setText(post.content);
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm - dd MMM");
            String formattedDate = dateFormat.format(post.date);
            holder.timeTextView.setText(formattedDate);

            final String postKey = getSnapshots().getSnapshot(position).getId();
            final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if(post.likes.containsKey(uid)) {
                holder.likeImageView.setImageResource(R.drawable.like_on);

            }
            else{
                holder.likeImageView.setImageResource(R.drawable.like);
            }
            holder.numLikesTextView.setText(String.valueOf(post.likes.size()));
            holder.likeImageView.setOnClickListener(view -> {
                FirebaseFirestore.getInstance().collection("posts")
                        .document(postKey)
                        .update("likes."+uid, post.likes.containsKey(uid) ?
                                FieldValue.delete() : true);
                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            count = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Boolean> likes = (Map<String, Boolean>) document.get("likes");
                                if (likes != null) {
                                    count += likes.size();
                                }
                            }
                            likesTextView.setText(count+"");
                        }
                    }
                });

            });

            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Obtener la referencia del post
                    final DocumentReference postRef = FirebaseFirestore.getInstance().collection("posts").document(postKey);
                    // Eliminar el post
                    postRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Eliminar los likes asociados al post
                            FirebaseFirestore.getInstance().collection("likes").whereEqualTo("postId", postKey)
                                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot like : task.getResult()) {
                                                    like.getReference().delete();
                                                }
                                                // Mostrar mensaje de éxito
                                                Toast.makeText(getContext(), "Post eliminado", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    });
                }
            });

            // Miniatura de media
            if (post.mediaUrl != null) {
                holder.mediaImageView.setVisibility(View.VISIBLE);
                if ("audio".equals(post.mediaType)) {
                    Glide.with(requireView()).load(R.drawable.audio).centerCrop().into(holder.mediaImageView);
                } else {
                    Glide.with(requireView()).load(post.mediaUrl).centerCrop().into(holder.mediaImageView);
                }
                holder.mediaImageView.setOnClickListener(view -> {
                    appViewModel.postSeleccionado.setValue(post);
                    navController.navigate(R.id.mediaFragment);
                });
            } else {
                holder.mediaImageView.setVisibility(View.GONE);
            }

        }
        class PostViewHolder extends RecyclerView.ViewHolder {
            ImageView authorPhotoImageView, likeImageView, mediaImageView, deleteButton;
            TextView authorTextView, contentTextView, numLikesTextView, timeTextView;

            PostViewHolder(@NonNull View itemView) {
                super(itemView);
                authorPhotoImageView = itemView.findViewById(R.id.photoImageView);
                authorTextView = itemView.findViewById(R.id.authorTextView);
                contentTextView = itemView.findViewById(R.id.contentTextView);
                likeImageView = itemView.findViewById(R.id.likeImageView);
                numLikesTextView = itemView.findViewById(R.id.numLikesTextView);
                mediaImageView = itemView.findViewById(R.id.mediaImage);
                timeTextView = itemView.findViewById(R.id.timeTexView);
                deleteButton = itemView.findViewById(R.id.deleteButton);
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
}