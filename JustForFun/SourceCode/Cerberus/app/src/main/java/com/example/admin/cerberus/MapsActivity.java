package com.example.admin.cerberus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.model.JSONParser;
import com.example.admin.model.ParkingView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.thaonguyen.WSManager.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    String currentID;

    public class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        View myContentsView;

        public MyInfoWindowAdapter() {
            myContentsView = getLayoutInflater().inflate(R.layout.info_window_custom_layout, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            TextView tvTitle = ((TextView) myContentsView.findViewById(R.id.title));
            tvTitle.setText(marker.getTitle());

            TextView tvSnippet = ((TextView) myContentsView.findViewById(R.id.snippet));
            tvSnippet.setText(marker.getSnippet());

            return myContentsView;
        }
    }

    private static final int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String PERMISSION_TAG = "PERMISSIONS";
    private static final String LOCATION_TAG = "LOCATION";
    private static final String ERROR_TAG = "ERROR";

    String DATABASE_NAME = "dbCerberus.sqlite";
    private static final String DB_PATH_SUFFIX = "/databases/";
    SQLiteDatabase database = null;

    int id;
    String name = "";
    String address = "";
    String image = "";
    String banner = "";
    double latitude;
    double longitude;

    FloatingActionButton fabMaps;
    LinearLayout layoutBook;
    LinearLayout layoutRoute;

    private Animation fabMenuOpenAnimation;
    private Animation fabMenuCloseAnimation;
    private Animation fabOpenAnimation;
    private Animation fabCloseAnimation;
    private boolean isFabMenuOpen = false;

    ConnectAsyncTask connectAsyncTask;
    boolean doAsyncTask;
    ArrayList<Polyline> linesList = new ArrayList<>();

    ArrayList<ParkingView> parkingList = new ArrayList<>();
    LatLng source;
    LatLng destination;
    LatLng des = null;

    private boolean binded = false;
    private WSManagerService wsManagerService;

    JSONArray parkingLotInformationJsonArray;

    Handler handlerOpen = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Toast.makeText(getApplicationContext(), "OPEN", Toast.LENGTH_SHORT).show();

            try {
                JSONObject obj = new JSONObject();
                obj.put("act", "login");
                wsManagerService.sendMessage(obj.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    Handler handlerError = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);


        }
    };

    Handler handlerMessage = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            String json = (String) msg.obj;
            Toast.makeText(getApplicationContext(), json, Toast.LENGTH_SHORT).show();
            try {
                JSONObject object = new JSONObject(json);
                String type = object.getString("type");

                if (type.equals("booking")) {
                    parkingLotInformationJsonArray = object.getJSONArray("data");
                    addMarkerToMap();
                } else if (type.equals("bookFail")) {
                    //Toast.makeText(getApplicationContext(), "Your slot has bean expired", Toast.LENGTH_LONG).show();
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MapsActivity.this);
                    alertDialogBuilder.setTitle("Your booked slot has been expired");
                    alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    alertDialogBuilder.show();
                } else if (type.equals("sendmap")) {
                    JSONArray slots = object.getJSONArray("data");

                    Intent intent = new Intent(MapsActivity.this, ShowParkingMap.class);
                    intent.putExtra("data", slots.toString());
                    startActivity(intent);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

//            Toast.makeText(getApplicationContext(), json, Toast.LENGTH_SHORT).show();
//            try {
//                parkingLotInformationJsonArray = new JSONArray(json);
//
//                addMarkerToMap();
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }

        }
    };

    private String findCapacityInfo(int parkingLotId) {
        String result = "";

        try {
            if (parkingLotInformationJsonArray != null) {
                JSONObject object;
                for (int i = 0; i < parkingLotInformationJsonArray.length(); i++) {
                    object = parkingLotInformationJsonArray.getJSONObject(i);
                    if (object.getInt("id") == parkingLotId) {
                        int cap = object.getInt("amout");
                        int available = object.getInt("blank");

                        result = "Slot: " + available + "/" + cap;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    ;

    private void addMarkerToMap() {
        mMap.clear();

        for (ParkingView parking : parkingList) {
            double distance = getDistance(
                    source.latitude,
                    source.longitude,
                    parking.getLatitude(),
                    parking.getLongitude()
            );
            if (distance <= 10) {
                int id = parking.getId();
                String name = parking.getName();
                String address = parking.getAddress();
                String banner = parking.getBanner();
                double latitude = parking.getLatitude();
                double longitude = parking.getLongitude();

                mMap.addMarker(
                        new MarkerOptions()
                                .position(new LatLng(latitude, longitude))
                                .title(name)
                                .snippet("Address: " + address + "\n" + findCapacityInfo(id))
                                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons()))
                );
            }
        }
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            WSManagerService.LocalWeatherBinder binder = (WSManagerService.LocalWeatherBinder) iBinder;

            wsManagerService = binder.getService();
            wsManagerService.setOnOpenCallback(new IOnOpenWSManager() {
                @Override
                public void onOpen() {
                    Message msg = handlerMessage.obtainMessage(1, "OPEN");
                    handlerOpen.sendMessage(msg);
                }
            });
            wsManagerService.setOnCloseCallback(new IOnCloseWSManager() {
                @Override
                public void onClose() {
                    Message msg = handlerMessage.obtainMessage(1, "CLOSE");
                    //handlerMessage.sendMessage(msg);
                }
            });
            wsManagerService.setOnErrorCallback(new IOnErrorWSManager() {
                @Override
                public void onError(Exception e) {
                    Message msg = handlerMessage.obtainMessage(1, "ERROR");
                    //handlerMessage.sendMessage(msg);
                }
            });
            wsManagerService.setOnMessageCallback(new IOnMessageWSManager() {
                @Override
                public void onMessage(String s) {
                    Message msg = handlerMessage.obtainMessage(1, s);
                    handlerMessage.sendMessage(msg);
                }
            });

            wsManagerService.connect();

            binded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            binded = false;
        }
    };

    boolean firstOpen = true;
    private GoogleMap mMap;
    GoogleMap.OnMyLocationChangeListener locationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            source = new LatLng((location.getLatitude()), location.getLongitude());

            if (firstOpen) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(source, 14f));
                firstOpen = false;
            }

            // addMarkerToMap();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        processCopy();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, WSManagerService.class);
        this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (binded) {
            this.unbindService(serviceConnection);
            binded = false;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getCurrentPosition();
        getParkingFromDatabase();

        addControls();
        addEvents();

        mMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
    }

    private void addEvents() {
        fabMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFabMenuOpen) {
                    collapseFabMenu();
                    isFabMenuOpen = false;

                    destination = new LatLng(des.latitude, des.longitude);
                } else {
                    expandFabMenu();
                    isFabMenuOpen = true;
                }
            }
        });

        layoutRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFabMenuOpen) {
                    collapseFabMenu();
                    isFabMenuOpen = false;
                }

                if (des != null) {
                    destination = new LatLng(des.latitude, des.longitude);
                }
                if (source != null && destination != null) {
                    Log.i(LOCATION_TAG, Double.toString(source.latitude));
                    Log.i(LOCATION_TAG, Double.toString(source.longitude));
                    Log.i(LOCATION_TAG, Double.toString(destination.latitude));
                    Log.i(LOCATION_TAG, Double.toString(destination.longitude));

                    String url = makeURL(source.latitude, source.longitude, destination.latitude, destination.longitude);
                    doAsyncTask = true;
                    connectAsyncTask = new ConnectAsyncTask(url);
                    connectAsyncTask.execute();
                }
            }
        });

        layoutBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFabMenuOpen) {
                    collapseFabMenu();
                    isFabMenuOpen = false;
                }

//                Intent intent = new Intent(MapsActivity.this, BookActivity.class);
//                startActivity(intent);

                try {
                    JSONObject obj = new JSONObject();
                    obj.put("act", "booking");

                    int id = -1;

                    switch (currentID) {
                        case "m1":
                            id = 2;
                            break;
                        case "m2":
                            id = 1;
                            break;
                        case "m3":
                            id = 4;
                            break;
                        default:
                            id = -1;
                            break;
                    }

                    obj.put("id", id);
                    obj.put("uid", "1_2_3_4_");
                    if (id != -1) {
                        wsManagerService.sendMessage(obj.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (isFabMenuOpen) {
                    collapseFabMenu();
                    isFabMenuOpen = false;
                }
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (isFabMenuOpen) {
                    collapseFabMenu();
                    isFabMenuOpen = false;
                }

                des = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);

                currentID = marker.getId();

                return false;
            }
        });

        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                if (isFabMenuOpen) {
                    collapseFabMenu();
                    isFabMenuOpen = false;
                }

                if (linesList.size() > 0) {
                    for (Polyline lines : linesList) {
                        if (lines.getId().equals(polyline.getId())) {
                            lines.setColor(Color.BLUE);
                        } else {
                            lines.remove();
                        }
                    }
                }
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(MapsActivity.this, ShowParkingMap.class);
                intent.putExtra("data", (new JSONArray()).toString());
                startActivity(intent);
            }
        });
    }

    private void addControls() {
        fabMaps = findViewById(R.id.fabMaps);
        layoutBook = findViewById(R.id.layoutBook);
        layoutRoute = findViewById(R.id.layoutRoute);

        fabMenuCloseAnimation = AnimationUtils.loadAnimation(MapsActivity.this, R.anim.fab_closing_menu);
        fabMenuOpenAnimation = AnimationUtils.loadAnimation(MapsActivity.this, R.anim.fab_openning_menu);
        fabCloseAnimation = AnimationUtils.loadAnimation(MapsActivity.this, R.anim.fab_closing);
        fabOpenAnimation = AnimationUtils.loadAnimation(MapsActivity.this, R.anim.fab_openning);
    }

    public Bitmap resizeMapIcons() {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("parking_full", "drawable", getPackageName()));
        return Bitmap.createScaledBitmap(imageBitmap, 60, 60, false);
    }

    private void getCurrentPosition() {
        int buildVer = Build.VERSION.SDK_INT;
        if (buildVer >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.i(PERMISSION_TAG, "ACCESS_FINE_LOCATION granted");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ASK_PERMISSIONS);
                Log.i(PERMISSION_TAG, "ACCESS_FINE_LOCATION revoke");
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationChangeListener(locationChangeListener);
        }
    }

    private void expandFabMenu() {
        layoutRoute.startAnimation(fabMenuOpenAnimation);
        layoutRoute.setVisibility(View.VISIBLE);
        layoutRoute.setClickable(true);

        layoutBook.startAnimation(fabMenuOpenAnimation);
        layoutBook.setVisibility(View.VISIBLE);
        layoutBook.setClickable(true);

        fabMaps.startAnimation(fabOpenAnimation);
    }

    private void collapseFabMenu() {
        layoutRoute.startAnimation(fabMenuCloseAnimation);
        layoutRoute.setVisibility(View.INVISIBLE);
        layoutRoute.setClickable(false);

        layoutBook.startAnimation(fabMenuCloseAnimation);
        layoutBook.setVisibility(View.INVISIBLE);
        layoutBook.setClickable(false);

        fabMaps.startAnimation(fabCloseAnimation);
    }

    private double getDistance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private void getParkingFromDatabase() {
        database = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
        Cursor cursor = database.rawQuery("SELECT * FROM Parking order by name", null);
        cursor.moveToFirst();
        do {
            id = cursor.getInt(0);
            name = cursor.getString(1);
            address = cursor.getString(2);
            image = cursor.getString(3);
            banner = cursor.getString(4);
            latitude = cursor.getDouble(5);
            longitude = cursor.getDouble(6);

            parkingList.add(new ParkingView(id, name, address, image, banner, latitude, longitude));
        }
        while (cursor.moveToNext());
        cursor.close();
    }

    private void processCopy() {
        File dbFile = getDatabasePath(DATABASE_NAME);
        if (!dbFile.exists()) {
            try {
                CopyDataBaseFromAsset();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void CopyDataBaseFromAsset() {
        try {
            InputStream myInput;

            myInput = getAssets().open(DATABASE_NAME);

            //Path to the just created empty db
            String outFileName = getDatabasePath();

            //if the path doesn't exist first, create it
            File f = new File(getApplicationInfo().dataDir + DB_PATH_SUFFIX);
            if (!f.exists())
                f.mkdir();

            // Open the empty db as the output stream
            OutputStream myOutput = new FileOutputStream(outFileName);

            // transfer bytes from the input file to the output file
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }

            // Close the streams
            myOutput.flush();
            myOutput.close();
            myInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private String getDatabasePath() {
        return getApplicationInfo().dataDir + DB_PATH_SUFFIX + DATABASE_NAME;
    }

    public String makeURL(double sourceLat, double sourceLog, double destLat, double destLog) {
        return "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=" +// from
                Double.toString(sourceLat) +
                "," +
                Double.toString(sourceLog) +
                "&destination=" +// to
                Double.toString(destLat) +
                "," +
                Double.toString(destLog) +
                "&sensor=false&mode=driving&alternatives=true" +
                "&key=AIzaSyD5xfOA86xXWOyO10qUq-NkRvCPtnKlc-U";
    }

    public void drawPath(String result) {
        try {
            if (linesList.size() > 0) {
                for (Polyline polyline : linesList) {
                    polyline.remove();
                }
                linesList.clear();
            }

            //Transform the string into a json object
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");

            for (int i = 0; i < routeArray.length(); i++) {
                JSONObject routes = routeArray.getJSONObject(i);
                JSONObject overviewPolyline = routes.getJSONObject("overview_polyline");
                String encodedString = overviewPolyline.getString("points");
                List<LatLng> list = decodePoly(encodedString);

                PolylineOptions options = new PolylineOptions()
                        .width(18)
                        .color(Color.LTGRAY)
                        .geodesic(true)
                        .clickable(true);
                for (int z = 0; z < list.size(); z++) {
                    LatLng point = list.get(z);
                    options.add(point);
                }

                linesList.add(mMap.addPolyline(options));
            }

            if (linesList.size() == 1) {
                linesList.get(0).setColor(Color.BLUE);
            }
        } catch (JSONException e) {
            Log.i(ERROR_TAG, e.getMessage());
        }
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    @SuppressLint("StaticFieldLeak")
    private class ConnectAsyncTask extends AsyncTask<Void, Void, String> {
        private ProgressDialog progressDialog;
        String url;

        ConnectAsyncTask(String urlPass) {
            url = urlPass;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MapsActivity.this);
            progressDialog.setMessage("Fetching route, Please wait...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String json = null;
            if (doAsyncTask) {
                JSONParser jParser = new JSONParser();
                json = jParser.getJSONFromUrl(url);
            }
            return json;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.i(LOCATION_TAG, result);

            progressDialog.dismiss();
            doAsyncTask = false;
            if (result != null) {
                drawPath(result);
            }
        }
    }

}
