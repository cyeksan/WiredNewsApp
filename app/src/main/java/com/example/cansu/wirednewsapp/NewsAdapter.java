package com.example.cansu.wirednewsapp;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class NewsAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<HashMap<String, String>> stringList;
    private ArrayList<HashMap<String, Drawable>> drawableList;
    private static LayoutInflater inflater = null;
    private HashMap<String, String> stringHashMap;
    private HashMap<String, Drawable> drawableHashMap;
    private String author;
    private String description;
    public static View v;
    private Resources res;
    private HashMap<String, String> mData = new HashMap();
    public static String[] mKeys;


    public NewsAdapter(Context c, ArrayList<HashMap<String, String>> s, ArrayList<HashMap<String, Drawable>> d) {

        context = c;
        stringList = s;
        drawableList = d;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mKeys = mData.keySet().toArray(new String[stringList.size()]);

    }


    public int getCount() {

        return stringList.size();

    }


    public Object getItem(int position) {

        return mData.get(mKeys[position]);

    }


    public long getItemId(int position) {

        return position;

    }


    @Override

    public View getView(int position, View convertView, ViewGroup parent) {

        v = convertView;

        if (convertView == null)

            v = inflater.inflate(R.layout.list_item, null);

        ImageView imageView = v.findViewById(R.id.thumbnail);

        TextView titleTv = v.findViewById(R.id.title);

        TextView descriptionTv = v.findViewById(R.id.description);

        TextView authorTv = v.findViewById(R.id.author);

        TextView dateTv = v.findViewById(R.id.date);


        try {

            stringHashMap = stringList.get(position);

        } catch (IndexOutOfBoundsException e) {

            e.printStackTrace();

        }

        res = context.getResources();

        // Get image from hashmap and set it to the ImageView.

        try {

            drawableHashMap = drawableList.get(position);

        } catch (IndexOutOfBoundsException e) {

            e.printStackTrace();

        }

        imageView.setImageDrawable(drawableHashMap.get(res.getString(R.string.thumbnail)));

        // Get title from hashmap and set it to Title TextView.
        titleTv.setText(stringHashMap.get(res.getString(R.string.title)));

        // Get description from hashmap and set it to Description TextView.
        description = stringHashMap.get(res.getString(R.string.description));
        descriptionTv.setText(description);

        if (description != null && !description.isEmpty()) {

            descriptionTv.setVisibility(View.VISIBLE);

        }

        // Get author from hashmap and set it to Author TextView.

        author = stringHashMap.get(res.getString(R.string.author));
        authorTv.setText(String.format("%s%s", res.getString(R.string.written_by), author));

        if (author != null && !author.isEmpty()) {

            authorTv.setVisibility(View.VISIBLE);

        }

        // Get date from hashmap, format it by parseDateToddMMyyyy method and set it to Date TextView.
        String date = stringHashMap.get(res.getString(R.string.web_publication_date));
        String d = date.replace("T", " ");
        String resultDate = d.replace("Z", "");
        String formattedDate = parseDateToddMMyyyy(resultDate);
        dateTv.setText(formattedDate);

        if (formattedDate != null && !formattedDate.isEmpty()) {

            dateTv.setVisibility(View.VISIBLE);

        }

        return v;

    }

    // In this method, date format is converted to a simpler format:

    private String parseDateToddMMyyyy(String date) {

        String inputPattern = "yyyy-MM-dd HH:mm:ss";
        String outputPattern = "dd-MMM-yyyy h:mm a";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date mDate;
        String mStr = null;

        try {

            mDate = inputFormat.parse(date);
            mStr = outputFormat.format(mDate);

        } catch (ParseException e) {

            e.printStackTrace();

        }

        return mStr;

    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}