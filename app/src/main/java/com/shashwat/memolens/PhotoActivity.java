package com.shashwat.memolens;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.ayush.imagesteganographylibrary.Text.AsyncTaskCallback.TextDecodingCallback;
import com.ayush.imagesteganographylibrary.Text.ImageSteganography;
import com.ayush.imagesteganographylibrary.Text.TextDecoding;
import com.bumptech.glide.Glide;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.graphics.Bitmap;

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


        ImageView fullImageView = findViewById(R.id.fullImageView);
        decodedMessageView = findViewById(R.id.decodedMessage);
        decodedDateView = findViewById(R.id.captureDate);
        ImageView deleteIcon = findViewById(R.id.deleteIcon);


        String captureDate = getIntent().getStringExtra("captureDate");
        Log.d("PhotoActivity", "Capture Date: " + captureDate);
        decodedDateView.setText(captureDate);
        // Get the image path from the intent
        String imagePath = getIntent().getStringExtra("imagePath");

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
