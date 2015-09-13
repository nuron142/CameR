package com.sunil.camer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by sunil on 12/09/15.
 */

public class HomePage extends AppCompatActivity {

    public static final int CAPTURE_IMAGE_FULLSIZE_ACTIVITY_REQUEST_CODE = 142;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;

    ImageInfoRecyclerAdapter imageInfoRecyclerAdapter;

    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        imageInfoRecyclerAdapter = new ImageInfoRecyclerAdapter(this);
        recyclerView.setAdapter(imageInfoRecyclerAdapter);

    }

    @OnClick(R.id.fab)
    public void fabClick() {
        runCameraProgram();
    }

    private void runCameraProgram() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "SUNIL_" + timeStamp + "_img";

        file = new File(getExternalFilesDir(null), imageFileName + ".jpg");

        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        startActivityForResult(intent, CAPTURE_IMAGE_FULLSIZE_ACTIVITY_REQUEST_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_FULLSIZE_ACTIVITY_REQUEST_CODE) {

            Intent intent = new Intent(this, UpdatePage.class);
            intent.putExtra("filePath", file.getPath());
            if (file.exists())
                startActivity(intent);
            else
                Toast.makeText(this, "Couldn't save picture", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFromDB() {

        imageInfoRecyclerAdapter.clear();
        Realm realm = Realm.getInstance(this);
        RealmResults<ImageInfo> query = realm.where(ImageInfo.class)
                .findAllSorted("updateTime", RealmResults.SORT_ORDER_DESCENDING);
        if (query.size() < 1)
            return;

        for (ImageInfo imageInfo : query) {
            ImageInfo mInfo = new ImageInfo();

            mInfo.setImagePath(imageInfo.getImagePath());
            mInfo.setLatitude(imageInfo.getLatitude());
            mInfo.setLongitude(imageInfo.getLongitude());
            mInfo.setAddress(imageInfo.getAddress());
            mInfo.setUpdateTime(imageInfo.getUpdateTime());

            imageInfoRecyclerAdapter.addData(mInfo);
        }
        imageInfoRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFromDB();
    }


}
