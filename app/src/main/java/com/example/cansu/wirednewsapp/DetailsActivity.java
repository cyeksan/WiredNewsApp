package com.example.cansu.wirednewsapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DetailsActivity extends Activity implements ArticleParserTask.ArticleParseListener {

    private static final String TAG = "DetailsActivity";
    private static final String KEY_TAG = "key=";
    private static final String VALUE_TAG = " value=";
    private static final int MAX_WORD_COUNT = 5;
    private static final int PROGRESS_COMPLETE = 100;
    public Map<String, Integer> wordCountMap = new HashMap<>();
    List<String> wordsIgnoredList;
    String[] wordsIgnoredArray;
    WebView webView;
    String url = "";
    ProgressBar loaderIndicator;
    Integer frequency = null;
    String mostFrequentWord = null;
    TextView mostFrequentWordTv;
    String mostFrequentWordString = "";
    Handler textViewHandler;
    String turkishWordString = "";
    String englishAndTurkishWordString = " ";
    TextView waitTv;
    TextView keywordTitleTv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_activity);

        wordsIgnoredArray = getResources().getStringArray(R.array.words_to_be_ignored);
        wordsIgnoredList = new ArrayList<>(Arrays.asList(wordsIgnoredArray));


        webView = findViewById(R.id.details_webview);
        loaderIndicator = findViewById(R.id.loader);
        mostFrequentWordTv = findViewById(R.id.keyword_tv);
        waitTv = findViewById(R.id.wait_tv);
        keywordTitleTv = findViewById(R.id.keyword_title_tv);
        keywordTitleTv.setText(getResources().getString(R.string.keywords) + " ");


        // Enable Javascript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Get url from intent
        Intent intent = getIntent();
        url = intent.getStringExtra(getResources().getString(R.string.url_key));

        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.loadUrl(url);


        // Set progress bar, please wait string and keywords in accordance with webview loading
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress == PROGRESS_COMPLETE) {
                    loaderIndicator.setVisibility(View.GONE);
                    waitTv.setVisibility(View.GONE);
                    keywordTitleTv.setVisibility(View.VISIBLE);
                    mostFrequentWordTv.setVisibility(View.VISIBLE);
                } else {
                    loaderIndicator.setVisibility(View.VISIBLE);
                    waitTv.setVisibility(View.VISIBLE);
                    keywordTitleTv.setVisibility(View.GONE);
                    mostFrequentWordTv.setVisibility(View.GONE);
                }
            }
        });

        // Content of the web is obtained by jsoup (by using article tag)

        getContentWithJsoup(url);

    }

    private void getContentWithJsoup(String url) {
        new ArticleParserTask(this).execute(url);
    }

    @Override
    public void onArticleParsed(String article) {
        showMostFrequentWords(article);
    }

    protected void showMostFrequentWords(String content) {

        // Remove unnecessary chars, turn letters to lowercase and get rid of "ı" character:
        content = content.replaceAll("[^a-zA-Z\\s]", "");
        content = content.toLowerCase().replace("ı", "i");

        // Remove unnecessary words listed in string-array:
        List<String> words = new ArrayList<>(Arrays.asList(content.split(" ")));
        words.removeAll(wordsIgnoredList);

        // Find the 5 most frequent word in the content:
        words.removeAll(Collections.singletonList(mostFrequentWord));

        for (int i = 0; i < words.size(); i++) {

            String s = words.get(i);
            if (s.isEmpty()) continue;
            if (wordCountMap.keySet().contains(s)) {
                Integer count = wordCountMap.get(s) + 1;
                wordCountMap.put(s, count);
            } else
                wordCountMap.put(s, 1);

        }

        frequency = 0;
        for (String s : wordCountMap.keySet()) {
            Integer i = wordCountMap.get(s);

            if (i >= frequency) {
                frequency = i;
                mostFrequentWord = s;
            }
        }

        Log.e(TAG, mostFrequentWord);

        // SortByComparator method gives the 5 most frequent words between the words (because ascending order is used in the method):

        Map<String, Integer> sortedMap = sortByComparator(wordCountMap, false);

        int wordCount = 0;
        for (final Map.Entry<String, Integer> entry : sortedMap.entrySet()) {


            if (wordCount == MAX_WORD_COUNT) {
                break;
            }

            mostFrequentWordString = mostFrequentWordString + entry.getKey() + getResources().getString(R.string.split_expression);

            ArrayList<String> englishWords = new ArrayList<>(Arrays.asList(mostFrequentWordString.split(getResources().getString(R.string.split_expression))));

            Log.d(TAG, KEY_TAG + entry.getKey() + VALUE_TAG + entry.getValue());


            try {

                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

                StrictMode.setThreadPolicy(policy);

                textViewHandler = new Handler();

                // Google translate process is caried out here:

                TranslateOptions translateOptions = TranslateOptions.newBuilder().setApiKey(getResources().getString(R.string.translate_api_key)).build();

                Translate translate = translateOptions.getService();

                Translation translation = translate.translate(entry.getKey(), Translate.TranslateOption.targetLanguage(getResources().getString(R.string.turkish)));

                turkishWordString = turkishWordString + translation.getTranslatedText().toString().toLowerCase() + getResources().getString(R.string.split_expression);

                ArrayList<String> turkishWords = new ArrayList<>(Arrays.asList(turkishWordString.split(getResources().getString(R.string.split_expression))));


                // English and Turkish words are adjoined:

                englishAndTurkishWordString = englishAndTurkishWordString + englishWords.get(wordCount).toString() + " (" + turkishWords.get(wordCount).toString() + "), ";

                if (wordCount == MAX_WORD_COUNT - 1) {

                    // The last comma is deleted:

                    englishAndTurkishWordString = englishAndTurkishWordString.substring(0, englishAndTurkishWordString.length() - 2);
                }

                mostFrequentWordTv.setText(englishAndTurkishWordString);

                wordCount++;

            } catch (Exception e) {
                e.printStackTrace();
                mostFrequentWordTv.setText(getResources().getString(R.string.no_translation));
            }

        }

    }

    private static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap, final boolean ascending) {

        List<Map.Entry<String, Integer>> list = new LinkedList<>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                if (ascending) {
                    return o1.getValue().compareTo(o2.getValue());
                } else {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion ascending with the help of LinkedList
        Map<String, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

}




