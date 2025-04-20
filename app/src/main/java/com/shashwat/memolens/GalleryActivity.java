package com.shashwat.memolens;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class GalleryActivity extends AppCompatActivity {
    private static final String TAG = "GalleryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

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

        // Check and log valid image files
        if (files != null && files.length > 0) {
            Log.d(TAG, "Fetched files:");
            for (File file : files) {
                // Check if the file exists and is readable
                if (file.exists() && file.canRead()) {
                    Log.d(TAG, "File: " + file.getAbsolutePath());

                    // Read image file into Bitmap
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

                    // Extract the message from the image (no embedding here)
                    String extractedMessage = extractTextSteganography(bitmap);
                    Log.d(TAG, "Extracted message: " + extractedMessage);
                } else {
                    Log.d(TAG, "Skipping deleted or unreadable file: " + file.getAbsolutePath());
                }
            }
        } else {
            Log.d(TAG, "No image files found in folder.");
            Toast.makeText(this, "No images found", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to extract the embedded text from the image using LSB steganography
    public String extractTextSteganography(Bitmap encodedBitmap) {
        StringBuilder binaryMessage = new StringBuilder();
        StringBuilder message = new StringBuilder();

        int width = encodedBitmap.getWidth();
        int height = encodedBitmap.getHeight();
        int eomMatchCount = 0;

        outerLoop:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = encodedBitmap.getPixel(x, y);
                int blue = pixel & 0xFF;

                binaryMessage.append(blue & 1); // Get the LSB of the blue channel

                // Check if we have collected 8 bits (1 byte)
                if (binaryMessage.length() >= 8) {
                    String byteStr = binaryMessage.substring(binaryMessage.length() - 8);
                    char c = (char) Integer.parseInt(byteStr, 2);  // Convert the 8 bits to a character
                    if (c == 'ÿ') {  // Check for end-of-message marker (ÿ)
                        eomMatchCount++;
                        if (eomMatchCount == 3) break outerLoop;  // End of message
                    } else {
                        eomMatchCount = 0;
                        message.append(c);  // Append the character to the message
                    }
                }
            }
        }

        return message.toString();
    }
}
