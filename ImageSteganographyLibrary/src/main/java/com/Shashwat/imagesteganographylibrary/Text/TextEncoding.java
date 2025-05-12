package com.Shashwat.imagesteganographylibrary.Text;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.Shashwat.imagesteganographylibrary.Text.AsyncTaskCallback.TextEncodingCallback;
import com.Shashwat.imagesteganographylibrary.Utils.Utility;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TextEncoding {

    private static final String TAG = TextEncoding.class.getName();

    private final Activity activity;
    private final TextEncodingCallback callbackInterface;

    public TextEncoding(Activity activity, TextEncodingCallback callbackInterface) {
        this.activity = activity;
        this.callbackInterface = callbackInterface;
    }

    public void encode(ImageSteganography textStegnography) {
        Log.d(TAG, "Starting encoding process...");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ImageSteganography result = new ImageSteganography();

            Bitmap bitmap = textStegnography.getImage();
            Log.d(TAG, "Image received. Width: " + bitmap.getWidth() + ", Height: " + bitmap.getHeight());

            int originalHeight = bitmap.getHeight();
            int originalWidth = bitmap.getWidth();

            Log.d(TAG, "Splitting image into chunks...");
            List<Bitmap> src_list = Utility.splitImage(bitmap);
            Log.d(TAG, "Image split into " + src_list.size() + " chunks.");

            String encryptedMessage = textStegnography.getEncrypted_message();
            Log.d(TAG, "original message: " + textStegnography.getMessage());
            Log.d(TAG, "Encrypted message: " + encryptedMessage);  // Log the encrypted message

            Log.d(TAG, "Encoding message into image...");
            List<Bitmap> encoded_list = EncodeDecode.encodeMessage(src_list, encryptedMessage, null);

            for (Bitmap bitm : src_list) bitm.recycle();
            System.gc();
            Log.d(TAG, "Recycled split images and ran garbage collection.");

            Log.d(TAG, "Merging encoded chunks back into a single image...");
            Bitmap srcEncoded = Utility.mergeImage(encoded_list, originalHeight, originalWidth);

            result.setEncoded_image(srcEncoded);
            result.setEncoded(true);
            Log.d(TAG, "Encoding completed. Setting encoded image.");

            new Handler(Looper.getMainLooper()).post(() -> {
                Log.d(TAG, "Calling onCompleteTextEncoding callback...");
                callbackInterface.onCompleteTextEncoding(result);
            });
        });
    }



}
