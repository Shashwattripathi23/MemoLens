package com.shashwat.memolens;
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
                Bitmap stegoBitmap = originalBitmap;
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
