package com.innopolis.maps.innomaps.events;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.apradanas.simplelinkabletext.Link;
import com.apradanas.simplelinkabletext.LinkableTextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.innopolis.maps.innomaps.R;
import com.innopolis.maps.innomaps.app.MainActivity;
import com.innopolis.maps.innomaps.database.DBHelper;
import com.innopolis.maps.innomaps.utils.Utils;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import xyz.hanks.library.SmallBang;

import static com.innopolis.maps.innomaps.database.TableFields.BUILDING;
import static com.innopolis.maps.innomaps.database.TableFields.END;
import static com.innopolis.maps.innomaps.database.TableFields.EVENTS;
import static com.innopolis.maps.innomaps.database.TableFields.EVENT_TYPE;
import static com.innopolis.maps.innomaps.database.TableFields.FAV;
import static com.innopolis.maps.innomaps.database.TableFields.FLOOR;
import static com.innopolis.maps.innomaps.database.TableFields.LATITUDE;
import static com.innopolis.maps.innomaps.database.TableFields.LINK;
import static com.innopolis.maps.innomaps.database.TableFields.LOCATION;
import static com.innopolis.maps.innomaps.database.TableFields.LONGITUDE;
import static com.innopolis.maps.innomaps.database.TableFields.ROOM;
import static com.innopolis.maps.innomaps.database.TableFields.START;
import static com.innopolis.maps.innomaps.database.TableFields.SUMMARY;


public class DetailedEvent extends Fragment {

    Context context;
    DBHelper dbHelper;
    SQLiteDatabase database;

    TextView eventName;
    TextView timeLeft;
    TextView location;
    TextView dateTime;
    LinkableTextView description;
    TextView duration;


    private static GoogleMap mMap;
    private static UiSettings mSettings;
    static SupportMapFragment mSupportMapFragment;


    final private String NULL = "";

    String summary, htmlLink, start, end, descriptionStr, creator,
            eventID, building, floor, room, latitude, longitude, checked;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.detailed_menu_toolbar, menu);
        MenuItem item = menu.findItem(R.id.toolbar_share);
        ShareActionProvider shareAction = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        //Consider changing content for relevant share information
        shareIntent.putExtra(Intent.EXTRA_TEXT, (eventName.getText() + " begins in " + dateTime.getText() + ". Join us!"));
        shareAction.setShareIntent(shareIntent);

        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        ((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context = getActivity().getApplicationContext();
        View view = inflater.inflate(R.layout.event_desc, container, false);
        dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();
        eventName = (TextView) view.findViewById(R.id.eventName);
        timeLeft = (TextView) view.findViewById(R.id.timeLeft);
        location = (TextView) view.findViewById(R.id.location);
        dateTime = (TextView) view.findViewById(R.id.dateTime);
        description = (LinkableTextView) view.findViewById(R.id.description);
        description.setMovementMethod(new ScrollingMovementMethod());
        duration = (TextView) view.findViewById(R.id.duration);
        final CheckBox favCheckBox = (CheckBox) view.findViewById(R.id.favCheckBox);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            eventID = bundle.getString("eventID", NULL);
        }
        final Cursor cursor = database.query(EVENTS, null, "eventID=?", new String[]{eventID}, null, null, null);
        cursor.moveToFirst();
        do {
            int summary, htmlLink, start, end, checked;
            summary = cursor.getColumnIndex(SUMMARY);
            htmlLink = cursor.getColumnIndex(LINK);
            start = cursor.getColumnIndex(START);
            end = cursor.getColumnIndex(END);
            checked = cursor.getColumnIndex(FAV);
            this.summary = cursor.getString(summary);
            this.htmlLink = cursor.getString(htmlLink);
            this.start = cursor.getString(start);
            this.end = cursor.getString(end);
            this.checked = cursor.getString(checked);
            String[] summaryArgs = new String[]{cursor.getString(summary)};
            Cursor cursor1 = database.query(EVENT_TYPE, null, "summary=?", summaryArgs, null, null, null);
            cursor1.moveToFirst();
            int description = cursor1.getColumnIndex("description");
            int creator_name = cursor1.getColumnIndex("creator_name");
            this.descriptionStr = cursor1.getString(description);
            this.creator = cursor1.getString(creator_name);

            cursor1.close();
        } while (cursor.moveToNext());
        cursor.close();
        final String[] eventIDArgs = new String[]{eventID};
        Cursor locationC = database.query(LOCATION, null, "eventID=?", eventIDArgs, null, null, null);
        if (locationC.moveToFirst()) {
            building = locationC.getString(locationC.getColumnIndex(BUILDING));
            floor = locationC.getString(locationC.getColumnIndex(FLOOR));
            room = locationC.getString(locationC.getColumnIndex(ROOM));
            latitude = locationC.getString(locationC.getColumnIndex(LATITUDE));
            longitude = locationC.getString(locationC.getColumnIndex(LONGITUDE));
        }
        database.close();

        Link linkUsername = new Link(Pattern.compile("(@\\w+)"))
                .setUnderlined(false)
                .setTextColor(Color.parseColor("#D00000"))
                .setTextStyle(Link.TextStyle.BOLD)
                .setClickListener(new Link.OnClickListener() {
                    @Override
                    public void onClick(String text) {
                        telegramTransfer(text, text);
                    }
                });

        List<Link> links = new ArrayList<>();
        links.add(linkUsername);

        eventName.setText(summary);
        Date startDate = null;
        Date endDate = null;
        try {
            startDate = Utils.googleTimeFormat.parse(start);
            endDate = Utils.googleTimeFormat.parse(end);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        timeLeft.setText(Utils.prettyTime.format(startDate));
        String[] locationText = {building, floor, room};
        location.setText(StringUtils.join(Utils.clean(locationText), ", "));
        dateTime.setText(Utils.commonTime.format(startDate));
        Long durationTime = TimeUnit.MILLISECONDS.toMinutes(endDate.getTime() - startDate.getTime());
        duration.setText("Duration: " + String.valueOf(durationTime) + "min");

        description.setText(descriptionStr)
                .addLinks(links)
                .build();


        if (checked.equals("1")) {
            favCheckBox.setChecked(true);
        } else {
            favCheckBox.setChecked(false);
        }
        final SmallBang mSmallBang = SmallBang.attach2Window(getActivity());

        favCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSmallBang.bang(favCheckBox);
                String isFav = (favCheckBox.isChecked()) ? "1" : "0";
                ContentValues cv = new ContentValues();
                dbHelper = new DBHelper(context);
                database = dbHelper.getWritableDatabase();
                cv.put(FAV, isFav);
                database.update(EVENTS, cv, "eventID = ?", new String[]{eventID});
                dbHelper.close();
            }
        });


        FloatingActionButton fabButton = (FloatingActionButton) view.findViewById(R.id.fabButton);
        fabButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new MapFragmentAskForRouteDialog();
                Bundle bundle = new Bundle();
                bundle.putString("summary", summary);
                newFragment.setArguments(bundle);
                newFragment.show(getActivity().getSupportFragmentManager(), "FindRoute");

            }
        });
        initializeMap(latitude, longitude);


        return view;

    }


    private void telegramTransfer(final String dialogText, final String telegramLink) {

        description.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TelegramOpenDialog();
                Bundle bundle = new Bundle();
                bundle.putString("dialogText", dialogText);
                bundle.putString("dialogUrl", telegramLink);
                newFragment.setArguments(bundle);
                newFragment.show(getActivity().getSupportFragmentManager(), "Telegram");
            }
        });
    }


    public void initializeMap(final String latitude, final String longitude) {
        mSupportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapDesc);
        if (mSupportMapFragment == null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            mSupportMapFragment = SupportMapFragment.newInstance();
            fragmentTransaction.replace(R.id.mapWrapper, mSupportMapFragment).commit();
        }
        if (mSupportMapFragment != null) {
            mSupportMapFragment.getMapAsync(new OnMapReadyCallback() {

                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    mSettings = mMap.getUiSettings();
                    mSettings.setMapToolbarEnabled(false);
                    mSettings.setMyLocationButtonEnabled(false);
                    mMap.setMyLocationEnabled(true);
                    if (latitude != null && longitude != null) {
                        LatLng position = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
                        mMap.addMarker(new MarkerOptions().position(position).title(summary));
                    }

                    mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

                        @Override
                        public void onInfoWindowClick(Marker marker) {
                            DialogFragment newFragment = new MapFragmentAskForRouteDialog();
                            Bundle bundle = new Bundle();
                            bundle.putString("summary", summary);
                            newFragment.setArguments(bundle);
                            newFragment.show(getActivity().getSupportFragmentManager(), "FindRoute");
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Tracking the screen view
        MainActivity.getInstance().trackScreenView("Detailed Events Fragment");
    }


}