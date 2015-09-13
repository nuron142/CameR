package com.sunil.camer.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sunil.camer.ImageFullActivity;
import com.sunil.camer.Model.ImageInfo;
import com.sunil.camer.R;

import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by sunil on 12/09/15.
 */
public class ImageInfoRecyclerAdapter extends RecyclerView.Adapter<ImageInfoRecyclerAdapter.ViewHolder> {
    List<ImageInfo> mItems;
    Context context;
    DecimalFormat formatter;

    public ImageInfoRecyclerAdapter(Context context1) {
        super();
        context = context1;
        mItems = new ArrayList<>();
    }

    public void addData(ImageInfo imageInfo) {
        mItems.add(imageInfo);
        notifyDataSetChanged();
    }

    public void clear() {
        mItems.clear();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recycler_view, viewGroup, false);
        formatter = new DecimalFormat("##.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        final ImageInfo imageInfo = mItems.get(i);

        Glide.with(context)
                .load(new File(imageInfo.getImagePath()))
                .into(viewHolder.imageView);

        viewHolder.latLongView.setText("Lat = " + formatter.format(imageInfo.getLatitude()) + " Long = "
                + formatter.format(imageInfo.getLongitude()));

        viewHolder.addressView.setText("Address : " + imageInfo.getAddress());
        viewHolder.updatedView.setText("Updated : " + imageInfo.getUpdateTime());

        viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle args = new Bundle();
                args.putString("filePath", imageInfo.getImagePath());
                args.putDouble("lat", imageInfo.getLatitude());
                args.putDouble("long", imageInfo.getLongitude());
                args.putString("address", imageInfo.getAddress());

                Intent intent = new Intent(context, ImageFullActivity.class);
                intent.putExtras(args);
                v.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.imageView)
        ImageView imageView;
        @Bind(R.id.latLongView)
        TextView latLongView;
        @Bind(R.id.addressView)
        TextView addressView;
        @Bind(R.id.updatedView)
        TextView updatedView;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}