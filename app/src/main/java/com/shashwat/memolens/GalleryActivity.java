package com.shashwat.memolens;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.view.View;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.content.Intent;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GalleryActivity extends AppCompatActivity {
    private static final String TAG = "GalleryActivity";

    private RecyclerView recyclerView;
    private ProgressBar loader;
    private final List<Bitmap> imageList = new ArrayList<>();
    private final List<Bitmap> decodedImages = new ArrayList<>();
    private int pendingImages = 0;

    // Declare the ActivityResultLauncher
    private ActivityResultLauncher<Intent> getContent;


    private File getFileFromUri(Uri uri) {
        String path = null;
        if (uri.getScheme().equals("content")) {
            // If the URI is from content provider (like gallery)
            String[] projection = { android.provider.MediaStore.Images.Media.DATA };
            try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(projection[0]);
                    path = cursor.getString(columnIndex);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (uri.getScheme().equals("file")) {
            // If it's a file URI
            path = uri.getPath();
        }

        if (path != null) {
            return new File(path);
        }
        return null;  // Return null if unable to resolve
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageView backIcon = findViewById(R.id.btn_back);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Just finish the current activity to go back
                finish();
            }
        });

        // Initialize the ActivityResultLauncher
        getContent = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri selectedImageUri = data.getData();

                            // Resolve the Uri to a real file path
                            File selectedImageFile = getFileFromUri(selectedImageUri);

                            if (selectedImageFile != null) {
                                // Start PhotoActivity with the selected image File
                                Intent intent = new Intent(this, PhotoActivity.class);
                                intent.putExtra("imagePath", selectedImageFile.getAbsolutePath());
                                String captureDate = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date(selectedImageFile.lastModified()));
                                intent.putExtra("captureDate", captureDate);
                                startActivity(intent);
                            } else {
                                Toast.makeText(this, "Failed to retrieve image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        // Open gallery when the gallery button is clicked
        ImageButton btnOpenGallery = findViewById(R.id.btn_gallery);
        btnOpenGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            getContent.launch(intent); // Launch gallery activity
        });

        // Set up RecyclerView for displaying images
        loader = findViewById(R.id.loader);
        recyclerView = findViewById(R.id.image_grid);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        // Folder where images are stored
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

        // Display images in RecyclerView
        if (files != null && files.length > 0) {
            recyclerView.setAdapter(new ImageAdapter(this, files));
            loader.setVisibility(View.GONE); // Hide loader
            recyclerView.setVisibility(View.VISIBLE); // Show RecyclerView
        } else {
            Toast.makeText(this, "No images found", Toast.LENGTH_SHORT).show();
        }
    }
}
