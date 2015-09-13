package com.sunil.camer;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.LocationRequest;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.sunil.camer.Model.ImageInfo;

import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by nuron on 12/09/15.
 */
public class UpdatePage extends AppCompatActivity {

    private final int LOCATION_TIMEOUT_IN_SECONDS = 10;
    @Bind(R.id.latLong_update_view)
    TextView locationView;
    @Bind(R.id.address_update_view)
    TextView addressView;
    @Bind(R.id.image_update_view)
    ImageView imageView;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.saveButton)
    Button saveButton;
    @Bind(R.id.retryButton)
    Button retryButton;
    @Bind(R.id.cancelButton)
    Button cancelButton;
    @Bind(R.id.progress_wheel)
    ProgressWheel progressWheel;
    File file;
    Context context;

    Subscription locationSub, addressSub;
    private ImageInfo mImageInfo = new ImageInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_page);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        context = this;

        saveButton.setClickable(false);
        saveButton.setBackgroundColor(Color.parseColor("#BDC3C7"));
        saveButton.setVisibility(View.VISIBLE);
        retryButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);

        String imagePath = getIntent().getExtras().getString("filePath");

        if (imagePath != null)
            file = new File(imagePath);

        if (file.exists()) {
            Glide.with(this).load(file).into(imageView);
            mImageInfo.setImagePath(imagePath);
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


    @OnClick(R.id.retryButton)
    public void retryButtonClick() {
        progressWheel.spin();
        getRxLocation();
    }

    @OnClick(R.id.cancelButton)
    public void cancelButtonClick() {
        finish();
    }


    // Add image info to realm DB
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

        LocationRequest req = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setExpirationDuration(TimeUnit.SECONDS.toMillis(LOCATION_TIMEOUT_IN_SECONDS))
                .setInterval(5);


        locationSub = locationProvider.getUpdatedLocation(req)
                .timeout(LOCATION_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .first()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Location>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                        progressWheel.stopSpinning();
                        Toast.makeText(context, "Couldn't get location", Toast.LENGTH_SHORT).show();
                        saveButton.setVisibility(View.GONE);
                        retryButton.setVisibility(View.VISIBLE);
                        cancelButton.setVisibility(View.VISIBLE);
                        locationView.setText("No Location found");
                    }

                    @Override
                    public void onNext(Location location) {

                        DecimalFormat formatter = new DecimalFormat("##.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
                        formatter.setRoundingMode(RoundingMode.HALF_UP);

                        locationView.setText("Lat = " + formatter.format(location.getLatitude()) + " Long = "
                                + formatter.format(location.getLongitude()));

                        mImageInfo.setLatitude(location.getLatitude());
                        mImageInfo.setLongitude(location.getLongitude());

                        if (isNetConnected())
                            getRxAddress(location);
                        else {
                            Toast.makeText(context, "Couldn't lookup address", Toast.LENGTH_LONG).show();
                            saveButton.setVisibility(View.GONE);
                            progressWheel.stopSpinning();
                            retryButton.setVisibility(View.VISIBLE);
                            cancelButton.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void getRxAddress(Location location) {

        ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(this);
        Observable<List<Address>> reverseGeocodeObservable = locationProvider
                .getReverseGeocodeObservable(location.getLatitude(), location.getLongitude(), 1);

        addressSub = reverseGeocodeObservable
                .timeout(LOCATION_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Address>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        progressWheel.stopSpinning();
                        Toast.makeText(context, "Couldn't lookup address", Toast.LENGTH_LONG).show();
                        saveButton.setVisibility(View.GONE);
                        retryButton.setVisibility(View.VISIBLE);
                        cancelButton.setVisibility(View.VISIBLE);
                        addressView.setText("No address");
                    }

                    @Override
                    public void onNext(List<Address> addresses) {

                        Address addressItem = addresses.get(0);

                        String address = addressItem.getAddressLine(0) + ", " + addressItem.getAddressLine(1);

                        mImageInfo.setAddress(address);
                        addressView.setText(address);
                        progressWheel.stopSpinning();

                        retryButton.setVisibility(View.GONE);
                        cancelButton.setVisibility(View.GONE);
                        saveButton.setVisibility(View.VISIBLE);
                        saveButton.setBackgroundColor(Color.parseColor("#1BBC9B"));
                        saveButton.setClickable(true);

                    }
                });
    }
    //endregion


    @Override
    protected void onStop() {
        super.onStop();
        if (locationSub != null && !locationSub.isUnsubscribed())
            locationSub.unsubscribe();

        if (addressSub != null && !addressSub.isUnsubscribed())
            addressSub.unsubscribe();
    }

}
