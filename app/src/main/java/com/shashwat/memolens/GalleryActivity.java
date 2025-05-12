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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {
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
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

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
            File file = new File(dir, name);
            return file.isFile() && (name.endsWith(".jpg") || name.endsWith(".png")) && !name.startsWith(".");
        });

        if (files != null && files.length > 0) {
            recyclerView.setAdapter(new ImageAdapter(this, files));
            loader.setVisibility(View.GONE); // Hide loader after setting the adapter
            recyclerView.setVisibility(View.VISIBLE); // Make the RecyclerView visible
        } else {
            Toast.makeText(this, "No images found", Toast.LENGTH_SHORT).show();
        }
    }
}