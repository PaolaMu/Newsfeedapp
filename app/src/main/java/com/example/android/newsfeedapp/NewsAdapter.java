package com.example.android.newsfeedapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class NewsAdapter extends ArrayAdapter<News> {

    private static final String LOCATION_SEPARATOR = " of ";

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /**
     * Constructs a new {@link NewsAdapter}.
     *
     * @param context of the app
     * @param news    is the list of news, which is the data source of the adapter
     */
    public NewsAdapter(Context context, ArrayList<News> news) {
        super(context, 0, news);
    }

    /**
     * Returns a news item list view that displays information about the news at the given position
     * in the list of news.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.news_item_list, parent, false);
        }

        // Find the news at the given position in the list of news
        News currentNews = getItem(position);

        // Find the TextView in the news_item_list.xml layout with the ID title
        TextView title_nameTextView = listItemView.findViewById(R.id.title);
        // Get the name from the current news and
        // set this text on the title TextView
        title_nameTextView.setText(currentNews.getTitleName());

        // Find the TextView in the news_item_list.xml layout with the ID section
        TextView sectionTextView = listItemView.findViewById(R.id.section);
        sectionTextView.setText(currentNews.getSectionName());

        String articleAuthor = "By " + currentNews.getContributor() + " ";
        /* Find TextView in news_item_list.xml layout with ID author */
        TextView contributorTextVIew = listItemView.findViewById(R.id.author);
        contributorTextVIew.setText(articleAuthor);

        /* Find ImageView in news_item_list.xml layout with ID news_img */
        Bitmap articlePhoto = currentNews.getThumbnail();
        ImageView bitmapView = listItemView.findViewById(R.id.news_img);
        if (articlePhoto != null) {
            bitmapView.setImageBitmap(articlePhoto);
        } else {
            bitmapView.setImageResource(R.drawable.logo);
        }

        try {
            Date dateObject = sdf.parse(currentNews.getDate());
            String formattedDate = formatDate(dateObject);
            // Display the date of the current news in that TextView
            TextView dateView = (TextView) listItemView.findViewById(R.id.date);
            dateView.setText(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Return the news item list view that is now showing the appropriate data
        return listItemView;
    }
    /**
     * Return the formatted date string (i.e. "Feb 12, 1987") from a Date object.
     */
    private String formatDate(Date dateObject) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("LLL dd, yyyy");
        return dateFormat.format(dateObject);
    }
}


