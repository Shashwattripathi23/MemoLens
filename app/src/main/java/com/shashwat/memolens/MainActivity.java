package com.shashwat.memolens;
import android.os.Build;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.util.List;
import android.provider.Settings;
import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.camera.core.AspectRatio;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.os.Bundle;
import android.widget.ProgressBar;
public class MainActivity extends AppCompatActivity {

//    private Camera/ previewView;
    private ImageCapture imageCapture;
    private CameraSelector cameraSelector;
    private boolean isBackCamera = true;  // Track the current camera
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;  // Move cameraProviderFuture to the class level
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_MICROPHONE_PERMISSION = 101;
    private static final int REQUEST_STORAGE_PERMISSION = 102;
    private static final int REQUEST_CODE = 101;
    // Method to get output file for the captured image
    private PreviewView previewView;
    private ProgressBar progressBar;

    private boolean isRecording = false;
    private Button btnStartVideo, btnStopVideo, btnRecordVideo;
    private LinearLayout layoutVideoControls;

    private File getOutputFile() {
        File dir = new File(getExternalFilesDir(null), "images");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, "IMG_" + System.currentTimeMillis() + ".jpg");
    }

    // Method to flip between front and back camera
    private void flipCamera() {
        // Toggle camera selection
        if (isBackCamera) {
            cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build();
        } else {
            cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build();
        }

        // Update the camera state
        isBackCamera = !isBackCamera;

        try {
            ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
            bindCameraUseCases(cameraProvider); // Pass only the cameraProvider as it is already using preview inside it
        } catch (ExecutionException | InterruptedException e) {
            Log.e("CameraX", "Error flipping camera", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start the camera
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_MICROPHONE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Microphone permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void checkPermissions() {
        // Check and request Camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            startCamera();
        }

        // Check and request Microphone permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MICROPHONE_PERMISSION);
        }

        // Check and request Storage permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progress_bar);

        findViewById(R.id.btn_open_gallery).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
            startActivity(intent);
        });
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        checkPermissions();

//        btnStartVideo = findViewById(R.id.btn_start_video);
//        btnStopVideo = findViewById(R.id.btn_stop_video);
//        btnRecordVideo = findViewById(R.id.btn_record_video);
//        layoutVideoControls = findViewById(R.id.layout_video_controls);
//
//// When user taps "Record", show start/stop buttons
//        btnRecordVideo.setOnClickListener(v -> {
//            layoutVideoControls.setVisibility(View.VISIBLE);
//            btnRecordVideo.setVisibility(View.GONE);
//        });
//
//// Start recording
//        btnStartVideo.setOnClickListener(v -> {
//            isRecording = true;
//            btnStartVideo.setEnabled(false);
//            btnStopVideo.setEnabled(true);
//
//            // TODO: Start actual video recording here
//            Toast.makeText(this, "Recording started...", Toast.LENGTH_SHORT).show();
//        });
//
//// Stop recording
//        btnStopVideo.setOnClickListener(v -> {
//            isRecording = false;
//            btnStartVideo.setEnabled(true);
//            btnStopVideo.setEnabled(false);
//
//            // TODO: Stop actual video recording here
//            Toast.makeText(this, "Recording stopped.", Toast.LENGTH_SHORT).show();
//        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 1);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }


        // Request microphone permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MICROPHONE_PERMISSION);
        }


        // Request storage permission if not granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            startCamera(); // If permission already granted, start the camera
        }

        previewView = findViewById(R.id.view_finder);

        // Request camera permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            startCamera();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }

        // Flip camera button
        ImageButton  btnFlip = findViewById(R.id.btn_flip_camera);
        btnFlip.setOnClickListener(v -> flipCamera());

        // Capture photo button
        ImageButton  btnCapture = findViewById(R.id.btn_capture_photo);
        btnCapture.setOnClickListener(v -> {
            if (imageCapture != null) {
                File photoFile = getOutputFile();
                progressBar.setVisibility(View.VISIBLE);
                ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

                imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                        new ImageCapture.OnImageSavedCallback() {
                            @Override
                            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                                Intent intent = new Intent(MainActivity.this, CaptionActivity.class);
                                intent.putExtra("image_path", photoFile.getAbsolutePath());
                                startActivity(intent);
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError(@NonNull ImageCaptureException exception) {
                                Toast.makeText(MainActivity.this, "Capture failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }


    // Method to start the camera
    private void startCamera() {
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA; // Default to back camera

        cameraProviderFuture = ProcessCameraProvider.getInstance(this); // Initialize the cameraProviderFuture at the class level
        cameraProviderFuture.addListener(() -> {
            try {
                // Get the camera provider
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Set up the Preview use case
                Preview preview = new Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        .build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Set up the ImageCapture use case
                imageCapture = new ImageCapture.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                        .build();

                // Bind the camera use cases
                bindCameraUseCases(cameraProvider); // Passing only cameraProvider as preview is part of it

            } catch (ExecutionException | InterruptedException e) {
                Log.e("CameraX", "Error starting camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // Method to bind camera use cases with parameters
    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        // Create a new preview instance here
        Preview preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Unbind previous use cases and bind the new use cases
        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);
    }
}
