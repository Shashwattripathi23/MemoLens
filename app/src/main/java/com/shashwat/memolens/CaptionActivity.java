package com.shashwat.memolens;

import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.Gravity;
import android.view.ViewGroup;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;
import java.io.IOException;
import java.io.File;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.SeekBar;
import java.io.FileOutputStream;
import android.widget.EditText;
import android.widget.Button;
import android.content.Intent;

import com.Shashwat.imagesteganographylibrary.Text.AsyncTaskCallback.TextEncodingCallback;
import com.Shashwat.imagesteganographylibrary.Text.AsyncTaskCallback.TextDecodingCallback;
import com.Shashwat.imagesteganographylibrary.Text.ImageSteganography;
import com.Shashwat.imagesteganographylibrary.Text.TextDecoding;
import com.Shashwat.imagesteganographylibrary.Text.TextEncoding;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CaptionActivity extends AppCompatActivity implements TextEncodingCallback , TextDecodingCallback{

    private MediaRecorder recorder;
    private String audioFilePath;
    private LinearLayout voiceContainer;
    private VideoView videoView;
    private ImageButton playButton;
    private Bitmap encoded_image;

    public interface TextEncodingCallback {

        public void onStart() ;
        public void onProgress(int progress);

    }

    public void onCompleteTextEncoding(ImageSteganography result) {

        //By the end of


            Log.d("MemoLenss", "ImageSteganography result:");
            Log.d("MemoLenss", "isEncoded: " + result.isEncoded());
            Log.d("MemoLenss", "Encoded Image: " + (result.getEncoded_image() != null ? "Available" : "Null"));
            Log.d("MemoLenss", "Original Message: " + result.getMessage());
            Log.d("MemoLenss", "Encrypted Message: " + result.getEncrypted_message());



        if (result != null && result.isEncoded()) {
            encoded_image = result.getEncoded_image();
            final Bitmap imgToSave = encoded_image;
            saveToInternalStorage(imgToSave);
        }
    }

    public interface TextDecodingCallback {

        public void onStart() ;
        public void onProgress(int progress);
        public void onCompleteTextEncoding(ImageSteganography result);
    }



    private ImageSteganography imageSteganography;

    private void startRecording() {
        try {
            File audioFile = new File(getExternalFilesDir(null), "voice_" + System.currentTimeMillis() + ".3gp");
            audioFilePath = audioFile.getAbsolutePath();

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setOutputFile(audioFilePath);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.prepare();
            recorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        if (recorder != null) {
            try {
                recorder.stop();
                recorder.release();
                recorder = null;
                showAudioPlayer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap getRotatedBitmap(String imagePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Matrix matrix = new Matrix();

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
            }

            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private void showAudioPlayer() {
        LinearLayout playerLayout = new LinearLayout(this);
        playerLayout.setOrientation(LinearLayout.HORIZONTAL);
        playerLayout.setGravity(Gravity.CENTER_VERTICAL);

        Button playPauseBtn = new Button(this);
        playPauseBtn.setText("Play");

        SeekBar seekBar = new SeekBar(this);
        seekBar.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
        ));

        MediaPlayer player = new MediaPlayer();

        try {
            player.setDataSource(audioFilePath);
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        seekBar.setMax(player.getDuration());

        playPauseBtn.setOnClickListener(v -> {
            if (player.isPlaying()) {
                player.pause();
                playPauseBtn.setText("Play");
            } else {
                player.start();
                playPauseBtn.setText("Pause");
            }
        });

        new Thread(() -> {
            while (player != null && player.getCurrentPosition() < player.getDuration()) {
                seekBar.setProgress(player.getCurrentPosition());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) player.seekTo(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

//        playerLayout.addView(playPauseBtn);
//        playerLayout.addView(seekBar);
//        voiceContainer.addView(playerLayout);
    }

    private void saveImageToGallery(Bitmap bitmap) {
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MemoLens");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String filename = "IMG_" + System.currentTimeMillis() + ".jpg";
        File imageFile = new File(folder, filename);

        try (FileOutputStream out = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));
            Log.d("MemoLens", "Image saved to gallery: " + imageFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("MemoLens", "Failed to save image");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        setContentView(R.layout.activity_caption);

        String imagePath = getIntent().getStringExtra("image_path");
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(getRotatedBitmap(imagePath));

        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                0.15f
        );
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setLayoutParams(imageParams);

        EditText captionInput = new EditText(this);
        captionInput.setHint("Add your caption here...");
        captionInput.setTextSize(16);
        captionInput.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Button btnRecord = new Button(this);
        btnRecord.setText("Hold to Record Voice Note");

        btnRecord.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startRecording();
                    return true;
                case MotionEvent.ACTION_UP:
                    stopRecording();
                    return true;
            }
            return false;
        });

        Button saveButton = new Button(this);
        saveButton.setText("Save");
        saveButton.setOnClickListener(v -> {
            String caption = captionInput.getText().toString();
            Bitmap originalBitmap = getRotatedBitmap(imagePath);

            // Initialize ImageSteganography with caption and image
            imageSteganography = new ImageSteganography(caption, "Shashwat", originalBitmap);

            // Initialize TextEncoding and encode with the callback
            TextEncoding textEncoding = new TextEncoding(CaptionActivity.this, CaptionActivity.this);
            textEncoding.encode(imageSteganography);  // Ensure encoding process triggers correctly

        });

//        voiceContainer = new LinearLayout(this);
//        voiceContainer.setOrientation(LinearLayout.VERTICAL);
//        voiceContainer.setId(R.id.voice_note_container);
//        voiceContainer.setLayoutParams(new LinearLayout.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
//        ));

        layout.addView(imageView);
        layout.addView(captionInput);
//        layout.addView(btnRecord);
//        layout.addView(voiceContainer);
        layout.addView(saveButton);
        setContentView(layout);
    }

    public void decodeImage(Bitmap stegoBitmap, String secretKey) {
        // Initialize the ImageSteganography instance for decoding
        ImageSteganography steganographyDecoder = new ImageSteganography(secretKey, stegoBitmap);

        // Assuming the image contains the encrypted message, now decrypt it
        TextDecoding textDecoding =  new TextDecoding(CaptionActivity.this,CaptionActivity.this);

        textDecoding.execute(steganographyDecoder);

//        if (extractedText != null && !extractedText.isEmpty()) {
//            // Display the extracted text
//            Toast.makeText(this, extractedText, Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(this, extractedText, Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    public void onStartTextEncoding() {
        // Optional loading UI can be added here
    }

    private void saveToInternalStorage(Bitmap bitmapImage) {
        // 1. Create the MemoLens directory in Pictures
        File folder = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MemoLens"
        );

        if (!folder.exists()) {
            folder.mkdirs(); // Create folder if it doesn't exist
        }

        // 2. Create the file
        String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault()).format(new Date());
        File file = new File(folder, "Memolens-" + timestamp + ".png");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            // 3. Save the Bitmap
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();

            // 4. Notify the MediaScanner to make it visible in Gallery
            MediaScannerConnection.scanFile(
                    getApplicationContext(),
                    new String[]{file.getAbsolutePath()},
                    new String[]{"image/png"},
                    null
            );

            // 5. Show success message
            Toast.makeText(this, "Image saved to Pictures/MemoLens", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }

    }


}
