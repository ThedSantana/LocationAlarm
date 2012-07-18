package com.personal.locationalarm;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;

public class MainActivity extends MapActivity {
	private LocationManager locationManager = null;
	private MapView map = null;
	private MyLocationOverlay myLocOverlay = null;
	
	static final private String PROVIDER = LocationManager.PASSIVE_PROVIDER; 
	static final private double DEFAULT_LATITUDE = -33.891196; 
	static final private double DEFAULT_LONGITUDE = 151.273613;
	static final private int DEFAULT_ZOOM = 17;
	static final private int MIN_TIME_LOC_UPDATE = 3600*1000; //milisecs (0 as freq as possible)
	static final private int MIN_DIST_LOC_UPDATE = 1000; //meters (0 as freq as possible)
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// add proximity alert here!
	    locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);
	    Location lastKnownLocation = locationManager.getLastKnownLocation(PROVIDER);
	    
		map = (MapView) findViewById(R.id.map);
		if (lastKnownLocation == null) 
			map.getController().setCenter(getPoint(DEFAULT_LATITUDE, DEFAULT_LONGITUDE));
		else
			map.getController().setCenter(getPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
		map.getController().setZoom(DEFAULT_ZOOM);
		map.setBuiltInZoomControls(true);

		Drawable marker = getResources().getDrawable(R.drawable.marker);
		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());

		map.getOverlays().add(new SitesOverlay(marker));
		myLocOverlay = new MyLocationOverlay(this, map);
		map.getOverlays().add(myLocOverlay);
	}

	@Override
	public void onResume() {
		super.onResume();
		myLocOverlay.enableMyLocation();
		myLocOverlay.enableCompass();
	    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_LOC_UPDATE,
	    									   MIN_DIST_LOC_UPDATE, onLocationChange);
	}

	@Override
	public void onPause() {
		super.onPause();
		myLocOverlay.disableMyLocation();
		myLocOverlay.disableCompass();
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
		private Drawable marker = null;
		private OverlayItem inDrag = null;
		private ImageView dragImage = null;
		private int xDragImageOffset = 0;
		private int yDragImageOffset = 0;
		private int xDragTouchOffset = 0;
		private int yDragTouchOffset = 0;

		public SitesOverlay(Drawable marker) {
			super(marker);
			this.marker = marker;
			dragImage = (ImageView) findViewById(R.id.drag);
			xDragImageOffset = dragImage.getDrawable().getIntrinsicWidth() / 2;
			yDragImageOffset = dragImage.getDrawable().getIntrinsicHeight();
			populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
			return (items.get(i));
		}

		@Override
		protected boolean onTap(int i) {
			// Toast.makeText(MainActivity.this, items.get(i).getSnippet(),
			// Toast.LENGTH_SHORT).show();

			return (true);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event, MapView mapView) {
			final int action = event.getAction();
			final int x = (int) event.getX();
			final int y = (int) event.getY();
			boolean result = false;
			boolean existing = false;

			if (action == MotionEvent.ACTION_DOWN) {
				for (OverlayItem item : items) {
					Point p = new Point(0, 0);
					map.getProjection().toPixels(item.getPoint(), p);
					if (hitTest(item, marker, x - p.x, y - p.y)) {
						result = true;
						inDrag = item;
						items.remove(inDrag);
						populate();

						xDragTouchOffset = 0;
						yDragTouchOffset = 0;

						setDragImagePosition(p.x, p.y);
						dragImage.setVisibility(View.VISIBLE);

						xDragTouchOffset = x - p.x;
						yDragTouchOffset = y - p.y;

						existing = true;
						break;
					}
				}
				if (!existing) {
					GeoPoint newPoint = map.getProjection().fromPixels(x, y);
					items.add(new OverlayItem(newPoint, "", ""));
					populate();
					
				}
			} else if (action == MotionEvent.ACTION_MOVE && inDrag != null) {
				setDragImagePosition(x, y);
				result = true;
			} else if (action == MotionEvent.ACTION_UP && inDrag != null) {
				dragImage.setVisibility(View.GONE);

				GeoPoint pt = map.getProjection().fromPixels(x - xDragTouchOffset, y - yDragTouchOffset);
				OverlayItem toDrop = new OverlayItem(pt, inDrag.getTitle(), inDrag.getSnippet());

				items.add(toDrop);
				populate();

				inDrag = null;
				result = true;
			}
			return (result || super.onTouchEvent(event, mapView));
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			super.draw(canvas, mapView, shadow);
			boundCenterBottom(marker);
		}

		@Override
		public int size() {
			return (items.size());
		}

		private void setDragImagePosition(int x, int y) {
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) dragImage.getLayoutParams();
			lp.setMargins(x - xDragImageOffset - xDragTouchOffset, y - yDragImageOffset - yDragTouchOffset, 0, 0);
			dragImage.setLayoutParams(lp);
		}
	}
	
	  LocationListener onLocationChange=new LocationListener() {
		    public void onLocationChanged(Location location) {
				map.getController().setCenter(getPoint(location.getLatitude(), location.getLongitude()));
		    }
		    
		    public void onProviderDisabled(String provider) {
		      // required for interface, not used
		    }
		    
		    public void onProviderEnabled(String provider) {
		      // required for interface, not used
		    }
		    
		    public void onStatusChanged(String provider, int status,
		                                  Bundle extras) {
		      // required for interface, not used
		    }
		  };
}
