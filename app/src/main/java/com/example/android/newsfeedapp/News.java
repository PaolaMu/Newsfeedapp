package com.example.android.newsfeedapp;

import android.graphics.Bitmap;

public class News {
    /**
     * Title of the news
     */
    private String nTitle;

    /**
     * Section of the news
     */
    private String nSection;

    /**
     * Author of the news
     */
    private String nContributor;

    /**
     * Date of the news
     */
    private String nDate;

    /**
     * Website URL of the news
     */
    private String nUrl;

    /**
     * Image of the news
     */
    private Bitmap nThumbnail;


    /**
     * Constructs a new {@link News} object.
     *
     * @param title       is the name of the news
     * @param section     is the location where the news is
     * @param contributor is the creator of the article (from the guardian) where the
     *                    news is public
     * @param date        is the day when the news happened
     * @param url         is the website URL to find more details about the news
     * @param thumbnail   is the image of the news
     */
    public News(String title, String section, String contributor, String date, String url, Bitmap thumbnail) {
        nTitle = title;
        nSection = section;
        nContributor = contributor;
        nDate = date;
        nUrl = url;
        nThumbnail = thumbnail;
    }

    public String getUrl() {
        return nUrl;

    }

    public String getDate() {
        return nDate;
    }

    public String getSection() {
        return nSection;
    }

    public String getTitleName() {
        return nTitle;
    }

    public String getSectionName() {
        return nSection;
    }

    public String getContributor() {
        return nContributor;
    }

    public Bitmap getThumbnail() {
        return nThumbnail;
    }
}


