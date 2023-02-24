package com.example.socialapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class ProfileFragment extends Fragment {

    NavController navController;   // <-----------------
    ImageView photoImageView;
    TextView displayNameTextView, emailTextView, authorTextView, likesTextView;
    String uid;
    String nombre;
    public ProfileFragment() {}
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);  // <-----------------
        photoImageView = view.findViewById(R.id.photoImageView);
        displayNameTextView = view.findViewById(R.id.displayNameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);

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
                    .load(R.drawable.anonymo)
                    .transform(new CircleCrop())
                    .into(photoImageView);

            String email = emailTextView.getText().toString();
            int indexArroba = email.indexOf("@");
            nombre = email.substring(0, indexArroba);
            displayNameTextView.setText(nombre);
            uid = user.getUid();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
}