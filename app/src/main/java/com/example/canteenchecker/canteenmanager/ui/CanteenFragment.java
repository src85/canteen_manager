package com.example.canteenchecker.canteenmanager.ui;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.canteenchecker.canteenmanager.CanteenManagerApplication;
import com.example.canteenchecker.canteenmanager.R;
import com.example.canteenchecker.canteenmanager.core.Canteen;
import com.example.canteenchecker.canteenmanager.proxy.ServiceProxy;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CanteenFragment extends Fragment {

    private static final String TAG = CanteenFragment.class.toString();
    private static final String CANTEEN_ID_KEY = "canteenId";
    private static final int DEFAULT_MAP_ZOOM_FACTOR = 17;

    public static Fragment create() {
        CanteenFragment canteenFragment = new CanteenFragment();
        Bundle arguments = canteenFragment.getArguments();
        if (arguments == null) {
            arguments = new Bundle();
        }
        canteenFragment.setArguments(arguments);
        return canteenFragment;
    }

    private Canteen canteen;
    private Marker marker;

    private View viwProgress;
    private View viwContent;

    private EditText edtCanteenName;
    private EditText edtCanteenAddress;
    private EditText edtCanteenPhone;
    private EditText edtCanteenWeb;
    private EditText edtSetCanteenMeal;
    private EditText edtSetCanteenMealPrice;
    private TextView txvCanteenWaitingTime;
    private SeekBar sbaCanteenSetWaitingTime;

    private Button btnChangesRevert;
    private Button btnCanteenSave;

    private SupportMapFragment mpfMap;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit_canteen, container, false);

        viwProgress = view.findViewById(R.id.viwProgress);
        viwContent = view.findViewById(R.id.viwContent);

        edtCanteenName = view.findViewById(R.id.edtCanteenName);
        edtCanteenAddress = view.findViewById(R.id.edtCanteenAddress);
        edtCanteenAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                new Timer().schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                updateMap(edtCanteenAddress.getText().toString());
                            }
                        },
                        2000
                );
            }
        });

        edtCanteenPhone = view.findViewById(R.id.edtCanteenPhone);
        edtCanteenWeb = view.findViewById(R.id.edtCanteenWeb);
        edtSetCanteenMeal = view.findViewById(R.id.edtSetCanteenMeal);
        edtSetCanteenMealPrice = view.findViewById(R.id.edtSetCanteenMealPrice);
        txvCanteenWaitingTime = view.findViewById(R.id.txvCanteenWaitingTime);

        sbaCanteenSetWaitingTime = view.findViewById(R.id.sbaCanteenWaitingTime);
        sbaCanteenSetWaitingTime.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        txvCanteenWaitingTime.setText(String.valueOf(progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                }
        );

        btnChangesRevert = view.findViewById(R.id.btnChangesRevert);
        btnChangesRevert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUIFields();
            }
        });

        btnCanteenSave = view.findViewById(R.id.btnCanteenSave);
        btnCanteenSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCanteen();
            }
        });

        loadCanteen(); // load canteen

        mpfMap = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mpfMap);
        mpfMap.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {

                UiSettings uiSettings = googleMap.getUiSettings();
                uiSettings.setAllGesturesEnabled(false);
                uiSettings.setZoomControlsEnabled(true);

                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng point) {
                        List<Address> addresses = new ArrayList<>();
                        try {
                            addresses = new Geocoder(getActivity())
                                    .getFromLocation(point.latitude, point.longitude, 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Address address = addresses.get(0);

                        if (address != null) {
                            edtCanteenAddress.setText(address.getAddressLine(0));

                            if (marker != null) {
                                marker.remove();
                            }
                        }

                        // place marker where clicked
                        marker = googleMap.addMarker(new MarkerOptions()
                                .position(point).title(getString(R.string.lab_canteen))
                                .icon(BitmapDescriptorFactory
                                        .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                    }
                }); // setOnMapClickListener()
            }
        }); // getMapAsync()

        return view;
    }

    private void loadCanteen() {

        new AsyncTask<String, Void, Canteen>() {
            @Override
            protected Canteen doInBackground(String... strings) {
                try {
                    return new ServiceProxy().getCanteen();
                } catch (IOException e) {
                    Log.e(TAG, getString(R.string.msg_LoadingCanteenFailed), e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Canteen canteen) {
                if (canteen != null) {
                    CanteenFragment.this.canteen = canteen;
                    getArguments().putString(CANTEEN_ID_KEY, canteen.getId());
                    getActivity().getIntent().putExtra(CANTEEN_ID_KEY, canteen.getId());
                    CanteenManagerApplication.getInstance().setCanteenId(canteen.getId());

                    setUIFields();
                    updateMap(canteen.getAddress());

                    viwProgress.setVisibility(View.GONE);
                    viwContent.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getActivity(),
                            R.string.msg_LoadingCanteenFailed, Toast.LENGTH_LONG).show();
                }
            }
        }.execute(CanteenManagerApplication.getInstance().getAuthenticationToken());
    }

    private void setUIFields() {

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(canteen.getName());
        }

        edtCanteenName.setText(canteen.getName());
        edtCanteenAddress.setText(canteen.getAddress());
        edtCanteenPhone.setText(canteen.getPhoneNumber());
        edtCanteenWeb.setText(canteen.getWebsite());
        edtSetCanteenMeal.setText(canteen.getMeal());
        edtSetCanteenMealPrice.setText(String.format("%.2f", canteen.getMealPrice()));
        txvCanteenWaitingTime.setText(String.valueOf(canteen.getAverageWaitingTime()));
        sbaCanteenSetWaitingTime.setProgress(canteen.getAverageWaitingTime());
    }

    private void updateMap(final String address) {
        new AsyncTask<String, Void, LatLng>() {
            @Override
            protected LatLng doInBackground(String... strings) {
                LatLng location = null;
                Geocoder geocoder = new Geocoder(getActivity());
                try {
                    List<Address> addresses = geocoder.getFromLocationName(strings[0], 1);
                    if (addresses != null && addresses.size() > 0) {
                        Address address = addresses.get(0);
                        location = new LatLng(address.getLatitude(), address.getLongitude());
                    } else {
                        Log.w(TAG, "Resolving failed!");
                    }
                } catch (IOException e) {
                    Log.w(TAG, "Resolving of Adress failed.");
                }
                return location;
            }

            @Override
            protected void onPostExecute(final LatLng latLng) {
                mpfMap.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        googleMap.clear();

                        if (latLng != null) {
                            marker = googleMap.addMarker(new MarkerOptions()
                                    .position(latLng));
                            googleMap
                                    .animateCamera(CameraUpdateFactory
                                            .newLatLngZoom(latLng,
                                                    DEFAULT_MAP_ZOOM_FACTOR));
                        } else {
                            googleMap.animateCamera(CameraUpdateFactory
                                    .newLatLngZoom(new LatLng(0, 0), 0));
                        }
                    }
                });
            }
        }.execute(address);
    }

    private void saveCanteen() {

        float mealPrice;
        try {
            mealPrice = Float.parseFloat(edtSetCanteenMealPrice.getText()
                    .toString().replaceAll(",", "."));
        } catch (Exception e) {
            mealPrice = this.canteen.getMealPrice();
        }

        setUIEnabled(false);
        viwProgress.setVisibility(View.VISIBLE);
        viwContent.setVisibility(View.GONE);
        new AsyncTask<Object, Void, Void>() {
            @Override
            protected Void doInBackground(Object... objects) {
                new ServiceProxy().updateCanteen((String) objects[0],
                        new Canteen((String) objects[1], (String) objects[2],
                                (String) objects[3], (String) objects[4],
                                (String) objects[5], (String) objects[6],
                                (float) objects[7], (float) objects[8],
                                (int) objects[9], null));
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                new AsyncTask<Void, Void, Canteen>() {
                    @Override
                    protected Canteen doInBackground(Void... voids) {
                        try {
                            return new ServiceProxy().getCanteen();
                        } catch (IOException e) {
                            Log.e(TAG, getString(R.string.msg_LoadingCanteenFailed), e);
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Canteen canteen) {
                        if (canteen != null) {
                            CanteenFragment.this.canteen = canteen;
                            setUIFields();
                            updateMap(canteen.getAddress());
                        } else {
                            Log.e(TAG, getString(R.string.msg_LoadingCanteenFailed));
                        }
                        setUIEnabled(true);
                        viwProgress.setVisibility(View.GONE);
                        viwContent.setVisibility(View.VISIBLE);
                    }
                }.execute();
            }
        }.execute(
                CanteenManagerApplication.getInstance().getAuthenticationToken(),
                canteen.getId(),//1
                edtCanteenName.getText().toString(),//2
                edtCanteenAddress.getText().toString(),//3
                edtCanteenPhone.getText().toString(),//4
                edtCanteenWeb.getText().toString(),//5
                edtSetCanteenMeal.getText().toString(),//6
                mealPrice,//7
                canteen.getAverageRating(),//8
                sbaCanteenSetWaitingTime.getProgress()//9
        );
    }

    private void setUIEnabled(boolean enabled) {
        edtCanteenName.setEnabled(enabled);
        edtCanteenAddress.setEnabled(enabled);
        edtCanteenPhone.setEnabled(enabled);
        edtCanteenWeb.setEnabled(enabled);
        edtSetCanteenMeal.setEnabled(enabled);
        edtSetCanteenMealPrice.setEnabled(enabled);
        sbaCanteenSetWaitingTime.setEnabled(enabled);
        btnCanteenSave.setEnabled(enabled);
        btnChangesRevert.setEnabled(enabled);
    }

}
