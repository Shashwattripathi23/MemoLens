package com.shashwat.memolens;
import android.os.Build;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.graphics.Outline;
import android.os.Build;
import android.view.View;
import android.view.ViewOutlineProvider;
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
import android.os.Bundle;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;
import android.view.View;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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


    private View overlay;
    private ExecutorService cameraExecutor;

    private boolean isRecording = false;
    private Button btnStartVideo, btnStopVideo, btnRecordVideo;
    private LinearLayout layoutVideoControls;


    private boolean isTorchOn = false;
    private CameraManager cameraManager;
    private String cameraId;

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


    private void toggleInfo() {
        Intent intent = new Intent(this, InfoActivity.class);
        startActivity(intent);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progress_bar);
        overlay = findViewById(R.id.main_layout);

        findViewById(R.id.btn_open_gallery).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
            startActivity(intent);
        });
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        checkPermissions();

        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0];  // Use the back camera (index 0 is usually the back camera)
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        cameraExecutor = Executors.newSingleThreadExecutor();

        ImageButton info_btn = findViewById(R.id.btn_torch);
        info_btn.setOnClickListener(v -> toggleInfo());

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float radius = 30f;  // e.g., 30 pixels for corner radius, adjust as needed
            previewView.setClipToOutline(true);
            previewView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
                }
            });
        }

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

    private void analyzeFrame(ImageProxy image) {
        Bitmap bitmap = imageProxyToBitmap(image);
        if (bitmap != null) {
            // Calculate the average color
            int averageColor = calculateAverageColor(bitmap);

            // Apply the average color to the main layout
            runOnUiThread(() -> {
                if (overlay != null) {
                    int[] colors = {averageColor, Color.BLACK}; // Gradient from average color to black
                    GradientDrawable gradientDrawable = new GradientDrawable(
                            GradientDrawable.Orientation.TOP_BOTTOM, colors
                    );
                    gradientDrawable.setCornerRadius(0f);
                    overlay.setBackground(gradientDrawable);
                }
            });
        }
        image.close();
    }


    private int calculateAverageColor(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        long sumRed = 0, sumGreen = 0, sumBlue = 0;
        int totalPixels = width * height;

        // Iterate through all pixels and accumulate RGB values
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = bitmap.getPixel(x, y);

                sumRed += Color.red(pixel);
                sumGreen += Color.green(pixel);
                sumBlue += Color.blue(pixel);
            }
        }

        // Calculate average for each color channel
        int avgRed = (int) (sumRed / totalPixels);
        int avgGreen = (int) (sumGreen / totalPixels);
        int avgBlue = (int) (sumBlue / totalPixels);

        // Return the averaged color
        return Color.rgb(avgRed, avgGreen, avgBlue);
    }





    private Bitmap imageProxyToBitmap(ImageProxy image) {
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        int width = image.getWidth();
        int height = image.getHeight();

        // Use ARGB_8888 for correct color format
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Extract pixel data in YUV_420_888 format
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        byte[] yBytes = new byte[yBuffer.remaining()];
        byte[] uBytes = new byte[uBuffer.remaining()];
        byte[] vBytes = new byte[vBuffer.remaining()];

        yBuffer.get(yBytes);
        uBuffer.get(uBytes);
        vBuffer.get(vBytes);

        // Convert YUV to ARGB
        int[] argb = new int[width * height];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int yIndex = i * width + j;
                int uvIndex = (i / 2) * (width / 2) + (j / 2);

                int y = yBytes[yIndex] & 0xFF;
                int u = uBytes[uvIndex] & 0xFF;
                int v = vBytes[uvIndex] & 0xFF;

                // Convert YUV to RGB (simplified)
                int r = (int) (y + 1.370705 * (v - 128));
                int g = (int) (y - 0.337633 * (u - 128) - 0.698001 * (v - 128));
                int b = (int) (y + 1.732446 * (u - 128));

                // Clip to valid color range
                r = Math.min(255, Math.max(0, r));
                g = Math.min(255, Math.max(0, g));
                b = Math.min(255, Math.max(0, b));

                argb[yIndex] = 0xFF000000 | (r << 16) | (g << 8) | b;
            }
        }

        bitmap.setPixels(argb, 0, width, 0, 0, width, height);
        return bitmap;
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
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
