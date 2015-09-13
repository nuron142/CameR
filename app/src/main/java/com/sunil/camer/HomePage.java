package com.sunil.camer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class HomePage extends AppCompatActivity {

    public static final int CAPTURE_IMAGE_FULLSIZE_ACTIVITY_REQUEST_CODE = 142;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
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
            startActivity(intent);


//            if (file.exists()){
//
//                ((ImageView) findViewById(R.id.image)).setImageURI(Uri.fromFile(file));
//                getRxLocation();
//            }
        }
    }
//
//    //region Rx Calls for Location
//    public boolean isNetConnected()
//    {
//        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//        return networkInfo != null && networkInfo.isConnected();
//    }
//
//    private void getRxLocation()
//    {
//        ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(this);
//        locationProvider.getLastKnownLocation()
//                .observeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<Location>() {
//                    @Override
//                    public void onCompleted() {
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                    }
//
//                    @Override
//                    public void onNext(Location location) {
//
//                        mLastLocation = location;
//                        DecimalFormat formatter = new DecimalFormat("##.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
//                        formatter.setRoundingMode(RoundingMode.HALF_UP);
//
//                        TextView myLocation = (TextView) findViewById(R.id.textview);
//                        myLocation.setText("Lat = " + formatter.format(location.getLatitude()) + " Long = "
//                                + formatter.format(location.getLongitude()));
//
//                        if(isNetConnected())
//                            getRxAddress(location);
//
//                    }
//
//                });
//    }
//
//    private void getRxAddress(Location location)
//    {
//
//        ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(this);
//        Observable<List<Address>> reverseGeocodeObservable = locationProvider
//                .getReverseGeocodeObservable(location.getLatitude(), location.getLongitude(), 1);
//
//        reverseGeocodeObservable
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<List<Address>>() {
//                    @Override
//                    public void onCompleted() {
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                    }
//
//                    @Override
//                    public void onNext(List<Address> addresses) {
//
//                        Address addressItem = addresses.get(0);
//
//                        String address = addressItem.getAddressLine(0) + ", "
//                                + addressItem.getLocality();
//
//                        Log.d("1", "Address : " + address);
//                        TextView myAddress = (TextView) findViewById(R.id.address_view);
//                        myAddress.setText(address);
//
//                    }
//                });
//    }
//    //endregion

}
