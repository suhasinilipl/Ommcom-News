package com.lipl.ommcom.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Parcel;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lipl.ommcom.R;
import com.lipl.ommcom.pojo.Advertisement;
import com.lipl.ommcom.pojo.BNews;
import com.lipl.ommcom.pojo.BreakingNews;
import com.lipl.ommcom.pojo.Category;
import com.lipl.ommcom.pojo.CitizenJournalistVideos;
import com.lipl.ommcom.pojo.ConferenceNews;
import com.lipl.ommcom.pojo.News;
import com.lipl.ommcom.util.AnimationUtil;
import com.lipl.ommcom.util.AnimatorPath;
import com.lipl.ommcom.util.Config;
import com.lipl.ommcom.util.CustomGCMService;
import com.lipl.ommcom.util.CustomTextView;
import com.lipl.ommcom.util.PathEvaluator;
import com.lipl.ommcom.util.PathPoint;
import com.lipl.ommcom.util.SingleTon;
import com.lipl.ommcom.util.Util;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import co.mobiwise.fastgcm.GCMListener;
import co.mobiwise.fastgcm.GCMManager;

public class HomeActivity1 extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GCMListener,
        CustomGCMService.OnConferenceStartListener,
        CustomGCMService.OnConferenceStopListener,
        CustomGCMService.OnBreakingNewsNotificationListener {

    private RelativeLayout layoutLogoImg;
    private Toolbar toolbar;
    private ObjectAnimator anim;
    private LinearLayout mToolbarBottomBorder;
    private NavigationView navigationView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private CustomTextView tvBreakingNews;
    private DisplayImageOptions options;
    private ImageLoader imageLoader;
    private BreakingNews breaking_news;
    private News mTopVideo;
    private News mViralVideo;

    private static final int LOADING_TIME = 3000;
    private static final String TAG = "HomeActivity";
    private static final int INIT_CENTER_LOG_MOTION_DURATION = 2000;

    private static final float LAYOUT_WEIGHT_HEADER_ADVERTISEMENT = 0.08f;
    private static final float LAYOUT_WEIGHT_TOP_FEATURED_NEWS = 0.2625f;
    private static final float LAYOUT_WEIGHT_TOP_NEWS = 0.13f;
    private static final float LAYOUT_WEIGHT_CONFERENCE = 0.13f;
    private static final float LAYOUT_WEIGHT_CITIZEN_JOURNALIST = 0.13f;
    private static final float LAYOUT_WEIGHT_BREAKING_NEWS = 0.0775f;
    private static final float LAYOUT_WEIGHT_FOOTER_ADVERTISEMENT = 0.08f;
    private static final float LAYOUT_WEIGHT_CATEGORY_NEWS = 0.2625f;

    private CitizenJournalistVideos citizenJournalistVideos = new CitizenJournalistVideos(Parcel.obtain());
    private News featured_news = new News(Parcel.obtain());
    private List<News> newsList = new ArrayList<News>();
    private ConferenceNews conferenceNews;
    private List<Advertisement> advertisementList = new ArrayList<Advertisement>();
    private List<News> categoryNews = new ArrayList<News>();
    private List<Category> categoryList = new ArrayList<Category>();

    private boolean mIsContentLoaded = false;
    private boolean mIsBreakingNewsLoaded = false;
    private boolean mIsCategoriesLoaded = false;
    private boolean mIsContentDisplayed = false;

    private int ADVERTISEMENT_HEADER = 0;
    private int ADVERTISEMENT_MIDDLE = 0;
    private int ADVERTISEMENT_FOOTER = 0;
    private boolean isFromNotification = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

//        int is_resume = getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 1).getInt(Config.SP_IS_FROM_CHILD_ACTIVITY, 0);
//        if(is_resume == 1){
//            loadAllData();
//        } else{
        if(getIntent() != null
                && getIntent().getExtras() != null
                && getIntent().getExtras().containsKey("isFromNotification")) {
            isFromNotification = getIntent().getExtras().getBoolean("isFromNotification");
        }
        CustomTextView tvBreakingNewsNo = (CustomTextView) findViewById(R.id.tvBreakingNewsNo);
        tvBreakingNewsNo.setVisibility(View.GONE);
            loadAllData();
            setupToolbar();
            init();
            intro();
            GCMManager.getInstance(this).registerListener(this);
            String breaking_news_notifiction_key = getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 1)
                    .getString(Config.SP_BREAKING_NEWS_KEY, "No News in preference");
            Log.i("HomeActivity", "Notification " + breaking_news_notifiction_key);
            SingleTon.getInstance().setBreakingNewsNotificationListener(this);
        //}
    }

    private void setupToolbar() {
        if(toolbar == null) {
            toolbar = (Toolbar) findViewById(R.id.toolbar);
        }
        if(mToolbarBottomBorder == null){
            mToolbarBottomBorder = (LinearLayout) findViewById(R.id.toolbar_bottom_border);
        }

//        TextView title = (TextView) toolbar.findViewById(R.id.title);
//        Typeface typeface = Typeface.createFromAsset(getAssets(), "font/times_new_roman.ttf");
//        title.setTypeface(typeface);
//        title.setTypeface(typeface);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
    }

    private void setToolbarInvisible(){
        if(toolbar == null) {
            toolbar = (Toolbar) findViewById(R.id.toolbar);
        }
        if(mToolbarBottomBorder == null){
            mToolbarBottomBorder = (LinearLayout) findViewById(R.id.toolbar_bottom_border);
        }
        toolbar.setVisibility(View.GONE);
        mToolbarBottomBorder.setVisibility(View.GONE);
    }

    private void setToolbarVisible(){
        if(toolbar == null) {
            toolbar = (Toolbar) findViewById(R.id.toolbar);
        }
        if(mToolbarBottomBorder == null){
            mToolbarBottomBorder = (LinearLayout) findViewById(R.id.toolbar_bottom_border);
        }
        toolbar.setVisibility(View.VISIBLE);
        mToolbarBottomBorder.setVisibility(View.VISIBLE);
    }

    private void init(){
        setToolbarInvisible();

        layoutLogoImg = (RelativeLayout) findViewById(R.id.layoutLogoImg);
        int yPosition= -(Util.getScreenHeight() / 2);
        AnimatorPath path = new AnimatorPath();
        path.moveTo(0, 0);
        path.lineTo(0, yPosition);
        anim = ObjectAnimator.ofObject(this, "buttonLoc",
                new PathEvaluator(), path.getPoints().toArray());
        anim.setDuration(INIT_CENTER_LOG_MOTION_DURATION);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                AnimationUtil.zoomOut(layoutLogoImg, INIT_CENTER_LOG_MOTION_DURATION, 0);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setToolbarVisible();
                AnimationUtil.slideInOfDifferentToolbarElements(toolbar);
                AnimationUtil.slideDown(mToolbarBottomBorder);
                displayContent();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        RelativeLayout layoutContent = (RelativeLayout) findViewById(R.id.layoutContentFirst);
        layoutContent.setVisibility(View.GONE);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void intro(){
        AnimationUtil.fadeIn(layoutLogoImg, 2000, 0);
        playAudio();
    }

    private void withoutAudioPlay(){

    }

    private void load(final boolean isRefresh){

        if(Util.getNetworkConnectivityStatus(HomeActivity1.this) == false){
            Util.showDialogToShutdownApp(HomeActivity1.this);
            return;
        }

        //final ProgressBar pBar = (ProgressBar) findViewById(R.id.pBar);
        new AsyncTask<Void, Void, Boolean>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //pBar.setVisibility(View.VISIBLE);
                isLatestData = false;
            }

            @Override
            protected void onPostExecute(Boolean aVoid) {
                super.onPostExecute(aVoid);
                //pBar.setVisibility(View.GONE);

                if(aVoid != null && aVoid.booleanValue() == false){
                    Util.showDialogToShutdownApp(HomeActivity1.this);
                    return;
                }

                mIsContentLoaded = true;
                if(isRefresh){
                    if(mSwipeRefreshLayout != null){
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    displayContent();
                } else if(mIsCategoriesLoaded
                        && mIsBreakingNewsLoaded
                        && mIsContentLoaded
                        && mIsContentDisplayed == false) {
                    if(mSwipeRefreshLayout != null){
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    if(isAudioDone) {
                        anim.start();
                    }
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                InputStream in = null;
                int resCode = -1;

                try {
                    String link = Config.API_BASE_URL + Config.HOME_SCREEN_DETAILS_API;
                    URL url = new URL(link);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    //conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setAllowUserInteraction(false);
                    //conn.setInstanceFollowRedirects(true);
                    conn.setRequestMethod("GET");
                    conn.connect();

                    resCode = conn.getResponseCode();
                    if (resCode == HttpURLConnection.HTTP_OK) {
                        in = conn.getInputStream();
                    }
                    Log.i("HomeActivity : load()", "Error :" + resCode);
                    if (in == null) {
                        return null;
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    String response = "", data = "";

                    while ((data = reader.readLine()) != null) {
                        response += data + "\n";
                    }

                    Log.i(TAG, "Response : " + response);

                    JSONObject res = new JSONObject(response);
                    if(res.isNull("FEATUREDNEWS") == false) {
                        JSONObject fnews = res.getJSONObject("FEATUREDNEWS");

                        if (fnews.isNull("id") == false) {
                            String id = fnews.getString("id");
                            featured_news.setId(id);
                        }

                        if (fnews.isNull("name") == false) {
                            String name = fnews.getString("name");
                            featured_news.setName(name);
                        }

                        if (fnews.isNull("slug") == false) {
                            String slug = fnews.getString("slug");
                            featured_news.setSlug(slug);
                        }

                        if (fnews.isNull("featured_image") == false) {
                            String featured_image = fnews.getString("featured_image");
                            featured_news.setImage(featured_image);
                        }

                        if (fnews.isNull("short_description") == false){
                            String short_description = fnews.getString("short_description");
                            featured_news.setShort_description(short_description);
                        }

                        if(fnews.isNull("file_path") == false) {
                            String file_path = fnews.getString("file_path");
                            featured_news.setFile_path(file_path);
                        }

                        if(fnews.isNull("approved_date") == false) {
                            String approved_date = fnews.getString("approved_date");
                            featured_news.setApproved_date(approved_date);
                        }

                        if(fnews.isNull("position") == false) {
                            String position = fnews.getString("position");
                            featured_news.setPosition(position);
                        }

                        if(fnews.isNull("user_id") == false) {
                            String user_id = fnews.getString("user_id");
                            featured_news.setUser_id(user_id);
                        }

                        if(fnews.isNull("username") ==false) {
                            String username = fnews.getString("username");
                            featured_news.setUser_name(username);
                        }
                        if(fnews.isNull("is_video") ==false) {
                            String is_video = fnews.getString("is_video");
                            featured_news.setIs_video(is_video);
                        }
                        if(fnews.isNull("is_image") ==false) {
                            String is_image = fnews.getString("is_image");
                            featured_news.setIs_image(is_image);
                        }
                    }
                    if(res.isNull("TOPNEWSNOW") == false){
                        JSONArray top_news_now = res.getJSONArray("TOPNEWSNOW");
                        for(int i = 0; i < top_news_now.length(); i++){
                            News news = new News(Parcel.obtain());

                            if(top_news_now.getJSONObject(i).isNull("id") == false) {
                                String id = top_news_now.getJSONObject(i).getString("id");
                                news.setId(id);
                            }
                            if(top_news_now.getJSONObject(i).isNull("name") == false) {
                                String id = top_news_now.getJSONObject(i).getString("name");
                                news.setName(id);
                            }
                            if(top_news_now.getJSONObject(i).isNull("featured_image") == false) {
                                String featured_image = top_news_now.getJSONObject(i).getString("featured_image");
                                news.setImage(featured_image);
                            }
                            if(top_news_now.getJSONObject(i).isNull("slug") == false) {
                                String slug = top_news_now.getJSONObject(i).getString("slug");
                                news.setSlug(slug);
                            }
                            if(top_news_now.getJSONObject(i).isNull("is_image") == false) {
                                String slug = top_news_now.getJSONObject(i).getString("is_image");
                                news.setIs_image(slug);
                            }
                            if(top_news_now.getJSONObject(i).isNull("file_path") == false) {
                                String slug = top_news_now.getJSONObject(i).getString("file_path");
                                news.setFile_path(slug);
                            }
                            if(top_news_now.getJSONObject(i).isNull("is_video") == false) {
                                String slug = top_news_now.getJSONObject(i).getString("is_video");
                                news.setIs_video(slug);
                            }
                            newsList.add(news);
                        }
                    }
                    if(res.isNull("CONFERENCE_NEWS") == false){
                        conferenceNews = new ConferenceNews(Parcel.obtain());
                        JSONObject conference_news = res.getJSONObject("CONFERENCE_NEWS");
                        if(conference_news.isNull("id") == false) {
                            String id = conference_news.getString("id");
                            conferenceNews.setId(id);
                        }
                        if(conference_news.isNull("featured_image") == false){
                            String featured_image = conference_news.getString("featured_image");
                            conferenceNews.setFeatured_image(featured_image);
                        }

                        if(conference_news.isNull("name") == false) {
                            String name = conference_news.getString("name");
                            conferenceNews.setName(name);
                        }
                        if(conference_news.isNull("short_desc") == false) {
                            String short_desc = conference_news.getString("short_desc");
                            conferenceNews.setShort_desc(short_desc);
                        }
                        if(conference_news.isNull("long_desc") == false) {
                            String long_desc = conference_news.getString("long_desc");
                            conferenceNews.setLong_desc(long_desc);
                        }
                        if(conference_news.isNull("conference_banner") == false) {
                            String conference_banner = conference_news.getString("conference_banner");
                            conferenceNews.setConference_banner(conference_banner);
                        }
                        if(conference_news.isNull("started_at") == false){
                            String started_at = conference_news.getString("started_at");
                            conferenceNews.setStarted_at(started_at);
                        }
                        if(conference_news.isNull("start_time") == false) {
                            String start_time = conference_news.getString("start_time");
                            conferenceNews.setStart_time(start_time);
                        }
                        if(conference_news.isNull("end_time") == false) {
                            String end_time = conference_news.getString("end_time");
                            conferenceNews.setEnd_time(end_time);
                        }
                    }
                    if(res.isNull("CITIZEN_CUSTOMIZE") == false){
                        JSONObject citizen_customize = res.getJSONObject("CITIZEN_CUSTOMIZE");
                        if(citizen_customize.isNull("name") == false) {
                            String name = citizen_customize.getString("name");
                            citizenJournalistVideos.setName(name);
                        }
                        if(citizen_customize.isNull("file_path") == false) {
                            String file_path = citizen_customize.getString("file_path");
                            citizenJournalistVideos.setFile_path(file_path);
                        }
                    }

                    /*
                    * {
                          "name": "Iphone",
                          "advertisement_section_id": "1",
                          "file_path": "150485319914622767751844484274-adv1.jpg",
                          "url_link": "https://www.google.co.in/"
                        }
                    * */
                    if(res.isNull("ADVERTISEMENT_HEADER") == false){
                        ADVERTISEMENT_HEADER = res.getInt("ADVERTISEMENT_HEADER");
                    }

                    if(res.isNull("ADVERTISEMENT_MIDDLE") == false){
                        ADVERTISEMENT_MIDDLE = res.getInt("ADVERTISEMENT_MIDDLE");
                    }

                    if(res.isNull("ADVERTISEMENT_FOOTER") == false){
                        ADVERTISEMENT_FOOTER = res.getInt("ADVERTISEMENT_FOOTER");
                    }

                    if(res.isNull("ADVERTISEMENT") == false){
                        JSONArray top_news_now = res.getJSONArray("ADVERTISEMENT");
                        for(int i = 0; i < top_news_now.length(); i++){
                            Advertisement advertisement = new Advertisement(Parcel.obtain());
                            if(top_news_now.getJSONObject(i).isNull("name") == false){
                                String name = top_news_now.getJSONObject(i).getString("name");
                                advertisement.setName(name);
                            }
                            if(top_news_now.getJSONObject(i).isNull("advertisement_section_id") == false){
                                String section = top_news_now.getJSONObject(i).getString("advertisement_section_id");
                                advertisement.setSection(section);
                            }
                            if(top_news_now.getJSONObject(i).isNull("file_path") == false){
                                String file_path = top_news_now.getJSONObject(i).getString("file_path");
                                advertisement.setFile_path(file_path);
                            }
                            if(top_news_now.getJSONObject(i).isNull("url_link") == false){
                                String url_link = top_news_now.getJSONObject(i).getString("url_link");
                                advertisement.setUrl_link(url_link);
                            }
                            advertisementList.add(advertisement);
                        }
                    }

                    if(res.isNull("CATEGORY_NEWS") == false){
                        JSONArray category_news_list = res.getJSONArray("CATEGORY_NEWS");
                        categoryNews.clear();
                        for(int i = 0; i < category_news_list.length(); i++){

                            News news = new News(Parcel.obtain());
                            if(category_news_list.getJSONObject(i).isNull("name") == false){
                                String name = category_news_list.getJSONObject(i).getString("name");
                                news.setName(name);
                            }
                            if(category_news_list.getJSONObject(i).isNull("slug") == false){
                                String slug = category_news_list.getJSONObject(i).getString("slug");
                                news.setSlug(slug);
                            }
                            if(category_news_list.getJSONObject(i).isNull("featured_image") == false){
                                String featured_image = category_news_list.getJSONObject(i).getString("featured_image");
                                news.setImage(featured_image);
                            }
                            if(category_news_list.getJSONObject(i).isNull("short_description") == false){
                                String short_description = category_news_list.getJSONObject(i).getString("short_description");
                                news.setShort_description(short_description);
                            }
                            if(category_news_list.getJSONObject(i).isNull("approved_date") == false){
                                String approved_date = category_news_list.getJSONObject(i).getString("approved_date");
                                news.setApproved_date(approved_date);
                            }
                            if(category_news_list.getJSONObject(i).isNull("username") == false){
                                String username = category_news_list.getJSONObject(i).getString("username");
                                news.setUser_name(username);
                            }
                            if(category_news_list.getJSONObject(i).isNull("categoryname") == false){
                                String categoryname = category_news_list.getJSONObject(i).getString("categoryname");
                                news.setCategory(categoryname);
                            }
                            if(category_news_list.getJSONObject(i).isNull("categoryslug") == false){
                                String categoryslug = category_news_list.getJSONObject(i).getString("categoryslug");
                                news.setCategoryslug(categoryslug);
                            }
                            categoryNews.add(news);
                        }
                    }

                    if(res.isNull("TOP_VIDEO") == false){
                        mTopVideo = new News(Parcel.obtain());
                        if(res.getJSONObject("TOP_VIDEO").isNull("file_path") == false){
                            String file_path = res.getJSONObject("TOP_VIDEO").getString("file_path");
                            mTopVideo.setFile_path(file_path);
                        }
                        if(res.getJSONObject("TOP_VIDEO").isNull("name") == false){
                            String name = res.getJSONObject("TOP_VIDEO").getString("name");
                            mTopVideo.setName(name);
                        }
                        if(res.getJSONObject("TOP_VIDEO").isNull("slug") == false){
                            String slug = res.getJSONObject("TOP_VIDEO").getString("slug");
                            mTopVideo.setSlug(slug);
                        }
                        if(res.getJSONObject("TOP_VIDEO").isNull("is_video") == false){
                            String is_video = res.getJSONObject("TOP_VIDEO").getString("is_video");
                            mTopVideo.setIs_video(is_video);
                        }
                    }
                    if(res.isNull("VIRAL_VIDEO") == false){
                        mViralVideo = new News(Parcel.obtain());
                        if(res.getJSONObject("VIRAL_VIDEO").isNull("file_path") == false){
                            String file_path = res.getJSONObject("VIRAL_VIDEO").getString("file_path");
                            mViralVideo.setFile_path(file_path);
                        }
                        if(res.getJSONObject("VIRAL_VIDEO").isNull("name") == false){
                            String name = res.getJSONObject("VIRAL_VIDEO").getString("name");
                            mViralVideo.setName(name);
                        }
                        if(res.getJSONObject("VIRAL_VIDEO").isNull("slug") == false){
                            String slug = res.getJSONObject("VIRAL_VIDEO").getString("slug");
                            mViralVideo.setSlug(slug);
                        }
                        if(res.getJSONObject("VIRAL_VIDEO").isNull("is_video") == false){
                            String is_video = res.getJSONObject("VIRAL_VIDEO").getString("is_video");
                            mViralVideo.setIs_video(is_video);
                        }
                    }
                    isLatestData = true;
                    return true;
                } catch(SocketTimeoutException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                    return false;
                } catch(ConnectException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                    return false;
                } catch(MalformedURLException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                    return false;
                } catch (IOException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                    return false;
                } catch(Exception exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                    return false;
                }
            }
        }.execute();
    }

    private void loadBreakingNews(){
        if(Util.getNetworkConnectivityStatus(HomeActivity1.this) == false){
            Util.showDialogToShutdownApp(HomeActivity1.this);
            return;
        }
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //pBar.setVisibility(View.VISIBLE);
                if(breaking_news == null){
                    breaking_news = new BreakingNews(Parcel.obtain());
                }
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                //pBar.setVisibility(View.GONE);
                mIsBreakingNewsLoaded = true;
                if(mIsCategoriesLoaded
                        && mIsBreakingNewsLoaded
                        && mIsContentLoaded
                        && mIsContentDisplayed == false) {
                    if(mSwipeRefreshLayout != null){
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    if(isAudioDone) {
                        anim.start();
                    }
                }
            }

            @Override
            protected Void doInBackground(Void... params) {
                InputStream in = null;
                int resCode = -1;

                try {
                    String link = Config.API_BASE_URL + Config.BREAKING_NEWS_API;
                    URL url = new URL(link);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    //conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setAllowUserInteraction(false);
                    //conn.setInstanceFollowRedirects(true);
                    conn.setRequestMethod("GET");
                    conn.connect();

                    resCode = conn.getResponseCode();
                    if (resCode == HttpURLConnection.HTTP_OK) {
                        in = conn.getInputStream();
                    }
                    if (in == null) {
                        return null;
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    String response = "", data = "";

                    while ((data = reader.readLine()) != null) {
                        response += data + "\n";
                    }

                    Log.i(TAG, "Response : " + response);

                    /*
                    * {
                        "BREAKINGNEWS": [
                            {
                            "id": "1",
                            "title": "A look at the funding crisis in Egypt's health service, from cost of medication to organ sales."
                            }
                        ],
                        "NEWSDETAILS": "Packed from end to end with stunning action and liberal doses of humour and emotion, the film presses. ** A look at the funding crisis in Egypt's health service, from cost of medication to organ sales and \"patients for hire\". ** UN estimate says at least 430 people killed in the past year after President Nkurinziza launched election campaign...\r\n\r\n\r\n\r\n ** Kadyrov has erased all traces of the war. Even as we were filming, he decided to go even further. ** Riot police fire tear gas at demonstrators protesting proposed labour reforms that they claim threaten workers' rights. ** Israeli Prime Minister Netanyahu says the best way to resolve the conflict is through direct, bilateral negotiations. ** NEW DELHI: Even if oil prices move a little higher, they will not be negative for India but \"exceedingly high\". ** North Korea has sentenced a Korean American man to 10 years' hard labour for subversion, China's Xinhua news agency ** Syria war: UN humanitarian chief laments situation in Syria, where people are facing \"appalling\" desolation, hunger and starvation. ** BERHAMPUR: For 500 families of Sirtiguda village under Kanjamendi-Nuagaon block of Kandhamal district."
                        }
                    * */
                    if(response != null && response.trim().length() > 0){
                        JSONObject obj = new JSONObject(response);
                        List<BNews> breakingNewsList = new ArrayList<BNews>();
                        if(obj.isNull("BREAKINGNEWS") == false){
                            JSONArray array = obj.getJSONArray("BREAKINGNEWS");
                            if(array != null && array.length() > 0){
                                for(int i = 0; i < array.length(); i++) {
                                    BNews bNews = new BNews(Parcel.obtain());
                                    if (array.getJSONObject(i).isNull("id") == false) {
                                        String id = array.getJSONObject(i).getString("id");
                                        bNews.setId(id);
                                    }
                                    if (array.getJSONObject(i).isNull("title") == false) {
                                        String name = array.getJSONObject(i).getString("title");
                                        bNews.setTitle(name);
                                    }
                                    breakingNewsList.add(bNews);
                                }
                            }
                        }
                        List<BNews> newsDetailsList = new ArrayList<BNews>();
                        if(obj.isNull("NEWSDETAILS") == false){
                            JSONArray array = obj.getJSONArray("NEWSDETAILS");
                            if(array != null && array.length() > 0){
                                for(int i = 0; i < array.length(); i++) {
                                    BNews bNews = new BNews(Parcel.obtain());
                                    if (array.getJSONObject(i).isNull("id") == false) {
                                        String id = array.getJSONObject(i).getString("id");
                                        bNews.setId(id);
                                    }
                                    if (array.getJSONObject(i).isNull("title") == false) {
                                        String name = array.getJSONObject(i).getString("title");
                                        bNews.setTitle(name);
                                    }
                                    newsDetailsList.add(bNews);
                                }
                            }
                        }
                        breaking_news.setbNewsList(breakingNewsList);
                        breaking_news.setNewsdetails(newsDetailsList);
                    }


                    return null;
                } catch(SocketTimeoutException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch(ConnectException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch(MalformedURLException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch (IOException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch(Exception exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                }
                return null;
            }
        }.execute();
    }

    private boolean isAudioDone = false;

    private void loadNavigationViewCategories(){
        if(Util.getNetworkConnectivityStatus(HomeActivity1.this) == false){
            Util.showDialogToShutdownApp(HomeActivity1.this);
            return;
        }
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //pBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                //pBar.setVisibility(View.GONE);
                mIsCategoriesLoaded = true;
                if(mIsCategoriesLoaded
                        && mIsBreakingNewsLoaded
                        && mIsContentLoaded
                        && mIsContentDisplayed == false) {
                    if(mSwipeRefreshLayout != null){
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    if(isAudioDone) {
                        anim.start();
                    }
                }
            }

            @Override
            protected Void doInBackground(Void... params) {
                InputStream in = null;
                int resCode = -1;

                try {
                    String link = Config.API_BASE_URL + Config.NAVIGATION_DRAWER_CATEGORIES_API;
                    URL url = new URL(link);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    //conn.setReadTimeout(10000);
                    //conn.setConnectTimeout(15000);
                    //conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setAllowUserInteraction(false);
                    //conn.setInstanceFollowRedirects(true);
                    conn.setRequestMethod("GET");
                    conn.connect();

                    resCode = conn.getResponseCode();
                    if (resCode == HttpURLConnection.HTTP_OK) {
                        in = conn.getInputStream();
                    }
                    if (in == null) {
                        return null;
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    String response = "", data = "";

                    while ((data = reader.readLine()) != null) {
                        response += data + "\n";
                    }

                    Log.i(TAG, "Response : " + response);

                    /*
                    * [
                          {
                            "id": "1",
                            "name": "World",
                            "slug": "world"
                          },
                          {
                            "id": "2",
                            "name": "City",
                            "slug": "city"
                          }
                      ]
                    * */

                    if(response != null && response.length() > 0) {
                        JSONArray resObj = new JSONArray(response);
                        if(resObj != null && resObj.length() > 0){
                            categoryList.clear();
                            for(int i = 0; i < resObj.length(); i++){
                                Category category = new Category(Parcel.obtain());
                                String id = resObj.getJSONObject(i).getString("id");
                                String name = resObj.getJSONObject(i).getString("name");
                                String slug = resObj.getJSONObject(i).getString("slug");

                                category.setName(name);
                                category.setId(id);
                                category.setSlug(slug);
                                categoryList.add(category);
                            }
                        }
                    }

                    return null;
                } catch(SocketTimeoutException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch(ConnectException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch(MalformedURLException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch (IOException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch(Exception exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                }
                return null;
            }
        }.execute();
    }

    private void playAudio(){
        MediaPlayer mediaPlayer= MediaPlayer.create(this,R.raw.winxp);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //loadAllData();
                isAudioDone = true;
                if(mIsCategoriesLoaded
                        && mIsBreakingNewsLoaded
                        && mIsContentLoaded
                        && mIsContentDisplayed == false) {
                    if(mSwipeRefreshLayout != null){
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    anim.start();
                }
            }
        });
    }

    private void loadAllData(){
        mIsContentLoaded = false;
        mIsCategoriesLoaded = false;
        mIsContentDisplayed = false;
        mIsBreakingNewsLoaded = false;

//        int is_resume = getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 1).getInt(Config.SP_IS_FROM_CHILD_ACTIVITY, 0);
//        if(is_resume == 1){
//           mSwipeRefreshLayout.setVisibility(View.VISIBLE);
//        }

        load(false);
        loadBreakingNews();
        loadNavigationViewCategories();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_about_us:
                startActivity(new Intent());
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id < categoryList.size()){
            for(int i = 0; i < categoryList.size(); i++){
                if (id == i) {
                    getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit().putInt(Config.SP_IS_FROM_CHILD_ACTIVITY, 1).commit();
                    Intent intent = new Intent(HomeActivity1.this, CategoryNewsListActivity.class);
                    intent.putExtra("slug", categoryList.get(i).getSlug());
                    startActivity(intent);
                    return true;
                }
            }
        }

        if(categoryList != null){
            if(id == categoryList.size()){
                //About Us
                startActivity(new Intent(HomeActivity1.this, AboutUsActivity.class));
            }
            if(id == categoryList.size() + 1){
                //Feedback
                startActivity(new Intent(HomeActivity1.this, FeedbackActivity.class));
            }
        } else{
            if(id == 111){
                //About Us
                startActivity(new Intent(HomeActivity1.this, AboutUsActivity.class));
            }
            if(id == 112){
                //Feedback
                startActivity(new Intent(HomeActivity1.this, FeedbackActivity.class));
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean isLatestData = false;
    public void setButtonLoc(PathPoint newLoc) {
        layoutLogoImg.setTranslationX(newLoc.mX);
        layoutLogoImg.setTranslationY(newLoc.mY);
    }

    private void setTextAppearance(TextView textView, Context context, int resId) {

        if (Build.VERSION.SDK_INT < 23) {
            textView.setTextAppearance(context, resId);
        } else {
            textView.setTextAppearance(resId);
        }
    }

    private void displayContent() {
        mIsContentDisplayed = true;

        CustomTextView tvBreakingNewsNo = (CustomTextView) findViewById(R.id.tvBreakingNewsNo);
        tvBreakingNewsNo.setVisibility(View.GONE);

        Menu menu = navigationView.getMenu();
        if(menu != null){
            menu.clear();
        }
        try {
            for (int i = 0; i < categoryList.size(); i++) {
                menu.add(Menu.NONE, i, i, categoryList.get(i).getName());
            }
        } catch(Exception exception){
            Log.e(TAG, "displayContent()", exception);
        }

        int xx = 111;
        if(categoryNews != null){
            xx = categoryList.size();
        }

        menu.add(Menu.NONE, xx, xx, "About Us");
        menu.add(Menu.NONE, xx + 1, xx + 1, "Feedback");

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.app_logo_color_blue, R.color.app_logo_color_maroon);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
             @Override
             public void onRefresh() {
                 load(true);
             }
         });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit().putInt(Config.SP_IS_FROM_CHILD_ACTIVITY, 1).commit();
                startActivity(new Intent(HomeActivity1.this, PollingActivity.class));
            }
        });

        RelativeLayout layoutContent = (RelativeLayout) findViewById(R.id.layoutContentFirst);
        layoutContent.setVisibility(View.VISIBLE);

        LinearLayout layoutBody = (LinearLayout) findViewById(R.id.layoutBody);
        if(isLatestData){
            layoutBody.removeAllViews();
        }
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.stub)//ic_stub
                .showImageForEmptyUri(R.drawable.empty)//ic_empty
                .showImageOnFail(R.drawable.error)//ic_error
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .displayer(new SimpleBitmapDisplayer())
                .build();

        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(HomeActivity1.this));

        RelativeLayout layoutTopAdvertisement = null;
        RelativeLayout layoutBottomAdvertisement = null;
        RelativeLayout layoutMostBottomAdvertisement = null;
        if(advertisementList != null && advertisementList.size() > 0) {
            for(int i = 0; i < advertisementList.size(); i++) {
                Advertisement advertisement = advertisementList.get(i);
                if(advertisement != null && advertisement.getSection() != null && advertisement.getSection().length() > 0
                        && advertisement.getSection().equalsIgnoreCase(ADVERTISEMENT_HEADER+"")){
                    //Top Advertisement
                    layoutTopAdvertisement = new RelativeLayout(HomeActivity1.this);
                    int height_layout_top_advertisement = (int)(Util.getScreenHeight() * LAYOUT_WEIGHT_HEADER_ADVERTISEMENT);
                    //RelativeLayout.LayoutParams layout_params_top_advertisement = new RelativeLayout.LayoutParams
                            //(RelativeLayout.LayoutParams.MATCH_PARENT, height_layout_top_advertisement);
                    RelativeLayout.LayoutParams layout_params_top_advertisement = new RelativeLayout.LayoutParams
                            (RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    layoutTopAdvertisement.setLayoutParams(layout_params_top_advertisement);
                    ImageView img_header_adv = new ImageView(HomeActivity1.this);
                    LinearLayout.LayoutParams layout_params_header_adv_img = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
                    img_header_adv.setLayoutParams(layout_params_header_adv_img);
                    layoutTopAdvertisement.addView(img_header_adv);

                    String adv_header_image = "";
                    if(advertisement != null && advertisement.getFile_path() != null
                            && advertisement.getFile_path().trim().length() > 0){
                        adv_header_image = advertisement.getFile_path().trim();
                    }
                    imageLoader.displayImage(Config.IMAGE_DOWNLOAD_BASE_URL
                            + Config.FOLDER_ADVERTISEMENT + Config.FOLDER_VIDEO + "/" +
                                    adv_header_image , img_header_adv, options);

                    final String url = advertisement.getUrl_link();
                    if(url != null && url.trim().length() > 0){
                        layoutTopAdvertisement.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String _url = url;
                                if (!url.startsWith("http://") && !url.startsWith("https://"))
                                    _url = "http://" + url;
                                getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit().putInt(Config.SP_IS_FROM_CHILD_ACTIVITY, 1).commit();
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(_url));
                                startActivity(intent);
                            }
                        });
                    }
                } else if(advertisement != null && advertisement.getSection() != null && advertisement.getSection().length() > 0
                            && advertisement.getSection().equalsIgnoreCase(ADVERTISEMENT_MIDDLE+"")){
                    layoutBottomAdvertisement = new RelativeLayout(HomeActivity1.this);
                    int height_layout_footer_adv = (int)(Util.getScreenHeight() * LAYOUT_WEIGHT_FOOTER_ADVERTISEMENT);
                    //RelativeLayout.LayoutParams layout_params_footer_adv = new RelativeLayout.LayoutParams
                      //      (RelativeLayout.LayoutParams.MATCH_PARENT, height_layout_footer_adv);
                    RelativeLayout.LayoutParams layout_params_footer_adv = new RelativeLayout.LayoutParams
                            (RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    layoutBottomAdvertisement.setLayoutParams(layout_params_footer_adv);
                    ImageView imgFooterAdv = new ImageView(HomeActivity1.this);
                    LinearLayout.LayoutParams layout_params_footer_adv_img = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
                    imgFooterAdv.setLayoutParams(layout_params_footer_adv_img);
                    layoutBottomAdvertisement.addView(imgFooterAdv);

                    String adv_footer_image = advertisement.getFile_path().trim();
                    imageLoader.displayImage(Config.IMAGE_DOWNLOAD_BASE_URL + Config.FOLDER_ADVERTISEMENT
                            + Config.FOLDER_VIDEO + "/" +
                                    adv_footer_image//"http://timesofindia.indiatimes.com/" +
                            //"thumb/msid-51900429,width-400,resizemode-4/51900429.jpg"
                            , imgFooterAdv, options);
                    final String url = advertisement.getUrl_link();
                    if(url != null && url.trim().length() > 0){
                        layoutBottomAdvertisement.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String _url = url;
                                if (!url.startsWith("http://") && !url.startsWith("https://"))
                                    _url = "http://" + url;
                                getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit().putInt(Config.SP_IS_FROM_CHILD_ACTIVITY, 1).commit();
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(_url));
                                startActivity(intent);
                            }
                        });
                    }
                } else if(advertisement != null && advertisement.getSection() != null && advertisement.getSection().length() > 0
                        && advertisement.getSection().equalsIgnoreCase(ADVERTISEMENT_FOOTER+"")){
                    layoutMostBottomAdvertisement = new RelativeLayout(HomeActivity1.this);
                    int height_layout_footer_adv = (int)(Util.getScreenHeight() * LAYOUT_WEIGHT_FOOTER_ADVERTISEMENT);
                    //RelativeLayout.LayoutParams layout_params_footer_adv = new RelativeLayout.LayoutParams
                    //      (RelativeLayout.LayoutParams.MATCH_PARENT, height_layout_footer_adv);
                    RelativeLayout.LayoutParams layout_params_footer_adv = new RelativeLayout.LayoutParams
                            (RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    layoutMostBottomAdvertisement.setLayoutParams(layout_params_footer_adv);
                    ImageView imgFooterAdv = new ImageView(HomeActivity1.this);
                    LinearLayout.LayoutParams layout_params_footer_adv_img = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
                    imgFooterAdv.setLayoutParams(layout_params_footer_adv_img);
                    layoutMostBottomAdvertisement.addView(imgFooterAdv);

                    String adv_footer_image = advertisement.getFile_path().trim();
                    imageLoader.displayImage(Config.IMAGE_DOWNLOAD_BASE_URL + Config.FOLDER_ADVERTISEMENT
                                    + Config.FOLDER_VIDEO + "/" +
                                    adv_footer_image//"http://timesofindia.indiatimes.com/" +
                            //"thumb/msid-51900429,width-400,resizemode-4/51900429.jpg"
                            , imgFooterAdv, options);
                    final String url = advertisement.getUrl_link();
                    if(url != null && url.trim().length() > 0){
                        layoutMostBottomAdvertisement.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String _url = url;
                                if (!url.startsWith("http://") && !url.startsWith("https://"))
                                    _url = "http://" + url;
                                getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit().putInt(Config.SP_IS_FROM_CHILD_ACTIVITY, 1).commit();
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(_url));
                                startActivity(intent);
                            }
                        });
                    }
                }
            }
        }

        //Featured News
        RelativeLayout layoutFeaturedNews = new RelativeLayout(HomeActivity1.this);
        int height_layout_featured_news_section = (int)(Util.getScreenHeight() * LAYOUT_WEIGHT_TOP_FEATURED_NEWS);
        RelativeLayout.LayoutParams layout_params_featured_news = new RelativeLayout.LayoutParams
                (RelativeLayout.LayoutParams.MATCH_PARENT, height_layout_featured_news_section);
        //RelativeLayout.LayoutParams layout_params_featured_news = new RelativeLayout.LayoutParams
          //      (RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutFeaturedNews.setLayoutParams(layout_params_featured_news);
        View view = LayoutInflater.from(HomeActivity1.this).inflate(R.layout.layout_featured_news, null);
        layoutFeaturedNews.addView(view);
        layoutBody.addView(layoutFeaturedNews);

        LinearLayout layoutTopNews = new LinearLayout(HomeActivity1.this);
        int height_layout_news = (int)(Util.getScreenHeight() * LAYOUT_WEIGHT_TOP_NEWS);
        LinearLayout.LayoutParams layout_params_news = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.MATCH_PARENT, height_layout_news);
//        LinearLayout.LayoutParams layout_params_news = new LinearLayout.LayoutParams
//        (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutTopNews.setLayoutParams(layout_params_news);
        View view_top_news = LayoutInflater.from(HomeActivity1.this).inflate(R.layout.layout_top_news, null);
        layoutTopNews.addView(view_top_news);
        CustomTextView tvTopNewsTitle = (CustomTextView) view_top_news.findViewById(R.id.tvTopNewsTitle);
//        int text_size_in_pixel = getResources().getInteger(R.integer.news_title_size);
//        tvTopNewsTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, Config.get_text_size(HomeActivity.this, text_size_in_pixel));
        layoutBody.addView(layoutTopNews);

        RelativeLayout layoutDebate = new RelativeLayout(HomeActivity1.this);
        int height_layout_conference = (int)(Util.getScreenHeight() * LAYOUT_WEIGHT_CONFERENCE);
        //RelativeLayout.LayoutParams layout_params_conference = new RelativeLayout.LayoutParams
                //(RelativeLayout.LayoutParams.MATCH_PARENT, height_layout_conference);
        RelativeLayout.LayoutParams layout_params_conference = new RelativeLayout.LayoutParams
                (RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutDebate.setLayoutParams(layout_params_conference);
        View view_debate = LayoutInflater.from(HomeActivity1.this).inflate(R.layout.layout_conference, null);
        layoutDebate.addView(view_debate);
        layoutBody.addView(layoutDebate);

        RelativeLayout layoutCitizenJournalist = new RelativeLayout(HomeActivity1.this);
        int height_layout_citizen_journalist = (int)(Util.getScreenHeight() * LAYOUT_WEIGHT_CITIZEN_JOURNALIST);
        RelativeLayout.LayoutParams layout_params_citizen_journalist = new RelativeLayout.LayoutParams
                (RelativeLayout.LayoutParams.MATCH_PARENT, height_layout_citizen_journalist);
        layoutCitizenJournalist.setLayoutParams(layout_params_citizen_journalist);
        View view_cj = LayoutInflater.from(HomeActivity1.this).inflate(R.layout.layout_citizen_journalist, null);
        layoutCitizenJournalist.addView(view_cj);
        CustomTextView tvCJTitle = (CustomTextView) view_cj.findViewById(R.id.tvCJTitle);
//        int text_size_in_pixel_cj = getResources().getInteger(R.integer.news_title_size);
//        tvCJTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, Config.get_text_size(HomeActivity.this, text_size_in_pixel_cj));
        layoutBody.addView(layoutCitizenJournalist);

        /*RelativeLayout layoutBreakingNews = new RelativeLayout(HomeActivity.this);
        int height_layout_breaking_news = (int)(Util.getScreenHeight() * LAYOUT_WEIGHT_BREAKING_NEWS);
        RelativeLayout.LayoutParams layout_params_breaking_news = new RelativeLayout.LayoutParams
                (RelativeLayout.LayoutParams.MATCH_PARENT, height_layout_breaking_news);
        layoutBreakingNews.setLayoutParams(layout_params_breaking_news);
        View view_breaking_news = LayoutInflater.from(HomeActivity.this).inflate(R.layout.layout_breaking_news, null);
        layoutBreakingNews.addView(view_breaking_news);
        layoutBody.addView(layoutBreakingNews);*/
        tvBreakingNews = (CustomTextView) findViewById(R.id.tvBreakingNews);

        if(layoutBottomAdvertisement != null) {
            layoutBody.addView(layoutBottomAdvertisement);
        }

        if(layoutTopAdvertisement != null){
            layoutBody.addView(layoutTopAdvertisement, 0);
        }

        if(layoutTopAdvertisement != null) {
            AnimationUtil.slideInFromLeft(layoutTopAdvertisement, 500, 500);
        }
        AnimationUtil.slideInFromRight(layoutFeaturedNews, 500, 700);
        AnimationUtil.slideInFromLeft(layoutTopNews, 500, 900);
        AnimationUtil.slideInFromRight(layoutDebate, 500, 1100);
        AnimationUtil.slideInFromLeft(layoutCitizenJournalist, 500, 1300);
        if(layoutBottomAdvertisement != null) {
            AnimationUtil.slideInFromRight(layoutBottomAdvertisement, 500, 1700);
        }

        String featured_image_file_name = Util.getImageFilePathForNews(featured_news, null);
        ImageView fn_display_image_view = (ImageView) findViewById(R.id.imgDisplayPicture);
        if(featured_news != null && featured_news.getIs_video() != null
                && featured_news.getIs_video().trim().equalsIgnoreCase("1")){
            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.stub)//ic_stub
                    .showImageForEmptyUri(R.drawable.video_default)//ic_empty
                    .showImageOnFail(R.drawable.video_default)//ic_error
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .bitmapConfig(Bitmap.Config.ARGB_8888)
                    .displayer(new SimpleBitmapDisplayer())
                    .build();
        }
        imageLoader.displayImage(featured_image_file_name//"http://cdn.ndtv.com/tech/new_chromecast_black.jpg"
                , fn_display_image_view, options);

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.stub)//ic_stub
                .showImageForEmptyUri(R.drawable.empty)//ic_empty
                .showImageOnFail(R.drawable.error)//ic_error
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .displayer(new SimpleBitmapDisplayer())
                .build();

        String top_news_one_file_name = "";
        if(newsList != null && newsList.size() > 0
                && newsList.get(0) != null
                ){
            top_news_one_file_name = Util.getImageFilePathForTopNews(newsList.get(0));
        }
        ImageView img_top_news_one = (ImageView) findViewById(R.id.imgTopImg1);
        imageLoader.displayImage(top_news_one_file_name//"http://timesofindia.indiatimes.com/thumb/" +
                //"msid-51902387,width-400,resizemode-4/51902387.jpg"
                , img_top_news_one, options);

        String top_news_two_file_name = "";
        if(newsList != null && newsList.size() > 1
                && newsList.get(1) != null){
            top_news_two_file_name = Util.getImageFilePathForTopNews(newsList.get(1));
        }
        ImageView img_top_news_two = (ImageView) findViewById(R.id.imgTopImg2);
        imageLoader.displayImage(top_news_two_file_name//"http://timesofindia.indiatimes.com/" +
                //"thumb/msid-51900429,width-400,resizemode-4/51900429.jpg"
                , img_top_news_two, options);

        String top_news_three_file_name = "";
        if(newsList != null && newsList.size() > 2
                && newsList.get(2) != null){
            top_news_three_file_name = Util.getImageFilePathForTopNews(newsList.get(2));
        }
        ImageView img_top_news_three = (ImageView) findViewById(R.id.imgTopImg3);
        imageLoader.displayImage(top_news_three_file_name//"http://timesofindia.indiatimes.com/" +
                //"thumb/msid-51900429,width-400,resizemode-4/51900429.jpg"
                , img_top_news_three, options);

        String cj_image = "";
        if(citizenJournalistVideos != null
                && citizenJournalistVideos.getFile_path() != null
                && citizenJournalistVideos.getFile_path().length() > 0){
            cj_image = citizenJournalistVideos.getFile_path().trim();
        }
        ImageView citizenJournalistImage = (ImageView) findViewById(R.id.citizenJournalistImage);
        imageLoader.displayImage(Config.IMAGE_DOWNLOAD_BASE_URL + Config.FOLDER_CITIZEN_JOURNALIST + Config.getFolderForCj() +
                        cj_image//"http://timesofindia.indiatimes.com/" +
                //"thumb/msid-51900429,width-400,resizemode-4/51900429.jpg"
                , citizenJournalistImage, options);

        String postedBy = featured_news.getUser_name();
        CustomTextView tvNewsPostedBy = (CustomTextView) findViewById(R.id.tvNewsPostedBy);

        tvNewsPostedBy.setText(" " + postedBy);
        String postedAt = " " + Util.getTime(featured_news.getApproved_date());
        CustomTextView tvNewsPostedAt = (CustomTextView) findViewById(R.id.tvNewsPostedAt);
        tvNewsPostedAt.setText(postedAt);

        String title = featured_news.getName();
        CustomTextView tvNewsTitle = (CustomTextView) findViewById(R.id.tvNewsTitle);
//        int text_size_in_pixel_fn = getResources().getInteger(R.integer.news_title_size);
//        tvNewsTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, Config.get_text_size(HomeActivity.this, text_size_in_pixel_fn));
        tvNewsTitle.setText(title);

        setBreakingNews();

        if(conferenceNews != null && conferenceNews.getStarted_at() != null) {

            CustomTextView tvStartedAt = (CustomTextView) findViewById(R.id.tvStartedAt);
            String startedAt = conferenceNews.getStarted_at(); //2016-04-23 08:04:26

            if(startedAt != null && startedAt.contains(" ")) {
                String[] ss = startedAt.split(" ");
                if (conferenceNews.getStart_time() != null && ss.length > 0) {
                    String startTime = conferenceNews.getStart_time();
                    String final_to_show = ss[0] + " " + startTime;
                    tvStartedAt.setText(" " + final_to_show);
                } else if(ss.length > 0){
                    String final_to_show = ss[0];
                    tvStartedAt.setText(" " + final_to_show);
                }
            }
            ImageView imgConTime = (ImageView) findViewById(R.id.imgConTime);
            imgConTime.setVisibility(View.VISIBLE);
            CustomTextView tvLive = (CustomTextView) findViewById(R.id.tvLive);
            tvLive.setVisibility(View.VISIBLE);
        } else{
            ImageView imgConTime = (ImageView) findViewById(R.id.imgConTime);
            imgConTime.setVisibility(View.GONE);
            CustomTextView tvLive = (CustomTextView) findViewById(R.id.tvLive);
            tvLive.setVisibility(View.GONE);
        }

        if(conferenceNews != null && conferenceNews.getName() != null) {
            CustomTextView tvDebateTitleName = (CustomTextView) findViewById(R.id.tvDebateTitleName);
            tvDebateTitleName.setText(conferenceNews.getName().trim());
            CustomTextView tvDebateTitle = (CustomTextView) findViewById(R.id.tvDebateTitle);
            tvDebateTitle.setText("DEBATE OF THE DAY");
        }

        CustomTextView tvDebateTitle = (CustomTextView) findViewById(R.id.tvDebateTitle);

        String c_image = "";
        if(conferenceNews != null
                && conferenceNews.getConference_banner() != null
                && conferenceNews.getConference_banner().length() > 0) {
            c_image = Config.IMAGE_DOWNLOAD_BASE_URL + Config.FOLDER_CONFERENCE + Config.getFolderForDP()
                    + conferenceNews.getConference_banner().trim();
        } else{
            c_image = Config.DOMAIN + "/images/debateBg.jpg";
        }
        ImageView imgConference = (ImageView) findViewById(R.id.imgConference);
        imageLoader.displayImage(c_image, imgConference, options);
        layoutFeaturedNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit().putInt(Config.SP_IS_FROM_CHILD_ACTIVITY, 1).commit();
                Intent intent = new Intent(HomeActivity1.this, NewsDetailsActivity.class);
                intent.putExtra("news", featured_news);
                startActivity(intent);
            }
        });

        layoutTopNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit().putInt(Config.SP_IS_FROM_CHILD_ACTIVITY, 1).commit();
                Intent intent = new Intent(HomeActivity1.this, NewsListActivity.class);
                intent.putExtra("isVideo", true);
                intent.putExtra("is_from_top_news", true);
                startActivity(intent);
            }
        });

        layoutDebate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(HomeActivity.this, ConferenceActivity.class);
//                intent.putExtra("conference", conferenceNews);
//                startActivity(intent);

                getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit().putInt(Config.SP_IS_FROM_CHILD_ACTIVITY, 1).commit();
                PackageManager manager = getPackageManager();
                try {
                    Intent i = manager.getLaunchIntentForPackage("com.google.android.exoplayer.demo");
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    i.putExtra("conference_id", conferenceNews.getId());
                    i.putExtra("conference_title", conferenceNews.getName());

                    Intent goToMarket = new Intent(Intent.ACTION_VIEW)
                            .setData(Uri.parse("market://details?id=com.package.name"));
                    if(i != null && Util.isIntentSafe(HomeActivity1.this, i)) {
                        startActivity(i);
                    } else if(Util.isIntentSafe(HomeActivity1.this, goToMarket)){
                        startActivity(goToMarket);
                    } else {
                        new AlertDialog.Builder(HomeActivity1.this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar)
                                .setIcon(R.mipmap.ic_warning)
                                .setTitle("Conference")
                                .setMessage("Some error occurred.")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setCancelable(false)
                                .show();
                    }
                } catch (Exception e) {

                }
            }
        });

        layoutCitizenJournalist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit().putInt(Config.SP_IS_FROM_CHILD_ACTIVITY, 1).commit();
                Intent intent = new Intent(HomeActivity1.this, CitizenJournalistActivity.class);
                //Intent intent = new Intent(HomeActivity.this, PaginationActivity.class);
                startActivity(intent);
            }
        });

        /*if(mTopVideo != null && mViralVideo != null){
            addTopAndViralVideoToView(layoutBody);
        } else if(mTopVideo != null){
            addTopAndViralVideoToView(layoutBody, mTopVideo, true);
        } else */if(mViralVideo != null){
            addTopAndViralVideoToView(layoutBody);//, mViralVideo, false);
        }

        if(categoryNews != null && categoryNews.size() > 0){
            int i = 0;
            int count = 2;
            while(i < categoryNews.size()){
                if(count % 2 == 0){

                    RelativeLayout layout_cat_news = new RelativeLayout(HomeActivity1.this);
                    int height_layout_cat_news_section = (int)(Util.getScreenHeight() * LAYOUT_WEIGHT_CATEGORY_NEWS);
                    RelativeLayout.LayoutParams layout_params_cat_news = new RelativeLayout.LayoutParams
                            (RelativeLayout.LayoutParams.MATCH_PARENT, height_layout_cat_news_section);
                    layout_cat_news.setLayoutParams(layout_params_cat_news);

                    if(i >= categoryNews.size()){
                        break;
                    }

                    final News news = categoryNews.get(i);
                    View view_cat_two = LayoutInflater.from(HomeActivity1.this).inflate(R.layout.layout_category_news_two, null);
                    ImageView img_cat_one = (ImageView) view_cat_two.findViewById(R.id.imgNewsOne);
                    String cat_news_img_one_file_path = "";
                    if(news != null){
                        cat_news_img_one_file_path = Util.getImageFilePathForNews(news, null);
                    }
                    imageLoader.displayImage(cat_news_img_one_file_path//"http://timesofindia.indiatimes.com/" +
                            //"thumb/msid-51900429,width-400,resizemode-4/51900429.jpg"
                            , img_cat_one, options);
                    CustomTextView tvcatNewsTitle = (CustomTextView) view_cat_two.findViewById(R.id.tvNewsTitle);
                    tvcatNewsTitle.setText(news.getName());

                    CustomTextView tvcatNewsPostedAt = (CustomTextView) view_cat_two.findViewById(R.id.tvNewsPostedAt);
                    tvcatNewsPostedAt.setText(" " + Util.getTime(news.getApproved_date()));

                    CustomTextView tvNewsCategoryName = (CustomTextView) view_cat_two.findViewById(R.id.tvNewsCategoryName);
                    tvNewsCategoryName.setText(news.getCategory());

                    img_cat_one.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit().putInt(Config.SP_IS_FROM_CHILD_ACTIVITY, 1).commit();
                            Intent intent = new Intent(HomeActivity1.this, CategoryNewsListActivity.class);
                            intent.putExtra("slug", news.getCategoryslug());
                            startActivity(intent);
                        }
                    });
                    if(i+1 >= categoryNews.size()){
                        break;
                    }
                    final News news2 = categoryNews.get(i + 1);
                    ImageView img_cat_two = (ImageView) view_cat_two.findViewById(R.id.imgNewsTwo);
                    String cat_news_img_two_file_path = "";
                    if(news2 != null){
                        cat_news_img_two_file_path = Util.getImageFilePathForNews(news2, null);
                    }
                    imageLoader.displayImage(cat_news_img_two_file_path//"http://timesofindia.indiatimes.com/" +
                            //"thumb/msid-51900429,width-400,resizemode-4/51900429.jpg"
                            , img_cat_two, options);
                    CustomTextView tvcatNewsTitleTwo = (CustomTextView) view_cat_two.findViewById(R.id.tvNewsTitleTwo);
                    tvcatNewsTitleTwo.setText(news2.getName());

                    CustomTextView tvNewsCategoryNameTwo = (CustomTextView) view_cat_two.findViewById(R.id.tvNewsCategoryNameTwo);
                    tvNewsCategoryNameTwo.setText(news2.getCategory());

                    CustomTextView tvcatNewsPostedAtTwo = (CustomTextView) view_cat_two.findViewById(R.id.tvNewsPostedAtTwo);
                    tvcatNewsPostedAtTwo.setText(" " + Util.getTime(news2.getApproved_date()));

                    img_cat_two.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit().putInt(Config.SP_IS_FROM_CHILD_ACTIVITY, 1).commit();
                            //Intent intent = new Intent(HomeActivity.this, NewsDetailsActivity.class);
                            //intent.putExtra("news", news2);
                            Intent intent = new Intent(HomeActivity1.this, CategoryNewsListActivity.class);
                            intent.putExtra("slug", news2.getCategoryslug());
                            startActivity(intent);
                        }
                    });

                    i = i + 2;
                    layout_cat_news.addView(view_cat_two);
                    layoutBody.addView(layout_cat_news);
                } else{

                    RelativeLayout layout_cat_news = new RelativeLayout(HomeActivity1.this);
                    int height_layout_cat_news_section = (int)(Util.getScreenHeight() * LAYOUT_WEIGHT_CATEGORY_NEWS);
                    RelativeLayout.LayoutParams layout_params_cat_news = new RelativeLayout.LayoutParams
                            (RelativeLayout.LayoutParams.MATCH_PARENT, height_layout_cat_news_section);
                    layout_cat_news.setLayoutParams(layout_params_cat_news);

                    if(i >= categoryNews.size()){
                        break;
                    }
                    final News news = categoryNews.get(i);
                    View view_cat_one = LayoutInflater.from(HomeActivity1.this).inflate(R.layout.layout_category_news_one, null);
                    ImageView img_cat_one = (ImageView) view_cat_one.findViewById(R.id.imgNewsOne);
                    String cat_news_img_one_file_path = "";
                    if(news != null){
                        cat_news_img_one_file_path = Util.getImageFilePathForNews(news, null);
                    }
                    imageLoader.displayImage(cat_news_img_one_file_path//"http://timesofindia.indiatimes.com/" +
                            //"thumb/msid-51900429,width-400,resizemode-4/51900429.jpg"
                            , img_cat_one, options);
                    CustomTextView tvcatNewsTitle = (CustomTextView) view_cat_one.findViewById(R.id.tvNewsTitle);
                    tvcatNewsTitle.setText(news.getName());

                    CustomTextView tvcatNewsPostedAt = (CustomTextView) view_cat_one.findViewById(R.id.tvNewsPostedAt);
                    tvcatNewsPostedAt.setText(" " + Util.getTime(news.getApproved_date()));

                    CustomTextView tvNewsCategoryName = (CustomTextView) view_cat_one.findViewById(R.id.tvNewsCategoryName);
                    tvNewsCategoryName.setText(news.getCategory());
                    img_cat_one.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit().putInt(Config.SP_IS_FROM_CHILD_ACTIVITY, 1).commit();
                            //Intent intent = new Intent(HomeActivity.this, NewsDetailsActivity.class);
                            //intent.putExtra("news", news);
                            Intent intent = new Intent(HomeActivity1.this, CategoryNewsListActivity.class);
                            intent.putExtra("slug", news.getCategoryslug());
                            startActivity(intent);
                        }
                    });

                    i = i + 1;
                    layout_cat_news.addView(view_cat_one);
                    layoutBody.addView(layout_cat_news);
                }
                count++;
            }
        }

        //layoutBottomAdvertisement.addView(getAdvertisementView("http://swisswatchwire.com/images/2013/01/movado.jpg"));
        //layoutTopAdvertisement.addView(getAdvertisementView("http://popsop.com/wp-content/uploads/181845-sprite-sprite.jpg"));
        if(layoutMostBottomAdvertisement != null) {
            layoutBody.addView(layoutMostBottomAdvertisement);
        }
    }

    private void setBreakingNews(){

        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(1000); //You can manage the blinking time with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(5);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                CustomTextView tvBreakingNews = (CustomTextView) findViewById(R.id.tvBreakingNews);
                //AnimationUtil.slideInFromTop(tvBreakingNews, 1000, 0);
                CustomTextView tvBreakingNewsNo = (CustomTextView) findViewById(R.id.tvBreakingNewsNo);
                tvBreakingNewsNo.setVisibility(View.GONE);
                if(breaking_news != null && breaking_news.getbNewsList() != null
                        && breaking_news.getbNewsList().size() > 0){
                    //blink();
                    slideBreakingNewsContinuously(false);
                } else if(breaking_news != null && breaking_news.getNewsdetails() != null
                        && breaking_news.getNewsdetails().size() > 0) {
                    slideBreakingNewsContinuously(true);
                } else{
                    tvBreakingNews.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        if(breaking_news != null && breaking_news.getbNewsList() != null
                && breaking_news.getbNewsList().size() > 0){
            //blink();
            CustomTextView tvBreakingNews = (CustomTextView) findViewById(R.id.tvBreakingNews);
            tvBreakingNews.setText("BREAKING NEWS");
            tvBreakingNews.startAnimation(anim);
        } else if(breaking_news != null && breaking_news.getNewsdetails() != null
                && breaking_news.getNewsdetails().size() > 0) {
            CustomTextView tvBreakingNews = (CustomTextView) findViewById(R.id.tvBreakingNews);
            tvBreakingNews.setText("NEWS UPDATE");
            tvBreakingNews.startAnimation(anim);
        } else{
            CustomTextView tvBreakingNews = (CustomTextView) findViewById(R.id.tvBreakingNews);
            tvBreakingNews.setVisibility(View.GONE);
        }
    }

    private void splitTextBN(String text, List<String> lll){
        String[] bbb = text.split(" ");
        int count = 0;
        String hhh = "";

        if(bbb != null && bbb.length > 0){
            int shown = 0;
            int remains = -1;
            int total = bbb.length;
            for(int i = 0; i < bbb.length; i++){
                hhh = hhh + " " +bbb[i];
                count++;
                if(count >= 7){
                    shown = shown + count;
                    count = 0;
                    lll.add(hhh);
                    hhh = "";
                    remains = total - shown;
                }

                if(remains < 7 && remains > 0 && count == remains){
                    count = 0;
                    lll.add(hhh);
                    hhh = "";
                }
            }
        }
    }

    private int indexxx = 0;
    private void slideBreakingNewsContinuously(final boolean isNewsUpdate){

        int index = 0;
        int count = 0;
        final List<String> allNews = new ArrayList<String>();
        for(int j = 0; j < breaking_news.getNewsdetails().size(); j++){
            String text = breaking_news.getNewsdetails().get(j).getTitle();
            String[] sss = text.split(" ");
            String xx = "";
            int con = 0;
            int remain = 0;
            int total = sss.length;
            for(int ik = 0; ik < sss.length; ik++){

                xx = xx + sss[ik] + " ";
                con++;
                if(con == 6){
                    allNews.add(xx);
                    xx = "";
                    remain = total - con;
                    con = 0;
                }
                if(remain <= 6 && remain > 0 && con == 0){
                    allNews.add(xx);
                    xx = "";
                    con = 0;
                }
            }
            count++;
            if(count == 3){
                if(index == breaking_news.getbNewsList().size()){
                    index = 0;
                }
                String[] sss1 = text.split(" ");
                String xx1 = "";
                int con1 = 0;
                int remain1 = 0;
                int total1 = sss.length;
                for(int ik = 0; ik < sss1.length; ik++){
                    xx1 = xx1 + sss1[ik] + " ";
                    con1++;
                    if(con1 == 6){
                        allNews.add(xx);
                        xx1 = "";
                        remain1 = total1 - con1;
                        con1 = 0;
                    }
                    if(remain1 <= 6 && remain1 > 0 && con1 == 0){
                        allNews.add(xx1);
                        xx1 = "";
                        con1 = 0;
                    }
                }
                index++;
                count = 0;
            }
        }
        showNews(allNews);
    }

    private void showNews(final List<String> allNews){

        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(indexxx == allNews.size()){
                            indexxx = 0;
                        }
                        CustomTextView tvBreakingNews = (CustomTextView) findViewById(R.id.tvBreakingNews);
                        tvBreakingNews.setText(allNews.get(indexxx).toString());
                        AnimationUtil.fadeIn(tvBreakingNews, 800, 0);
                        indexxx++;
                        showNews(allNews);
                    }
                }, 5000);
            }
        }).start();
    }

    @Override
    public void onDeviceRegisted(final String token) {
        Log.i(TAG, "GCM Token : "+token);
        if(Util.getNetworkConnectivityStatus(HomeActivity1.this) == false){
            Util.showDialogToShutdownApp(HomeActivity1.this);
            return;
        }

        new AsyncTask<Void, Void, Integer>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Integer doInBackground(Void... params) {

                InputStream in = null;
                int resCode = -1;

                try {
                    String link = Config.API_BASE_URL + Config.DEVICE_REGISTER;
                    URL url = new URL(link);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    //conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setAllowUserInteraction(false);
                    conn.setRequestMethod("POST");
                    conn.connect();

                    String email = Util.getEmailAddress(HomeActivity1.this);
                    String is_notification_enable_status = "1";
                    Uri.Builder builder = new Uri.Builder()
                            .appendQueryParameter("uid", token)
                            .appendQueryParameter("email", email)
                            .appendQueryParameter("notification", is_notification_enable_status);
                    Log.i(TAG, "Email : "+email);
                    //.appendQueryParameter("deviceid", deviceid);
                    String query = builder.build().getEncodedQuery();

                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));
                    writer.write(query);
                    writer.flush();
                    writer.close();
                    os.close();

                    conn.connect();
                    resCode = conn.getResponseCode();
                    if (resCode == HttpURLConnection.HTTP_OK) {
                        in = conn.getInputStream();
                    }
                    if(in == null){
                        return null;
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    String response = "", data = "";

                    while ((data = reader.readLine()) != null) {
                        response += data + "\n";
                    }

                    Log.i(TAG, "Device Register Response : " + response);

                    if(response != null && response.trim().equalsIgnoreCase("1")){
                        return 1;
                    } else{
                        return 0;
                    }
                } catch (SocketTimeoutException exception) {
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch (ConnectException exception) {
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch (MalformedURLException exception) {
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch (IOException exception) {
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch (Exception exception) {
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Integer aVoid) {
                super.onPostExecute(aVoid);
            }
        }.execute();
    }

    @Override
    public void onMessage(String from, Bundle bundle) {}

    @Override
    public void onPlayServiceError() {}

    @Override
    protected void onDestroy() {
        getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit().putInt(Config.SP_IS_FROM_CHILD_ACTIVITY, 0).commit();
        GCMManager.getInstance(this).unRegisterListener();
        if(timer != null){
            timer.cancel();
            timer = null;
        }
        super.onDestroy();
    }

    @Override
    public void onConferenceStart(String message) {
        loadAllData();
    }

    @Override
    public void onConferenceStop() {
        loadAllData();
    }

    @Override
    public void onBreakingNewsNotification() {
        loadAllData();
    }

    private CounterClass timer = null;
    private void setCountDownForCopnference(long total_time_remain_in_milliseconds){
        timer = new CounterClass(total_time_remain_in_milliseconds,1000);
        timer.start();
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @SuppressLint("NewApi")
    public class CounterClass extends CountDownTimer {
        public CounterClass(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }
        @Override
        public void onFinish() {
            CustomTextView tvCoundowntimer = (CustomTextView) findViewById(R.id.tvCoundowntimer);
            tvCoundowntimer.setVisibility(View.INVISIBLE);
        }
        @SuppressLint("NewApi")
        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        @Override public void onTick(long millisUntilFinished) {
            CustomTextView tvCoundowntimer = (CustomTextView) findViewById(R.id.tvCoundowntimer);
            tvCoundowntimer.setVisibility(View.VISIBLE);
            long millis = millisUntilFinished;
            String hms = String.format("%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
            System.out.println(hms); tvCoundowntimer.setText(hms);
        }
    }

    private void addTopAndViralVideoToView(LinearLayout layoutBody){
        RelativeLayout layout_cat_news = new RelativeLayout(HomeActivity1.this);
        int height_layout_cat_news_section = (int)(Util.getScreenHeight() * LAYOUT_WEIGHT_CATEGORY_NEWS);
        RelativeLayout.LayoutParams layout_params_cat_news = new RelativeLayout.LayoutParams
                (RelativeLayout.LayoutParams.MATCH_PARENT, height_layout_cat_news_section);
        layout_cat_news.setLayoutParams(layout_params_cat_news);

        /*final News news = mTopVideo;
        View view_cat_two = LayoutInflater.from(HomeActivity.this).inflate(R.layout.layout_category_news_two, null);
        ImageView img_cat_one = (ImageView) view_cat_two.findViewById(R.id.imgNewsOne);
        String cat_news_img_one_file_path = "";
        if(news != null){
            cat_news_img_one_file_path = Util.getImageFilePathForNews(news, null);
        }
        imageLoader.displayImage(cat_news_img_one_file_path//"http://timesofindia.indiatimes.com/" +
                //"thumb/msid-51900429,width-400,resizemode-4/51900429.jpg"
                , img_cat_one, options);
        CustomTextView tvcatNewsTitle = (CustomTextView) view_cat_two.findViewById(R.id.tvNewsTitle);
        tvcatNewsTitle.setText(news.getName());

        CustomTextView tvcatNewsPostedAt = (CustomTextView) view_cat_two.findViewById(R.id.tvNewsPostedAt);
        tvcatNewsPostedAt.setText(" " + Util.getTime(news.getApproved_date()));

        CustomTextView tvNewsCategoryName = (CustomTextView) view_cat_two.findViewById(R.id.tvNewsCategoryName);
        tvNewsCategoryName.setText("Top Video");

        ImageView imgPostedAt = (ImageView) view_cat_two.findViewById(R.id.imgPostedAt);
        imgPostedAt.setVisibility(View.GONE);

        ImageView imgPlayTwo = (ImageView) view_cat_two.findViewById(R.id.imgPlayTwo);
        imgPlayTwo.setVisibility(View.VISIBLE);

        ImageView imgPlay = (ImageView) view_cat_two.findViewById(R.id.imgPlay);
        imgPlay.setVisibility(View.VISIBLE);

        img_cat_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit().putInt(Config.SP_IS_FROM_CHILD_ACTIVITY, 1).commit();
                Intent intent = new Intent(HomeActivity.this, CategoryNewsListActivity.class);
                intent.putExtra("news", news);
                intent.putExtra("isTopVideo", true);
                intent.putExtra("isViralVideo", false);
                startActivity(intent);
            }
        });*/


        View view_cat_two = LayoutInflater.from(HomeActivity1.this).inflate(R.layout.layout_category_news_one, null);
        final News news2 = mViralVideo;
        ImageView img_cat_two = (ImageView) view_cat_two.findViewById(R.id.imgNewsOne);
        String cat_news_img_two_file_path = "";
        if(news2 != null){
            cat_news_img_two_file_path = Util.getImageFilePathForNews(news2, null);
        }
        imageLoader.displayImage(cat_news_img_two_file_path//"http://timesofindia.indiatimes.com/" +
                //"thumb/msid-51900429,width-400,resizemode-4/51900429.jpg"
                , img_cat_two, options);
        CustomTextView tvcatNewsTitleTwo = (CustomTextView) view_cat_two.findViewById(R.id.tvNewsTitle);
        tvcatNewsTitleTwo.setText(news2.getName());

        CustomTextView tvNewsCategoryNameTwo = (CustomTextView) view_cat_two.findViewById(R.id.tvNewsCategoryName);
        tvNewsCategoryNameTwo.setText("Viral Video");

        ImageView imgPostedAtTwo = (ImageView) view_cat_two.findViewById(R.id.imgPostedAt);
        imgPostedAtTwo.setVisibility(View.GONE);

        CustomTextView tvcatNewsPostedAtTwo = (CustomTextView) view_cat_two.findViewById(R.id.tvNewsPostedAt);
        tvcatNewsPostedAtTwo.setText(" " + Util.getTime(news2.getApproved_date()));

        img_cat_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit().putInt(Config.SP_IS_FROM_CHILD_ACTIVITY, 1).commit();
                Intent intent = new Intent(HomeActivity1.this, CategoryNewsListActivity.class);
                intent.putExtra("news", news2);
                intent.putExtra("isTopVideo", false);
                intent.putExtra("isViralVideo", true);
                startActivity(intent);
            }
        });

        layout_cat_news.addView(view_cat_two);
        layoutBody.addView(layout_cat_news);
    }

    private void addTopAndViralVideoToView(LinearLayout layoutBody, final News news, final boolean isTopVideo){
        RelativeLayout layout_cat_news = new RelativeLayout(HomeActivity1.this);
        int height_layout_cat_news_section = (int)(Util.getScreenHeight() * LAYOUT_WEIGHT_CATEGORY_NEWS);
        RelativeLayout.LayoutParams layout_params_cat_news = new RelativeLayout.LayoutParams
                (RelativeLayout.LayoutParams.MATCH_PARENT, height_layout_cat_news_section);
        layout_cat_news.setLayoutParams(layout_params_cat_news);

        View view_cat_two = LayoutInflater.from(HomeActivity1.this).inflate(R.layout.layout_category_news_one, null);
        ImageView img_cat_one = (ImageView) view_cat_two.findViewById(R.id.imgNewsOne);
        String cat_news_img_one_file_path = "";
        if(news != null){
            cat_news_img_one_file_path = Util.getImageFilePathForNews(news, null);
        }
        imageLoader.displayImage(cat_news_img_one_file_path//"http://timesofindia.indiatimes.com/" +
                //"thumb/msid-51900429,width-400,resizemode-4/51900429.jpg"
                , img_cat_one, options);

        ImageView imgPostedAt = (ImageView) view_cat_two.findViewById(R.id.imgPostedAt);
        imgPostedAt.setVisibility(View.GONE);

        CustomTextView tvcatNewsTitle = (CustomTextView) view_cat_two.findViewById(R.id.tvNewsTitle);
        tvcatNewsTitle.setText(news.getName());

        CustomTextView tvcatNewsPostedAt = (CustomTextView) view_cat_two.findViewById(R.id.tvNewsPostedAt);
        tvcatNewsPostedAt.setText(" " + Util.getTime(news.getApproved_date()));

        CustomTextView tvNewsCategoryName = (CustomTextView) view_cat_two.findViewById(R.id.tvNewsCategoryName);
        if(isTopVideo) {
            tvNewsCategoryName.setText("Top Video");
        } else{
            tvNewsCategoryName.setText("Viral Video");
        }

        img_cat_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit().putInt(Config.SP_IS_FROM_CHILD_ACTIVITY, 1).commit();
                Intent intent = new Intent(HomeActivity1.this, CategoryNewsListActivity.class);
                intent.putExtra("news", news);
                if(isTopVideo) {
                    intent.putExtra("isTopVideo", true);
                } else{
                    intent.putExtra("isViralVideo", false);
                }
                startActivity(intent);
            }
        });

        ImageView imgPlay = (ImageView) view_cat_two.findViewById(R.id.imgPlay);
        imgPlay.setVisibility(View.VISIBLE);

        layout_cat_news.addView(view_cat_two);
        layoutBody.addView(layout_cat_news);
    }

    private void loadTopViralVideos(final boolean isRefresh){

        if(Util.getNetworkConnectivityStatus(HomeActivity1.this) == false){
            Util.showDialogToShutdownApp(HomeActivity1.this);
            return;
        }

        //final ProgressBar pBar = (ProgressBar) findViewById(R.id.pBar);
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //pBar.setVisibility(View.VISIBLE);
                isLatestData = false;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                //pBar.setVisibility(View.GONE);
                mIsContentLoaded = true;
                if(isRefresh){
                    if(mSwipeRefreshLayout != null){
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    displayContent();
                } else if(mIsCategoriesLoaded
                        && mIsBreakingNewsLoaded
                        && mIsContentLoaded
                        && mIsContentDisplayed == false) {
                    if(mSwipeRefreshLayout != null){
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    anim.start();
                }
            }

            @Override
            protected Void doInBackground(Void... params) {
                InputStream in = null;
                int resCode = -1;

                try {
                    String link = Config.API_BASE_URL + Config.HOME_SCREEN_TOP_VIRAL_VIDEO;
                    URL url = new URL(link);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    //conn.setReadTimeout(10000);
                    //conn.setConnectTimeout(15000);
                    //conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setAllowUserInteraction(false);
                    //conn.setInstanceFollowRedirects(true);
                    conn.setRequestMethod("GET");
                    conn.connect();

                    resCode = conn.getResponseCode();
                    if (resCode == HttpURLConnection.HTTP_OK) {
                        in = conn.getInputStream();
                    }
                    Log.i("HomeActivity : load()", "Error :" + resCode);
                    if (in == null) {
                        return null;
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    String response = "", data = "";

                    while ((data = reader.readLine()) != null) {
                        response += data + "\n";
                    }

                    Log.i(TAG, "Response : " + response);

                    /*{
                               "TOP_VIDEO": {
                                    "file_path": "185134078014649630961671479371.mp4",
                                    "name": "Demo",
                                    "slug": "demo",
                                    "is_video": "1"
                                },
                                "VIRAL_VIDEO": {
                                    "file_path": "185134078014649630961671479371.mp4",
                                    "name": "Demo",
                                    "slug": "demo",
                                    "is_video": "1"
                                }
                    *    }
                    * */

                    JSONObject res = new JSONObject(response);
                    if(res.isNull("TOP_VIDEO") == false){
                        mTopVideo = new News(Parcel.obtain());
                        if(res.getJSONObject("TOP_VIDEO").isNull("file_path") == false){
                            String file_path = res.getJSONObject("TOP_VIDEO").getString("file_path");
                            mTopVideo.setFile_path(file_path);
                        }
                        if(res.getJSONObject("TOP_VIDEO").isNull("name") == false){
                            String name = res.getJSONObject("TOP_VIDEO").getString("name");
                            mTopVideo.setName(name);
                        }
                        if(res.getJSONObject("TOP_VIDEO").isNull("slug") == false){
                            String slug = res.getJSONObject("TOP_VIDEO").getString("slug");
                            mTopVideo.setSlug(slug);
                        }
                        if(res.getJSONObject("TOP_VIDEO").isNull("is_video") == false){
                            String is_video = res.getJSONObject("TOP_VIDEO").getString("is_video");
                            mTopVideo.setIs_video(is_video);
                        }
                    }
                    if(res.isNull("VIRAL_VIDEO") == false){
                        mViralVideo = new News(Parcel.obtain());
                        if(res.getJSONObject("VIRAL_VIDEO").isNull("file_path") == false){
                            String file_path = res.getJSONObject("VIRAL_VIDEO").getString("file_path");
                            mViralVideo.setFile_path(file_path);
                        }
                        if(res.getJSONObject("VIRAL_VIDEO").isNull("name") == false){
                            String name = res.getJSONObject("VIRAL_VIDEO").getString("name");
                            mViralVideo.setName(name);
                        }
                        if(res.getJSONObject("VIRAL_VIDEO").isNull("slug") == false){
                            String slug = res.getJSONObject("VIRAL_VIDEO").getString("slug");
                            mViralVideo.setSlug(slug);
                        }
                        if(res.getJSONObject("VIRAL_VIDEO").isNull("is_video") == false){
                            String is_video = res.getJSONObject("VIRAL_VIDEO").getString("is_video");
                            mViralVideo.setIs_video(is_video);
                        }
                    }
                    isLatestData = true;
                    return null;
                } catch(SocketTimeoutException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch(ConnectException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch(MalformedURLException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch (IOException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch(Exception exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                }
                return null;
            }
        }.execute();
    }

    private void loadAdvertisementPost(final boolean isRefresh){

        if(Util.getNetworkConnectivityStatus(HomeActivity1.this) == false){
            Util.showDialogToShutdownApp(HomeActivity1.this);
            return;
        }

        //final ProgressBar pBar = (ProgressBar) findViewById(R.id.pBar);
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //pBar.setVisibility(View.VISIBLE);
                isLatestData = false;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                //pBar.setVisibility(View.GONE);
                mIsContentLoaded = true;
                if(isRefresh){
                    if(mSwipeRefreshLayout != null){
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    displayContent();
                } else if(mIsCategoriesLoaded
                        && mIsBreakingNewsLoaded
                        && mIsContentLoaded
                        && mIsContentDisplayed == false) {
                    if(mSwipeRefreshLayout != null){
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    anim.start();
                }
            }

            @Override
            protected Void doInBackground(Void... params) {
                InputStream in = null;
                int resCode = -1;

                try {
                    String link = Config.API_BASE_URL + Config.HOME_SCREEN_ADVERTISEMENT_POST;
                    URL url = new URL(link);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    //conn.setReadTimeout(10000);
                    //conn.setConnectTimeout(15000);
                    //conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setAllowUserInteraction(false);
                    //conn.setInstanceFollowRedirects(true);
                    conn.setRequestMethod("GET");
                    conn.connect();

                    resCode = conn.getResponseCode();
                    if (resCode == HttpURLConnection.HTTP_OK) {
                        in = conn.getInputStream();
                    }
                    Log.i("HomeActivity : load()", "Error :" + resCode);
                    if (in == null) {
                        return null;
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    String response = "", data = "";

                    while ((data = reader.readLine()) != null) {
                        response += data + "\n";
                    }

                    Log.i(TAG, "Response : " + response);

                    JSONObject res = new JSONObject(response);
                    if(res.isNull("ADVERTISEMENT_HEADER") == false){
                        ADVERTISEMENT_HEADER = res.getInt("ADVERTISEMENT_HEADER");
                    }

                    if(res.isNull("ADVERTISEMENT_MIDDLE") == false){
                        ADVERTISEMENT_MIDDLE = res.getInt("ADVERTISEMENT_MIDDLE");
                    }

                    if(res.isNull("ADVERTISEMENT_FOOTER") == false){
                        ADVERTISEMENT_FOOTER = res.getInt("ADVERTISEMENT_FOOTER");
                    }

                    if(res.isNull("ADVERTISEMENT") == false){
                        JSONArray top_news_now = res.getJSONArray("ADVERTISEMENT");
                        for(int i = 0; i < top_news_now.length(); i++){
                            Advertisement advertisement = new Advertisement(Parcel.obtain());
                            if(top_news_now.getJSONObject(i).isNull("name") == false){
                                String name = top_news_now.getJSONObject(i).getString("name");
                                advertisement.setName(name);
                            }
                            if(top_news_now.getJSONObject(i).isNull("advertisement_section_id") == false){
                                String section = top_news_now.getJSONObject(i).getString("advertisement_section_id");
                                advertisement.setSection(section);
                            }
                            if(top_news_now.getJSONObject(i).isNull("file_path") == false){
                                String file_path = top_news_now.getJSONObject(i).getString("file_path");
                                advertisement.setFile_path(file_path);
                            }
                            if(top_news_now.getJSONObject(i).isNull("url_link") == false){
                                String url_link = top_news_now.getJSONObject(i).getString("url_link");
                                advertisement.setUrl_link(url_link);
                            }
                            advertisementList.add(advertisement);
                        }
                    }

                    isLatestData = true;
                    return null;
                } catch(SocketTimeoutException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch(ConnectException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch(MalformedURLException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch (IOException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch(Exception exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                }
                return null;
            }
        }.execute();
    }

    private void loadCategoryNews(final boolean isRefresh){

        if(Util.getNetworkConnectivityStatus(HomeActivity1.this) == false){
            Util.showDialogToShutdownApp(HomeActivity1.this);
            return;
        }

        //final ProgressBar pBar = (ProgressBar) findViewById(R.id.pBar);
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //pBar.setVisibility(View.VISIBLE);
                isLatestData = false;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                //pBar.setVisibility(View.GONE);
                mIsContentLoaded = true;
                if(isRefresh){
                    if(mSwipeRefreshLayout != null){
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    displayContent();
                } else if(mIsCategoriesLoaded
                        && mIsBreakingNewsLoaded
                        && mIsContentLoaded
                        && mIsContentDisplayed == false) {
                    if(mSwipeRefreshLayout != null){
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    anim.start();
                }
            }

            @Override
            protected Void doInBackground(Void... params) {
                InputStream in = null;
                int resCode = -1;

                try {
                    String link = Config.API_BASE_URL + Config.HOME_SCREEN_CATEGORY_NEWS;
                    URL url = new URL(link);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    //conn.setReadTimeout(10000);
                    //conn.setConnectTimeout(15000);
                    //conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setAllowUserInteraction(false);
                    //conn.setInstanceFollowRedirects(true);
                    conn.setRequestMethod("GET");
                    conn.connect();

                    resCode = conn.getResponseCode();
                    if (resCode == HttpURLConnection.HTTP_OK) {
                        in = conn.getInputStream();
                    }
                    Log.i("HomeActivity : load()", "Error :" + resCode);
                    if (in == null) {
                        return null;
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    String response = "", data = "";

                    while ((data = reader.readLine()) != null) {
                        response += data + "\n";
                    }

                    Log.i(TAG, "Response : " + response);

                    JSONArray category_news_list = new JSONArray(response);
                    categoryNews.clear();
                    for(int i = 0; i < category_news_list.length(); i++){
                        News news = new News(Parcel.obtain());
                        if(category_news_list.getJSONObject(i).isNull("name") == false){
                            String name = category_news_list.getJSONObject(i).getString("name");
                            news.setName(name);
                        }
                        if(category_news_list.getJSONObject(i).isNull("slug") == false){
                            String slug = category_news_list.getJSONObject(i).getString("slug");
                            news.setSlug(slug);
                        }
                        if(category_news_list.getJSONObject(i).isNull("featured_image") == false){
                            String featured_image = category_news_list.getJSONObject(i).getString("featured_image");
                            news.setImage(featured_image);
                        }
                        if(category_news_list.getJSONObject(i).isNull("short_description") == false){
                            String short_description = category_news_list.getJSONObject(i).getString("short_description");
                            news.setShort_description(short_description);
                        }
                        if(category_news_list.getJSONObject(i).isNull("approved_date") == false){
                            String approved_date = category_news_list.getJSONObject(i).getString("approved_date");
                            news.setApproved_date(approved_date);
                        }
                        if(category_news_list.getJSONObject(i).isNull("username") == false){
                            String username = category_news_list.getJSONObject(i).getString("username");
                            news.setUser_name(username);
                        }
                        if(category_news_list.getJSONObject(i).isNull("categoryname") == false){
                            String categoryname = category_news_list.getJSONObject(i).getString("categoryname");
                            news.setCategory(categoryname);
                        }
                        categoryNews.add(news);
                    }
                    isLatestData = true;
                    return null;
                } catch(SocketTimeoutException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch(ConnectException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch(MalformedURLException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch (IOException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch(Exception exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                }
                return null;
            }
        }.execute();
    }

    private void loadCitizenCustomize(final boolean isRefresh){
        {

            if(Util.getNetworkConnectivityStatus(HomeActivity1.this) == false){
                Util.showDialogToShutdownApp(HomeActivity1.this);
                return;
            }

            //final ProgressBar pBar = (ProgressBar) findViewById(R.id.pBar);
            new AsyncTask<Void, Void, Void>(){
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    //pBar.setVisibility(View.VISIBLE);
                    isLatestData = false;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    //pBar.setVisibility(View.GONE);
                    mIsContentLoaded = true;
                    if(isRefresh){
                        if(mSwipeRefreshLayout != null){
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                        displayContent();
                    } else if(mIsCategoriesLoaded
                            && mIsBreakingNewsLoaded
                            && mIsContentLoaded
                            && mIsContentDisplayed == false) {
                        if(mSwipeRefreshLayout != null){
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                        anim.start();
                    }
                }

                @Override
                protected Void doInBackground(Void... params) {
                    InputStream in = null;
                    int resCode = -1;

                    try {
                        String link = Config.API_BASE_URL + Config.HOME_SCREEN_CITIZEN_CUSTOMIZE;
                        URL url = new URL(link);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        //conn.setReadTimeout(10000);
                        //conn.setConnectTimeout(15000);
                        //conn.setDoInput(true);
                        conn.setDoOutput(true);
                        conn.setAllowUserInteraction(false);
                        //conn.setInstanceFollowRedirects(true);
                        conn.setRequestMethod("GET");
                        conn.connect();

                        resCode = conn.getResponseCode();
                        if (resCode == HttpURLConnection.HTTP_OK) {
                            in = conn.getInputStream();
                        }
                        Log.i("HomeActivity : load()", "Error :" + resCode);
                        if (in == null) {
                            return null;
                        }
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                        String response = "", data = "";

                        while ((data = reader.readLine()) != null) {
                            response += data + "\n";
                        }

                        Log.i(TAG, "Response : " + response);

                        JSONObject res = new JSONObject(response);
                        if(res != null){
                            if(res.isNull("name") == false) {
                                String name = res.getString("name");
                                citizenJournalistVideos.setName(name);
                            }
                            if(res.isNull("file_path") == false) {
                                String file_path = res.getString("file_path");
                                citizenJournalistVideos.setFile_path(file_path);
                            }
                        }
                        isLatestData = true;
                        return null;
                    } catch(SocketTimeoutException exception){
                        Log.e(TAG, "LoginAsync : doInBackground", exception);
                    } catch(ConnectException exception){
                        Log.e(TAG, "LoginAsync : doInBackground", exception);
                    } catch(MalformedURLException exception){
                        Log.e(TAG, "LoginAsync : doInBackground", exception);
                    } catch (IOException exception){
                        Log.e(TAG, "LoginAsync : doInBackground", exception);
                    } catch(Exception exception){
                        Log.e(TAG, "LoginAsync : doInBackground", exception);
                    }
                    return null;
                }
            }.execute();
        }
    }



    private void loadConferenceNews(final boolean isRefresh){

        if(Util.getNetworkConnectivityStatus(HomeActivity1.this) == false){
            Util.showDialogToShutdownApp(HomeActivity1.this);
            return;
        }

        //final ProgressBar pBar = (ProgressBar) findViewById(R.id.pBar);
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //pBar.setVisibility(View.VISIBLE);
                isLatestData = false;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                //pBar.setVisibility(View.GONE);
                mIsContentLoaded = true;
                if(isRefresh){
                    if(mSwipeRefreshLayout != null){
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    displayContent();
                } else if(mIsCategoriesLoaded
                        && mIsBreakingNewsLoaded
                        && mIsContentLoaded
                        && mIsContentDisplayed == false) {
                    if(mSwipeRefreshLayout != null){
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    anim.start();
                }
            }

            @Override
            protected Void doInBackground(Void... params) {
                InputStream in = null;
                int resCode = -1;

                try {
                    String link = Config.API_BASE_URL + Config.HOME_SCREEN_CONFERENCE;
                    URL url = new URL(link);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    //conn.setReadTimeout(10000);
                    //conn.setConnectTimeout(15000);
                    //conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setAllowUserInteraction(false);
                    //conn.setInstanceFollowRedirects(true);
                    conn.setRequestMethod("GET");
                    conn.connect();

                    resCode = conn.getResponseCode();
                    if (resCode == HttpURLConnection.HTTP_OK) {
                        in = conn.getInputStream();
                    }
                    Log.i("HomeActivity : load()", "Error :" + resCode);
                    if (in == null) {
                        return null;
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    String response = "", data = "";

                    while ((data = reader.readLine()) != null) {
                        response += data + "\n";
                    }

                    Log.i(TAG, "Response : " + response);

                    JSONObject conference_news = new JSONObject(response);
                    if(conference_news != null){
                        conferenceNews = new ConferenceNews(Parcel.obtain());
                        if(conference_news.isNull("id") == false) {
                            String id = conference_news.getString("id");
                            conferenceNews.setId(id);
                        }
                        if(conference_news.isNull("featured_image") == false){
                            String featured_image = conference_news.getString("featured_image");
                            conferenceNews.setFeatured_image(featured_image);
                        }

                        if(conference_news.isNull("name") == false) {
                            String name = conference_news.getString("name");
                            conferenceNews.setName(name);
                        }
                        if(conference_news.isNull("short_desc") == false) {
                            String short_desc = conference_news.getString("short_desc");
                            conferenceNews.setShort_desc(short_desc);
                        }
                        if(conference_news.isNull("long_desc") == false) {
                            String long_desc = conference_news.getString("long_desc");
                            conferenceNews.setLong_desc(long_desc);
                        }
                        if(conference_news.isNull("conference_banner") == false) {
                            String conference_banner = conference_news.getString("conference_banner");
                            conferenceNews.setConference_banner(conference_banner);
                        }
                        if(conference_news.isNull("started_at") == false){
                            String started_at = conference_news.getString("started_at");
                            conferenceNews.setStarted_at(started_at);
                        }
                        if(conference_news.isNull("start_time") == false) {
                            String start_time = conference_news.getString("start_time");
                            conferenceNews.setStart_time(start_time);
                        }
                        if(conference_news.isNull("end_time") == false) {
                            String end_time = conference_news.getString("end_time");
                            conferenceNews.setEnd_time(end_time);
                        }
                    }
                    isLatestData = true;
                    return null;
                } catch(SocketTimeoutException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch(ConnectException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch(MalformedURLException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch (IOException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch(Exception exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                }
                return null;
            }
        }.execute();
    }

    private void loadFeaturedNews(final boolean isRefresh){

        if(Util.getNetworkConnectivityStatus(HomeActivity1.this) == false){
            Util.showDialogToShutdownApp(HomeActivity1.this);
            return;
        }

        //final ProgressBar pBar = (ProgressBar) findViewById(R.id.pBar);
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //pBar.setVisibility(View.VISIBLE);
                isLatestData = false;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                //pBar.setVisibility(View.GONE);
                mIsContentLoaded = true;
                if(isRefresh){
                    if(mSwipeRefreshLayout != null){
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    displayContent();
                } else if(mIsCategoriesLoaded
                        && mIsBreakingNewsLoaded
                        && mIsContentLoaded
                        && mIsContentDisplayed == false) {
                    if(mSwipeRefreshLayout != null){
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    anim.start();
                }
            }

            @Override
            protected Void doInBackground(Void... params) {
                InputStream in = null;
                int resCode = -1;

                try {
                    String link = Config.API_BASE_URL + Config.HOME_SCREEN_FEATURED_NEWS;
                    URL url = new URL(link);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setAllowUserInteraction(false);
                    conn.setRequestMethod("GET");
                    conn.connect();

                    resCode = conn.getResponseCode();
                    if (resCode == HttpURLConnection.HTTP_OK) {
                        in = conn.getInputStream();
                    }
                    Log.i("HomeActivity : load()", "Error :" + resCode);
                    if (in == null) {
                        return null;
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    String response = "", data = "";

                    while ((data = reader.readLine()) != null) {
                        response += data + "\n";
                    }

                    Log.i(TAG, "Response : " + response);

                    JSONObject fnews = new JSONObject(response);
                    if(fnews != null) {

                        if (fnews.isNull("id") == false) {
                            String id = fnews.getString("id");
                            featured_news.setId(id);
                        }

                        if (fnews.isNull("name") == false) {
                            String name = fnews.getString("name");
                            featured_news.setName(name);
                        }

                        if (fnews.isNull("slug") == false) {
                            String slug = fnews.getString("slug");
                            featured_news.setSlug(slug);
                        }

                        if (fnews.isNull("featured_image") == false) {
                            String featured_image = fnews.getString("featured_image");
                            featured_news.setImage(featured_image);
                        }

                        if (fnews.isNull("short_description") == false){
                            String short_description = fnews.getString("short_description");
                            featured_news.setShort_description(short_description);
                        }

                        if(fnews.isNull("file_path") == false) {
                            String file_path = fnews.getString("file_path");
                            featured_news.setFile_path(file_path);
                        }

                        if(fnews.isNull("approved_date") == false) {
                            String approved_date = fnews.getString("approved_date");
                            featured_news.setApproved_date(approved_date);
                        }

                        if(fnews.isNull("position") == false) {
                            String position = fnews.getString("position");
                            featured_news.setPosition(position);
                        }

                        if(fnews.isNull("user_id") == false) {
                            String user_id = fnews.getString("user_id");
                            featured_news.setUser_id(user_id);
                        }

                        if(fnews.isNull("username") ==false) {
                            String username = fnews.getString("username");
                            featured_news.setUser_name(username);
                        }
                    }

                    isLatestData = true;
                    return null;
                } catch(SocketTimeoutException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch(ConnectException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch(MalformedURLException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch (IOException exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                } catch(Exception exception){
                    Log.e(TAG, "LoginAsync : doInBackground", exception);
                }
                return null;
            }
        }.execute();
    }
}
