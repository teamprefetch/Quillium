package com.quillium.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.quillium.Adapter.FollowersAdapter;
import com.quillium.Model.Follow;
import com.quillium.R;
import com.quillium.User;
import com.quillium.databinding.FragmentProfileBinding;
import com.quillium.utils.Constants;
import com.quillium.utils.PreferenceManager;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import android.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    ImageView coverPhoto, cPhoto, profile,profilePhoto;
    TextView name, id, followCount;
    FragmentProfileBinding binding;
    FirebaseStorage storage;
    FirebaseDatabase database;
    private Uri imageUri;
    DatabaseReference userRef, databaseReference;
    RecyclerView recyclerView;
    ArrayList<Follow> list;
    ProgressDialog dialog;
    String encodeImage;
    private PreferenceManager preferenceManager;


    private static final int PICK_IMAGE_REQUEST_PROFILE = 1;
    private static final int PICK_IMAGE_REQUEST_COVER = 2;
    private static final int RESULT_OK = -1; // or Activity.RESULT_OK



    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        dialog = new ProgressDialog(getContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        coverPhoto = view.findViewById(R.id.changeCoverPhoto);
        cPhoto = view.findViewById(R.id.coverPhoto);
        profile = view.findViewById(R.id.changeProfilePhoto);
        profilePhoto = view.findViewById(R.id.profile_picture_image);
        name = view.findViewById(R.id.username);
        id = view.findViewById(R.id.userId);
        recyclerView = view.findViewById(R.id.followersRV);
        followCount = view.findViewById(R.id.followers);

        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Profile photo is uploading");
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);

        list = new ArrayList<>();

        preferenceManager = new PreferenceManager(getActivity());


        FollowersAdapter adapter = new FollowersAdapter(list,getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        database.getReference().child("users")
                .child(FirebaseAuth.getInstance().getUid())
                .child("followers").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Follow follow = dataSnapshot.getValue(Follow.class);
                            list.add(follow);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        userRef = database.getReference("users").child(FirebaseAuth.getInstance().getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    String coverPhotoUrl = user.getCoverPhotoUrl();
                    String profilePhotoUrl = user.getProfilePhotoUrl();

                   // Load cover photo using Picasso or any other image loading library
                    Picasso.get()
                            .load(coverPhotoUrl)
                            .placeholder(R.drawable.cover_photo_placeholder)
                            .into(cPhoto);

                    // Load Profile photo using Picasso or any other image loading library
                    Picasso.get()
                            .load(profilePhotoUrl)
                            .placeholder(R.drawable.profile_photo_placeholder)
                            .into(profilePhoto);

                    String fullname = user.getFullname();
                    String email = user.getEmail();
                    String follows = String.valueOf(user.getFollowerCount());

                    // Set the fullname and email to the TextViews
                    name.setText(fullname);
                    id.setText(email);
                    followCount.setText(follows);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error if needed
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                pickImage.;
                openGalleryForProfile();
            }
        });

        coverPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGalleryForCoverPhoto();
            }
        });

        return view;
    }

    private void openGalleryForProfile() {
        // Open gallery for profile photo
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_PROFILE);
    }

    private void openGalleryForCoverPhoto() {
        // Open gallery for cover photo
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_COVER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            if (requestCode == PICK_IMAGE_REQUEST_PROFILE) {
                // Set the selected image to the profile photo
                profilePhoto.setImageURI(imageUri);

                dialog.show();

//                try {
//                    InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
//                    if (inputStream != null) {
//                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//                        if (bitmap != null) {
//                            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
//                            String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
//
//                            if (currentUserId != null) {
//                                HashMap<String, Object> updateData = new HashMap<>();
//                                // Encode the bitmap to a byte array if necessary
//                                // For now, let's just put the bitmap itself
//                                updateData.put(Constants.KEY_IMAGE, bitmap);
//
//                                firestore.collection(Constants.KEY_COLLECTION_USERS)
//                                        .document(currentUserId)
//                                        .update(updateData)
//                                        .addOnSuccessListener(aVoid -> {
//                                            // Profile photo uploaded successfully
//                                            dialog.dismiss(); // dismiss the dialog
//                                            Toast.makeText(getActivity(), "Profile photo uploaded to Firestore", Toast.LENGTH_SHORT).show();
//                                        })
//                                        .addOnFailureListener(e -> {
//                                            // Handle failure
//                                            dialog.dismiss(); // dismiss the dialog
//                                            Toast.makeText(getActivity(), "Failed to upload profile photo to Firestore", Toast.LENGTH_SHORT).show();
//                                        });
//                            } else {
//                                // Handle case where current user ID is null
//                                dialog.dismiss(); // dismiss the dialog
//                                Toast.makeText(getActivity(), "User ID not found", Toast.LENGTH_SHORT).show();
//                            }
//                        } else {
//                            // Bitmap is null, handle accordingly
//                            dialog.dismiss(); // dismiss the dialog
//                            Toast.makeText(getActivity(), "Failed to decode bitmap", Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        // InputStream is null, handle accordingly
//                        dialog.dismiss(); // dismiss the dialog
//                        Toast.makeText(getActivity(), "Failed to open input stream", Toast.LENGTH_SHORT).show();
//                    }
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                    dialog.dismiss();
//                    Toast.makeText(getActivity(), "Failed to open image", Toast.LENGTH_SHORT).show();
//                }

                // Upload the profile photo to Firebase if needed
                 uploadProfilePhotoToFirebase();
            } else if (requestCode == PICK_IMAGE_REQUEST_COVER) {
                // Set the selected image to the cover photo
                cPhoto.setImageURI(imageUri);

                dialog.show();

                // Upload the cover photo to Firebase if needed
                 uploadCoverPhotoToFirebase();
            }
        }
    }

//    private void uploadProfilePhotoToFirestore(Uri profilePhotoUri) {
//        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
//        String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
//
//        if (currentUserId != null) {
//            firestore.collection(Constants.KEY_COLLECTION_USERS)
//                    .document(currentUserId)
//                    .update(Constants.KEY_IMAGE, profilePhotoUri.toString())
//                    .addOnSuccessListener(aVoid -> {
//                        // Profile photo URI updated successfully
//                        Toast.makeText(getActivity(), "Profile photo uploaded to Firestore", Toast.LENGTH_SHORT).show();
//                    })
//                    .addOnFailureListener(e -> {
//                        // Handle failure
//                        Toast.makeText(getActivity(), "Failed to upload profile photo to Firestore", Toast.LENGTH_SHORT).show();
//                    });
//        } else {
//            // Handle case where current user ID is null
//            Toast.makeText(getActivity(), "User ID not found", Toast.LENGTH_SHORT).show();
//        }
//    }

//    private String encodeImage(Bitmap bitmap) {
//        int previewWidth = 150;
//        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
//        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
//        byte[] bytes = byteArrayOutputStream.toByteArray();
//        return Base64.encodeToString(bytes, Base64.DEFAULT);
//    }
//
//    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            result -> {
//                if(result.getResultCode() == RESULT_OK){
//                    if (result.getData() != null){
//                        Uri imageUri = result.getData().getData();
//                        try {
//                            InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
//                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
////                            binding.imageProfile.setImageBitmap(bitmap);
//                            encodeImage = encodeImage(bitmap);
//                        }
//                        catch (FileNotFoundException e){
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//    );

    private void uploadProfilePhotoToFirestore(Bitmap profilePhotoBitmap) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);

//        Toast.makeText(getActivity(), "UserId "+currentUserId, Toast.LENGTH_SHORT).show();

        if (currentUserId != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            profilePhotoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageData = baos.toByteArray();
            String encodedImage = Base64.encodeToString(imageData, Base64.DEFAULT);

            HashMap<String, Object> updateData = new HashMap<>();
            updateData.put(Constants.KEY_IMAGE, encodedImage);

            firestore.collection(Constants.KEY_COLLECTION_USERS)
                    .document(currentUserId)
                    .update(updateData)
                    .addOnSuccessListener(aVoid -> {
                        // Profile photo uploaded successfully
                        dialog.dismiss(); // dismiss the dialog
                        Toast.makeText(getActivity(), "Profile photo uploaded to Firestore", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                        dialog.dismiss(); // dismiss the dialog
                        Toast.makeText(getActivity(), "Failed to upload profile photo to Firestore", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Handle case where current user ID is null
            dialog.dismiss(); // dismiss the dialog
            Toast.makeText(getActivity(), "User ID not found", Toast.LENGTH_SHORT).show();
        }
    }





    private void uploadCoverPhotoToFirebase() {
        if (imageUri != null) {
            final StorageReference reference = storage.getReference().child("cover_photo")
                    .child(FirebaseAuth.getInstance().getUid());

            reference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Image uploaded successfully
//                            Toast.makeText(getActivity(), "Image uploaded successfully", Toast.LENGTH_SHORT).show();

                            // Get the download URL of the uploaded image
                            reference.getDownloadUrl().addOnSuccessListener(uri -> {
                                // Update user data in the database with the image URL
                                updateCoverPhotoInDatabase(uri.toString());
                            });
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors that occurred during the upload
//                        Toast.makeText(getActivity(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });

        }
    }
    private void updateCoverPhotoInDatabase(String imageUrl) {
        // Get the reference to the user's data in the database
        userRef = database.getReference("users").child(FirebaseAuth.getInstance().getUid());

        // Create a Map to update the user's data
        Map<String, Object> updates = new HashMap<>();
        updates.put("coverPhotoUrl", imageUrl);

        // Update the user's data in the database
        userRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    // Database update successful
                    Toast.makeText(getActivity(), "Cover Photo uploaded successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that occurred during the database update
                    Toast.makeText(getActivity(), "Failed to upload Cover Photo", Toast.LENGTH_SHORT).show();
                });

        dialog.dismiss();
    }

    private void uploadProfilePhotoToFirebase() {
        if (imageUri != null) {
            final StorageReference reference = storage.getReference().child("profile_photo")
                    .child(FirebaseAuth.getInstance().getUid());

            reference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
//                        Toast.makeText(getActivity(), "Profile Photo uploaded successfully", Toast.LENGTH_SHORT).show();

                        // Get the download URL of the uploaded image
                        reference.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Update user data in the database with the profile photo URL
                            updateProfilePhotoInDatabase(uri.toString());
                        });
                    })
                    .addOnFailureListener(e -> {
//                        Toast.makeText(getActivity(), "Failed to upload profile photo", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateProfilePhotoInDatabase(String imageUrl) {
        userRef = database.getReference("users").child(FirebaseAuth.getInstance().getUid());

        Map<String, Object> updates = new HashMap<>();
        updates.put("profilePhotoUrl", imageUrl);

        userRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(getActivity(), "Profile Photo uploaded successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to upload Profile Photo", Toast.LENGTH_SHORT).show());

        dialog.dismiss();
    }
}