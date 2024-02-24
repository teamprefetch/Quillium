package com.quillium;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.quillium.databinding.ActivityMessengerHomePageBinding;
import com.quillium.utils.Constants;
import com.quillium.utils.PreferenceManager;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessengerHomePage extends AppCompatActivity {

    private ActivityMessengerHomePageBinding binding;
    DatabaseReference userRef;
    FirebaseDatabase database;
    private PreferenceManager preferenceManager;
    FloatingActionButton fab;
    CircleImageView imageProfile;
    TextView fullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessengerHomePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();

        preferenceManager = new PreferenceManager(getApplicationContext());

        fab = findViewById(R.id.fabNewChat);
        imageProfile = findViewById(R.id.messageImageViewID);
        fullName = findViewById(R.id.chatsTextID);

        loadUserData();
        getToken();

        // Set a click listener on the FAB
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MessengerUsersActivity.class);
                startActivity(intent);
            }
        });
    }

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);
        if (userId != null) {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            DocumentReference documentReference = firestore.collection(Constants.KEY_COLLECTION_USERS).document(userId);
            documentReference.update(Constants.KEY_FCM_TOKEN, token)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(getApplicationContext(), "Token Update Successfully "+userId, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Token Update Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Handle the case where the user ID is null
            Toast.makeText(getApplicationContext(), "User ID is null", Toast.LENGTH_SHORT).show();
        }
    }

//    private void loadUserData() {
////        userRef = database.getReference("users").child(FirebaseAuth.getInstance().getUid());
////        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
////            @Override
////            public void onDataChange(@NonNull DataSnapshot snapshot) {
////                if (snapshot.exists()) {
////                    User user = snapshot.getValue(User.class);
////                    String profilePhotoUrl = user.getProfilePhotoUrl();
////
////                    // Load Profile photo using Picasso or any other image loading library
////                    Picasso.get()
////                            .load(profilePhotoUrl)
////                            .placeholder(R.drawable.profile_photo_placeholder)
////                            .into(binding.messageImageViewID);
////
////                    String fullname = user.getFullname();
////                    String email = user.getEmail();
////
////                    // Set the fullname and email to the TextViews
////                    binding.chatsTextID.setText(fullname);
////                }
////            }
////
////            @Override
////            public void onCancelled(@NonNull DatabaseError error) {
////                // Handle database error if needed
////            }
////        });
//
//
//        fullName.setText(preferenceManager.getString(Constants.KEY_NAME));
//        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
//        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
//        imageProfile.setImageBitmap(bitmap);
//    }

    private void loadUserData() {
        fullName.setText(preferenceManager.getString(Constants.KEY_NAME));

        String base64Image = preferenceManager.getString(Constants.KEY_IMAGE);
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] bytes = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap != null) {
                    imageProfile.setImageBitmap(bitmap);
                } else {
                    // Handle case where bitmap decoding failed
                    imageProfile.setImageResource(R.drawable.man); // or any other default image
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                // Handle invalid Base64 string
            }
        } else {
            // Handle case where base64Image is null or empty
            imageProfile.setImageResource(R.drawable.man); // or any other default image
        }
    }

}