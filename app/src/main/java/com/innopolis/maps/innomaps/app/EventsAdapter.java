package com.innopolis.maps.innomaps.app;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.innopolis.maps.innomaps.R;
import com.innopolis.maps.innomaps.utils.Utils;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import xyz.hanks.library.SmallBang;

/**
 * Created by Nikolay on 04.02.2016.
 */
public class EventsAdapter extends BaseAdapter {

    Context ctx;
    LayoutInflater lInflater;
    ArrayList<HashMap<String, String>> events;
    DBHelper dbHelper;
    SQLiteDatabase database;
    Activity activity;
    FragmentManager fm;

    public EventsAdapter(Context ctx, FragmentManager fm, ArrayList<HashMap<String, String>> events, Activity activity) {
        this.ctx = ctx;
        lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.events = events;
        this.activity = activity;
        this.fm = fm;
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public Object getItem(int position) {
        return events.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.single_event, parent, false);
        }

        final HashMap<String, String> values = events.get(position);

        TextView timeLeft = (TextView) view.findViewById(R.id.timeLeft);
        TextView nameEvent = (TextView) view.findViewById(R.id.nameEvent);
        TextView location = (TextView) view.findViewById(R.id.location);
        TextView dateTime = (TextView) view.findViewById(R.id.dateTime);
        final CheckBox favCheckBox = (CheckBox) view.findViewById(R.id.favCheckBox);

        nameEvent.setText(values.get(DBHelper.COLUMN_SUMMARY));
        String[] locationText = new String[3];
        locationText[0] = (values.get(DBHelper.COLUMN_BUILDING) != null) ? values.get(DBHelper.COLUMN_BUILDING) : "null";
        locationText[1] = (values.get(DBHelper.COLUMN_FLOOR) != null) ? values.get(DBHelper.COLUMN_FLOOR) : "null";
        locationText[2] = (values.get(DBHelper.COLUMN_ROOM) != null) ? values.get(DBHelper.COLUMN_ROOM) : "null";
        location.setText(StringUtils.join(Utils.clean(locationText), ", "));
        Date startTime = null;
        Date endTime = null;

        try {
            startTime = Utils.googleTimeFormat.parse(values.get(DBHelper.COLUMN_START));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (startTime != null) {
            dateTime.setText(Utils.commonTime.format(startTime));
            timeLeft.setText(Utils.prettyTime.format(startTime));
        }
        if (values.get("checked").equals("1")) {
            favCheckBox.setChecked(true);
        } else {
            favCheckBox.setChecked(false);
        }
        final SmallBang mSmallBang = SmallBang.attach2Window(activity);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new DetailedEvent();
                Bundle bundle = new Bundle();
                bundle.putString("eventID", values.get(DBHelper.COLUMN_EVENT_ID));
                fragment.setArguments(bundle);
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.content_frame, fragment).addToBackStack("Detailed");
                ft.commit();
            }

        });
        favCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSmallBang.bang(favCheckBox);
                String isFav = (favCheckBox.isChecked()) ? "1" : "0";
                String eventID = values.get(DBHelper.COLUMN_EVENT_ID);
                ContentValues cv = new ContentValues();
                dbHelper = new DBHelper(ctx);
                database = dbHelper.getWritableDatabase();
                cv.put(DBHelper.COLUMN_FAV, isFav);
                database.update(DBHelper.TABLE1, cv, "eventID = ?", new String[]{eventID});
                dbHelper.close();
            }
        });
        return view;
    }

    HashMap<String, String> getEventRow(int position) {
        return ((HashMap<String, String>) getItem(position));
    }

}
