package com.shashwat.memolens;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.Shashwat.imagesteganographylibrary.Text.AsyncTaskCallback.TextDecodingCallback;
import com.Shashwat.imagesteganographylibrary.Text.ImageSteganography;
import com.Shashwat.imagesteganographylibrary.Text.TextDecoding;
import com.bumptech.glide.Glide;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.graphics.Bitmap;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;

public class PhotoActivity extends AppCompatActivity implements TextDecodingCallback {

    private static final String TAG = "PhotoActivity";
    private TextView decodedMessageView;
    private TextView decodedDateView;
    private File currentImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_photo);
        ImageView backIcon = findViewById(R.id.btn_back);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Just finish the current activity to go back
                finish();
            }
        });

        ImageView shareIcon = findViewById(R.id.shareIcon);




        ImageView fullImageView = findViewById(R.id.fullImageView);
        decodedMessageView = findViewById(R.id.decodedMessage);
        decodedDateView = findViewById(R.id.captureDate);
        ImageView deleteIcon = findViewById(R.id.deleteIcon);


        String captureDate = getIntent().getStringExtra("captureDate");
        Log.d("PhotoActivity", "Capture Date: " + captureDate);
        decodedDateView.setText(captureDate);
        // Get the image path from the intent
        String imagePath = getIntent().getStringExtra("imagePath");

        shareIcon.setOnClickListener(v -> shareImage(currentImageFile));

        if (imagePath != null) {
            currentImageFile = new File(imagePath);
            decode(currentImageFile);
            Glide.with(this)
                    .load(currentImageFile)
                    .into(fullImageView);
        }

        // Set up delete icon click listener
        deleteIcon.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void shareImage(File imageFile) {
        try {
            Uri imageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/*");  // Set MIME type as application/*
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Share Document"));
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Failed to share document", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    private void decode(File file){
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        ImageSteganography steganographyDecoder = new ImageSteganography("Shashwat", bitmap);
        TextDecoding textDecoding = new TextDecoding(PhotoActivity.this, PhotoActivity.this);
        textDecoding.execute(steganographyDecoder);
    }

    @Override
    public void onStartTextEncoding() {
        Log.d(TAG, "Decoding started...");
    }

    @Override
    public void onCompleteTextEncoding(ImageSteganography result) {
        if (result != null && result.isDecoded() && result.getMessage() != null) {
            Log.d(TAG, "Decoded Message: " + result.getMessage());
            decodedMessageView.setText(result.getMessage());
        } else {
            Log.e(TAG, "Decryption failed or message is null");
            decodedMessageView.setText("No hidden message found.");
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete this image?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (currentImageFile != null && currentImageFile.delete()) {
                        Toast.makeText(this, "Image deleted successfully.", Toast.LENGTH_SHORT).show();
                        finish();  // Close the activity after deleting
                    } else {
                        Toast.makeText(this, "Failed to delete image.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
