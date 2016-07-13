package com.lipl.ommcom.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lipl.ommcom.R;
import com.lipl.ommcom.pojo.BNews;
import com.lipl.ommcom.pojo.BreakingNews;
import com.lipl.ommcom.pojo.NewsDetailsForFlipModel;
import com.lipl.ommcom.util.AnimationUtil;
import com.lipl.ommcom.util.Config;
import com.lipl.ommcom.util.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private final static String TAG = "MainActivity";
    private ObjectAnimator objSlideRight = null;
    private ObjectAnimator objSlideLeft = null;
    private TextView tvBG = null;
    private ObjectAnimator objAnimatorShow = null;
    private ObjectAnimator objAnimatorHide = null;
    private BreakingNews mBreakingNews = null;
    private TextView tvBKContent = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_record);

        tvBG = (TextView) findViewById(R.id.tvBG);
        tvBKContent = (TextView) findViewById(R.id.tvBKContent);
        if(Util.getNetworkConnectivityStatus(MainActivity.this)){
            loadBreakingNews();
        }
    }

    private void showBK(){
        tvBG.setVisibility(View.VISIBLE);
        tvBKContent.setVisibility(View.VISIBLE);
        objAnimatorShow = AnimationUtil.zoomIn(tvBG, 1000, 0);
        objAnimatorShow.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                slideToLeft();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void slideToLeft(){
        objSlideLeft = AnimationUtil.slideInFromRightBK(tvBG, 1000, 0);
        objSlideLeft.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                showNews();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private List<String> textList = new ArrayList<String>();
    private void getBNewsListTotShow(){
        if(mBreakingNews != null && mBreakingNews.getbNewsList() != null && mBreakingNews.getbNewsList().size() > 0){
            for(int i = 0; i < mBreakingNews.getbNewsList().size(); i++){
                List<String> str = getAdjustStringList(mBreakingNews.getbNewsList().get(i).getTitle());
                if(str != null && str.size() > 0){
                    textList.addAll(str);
                }
            }
        } else if(mBreakingNews != null && mBreakingNews.getNewsdetails() != null && mBreakingNews.getNewsdetails().size() > 0){
            for(int i = 0; i < mBreakingNews.getNewsdetails().size(); i++){
                List<String> str = getAdjustStringList(mBreakingNews.getNewsdetails().get(i).getTitle());
                if(str != null && str.size() > 0){
                    textList.addAll(str);
                }
            }
        } else{
            return;
        }
        showBK();
    }

    private List<String> getAdjustStringList(String news){
        final CharSequence text = TextUtils.concat(news);
        final List<NewsDetailsForFlipModel> newsDetailsForFlipModels =
                new ArrayList<NewsDetailsForFlipModel>();
        final List<String> sss = new ArrayList<String>();
        tvBKContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
            // Removing layout listener to avoid multiple calls
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                tvBKContent.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            } else {
                tvBKContent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
            Pagination pagination = new Pagination(text,
                    tvBKContent.getWidth(),
                    tvBKContent.getHeight(),
                    tvBKContent.getPaint(),
                    tvBKContent.getLineSpacingMultiplier(),
                    tvBKContent.getLineSpacingExtra(),
                    tvBKContent.getIncludeFontPadding());


                if(pagination != null && pagination.size() > 0){
                    for(int i = 0; i < pagination.size(); i++){
                        if(pagination.get(i) != null) {
                            sss.add(pagination.get(i).toString());
                        }
                    }
                }
            }
        });
        return sss;
    }

    private int index = 0;
    private int limit = 0;
    private void showNews(){

        // show 3 news then animate
        int count = 0;
//        for(int i = start; i < end; i++){
//            tvBKContent.setText(textList.get(i));
//            AnimationUtil.slideInFromRight(tvBKContent, 1000, 0);
//            count++;
//            if(count == 3){
//                count = 0;
//                objSlideRight = AnimationUtil.slideInFromLeft(tvBG, 1000, 0);
//                objSlideRight.addListener(new Animator.AnimatorListener() {
//                    @Override
//                    public void onAnimationStart(Animator animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        hideBK();
//                    }
//
//                    @Override
//                    public void onAnimationCancel(Animator animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animator animation) {
//
//                    }
//                });
//            }
//        }
    }

    private void hideBK(){
        objAnimatorHide = AnimationUtil.zoomOut(tvBG, 1000, 0);
        objAnimatorHide.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                showBK();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    public void expand(final View v) {
        int ANIMATION_DURATION= 1000;//in milisecond
        v.measure(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        final int targtetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 0;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? RelativeLayout.LayoutParams.WRAP_CONTENT
                        : (int)(targtetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration(ANIMATION_DURATION);

        // a.setDuration((int)(targtetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }



    public void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration(3000);
        // a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    private void loadBreakingNews(){
        if(Util.getNetworkConnectivityStatus(MainActivity.this) == false){
            Util.showDialogToShutdownApp(MainActivity.this);
            return;
        }
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //pBar.setVisibility(View.VISIBLE);
                if(mBreakingNews == null){
                    mBreakingNews = new BreakingNews(Parcel.obtain());
                }
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                //pBar.setVisibility(View.GONE);
                if(mBreakingNews != null){
                    getBNewsListTotShow();
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
                        mBreakingNews.setbNewsList(breakingNewsList);
                        mBreakingNews.setNewsdetails(newsDetailsList);
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
}