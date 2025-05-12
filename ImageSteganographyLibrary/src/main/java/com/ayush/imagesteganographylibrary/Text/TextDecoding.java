package com.ayush.imagesteganographylibrary.Text;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.ayush.imagesteganographylibrary.Text.AsyncTaskCallback.TextDecodingCallback;
import com.ayush.imagesteganographylibrary.Utils.Utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TextDecoding {

    private static final String TAG = TextDecoding.class.getName();
    private final ExecutorService executorService;
    private final Handler mainHandler;
    private final TextDecodingCallback textDecodingCallback;
    private final FragmentActivity activity;
    private LoadingDialogFragment loadingDialog;

    public TextDecoding(FragmentActivity activity, TextDecodingCallback textDecodingCallback) {
        this.activity = activity;
        this.textDecodingCallback = textDecodingCallback;
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void execute(ImageSteganography imageSteganography) {
        showLoading();
        executorService.execute(() -> {
            ImageSteganography result = doInBackground(imageSteganography);

            mainHandler.post(() -> {
                dismissLoading();
                textDecodingCallback.onCompleteTextEncoding(result);
            });
        });
    }

    private ImageSteganography doInBackground(ImageSteganography imageSteganography) {
        ImageSteganography result = new ImageSteganography();
        if (imageSteganography != null) {
            Bitmap bitmap = imageSteganography.getImage();

            if (bitmap != null) {
                List<Bitmap> srcEncodedList = Utility.splitImage(bitmap);
                String decodedMessage = EncodeDecode.decodeMessage(srcEncodedList);

                Log.d(TAG, "Decoded_Message : " + decodedMessage);

                if (!Utility.isStringEmpty(decodedMessage)) {
                    result.setDecoded(true);
                }

                String decryptedMessage = ImageSteganography.decryptMessage(decodedMessage, imageSteganography.getSecret_key());
                Log.d(TAG, "Decrypted message : " + decryptedMessage);

                if (!Utility.isStringEmpty(decryptedMessage)) {
                    result.setSecretKeyWrong(false);
                    result.setMessage(decryptedMessage);
                }

                for (Bitmap b : srcEncodedList) {
                    b.recycle();
                }
                System.gc();
            }
        }
        return result;
    }

    private void showLoading() {
        loadingDialog = new LoadingDialogFragment();
        loadingDialog.setCancelable(false);
        loadingDialog.show(activity.getSupportFragmentManager(), "loading");
    }

    private void dismissLoading() {
        if (loadingDialog != null) {
            loadingDialog.dismissAllowingStateLoss();
        }
    }

    public static class LoadingDialogFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable android.os.Bundle savedInstanceState) {
            android.app.ProgressDialog dialog = new android.app.ProgressDialog(getActivity());
            dialog.setMessage("Loading, Please Wait...");
            dialog.setTitle("Decoding Message");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            return dialog;
        }



    }
}
