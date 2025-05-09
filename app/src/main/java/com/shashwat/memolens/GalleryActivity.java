package com.shashwat.memolens;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ayush.imagesteganographylibrary.Text.AsyncTaskCallback.TextDecodingCallback;
import com.ayush.imagesteganographylibrary.Text.ImageSteganography;
import com.ayush.imagesteganographylibrary.Text.TextDecoding;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity implements TextDecodingCallback {
    private static final String TAG = "GalleryActivity";

    private RecyclerView recyclerView;
    private ProgressBar loader;
    private final List<Bitmap> imageList = new ArrayList<>();
    private final List<Bitmap> decodedImages = new ArrayList<>();
    private int pendingImages = 0;


    private Bitmap decodeSampledBitmapFromFile(String filePath, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        loader = findViewById(R.id.loader);
        recyclerView = findViewById(R.id.image_grid);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));


        // Folder where images are stored (using getExternalFilesDir for API level 29 and higher)
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MemoLens");

        // Log the folder path
        Log.d(TAG, "Folder path: " + folder.getAbsolutePath());

        // Check if the folder exists
        if (!folder.exists() || !folder.isDirectory()) {
            Toast.makeText(this, "Folder does not exist", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get all image files from the folder
        File[] files = folder.listFiles((dir, name) -> {
            // Ensure the file is not a directory and is an image file
            File file = new File(dir, name);
            return file.isFile() && (name.endsWith(".jpg") || name.endsWith(".png")) && !name.startsWith(".");
        });

        recyclerView.setAdapter(null);
        recyclerView.setAdapter(new ImageAdapter(files));

        // Check and log valid image files
        if (files != null && files.length > 0) {
            Log.d(TAG, "Fetched files:");
            for (File file : files) {
                // Check if the file exists and is readable
                if (file.exists() && file.canRead()) {
                    Log.d(TAG, "File: " + file.getAbsolutePath());

                    pendingImages++;
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    imageList.add(bitmap);  // Add bitmap to the list
                    if (bitmap == null) {
                        Log.e(TAG, "Decoded bitmap is null for file: " + file.getAbsolutePath());
                    }

                } else {
                    Log.d(TAG, "Skipping deleted or unreadable file: " + file.getAbsolutePath());
                }
            }

            // After loading images, start decoding them
            decodeImages();
        } else {
            Log.d(TAG, "No image files found in folder.");
            Toast.makeText(this, "No images found", Toast.LENGTH_SHORT).show();
        }
    }

    // This function decodes the images one by one
    private void decodeImages() {
        for (Bitmap bitmap : imageList) {
            ImageSteganography steganographyDecoder = new ImageSteganography("Shashwat", bitmap);
            TextDecoding textDecoding = new TextDecoding(GalleryActivity.this, GalleryActivity.this);
            textDecoding.execute(steganographyDecoder);
            if (bitmap == null) {
                Log.e(TAG, "Decoded bitmap is null for file: " );
            }
        }
    }


    public void onCompleteTextEncoding(ImageSteganography result) {
        if (result != null && result.isDecoded() && result.getMessage() != null) {
            decodedImages.add(result.getImage());
            Log.d("Decoded_Message", result.getMessage());
        } else {
            Log.e("Decode_Failed", "Decryption failed or message is null");
        }

        pendingImages--;
        if (pendingImages == 0) {
            runOnUiThread(() -> {
                loader.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            });
        }
    }

    @Override
    public void onStartTextEncoding() {
        // No action needed here for now
    }
}