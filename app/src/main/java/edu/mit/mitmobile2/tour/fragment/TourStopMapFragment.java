package edu.mit.mitmobile2.tour.fragment;

import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import edu.mit.mitmobile2.Constants;
import edu.mit.mitmobile2.MitMobileApplication;
import edu.mit.mitmobile2.OttoBusEvent;
import edu.mit.mitmobile2.R;
import edu.mit.mitmobile2.maps.MITMapView;
import edu.mit.mitmobile2.maps.MapItem;
import edu.mit.mitmobile2.tour.callbacks.TourStopCallback;
import edu.mit.mitmobile2.tour.model.MITTour;
import edu.mit.mitmobile2.tour.model.MITTourStop;

public class TourStopMapFragment extends Fragment implements GoogleMap.OnMapLoadedCallback {

    private MITMapView mitMapView;
    private MITTour tour;
    private TourStopCallback callback;

    public TourStopMapFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tour_stop_map, null);

        callback = (TourStopCallback) getActivity();

        MapView googleMapView = (MapView) view.findViewById(R.id.tour_map);
        googleMapView.onCreate(savedInstanceState);

        mitMapView = new MITMapView(getActivity(), googleMapView, this);
        mitMapView.setMapViewExpanded(true);
        mitMapView.mapBoundsPadding = (int) getActivity().getResources().getDimension(R.dimen.map_bounds_quarter_padding);

        LinearLayout restrictions = (LinearLayout) view.findViewById(R.id.restrictions_text_view);
        restrictions.setAlpha(0.8f);
        restrictions.setBackgroundResource(R.drawable.map_header_selector);
        restrictions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.showTourDetailActivity(tour.getDescriptionHtml());
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(Constants.Tours.TOUR_KEY)) {
            tour = savedInstanceState.getParcelable(Constants.Tours.TOUR_KEY);
        } else {
            tour = callback.getTour();
        }

        if (tour != null) {
            updateMapItems((ArrayList) tour.getStops(), true);
            mitMapView.setToDefaultBounds(false, 0);
            drawRoutePath();
        }

        setupFABs(view);

        return view;
    }

    private void setupFABs(View view) {
        FloatingActionButton myLocationButton = (FloatingActionButton) view.findViewById(R.id.my_location_button);
        myLocationButton.setColorNormalResId(R.color.white);
        myLocationButton.setColorPressedResId(R.color.medium_grey);
        myLocationButton.setSize(FloatingActionButton.SIZE_NORMAL);
        myLocationButton.setIcon(R.drawable.ic_my_location);
        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location myLocation = mitMapView.getMap().getMyLocation();
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 14f);
                mitMapView.getMap().animateCamera(update, 400, null);
            }
        });

        FloatingActionButton listButton = (FloatingActionButton) view.findViewById(R.id.list_button);
        listButton.setColorPressedResId(R.color.mit_red_dark);
        listButton.setColorNormalResId(R.color.mit_red);
        listButton.setSize(FloatingActionButton.SIZE_NORMAL);
        listButton.setIcon(R.drawable.ic_list);
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.switchViews(true);
            }
        });
    }

    private void drawRoutePath() {
        for (MITTourStop stop : tour.getStops()) {
            if (stop.getDirection() != null) {
                PolylineOptions options = new PolylineOptions();

                for (List<Double> outerList : stop.getDirection().getPathList()) {
                    LatLng point = new LatLng(outerList.get(1), outerList.get(0));
                    options.add(point);
                }

                options.color(getResources().getColor(R.color.map_path_color));
                options.visible(true);
                options.width(12f);
                options.zIndex(100);

                mitMapView.getMap().addPolyline(options);
            }
        }
    }

    protected void updateMapItems(ArrayList mapItems, boolean fit) {
        if (mapItems.size() == 0 || ((MapItem) mapItems.get(0)).isDynamic()) {
            mitMapView.clearDynamic();
        }
        mitMapView.addMapItemList(mapItems, false, fit);
    }

    @Subscribe
    public void mitTourLoadedEvent(OttoBusEvent.TourInfoLoadedEvent event) {
        tour = event.getTour();
        updateMapItems((ArrayList) tour.getStops(), true);
        mitMapView.setToDefaultBounds(false, 0);
        drawRoutePath();
    }

    @Override
    public void onMapLoaded() {
    }

    @Override
    public void onDestroy() {
        mitMapView.getGoogleMapView().onDestroy();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        mitMapView.getGoogleMapView().onResume();
        MitMobileApplication.bus.register(this);
    }

    @Override
    public void onPause() {
        MitMobileApplication.bus.unregister(this);
        mitMapView.getGoogleMapView().onResume();
        super.onPause();
    }

    @Override
    public void onLowMemory() {
        mitMapView.getGoogleMapView().onLowMemory();
        super.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mitMapView.getGoogleMapView().onSaveInstanceState(outState);
        outState.putParcelable(Constants.Tours.TOUR_KEY, tour);
    }
}