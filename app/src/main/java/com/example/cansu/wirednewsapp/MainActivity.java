package com.example.cansu.wirednewsapp;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {

    private static final int NEWS_LOADER_ID = 1;
    private static final String URL_LINK = "https://newsapi.org/v2/top-headlines?sources=wired&pageSize=5&apiKey=";
    private static List<String> webPages = new ArrayList<>();
    private static String webUrl;
    private static NewsAdapter mAdapter;
    private ListView listView;
    ArrayList<HashMap<String, String>> newsList;
    ArrayList<HashMap<String, Drawable>> imageList;
    TextView warningTv;
    ImageView warningIv;
    View loadingIndicator;
    String apiKey;

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        newsList = new ArrayList<>();
        imageList = new ArrayList<>();
        listView = findViewById(R.id.list);
        warningTv = findViewById(R.id.warning);
        warningIv = findViewById(R.id.warning_image);
        apiKey = getString(R.string.news_api_key);
        loadingIndicator = findViewById(R.id.progress_bar);

        // Check network is connected, start loading process:
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {

            LoaderManager loaderManager = getLoaderManager();
            Loader<String> loader = loaderManager.getLoader(NEWS_LOADER_ID);

            if (loader == null) {

                loaderManager.initLoader(NEWS_LOADER_ID, null, this);

            } else {

                loaderManager.restartLoader(NEWS_LOADER_ID, null, this);

            }

        } else {

            // if network is not connected, set empty view and state there is no internet connection:

            View loadingIndicator = findViewById(R.id.progress_bar);
            loadingIndicator.setVisibility(View.GONE);
            warningTv.setText(getString(R.string.no_internet));
            listView.setEmptyView(warningTv);
            warningIv.setImageResource(R.drawable.ic_no_internet);
            listView.setEmptyView(warningIv);

        }

        // When list item is clicked, url of the specific item is sent to the DetailsActivity:


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override

            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Intent i = new Intent(MainActivity.this, DetailsActivity.class);

                i.putExtra(getResources().getString(R.string.url_key), webPages.get(position));

                startActivity(i);

                // After item click, news, image and web page lists is cleared:

                newsList.clear();
                imageList.clear();
                webPages.clear();


            }

        });


    }

    @SuppressLint("StaticFieldLeak")

    @Override

    public Loader<String> onCreateLoader(int i, Bundle bundle) {

        // TODO: Create a new loader for the given URL

        return new AsyncTaskLoader<String>(this) {

            String resultFromHttp;

            @Override

            public String loadInBackground() {

                HttpHandler handler = new HttpHandler();
                String url = URL_LINK + apiKey;
                String jsonString = "";

                try {

                    jsonString = handler.makeHttpRequest(createUrl(url));

                } catch (IOException e) {

                    return null;

                }

                if (jsonString != null) {

                    try {


                        JSONObject jsonObject = new JSONObject(jsonString);
                        JSONArray results = jsonObject.getJSONArray(getString(R.string.results));

                        // looping through all Contacts

                        for (int i = 0; i < results.length(); i++) {

                            try {

                                // Get JSON object and strings from api:

                                JSONObject article = results.getJSONObject(i);

                                webUrl = article.getString(getString(R.string.web_url));
                                webPages.add(webUrl);

                                String thumbnail = article.getString(getString(R.string.thumbnail));

                                String title = article.getString(getString(R.string.web_title));
                                String description = article.getString(getString(R.string.description));
                                String author = article.getString(getString(R.string.author));
                                String date = article.getString(getString(R.string.web_publication_date));

                                // Through input stream, url content is obtained and drawable is created from the content.
                                Drawable image = LoadImageFromWebOperations(thumbnail);

                                HashMap<String, String> textResult = new HashMap<>();

                                // add each child node to HashMap key (String) => value (String)

                                textResult.put(getString(R.string.title), title);
                                textResult.put(getString(R.string.description), description);
                                textResult.put(getString(R.string.author), author);
                                textResult.put(getString(R.string.web_publication_date), date);

                                HashMap<String, Drawable> imageResult = new HashMap<>();

                                // add each child node to HashMap (String) => value (Drawable)

                                imageResult.put(getString(R.string.thumbnail), image);

                                // adding a news to our news list by extracting hashmap values to related list:

                                newsList.add(textResult);

                                imageList.add(imageResult);

                                runOnUiThread(new Runnable() {

                                    @Override

                                    public void run() {

                                        try {

                                            mAdapter = new NewsAdapter(MainActivity.this, newsList, imageList);

                                            if (mAdapter != null) {

                                                mAdapter.notifyDataSetChanged();

                                            }

                                            // After list is adapted, progress bar disappears:

                                            listView.setAdapter(mAdapter);

                                            View loadingIndicator = findViewById(R.id.progress_bar);

                                            loadingIndicator.setVisibility(View.GONE);

                                        } catch (IllegalStateException e) {

                                            e.printStackTrace();

                                        }

                                    }

                                });

                            } catch (final JSONException e) {

                                runOnUiThread(new Runnable() {

                                    @Override

                                    public void run() {

                                        Toast.makeText(getApplicationContext(),

                                                getString(R.string.parsing_error) + e.getMessage(),

                                                Toast.LENGTH_LONG).show();
                                    }

                                });


                            }

                        }

                    } catch (final JSONException e) {

                        runOnUiThread(new Runnable() {

                            @Override

                            public void run() {

                                Toast.makeText(getApplicationContext(),

                                        getString(R.string.parsing_error) + e.getMessage(),

                                        Toast.LENGTH_LONG).show();

                            }

                        });

                    }


                } else {

                    runOnUiThread(new Runnable() {

                        @Override

                        public void run() {

                            Toast.makeText(getApplicationContext(),

                                    getString(R.string.not_get_json),
                                    Toast.LENGTH_LONG).show();

                        }

                    });

                }

                return null;

            }


            private URL createUrl(String stringUrl) {

                URL url;

                try {

                    url = new URL(stringUrl);

                } catch (MalformedURLException exception) {

                    return null;

                }

                return url;

            }

            private Drawable LoadImageFromWebOperations(String url) {

                try {

                    InputStream inputStream = (InputStream) new URL(url).getContent();

                    return Drawable.createFromStream(inputStream, getString(R.string.thumbnail));

                } catch (Exception e) {

                    return null;

                }

            }

            @Override

            protected void onStartLoading() {

                if (resultFromHttp != null) {

                    // To skip loadInBackground call

                    deliverResult(resultFromHttp);

                } else {

                    forceLoad();

                }

            }

        };

    }


    @SuppressLint("ResourceType")

    public void onLoadFinished(Loader<String> loader, String data) {

        // TODO: Update the UI with the result

        if (newsList != null && !newsList.isEmpty()) {

            warningIv.setVisibility(View.GONE);

            loadingIndicator.setVisibility(View.GONE);

        } else {

            loadingIndicator.setVisibility(View.VISIBLE);
            warningTv.setText(getString(R.string.no_news));
            listView.setEmptyView(warningTv);

        }

    }


    @Override

    public void onLoaderReset(Loader<String> loader) {

        // TODO: Loader reset, so we can clear out our existing data.

        newsList.clear();
        imageList.clear();
        webPages.clear();

    }

    public void onResume() {

        newsList.clear();
        imageList.clear();
        webPages.clear();

        super.onResume();

    }

}
