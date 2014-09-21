package com.example.getneighborposition;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.ConnectionResult;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;

import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;

import android.widget.TextView;

public class MainActivity extends FragmentActivity implements LocationListener {

	private SupportMapFragment mapFragment;
	private static final String HOGE_MAP_FRAGMENT_TAG = "maps_fragment";

	private GoogleMap gMap;
	public double latitude = 0;
	public double longitude = 0;

	private static final int MENU_A = 0;
	private static final int MENU_B = 1;

	private static final int RQS_GooglePlayServices = 1;

	private TextView textview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		textview = (TextView) findViewById(R.id.tv_location);

		// Google Play Servicesが使えるかどうかのステータス
		int status = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getBaseContext());

		// Showing status
		if (status != ConnectionResult.SUCCESS) {
			// Google Play Services が使えない場合
			int requestCode = 10;
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this,
					requestCode);
			dialog.show();

		} else {
			// Google Play Services が使える場合
			// activity_main.xmlのSupportMapFragmentへの参照を取得
			SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map);

			// fragmentからGoogleMap objectを取得
			gMap = fm.getMap();

			// Google MapのMyLocationレイヤーを使用可能にする
			gMap.setMyLocationEnabled(true);
			gMap.setIndoorEnabled(true);
			gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			gMap.setTrafficEnabled(true);

			// システムサービスのLOCATION_SERVICEからLocationManager objectを取得
			LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

			// retrieve providerへcriteria objectを生成
			Criteria criteria = new Criteria();

			// Best providerの名前を取得
			String provider = locationManager.getBestProvider(criteria,true);
			
			//providerがある＝位置情報が取得可能な状態であれば位置情報を取得する。
			if (provider != null) {
				Toast.makeText(this, provider + "を利用します。", Toast.LENGTH_LONG).show();

				Log.d("app", "the best provider is " + provider);
				// 現在位置を取得
				Location location = locationManager
						.getLastKnownLocation(provider);

				if (location != null) {
					onLocationChanged(location);
				}
				locationManager
						.requestLocationUpdates(provider, 20000, 0, this);
			} else { //GPS，ネットワークの両方が利用できない場合
				Toast.makeText(this, "位置情報が取得できません。", Toast.LENGTH_LONG).show();
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.activity_main, menu);

		menu.add(0, MENU_A, 0, "チャットルームに移動");
		//menu.add(0, MENU_B, 0, "Legal Notices");
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_A:
			// チャットルームの選択
			
			Intent intent = new Intent(this, SelectRoomActivity.class);
			
			startActivity(intent);
			
			return true;
		case MENU_B:
			// Legal Notices
			String LicenseInfo = GooglePlayServicesUtil
					.getOpenSourceSoftwareLicenseInfo(getApplicationContext());
			AlertDialog.Builder LicenseDialog = new AlertDialog.Builder(
					MainActivity.this);
			LicenseDialog.setTitle("Legal Notices");
			LicenseDialog.setMessage(LicenseInfo);
			LicenseDialog.show();

			return true;
		}
		return false;
	}

	@Override
	protected void onResume() {

		super.onResume();

	}

	private void moveCamera2Target(boolean animation_effect, LatLng target,
			float zoom, float tilt, float bearing) {
		CameraPosition pos = new CameraPosition(target, zoom, tilt, bearing);
		CameraUpdate camera = CameraUpdateFactory.newCameraPosition(pos);

		if (animation_effect == true) {
			//
			gMap.animateCamera(camera);
		} else {
			//
			gMap.moveCamera(camera);
		}
	}

	@Override
	public void onLocationChanged(Location location) {

		// 現在位置の緯度を取得
		latitude = location.getLatitude();

		// 現在位置の経度を取得
		longitude = location.getLongitude();

		textview.setText(latitude + "," + longitude);
		// reverse_geocode(latitude,longitude);

		// 現在位置からLatLng objectを生成
		LatLng latLng = new LatLng(latitude, longitude);

		// Google Mapに現在地を表示
		gMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

		// Google Mapの Zoom値を指定
		gMap.animateCamera(CameraUpdateFactory.zoomTo(15));

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
	}

}
