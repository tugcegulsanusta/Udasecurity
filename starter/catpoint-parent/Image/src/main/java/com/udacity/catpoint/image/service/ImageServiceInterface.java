package com.udacity.catpoint.image.service;

import java.awt.image.BufferedImage;

public interface ImageServiceInterface {
    public boolean imageContainsCat(BufferedImage image, float confidenceThreshhold);

}
