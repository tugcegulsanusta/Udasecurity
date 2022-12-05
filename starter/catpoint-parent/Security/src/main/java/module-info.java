module com.udacity.catpoint.Security {
    requires miglayout;
    requires java.desktop;
    requires java.prefs;
    requires java.sql;
    requires com.udacity.catpoint.Image;
    requires com.google.gson;
    requires com.google.common;
    opens com.udacity.catpoint.security.data to com.google.gson;
}