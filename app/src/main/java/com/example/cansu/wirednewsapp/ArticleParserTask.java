package com.example.cansu.wirednewsapp;


import android.os.AsyncTask;

import java.io.IOException;

import org.jsoup.Jsoup;


public final class ArticleParserTask extends AsyncTask<String, Void, String> {

    private static final String ARTICLE_TAG = "article";
    private ArticleParseListener listener;

    interface ArticleParseListener {
        void onArticleParsed(String article);
    }

    ArticleParserTask(ArticleParseListener listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {

            // Content of the specific news is obtained by "article" tag (right click on the page --> inspect element)
            // The news content is placed in the "article" tag in urls of news.
            String url = strings[0];
            return Jsoup.connect(url)
                    .execute()
                    .parse()
                    .getElementsByTag(ARTICLE_TAG)
                    .text();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String article) {
        if (article != null) {
            listener.onArticleParsed(article);
        }
    }
}
