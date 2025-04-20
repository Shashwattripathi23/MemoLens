package com.shashwat.memolens;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;
import android.graphics.Bitmap;
import android.graphics.Color;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    private final List<File> imageFiles;
    private final Context context;

    public GalleryAdapter(Context context, List<File> imageFiles) {
        this.context = context;
        this.imageFiles = imageFiles;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView captionView;

        public ViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.imageView);
            captionView = view.findViewById(R.id.captionView);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gallery_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        File file = imageFiles.get(position);
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        holder.imageView.setImageBitmap(bitmap);

        // Decode caption
        String caption = extractSteganography(bitmap);
        holder.captionView.setText(caption);
    }

    @Override
    public int getItemCount() {
        return imageFiles.size();
    }


    private String extractSteganography(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        StringBuilder binaryString = new StringBuilder();

        // Traverse through all the pixels of the image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);

                // Extract the least significant bit (LSB) of each color component
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);

                binaryString.append((r & 1));  // Least significant bit of red
                binaryString.append((g & 1));  // Least significant bit of green
                binaryString.append((b & 1));  // Least significant bit of blue
            }
        }

        // Convert the binary string to text
        return binaryToText(binaryString.toString());
    }

    private String binaryToText(String binaryString) {
        StringBuilder result = new StringBuilder();

        // Process each 8 bits (1 byte) as a character
        for (int i = 0; i < binaryString.length(); i += 8) {
            String byteString = binaryString.substring(i, i + 8);
            int charCode = Integer.parseInt(byteString, 2);
            result.append((char) charCode);
        }

        return result.toString();
    }

    // Paste the extractSteganography() method from earlier here
}
