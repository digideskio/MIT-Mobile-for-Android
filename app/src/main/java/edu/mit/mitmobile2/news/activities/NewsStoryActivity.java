package edu.mit.mitmobile2.news.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import edu.mit.mitmobile2.Constants;
import edu.mit.mitmobile2.MITActivity;
import edu.mit.mitmobile2.MITMainActivity;
import edu.mit.mitmobile2.R;
import edu.mit.mitmobile2.news.models.MITNewsGalleryImage;
import edu.mit.mitmobile2.news.models.MITNewsStory;
import edu.mit.mitmobile2.news.utils.NewsUtils;
import edu.mit.mitmobile2.shared.logging.LoggingManager.Timber;

public class NewsStoryActivity extends MITActivity {

    @InjectView(R.id.story_web_view)
    WebView storyWebView;
    @InjectView(R.id.story_image_view)
    ImageView storyImageView;

    private MITNewsStory story;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_story);
        ButterKnife.inject(this);

        setTitle("");
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        story = getIntent().getParcelableExtra(Constants.News.STORY);

        try {
            String originalCoverImageUrl = story.getOriginalCoverImageUrl();
            storyImageView.setVisibility(View.VISIBLE);
            Picasso.with(this).load(originalCoverImageUrl)
                    .placeholder(R.drawable.grey_rect).fit().centerInside().into(storyImageView);
        } catch (NullPointerException e) {
            Timber.e(e, "Failed");
            Picasso.with(this).load(R.drawable.grey_rect)
                    .placeholder(R.drawable.grey_rect).into(storyImageView);
            storyImageView.setVisibility(View.GONE);
        }

        storyWebView.getSettings().setJavaScriptEnabled(true);

        String template = readInHtmlTemplate();

        if (story.getTitle() != null) {
            template = template.replace("__TITLE__", story.getTitle());
        } else {
            template = template.replace("__TITLE__", "");
        }
        if (story.getAuthor() != null) {
            template = template.replace("__AUTHOR__", story.getAuthor());
        } else {
            template = template.replace("__AUTHOR__", "");
        }
        if (story.getPublishedAt() != null) {
            template = template.replace("__DATE__", NewsUtils.formatNewsPublishedTime(story.getPublishedAt()));
        } else {
            template = template.replace("__DATE__", "");
        }
        if (story.getDek() != null) {
            template = template.replace("__DEK__", story.getDek());
        } else {
            template = template.replace("__DEK__", "");
        }
        if (story.getBodyHtml() != null) {
            template = template.replace("__BODY__", story.getBodyHtml());
        } else {
            template = template.replace("__BODY__", "");
        }
        template = template.replace("__WIDTH__", String.valueOf(displayMetrics.widthPixels));

        storyWebView.loadData(template, "text/html;charset=utf-8", "UTF-8");
    }

    private String readInHtmlTemplate() {
        String template = "";
        try {
            InputStream is = getAssets().open("news_story_template.html");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            template = new String(buffer, "UTF-8");
        } catch (IOException e) {
            Timber.e(e, "HTML read Failed");
        }
        return template;
    }

    @OnClick(R.id.story_image_view)
    void goToImageGallery() {
        Intent intent = new Intent(this, NewsImageGalleryActivity.class);
        intent.putParcelableArrayListExtra(Constants.News.IMAGES_KEY, (ArrayList<MITNewsGalleryImage>) story.getGalleryImages());
        intent.putExtra(Constants.News.TITLE_KEY, story.getTitle());
        intent.putExtra(Constants.News.URL_KEY, story.getSourceUrl());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_news_story, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_share:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, story.getType() + ":" + story.getTitle());
                shareIntent.putExtra(Intent.EXTRA_TEXT, story.getTitle() + "  " + story.getSourceUrl());
                startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share_via)));
                break;
            case android.R.id.home:
                Intent backHomeIntent = new Intent(this, MITMainActivity.class);
                backHomeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(backHomeIntent);
                finish();
                break;
            default:
                break;
        }
        return true;
    }
}
