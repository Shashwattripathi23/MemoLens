package com.shashwat.memolens;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private final File[] imageFiles;
    private final Context context;

    public ImageAdapter(Context context, File[] imageFiles) {
        this.context = context;
        this.imageFiles = imageFiles;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        File imageFile = imageFiles[position];
        Glide.with(context)
                .load(imageFile)
                .centerCrop()
                .into(holder.imageView);

        // Handle image click
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, PhotoActivity.class);
            intent.putExtra("imagePath", imageFile.getAbsolutePath());
            String captureDate = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date(imageFile.lastModified()));
            intent.putExtra("captureDate", captureDate);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return imageFiles.length;
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
