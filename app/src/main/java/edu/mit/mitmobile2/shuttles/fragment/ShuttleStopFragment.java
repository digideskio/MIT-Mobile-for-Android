package edu.mit.mitmobile2.shuttles.fragment;

import android.content.ContentResolver;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.mit.mitmobile2.Constants;
import edu.mit.mitmobile2.DBAdapter;
import edu.mit.mitmobile2.EndlessFragmentStatePagerAdapter;
import edu.mit.mitmobile2.MitMapFragment;
import edu.mit.mitmobile2.MitMobileApplication;
import edu.mit.mitmobile2.R;
import edu.mit.mitmobile2.Schema;
import edu.mit.mitmobile2.shuttles.MITShuttlesProvider;
import edu.mit.mitmobile2.shuttles.MitCursorLoader;
import edu.mit.mitmobile2.shuttles.adapter.ShuttleStopViewPagerAdapter;
import edu.mit.mitmobile2.shuttles.callbacks.MapFragmentCallback;
import edu.mit.mitmobile2.shuttles.model.MITShuttleRoute;
import edu.mit.mitmobile2.shuttles.model.MITShuttleStop;
import timber.log.Timber;

public class ShuttleStopFragment extends MitMapFragment {

    ViewPager predictionViewPager;

    private ShuttleStopViewPagerAdapter stopViewPagerAdapter;
    private int currentPosition;
    private MITShuttleRoute route = new MITShuttleRoute();
    private List<MITShuttleStop> stops;
    private String routeId;
    private String stopId;
    private String uriString;
    private String selectionString;
    private MapFragmentCallback callback;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        View shuttleStopContent = inflater.inflate(R.layout.shuttle_stop_content, null);

        callback = (MapFragmentCallback) getActivity();

        predictionViewPager = (ViewPager) shuttleStopContent.findViewById(R.id.prediction_view_pager);
        View transparentView = shuttleStopContent.findViewById(R.id.transparent_map_overlay);

        routeId = getActivity().getIntent().getStringExtra(Constants.ROUTE_ID_KEY);
        stopId = getActivity().getIntent().getStringExtra(Constants.STOP_ID_KEY);

        uriString = MITShuttlesProvider.STOPS_URI + "/" + stopId;
        selectionString = Schema.Route.TABLE_NAME + "." + Schema.Stop.ROUTE_ID + "=\'" + routeId + "\'";
        queryDatabase();

        callback.setActionBarTitle(route.getTitle());

        updateMapItems((ArrayList) route.getStops(), false);
        displayMapItems();
        drawRoutePath(route);

        stops = route.getStops();
        List<String> stopIds = new ArrayList<>();
        for (MITShuttleStop stop : stops) {
            stopIds.add(stop.getId());
        }
        stopViewPagerAdapter = new ShuttleStopViewPagerAdapter(getFragmentManager(), routeId, stopIds);

        predictionViewPager.setAdapter(stopViewPagerAdapter);
        predictionViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int realPosition = stopViewPagerAdapter.getRealPosition(position);
                currentPosition = realPosition;
                callback.setActionBarSubtitle(stops.get(realPosition).getTitle());
                animateToStop(realPosition);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        int startPosition = getStartPosition();
        callback.setActionBarSubtitle(stops.get(startPosition).getTitle());
        int fakePosition = stops.size() * EndlessFragmentStatePagerAdapter.NUMBER_OF_LOOPS / 2 + startPosition;
        predictionViewPager.setCurrentItem(fakePosition);
        currentPosition = startPosition;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            addTransparentView(transparentView);
        } else {
            transparentView.setVisibility(View.GONE);
        }
        addShuttleStopContent(shuttleStopContent);

        updateData();
        getLoaderManager().initLoader(0, null, this);

        LatLng stopPosition = new LatLng(stops.get(startPosition).getLat(), stops.get(startPosition).getLon());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(stopPosition, STOP_ZOOM);
        getMapView().moveCamera(cameraUpdate);

        return view;
    }

    private int getStartPosition() {
        for (MITShuttleStop stop : stops) {
            if (stop.getId().equals(stopId)) {
                return stops.indexOf(stop);
            }
        }
        return 0;
    }

    private void animateToStop(int position) {
        LatLng stopPosition = new LatLng(stops.get(position).getLat() + latOffset, stops.get(position).getLon());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(stopPosition);
        getMapView().animateCamera(cameraUpdate, ANIMATION_LENGTH, null);
    }

    @Override
    protected void updateStopModeCamera(boolean mapViewExpanded) {
        LatLng newCenter;
        CameraUpdate cameraUpdate;
        if (mapViewExpanded) {
            newCenter = new LatLng(stops.get(currentPosition).getLat(), stops.get(currentPosition).getLon());
            cameraUpdate = CameraUpdateFactory.newLatLng(newCenter);
        } else {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                newCenter = new LatLng(stops.get(currentPosition).getLat(), stops.get(currentPosition).getLon() + lonOffset);
            } else {
                newCenter = new LatLng(stops.get(currentPosition).getLat() + latOffset, stops.get(currentPosition).getLon());
            }
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(newCenter, STOP_ZOOM);
        }
        getMapView().animateCamera(cameraUpdate, ANIMATION_LENGTH, null);
    }

    @Override
    protected void updateData() {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.Shuttles.MODULE_KEY, Constants.SHUTTLES);
        bundle.putString(Constants.Shuttles.PATH_KEY, Constants.Shuttles.ROUTE_INFO_PATH);
        bundle.putString(Constants.Shuttles.URI_KEY, uriString);

        HashMap<String, String> pathparams = new HashMap<>();
        pathparams.put("route", routeId);
        bundle.putString(Constants.Shuttles.PATHS_KEY, pathparams.toString());

        // FORCE THE SYNC
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);

        Timber.d("Requesting Predictions");

        ContentResolver.requestSync(MitMobileApplication.mAccount, MitMobileApplication.AUTHORITY, bundle);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new MitCursorLoader(getActivity(), Uri.parse(uriString), Schema.Stop.ALL_COLUMNS, selectionString, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();
        route.buildFromCursor(data, DBAdapter.getInstance());

        stops = route.getStops();
        int currentPosition = predictionViewPager.getCurrentItem();
        int realPosition = stopViewPagerAdapter.getRealPosition(currentPosition);
        stopViewPagerAdapter.updatePredictions(realPosition, stops.get(realPosition).getPredictions());

        //Update next and previous stops' predictions as well due to view pager storing adjacent fragments
        int previousPosition = (realPosition - 1) % stops.size();
        if (previousPosition < 0) {
            previousPosition += stops.size();
        }
        stopViewPagerAdapter.updatePredictions(previousPosition, stops.get(previousPosition).getPredictions());

        int nextPosition = (realPosition + 1) % stops.size();
        stopViewPagerAdapter.updatePredictions(nextPosition, stops.get(nextPosition).getPredictions());

        updateMapItems((ArrayList) route.getVehicles(), false);
    }

    @Override
    protected void queryDatabase() {
        Cursor cursor = getActivity().getContentResolver().query(Uri.parse(uriString), Schema.Stop.ALL_COLUMNS, selectionString, null, null);
        cursor.moveToFirst();
        route.buildFromCursor(cursor, DBAdapter.getInstance());
        cursor.close();
        updateMapItems((ArrayList) route.getVehicles(), false);
    }

    @Override
    public void onMapLoaded() {
        mitMapView.adjustCameraToShowInHeader(false, 0, getActivity().getResources().getConfiguration().orientation);
        LatLng target = mitMapView.getMap().getCameraPosition().target;
        latOffset = target.latitude - stops.get(getStartPosition()).getLat();
        lonOffset = target.longitude - stops.get(getStartPosition()).getLon();
    }
}
