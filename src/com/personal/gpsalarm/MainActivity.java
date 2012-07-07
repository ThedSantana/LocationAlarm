package com.personal.gpsalarm;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MainActivity extends MapActivity {
	private MapView map = null;
	private MyLocationOverlay me = null;
	private MyOverlay myOverlay = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		map = (MapView) findViewById(R.id.map);

		map.getController().setCenter(getPoint(40.76793169992044, -73.98180484771729));
		map.getController().setZoom(17);
		map.setBuiltInZoomControls(true);

		Drawable marker = getResources().getDrawable(R.drawable.marker);
		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());

		map.getOverlays().add(new SitesOverlay(marker));
		me = new MyLocationOverlay(this, map);
		myOverlay = new MyOverlay();
		map.getOverlays().add(me);
		map.getOverlays().add(myOverlay);
	}

	@Override
	public void onResume() {
		super.onResume();
		me.enableMyLocation();
		me.enableCompass();
	}

	@Override
	public void onPause() {
		super.onPause();
		me.disableMyLocation();
		me.disableCompass();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return (false);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_S) {
			map.setSatellite(!map.isSatellite());
			return (true);
		} else if (keyCode == KeyEvent.KEYCODE_Z) {
			map.displayZoomControls(true);
			return (true);
		}

		return (super.onKeyDown(keyCode, event));
	}

	private GeoPoint getPoint(double lat, double lon) {
		return (new GeoPoint((int) (lat * 1000000.0), (int) (lon * 1000000.0)));
	}

	private class SitesOverlay extends ItemizedOverlay<OverlayItem> {
		private List<OverlayItem> items = new ArrayList<OverlayItem>();

		public SitesOverlay(Drawable marker) {
			super(marker);

			boundCenterBottom(marker);

			items.add(new OverlayItem(getPoint(40.748963847316034, -73.96807193756104), "UN", "United Nations"));
			items.add(new OverlayItem(getPoint(40.76866299974387, -73.98268461227417), "Lincoln Center",
			        "Home of Jazz at Lincoln Center"));
			items.add(new OverlayItem(getPoint(40.765136435316755, -73.97989511489868), "Carnegie Hall",
			        "Where you go with practice, practice, practice"));
			items.add(new OverlayItem(getPoint(40.70686417491799, -74.01572942733765), "The Downtown Club",
			        "Original home of the Heisman Trophy"));

			populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
			return (items.get(i));
		}

		@Override
		protected boolean onTap(int i) {
			Toast.makeText(MainActivity.this, items.get(i).getSnippet(), Toast.LENGTH_SHORT).show();

			return (true);
		}

		@Override
		public int size() {
			return (items.size());
		}
	}

	private class MyOverlay extends Overlay {
		@Override
		public boolean onTap(GeoPoint point, MapView mapView) {
			Context contexto = mapView.getContext();
			String msg = "Lat: " + point.getLatitudeE6() / 1E6 + " - " + "Lon: " + point.getLongitudeE6() / 1E6;
			Toast toast = Toast.makeText(contexto, msg, Toast.LENGTH_SHORT);
			toast.show();

			return true;
		}
	}
}
