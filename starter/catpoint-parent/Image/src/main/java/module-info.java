module com.udacity.catpoint.Image {
    requires org.slf4j;
    requires java.desktop;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.core;
    requires software.amazon.awssdk.services.rekognition;
    requires software.amazon.awssdk.regions;
    exports com.udacity.catpoint.image.service;
}