package com.sunil.camer;

import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by nuron on 12/09/15.
 */
public class UpdatePage extends AppCompatActivity {

    @Bind(R.id.textview)
    TextView locationView;
    @Bind(R.id.address_view)
    TextView addressView;
    @Bind(R.id.image)
    ImageView imageView;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    File file;
    private ImageInfo mImageInfo = new ImageInfo();
    private int imageInfoValid = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_page);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);


        String imagePath = getIntent().getExtras().getString("filePath");

        if (imagePath != null)
            file = new File(imagePath);

        if (file.exists()) {

            mImageInfo.setImagePath(imagePath);
            imageView.setImageURI(Uri.fromFile(file));
            getRxLocation();
        }
    }

    @OnClick(R.id.saveButton)
    public void saveButtonClick() {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        mImageInfo.setUpdateTime(timeStamp);

        addToDB(mImageInfo);
        finish();
    }

    public void addToDB(ImageInfo imageInfo) {
        Realm realm = Realm.getInstance(getApplicationContext());
        realm.beginTransaction();
        ImageInfo mInfo = realm.createObject(ImageInfo.class);

        mInfo.setImagePath(imageInfo.getImagePath());
        mInfo.setLatitude(imageInfo.getLatitude());
        mInfo.setLongitude(imageInfo.getLongitude());
        mInfo.setAddress(imageInfo.getAddress());
        mInfo.setUpdateTime(imageInfo.getUpdateTime());

        realm.commitTransaction();
    }

    //region Rx Calls for Location
    public boolean isNetConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void getRxLocation() {
        ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(this);
        locationProvider.getLastKnownLocation()
                .observeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Location>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Location location) {

                        if (location == null)
                            return;

                        DecimalFormat formatter = new DecimalFormat("##.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
                        formatter.setRoundingMode(RoundingMode.HALF_UP);

                        locationView.setText("Lat = " + formatter.format(location.getLatitude()) + " Long = "
                                + formatter.format(location.getLongitude()));

                        mImageInfo.setLatitude(String.valueOf(location.getLatitude()));
                        mImageInfo.setLongitude(String.valueOf(location.getLongitude()));

                        if (isNetConnected())
                            getRxAddress(location);

                    }

                });
    }

    private void getRxAddress(Location location) {

        ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(this);
        Observable<List<Address>> reverseGeocodeObservable = locationProvider
                .getReverseGeocodeObservable(location.getLatitude(), location.getLongitude(), 1);

        reverseGeocodeObservable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Address>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(List<Address> addresses) {

                        Address addressItem = addresses.get(0);

                        String address = addressItem.getAddressLine(0) + ", "
                                + addressItem.getLocality();

                        mImageInfo.setAddress(address);

                        addressView.setText(address);

                    }
                });
    }
    //endregion

}
