package com.sunil.camer;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ImageFullActivity extends AppCompatActivity {

    @Bind(R.id.image_full_view)
    ImageView imageView;
    @Bind(R.id.latLong_full_view)
    TextView latLongView;
    @Bind(R.id.address_full_view)
    TextView addressView;

    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_full);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DecimalFormat formatter = new DecimalFormat("##.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        formatter.setRoundingMode(RoundingMode.HALF_UP);

        Bundle args = getIntent().getExtras();
        if (args != null) {

            latLongView.setText("Lat = " + formatter.format(args.getDouble("lat")) + " Long = "
                    + formatter.format(args.getDouble("long")));
            addressView.setText(args.getString("address"));

            String imagePath = args.getString("filePath");

            if (imagePath != null)
                file = new File(imagePath);

            if (file.exists()) {
                Glide.with(this).load(file).into(imageView);
            }
        }
    }

}
