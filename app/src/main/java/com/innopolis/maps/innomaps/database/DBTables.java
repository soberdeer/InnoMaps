package com.innopolis.maps.innomaps.database;

import static com.innopolis.maps.innomaps.database.TableTypeFields.*;


public class DBTables{

    public static abstract class TableColumns {

        public static String TABLE_EVENTS_CREATE = CREATE_TABLE + TableFields.EVENTS + " ("
                + ID_INTEGER + SUMMARY_TEXT + LINK_TEXT + START_TEXT + END_TEXT
                + EVENTID_TEXT + CHECKED_TEXT;

        public static String TABLE_EVENT_TYPE_CREATE = CREATE_TABLE + TableFields.EVENT_TYPE + " ("
                + ID_INTEGER + SUMMARY_TEXT + DESCRIPTION_TEXT + CREATOR_NAME_TEXT
                + CREATOR_EMAIL_TEXT;

       /* Consider eliminating table location when locations of
        * events in google calendar will be fixed*/
        public static String TABLE_LOCATION_CREATE = CREATE_TABLE + TableFields.LOCATION + " ("
                + ID_INTEGER + EVENTID_TEXT + BUILDING_TEXT + FLOOR_TEXT + ROOM_TEXT
                + LATITUDE_TEXT + LONGITUDE_TEXT;

        public static String TABLE_EVENT_POI_CREATE = CREATE_TABLE + TableFields.EVENT_POI + " ("
                + ID_INTEGER + EVENTID_TEXT + POI_ID_TEXT;

        public static String TABLE_POI_CREATE = CREATE_TABLE + TableFields.POI + " ("
                + ID_INTEGER + POI_NAME_TEXT + BUILDING_TEXT + FLOOR_TEXT + ROOM_TEXT
                + LATITUDE_TEXT + LONGITUDE_TEXT + TYPE_TEXT + ATTRIBUTE_TEXT;

    }

    public static String createTable(String string) {
        return string = removeLastChar(string) + ")";
    }


    private static String removeLastChar(String str) {
        return str.substring(0, str.length() - 2);
    }


}
