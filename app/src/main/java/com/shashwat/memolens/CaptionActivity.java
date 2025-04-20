package com.shashwat.memolens;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.widget.Toast;
import android.util.Base64;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;
import java.io.IOException;
import java.io.File;
import android.media.MediaPlayer;
import android.media.MediaRecorder;import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import android.os.Environment;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Button;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.widget.SeekBar;
import android.os.Handler;
import android.util.Log;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;



public class CaptionActivity extends AppCompatActivity {

    private MediaRecorder recorder;
    private String audioFilePath;
    private LinearLayout voiceContainer;
    private VideoView videoView;
    private ImageButton playButton;


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

    private static String decryptMessage(String encryptedData, String password) throws Exception {
        byte[] combined = Base64.decode(encryptedData, Base64.DEFAULT);
        byte[] iv = new byte[16];
        byte[] encrypted = new byte[combined.length - 16];
        System.arraycopy(combined, 0, iv, 0, 16);
        System.arraycopy(combined, 16, encrypted, 0, encrypted.length);

        SecretKeySpec key = generateKey(password);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, "UTF-8");
    }
    private static SecretKeySpec generateKey(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = password.getBytes("UTF-8");
        byte[] hashedKey = digest.digest(keyBytes);
        return new SecretKeySpec(hashedKey, "AES");
    }
    private static String encryptMessage(String message, String password) throws Exception {
        SecretKeySpec key = generateKey(password);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); // Better mode
        byte[] iv = new byte[16]; // Random IV for CBC mode
        new SecureRandom().nextBytes(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] encrypted = cipher.doFinal(message.getBytes("UTF-8"));

        // Prepend IV to the encrypted data (needed for decryption)
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.encodeToString(combined, Base64.DEFAULT);
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

    private static Bitmap embedTextSteganography(Bitmap originalBitmap, String message, String password) throws Exception {
        // 1. Encrypt the message with AES (CBC mode + IV for better security)
        String encryptedMessage = encryptMessage(message, password);

        // 2. Generate a random 3-byte end marker (less predictable)
        String endMarker = new String(new char[] {
                (char)(new SecureRandom().nextInt(256)),
                (char)(new SecureRandom().nextInt(256)),
                (char)(new SecureRandom().nextInt(256))
        });
        encryptedMessage += endMarker;

        // 3. Convert the encrypted message (+ end marker) to binary
        StringBuilder binaryMessage = new StringBuilder();
        for (char c : encryptedMessage.toCharArray()) {
            String bin = String.format("%8s", Integer.toBinaryString(c & 0xFF)).replace(' ', '0');
            binaryMessage.append(bin);
        }

        // 4. Create a mutable copy of the original bitmap
        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // 5. Check if the message fits in the image (1 bit per pixel)
        if (binaryMessage.length() > width * height) {
            throw new IllegalArgumentException("Message too long to fit in the image.");
        }

        // 6. Embed the binary message into the LSB of the BLUE channel
        int messageIndex = 0;
        outerLoop:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (messageIndex >= binaryMessage.length()) break outerLoop;

                int pixel = bitmap.getPixel(x, y);

                // Extract ARGB components
                int a = (pixel >> 24) & 0xff;
                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;

                // Modify only the LSB of the blue channel
                b = (b & 0xFE) | (binaryMessage.charAt(messageIndex++) - '0');

                // Reconstruct the pixel
                int newPixel = (a << 24) | (r << 16) | (g << 8) | b;
                bitmap.setPixel(x, y, newPixel);
            }
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

        // Update seek bar as audio plays
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

        // Allow seeking
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) player.seekTo(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        playerLayout.addView(playPauseBtn);
        playerLayout.addView(seekBar);

        voiceContainer.addView(playerLayout);
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

        LinearLayout layout = new LinearLayout(this);

        setContentView(R.layout.activity_caption);

//        ImageView polaroidImage = findViewById(R.id.polaroid_image); // Assuming you have a corresponding ImageView in the XML layout
//        if (polaroidImage != null) {
//            polaroidImage.setImageResource(R.drawable.polaroid); // Load the image from res/drawable folder
//            Log.d("Polaroid", "Image loaded successfully");
//        } else {
//            Log.d("Polaroid", "Image failed to load");
//        }

//        videoView = findViewById(R.id.video_view);
//        playButton = findViewById(R.id.btn_play);
//
//        String videoPath = getIntent().getStringExtra("video_path");
//        if (videoPath != null) {
//            videoView.setVideoURI(Uri.parse(videoPath));
//        }
//
//        playButton.setOnClickListener(v -> {
//            videoView.start();
//            playButton.setVisibility(View.GONE);
//        });
//
//        videoView.setOnCompletionListener(mp -> playButton.setVisibility(View.VISIBLE));

        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Load image
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

        // Caption input field
        EditText captionInput = new EditText(this);
        captionInput.setHint("Add your caption here...");
        captionInput.setTextSize(16);
        captionInput.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Record button
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

        Button saveButton = new Button( this);
        saveButton.setText("Save");
        saveButton.setOnClickListener(v -> {
            String caption = captionInput.getText().toString();
            Bitmap originalBitmap = getRotatedBitmap(imagePath);
            try {
                Bitmap stegoBitmap = embedTextSteganography(originalBitmap, caption, "Shashwat");
                imageView.setImageBitmap(stegoBitmap);
                saveImageToGallery(stegoBitmap);
                Toast.makeText(this, "Image saved with embedded text!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();

                Toast.makeText(this, "Error embedding text: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // Container for voice note playback
        voiceContainer = new LinearLayout(this);
        voiceContainer.setOrientation(LinearLayout.VERTICAL);
        voiceContainer.setId(R.id.voice_note_container);
        voiceContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        layout.addView(imageView);
        layout.addView(captionInput);
        layout.addView(btnRecord);
        layout.addView(voiceContainer);

        setContentView(layout);
        layout.addView(saveButton);
    }

}
