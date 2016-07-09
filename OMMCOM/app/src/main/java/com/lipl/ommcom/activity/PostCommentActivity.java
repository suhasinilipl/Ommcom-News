package com.lipl.ommcom.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.lipl.ommcom.R;
import com.lipl.ommcom.pojo.News;
import com.lipl.ommcom.twitter.Utils;
import com.lipl.ommcom.util.Config;
import com.lipl.ommcom.util.CustomTextView;
import com.lipl.ommcom.util.MultipartUtility;
import com.lipl.ommcom.util.RealPathOfAudioUtil;
import com.lipl.ommcom.util.RealPathOfImageUtil;
import com.lipl.ommcom.util.RealPathOfVideoUtil;
import com.lipl.ommcom.util.Util;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.apache.http.conn.ConnectTimeoutException;
import org.brickred.socialauth.Profile;
import org.brickred.socialauth.android.DialogListener;
import org.brickred.socialauth.android.SocialAuthAdapter;
import org.brickred.socialauth.android.SocialAuthError;
import org.brickred.socialauth.android.SocialAuthListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PostCommentActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUESTCODE_PICK_VIDEO = 1239;
    private static final int REQUESTCODE_PICK_IMAGE = 1238;
    private static final int REQUESTCODE_PICK_AUDIO = 1237;
    private News mNews;
    private File file_to_post = null;
    private boolean is_video_file = false;
    private boolean is_audio_file = false;
    private boolean is_image_file = false;

    private boolean is_video_upload = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_comment);

        setToolBar();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(Util.getNetworkConnectivityStatus(PostCommentActivity.this) == false){
            Util.showDialogToShutdownApp(PostCommentActivity.this);
            return;
        }

        if(getIntent().getExtras() != null){
            mNews = getIntent().getExtras().getParcelable("news");
            is_video_upload = getIntent().getExtras().getBoolean("is_video_upload");
        } else {
            return;
        }

        ImageView btnPostCommentFB = (ImageView) findViewById(R.id.btnPostCommentFB);
        ImageView btnResetTwitter = (ImageView) findViewById(R.id.btnResetTwitter);
        ImageView btnResetGooglePlus = (ImageView) findViewById(R.id.btnResetGooglePlus);
        ImageView btnResetEmail = (ImageView) findViewById(R.id.btnResetEmail);

        btnPostCommentFB.setOnClickListener(this);
        btnResetTwitter.setOnClickListener(this);
        btnResetGooglePlus.setOnClickListener(this);
        btnResetEmail.setOnClickListener(this);

        Button btnLogout = (Button) findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(this);
        setViewAsPerLoginStatus();

        MaterialEditText commenttext = (MaterialEditText) findViewById(R.id.commenttext);
        MaterialEditText long_desc_text = (MaterialEditText) findViewById(R.id.long_desc_text);
        MaterialEditText title = (MaterialEditText) findViewById(R.id.tvTitle);

        if(is_video_upload == false && mNews != null) {
            title.setVisibility(View.GONE);
            long_desc_text.setVisibility(View.GONE);
            commenttext.setHint("Comment");
        } else{
            title.setVisibility(View.VISIBLE);
            long_desc_text.setVisibility(View.VISIBLE);
            commenttext.setHint("Description");
        }

        Button btnReset = (Button) findViewById(R.id.btnReset);
        btnReset.setOnClickListener(this);

        Button btnChooseFile = (Button) findViewById(R.id.btnChooseFile);
        btnChooseFile.setOnClickListener(this);

        Button btnPostComment = (Button) findViewById(R.id.btnPostComment);
        btnPostComment.setOnClickListener(this);
    }

    private boolean otherLogin = false;

    @Override
    public void onClick(View v) {
        int id = v.getId();

        MaterialEditText email = (MaterialEditText) findViewById(R.id.email);
        MaterialEditText name = (MaterialEditText) findViewById(R.id.name);

        switch (id){
            case R.id.btnChooseFile:
                CharSequence colors[] = null;
                if(is_video_upload){
                    colors = new CharSequence[] {"Videos", "Images", "Audio"};
                } else{
                    colors = new CharSequence[] {"Videos", "Images", "Audio"};
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Choose one");
                builder.setItems(colors, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // the user clicked on colors[which]
                        switch (which) {
                            case 0:
                                Intent intentVideo = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                intentVideo.setAction(Intent.ACTION_GET_CONTENT);
                                intentVideo.addCategory(Intent.CATEGORY_OPENABLE);
                                intentVideo.setType("video/*");
                                startActivityForResult(Intent.createChooser(intentVideo, "Select video"), REQUESTCODE_PICK_VIDEO);
                                break;
                            case 1:
                                Intent intentImage = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                intentImage.setAction(Intent.ACTION_GET_CONTENT);
                                intentImage.setType("image/*");
                                intentImage.addCategory(Intent.CATEGORY_OPENABLE);
                                startActivityForResult(Intent.createChooser(intentImage, "Select image"), REQUESTCODE_PICK_IMAGE);
                                break;
                            case 2:
                                Intent intentAudio = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                intentAudio.setAction(Intent.ACTION_GET_CONTENT);
                                intentAudio.setType("audio/*");
                                intentAudio.addCategory(Intent.CATEGORY_OPENABLE);
                                startActivityForResult(Intent.createChooser(intentAudio, "Select audio"), REQUESTCODE_PICK_AUDIO);
                                break;
                            default:
                                break;
                        }
                    }
                });
                builder.show();
                break;

            case R.id.btnPostCommentFB:
                userLogin(0);
                otherLogin = false;
                break;
            case R.id.btnResetTwitter:
                userLogin(1);
                otherLogin = false;
                break;
            case R.id.btnResetGooglePlus:
                userLogin(2);
                otherLogin = false;
                break;
            case R.id.btnResetEmail:

                if(email.getVisibility() == View.VISIBLE
                        && name.getVisibility() == View.VISIBLE){
                    email.setVisibility(View.GONE);
                    name.setVisibility(View.GONE);
                    otherLogin = false;
                } else {
                    email.setVisibility(View.VISIBLE);
                    name.setVisibility(View.VISIBLE);
                    otherLogin = true;
                }
                break;
            case R.id.btnPostComment:
                if(Util.getNetworkConnectivityStatus(PostCommentActivity.this) == true) {
                    //EditText etname = (EditText) findViewById(R.id.name);
                    //String name = etname.getText().toString().trim();
                    //if(name == null || name.trim().length() <= 0){
                       // etname.setError("Enter your name");
                        //return;
                    //}
                    //EditText etemail = (EditText) findViewById(R.id.email);
                    //String email = etemail.getText().toString().trim();
                    //if(email == null || email.trim().length() <= 0){
                        //email = "";
                    //}
                    EditText etcomment = (EditText) findViewById(R.id.commenttext);
                    String comment = etcomment.getText().toString().trim();
                    if(comment == null || comment.trim().length() <= 0){
                        etcomment.setError("Enter your comment");
                        return;
                    }

                    CheckBox checkBox = (CheckBox) findViewById(R.id.checkBoxIsAnonymous);
                    String is_anonymous = "0";
                    if(checkBox.isChecked()){
                        is_anonymous = "1";
                    }

                    MaterialEditText long_desc_text = (MaterialEditText) findViewById(R.id.long_desc_text);
                    String long_description = long_desc_text.getText().toString().trim();


                    String title = "";
                    if(is_video_upload) {
                        MaterialEditText _title = (MaterialEditText) findViewById(R.id.tvTitle);
                        title = _title.getText().toString().trim();
                        if(title == null || title.trim().length() <= 0){
                            _title.setError("Enter title");
                            return;
                        }
                    }

                    email.setVisibility(View.VISIBLE);
                    name.setVisibility(View.VISIBLE);
                    String _email = "";
                    String _name = "";
                    //if(otherLogin){
                        if(email != null && email.getText() != null
                                && email.getText().toString() != null
                                && email.getText().toString().trim() != null){
                            _email = email.getText().toString().trim();
                        }
                        if(name != null && name.getText() != null
                                && name.getText().toString() != null
                                && name.getText().toString().trim() != null){
                            _name = name.getText().toString().trim();

                        }
                    //} else{
                        int login_status = getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 1).getInt(Config.SP_LOGIN_STATUS, 0);
                        if(login_status == 1){
                            _email = getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 1).getString(Config.SP_USER_EMAIL, "");
                            _name = getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 1).getString(Config.SP_USER_NAME, "");

                            email.setText(_email);
                            name.setText(_name);
                        }
                    //}

                    if(_email == null || _email.trim().length() <= 0){
                        email.setError("Enter your email.");
                        return;
                    }
                    if(_name == null || _name.trim().length() <= 0){
                        name.setError("Enter your name.");
                        return;
                    }

                    PostCommentAsyncTask asyncTask = new PostCommentAsyncTask();
                    if(is_video_upload == false && mNews != null) {
                        asyncTask.execute(_name, _email, comment, is_anonymous);
                    } else {
                        asyncTask.execute(_name, _email, comment, is_anonymous, long_description, title);
                    }
                }
                break;
            case R.id.btnReset:
                //MaterialEditText name = (MaterialEditText) findViewById(R.id.name);
                //name.setText("");
                //MaterialEditText email = (MaterialEditText) findViewById(R.id.email);
                //email.setText("");
                MaterialEditText commenttext = (MaterialEditText) findViewById(R.id.commenttext);
                commenttext.setText("");
                CustomTextView fileName = (CustomTextView) findViewById(R.id.fileName);
                fileName.setText("Choose File");
                file_to_post = null;
                LinearLayout layoutFileToShow = (LinearLayout) findViewById(R.id.layoutFileToShow);
                layoutFileToShow.removeAllViews();
                break;

            case R.id.btnLogout:

                new AlertDialog.Builder(PostCommentActivity.this,
                        android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar)
                        .setIcon(R.mipmap.ic_sentiment_dissatisfied)
                        .setTitle("Logout")
                        .setMessage("Are you sure want to logout?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(socialAuthAdapter != null){
                                    String social_site = getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 1).getString(Config.SP_LOGGED_IN_SOCIAL_SITE, "");
                                    boolean is_sign_out = false;
                                    if(social_site != null && social_site.equalsIgnoreCase("f")) {
                                        is_sign_out = socialAuthAdapter.signOut(PostCommentActivity.this, SocialAuthAdapter.Provider.FACEBOOK.name());
                                    } else if(social_site != null && social_site.equalsIgnoreCase("t")) {
                                        is_sign_out = socialAuthAdapter.signOut(PostCommentActivity.this, SocialAuthAdapter.Provider.TWITTER.name());
                                    } else if(social_site != null && social_site.equalsIgnoreCase("g")) {
                                        is_sign_out = socialAuthAdapter.signOut(PostCommentActivity.this, SocialAuthAdapter.Provider.GOOGLEPLUS.name());
                                    }
                                    if(is_sign_out){

                                        new AlertDialog.Builder(PostCommentActivity.this,
                                                android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar)
                                                .setIcon(R.mipmap.ic_done)
                                                .setTitle("Logout")
                                                .setMessage("Logout successfully.")
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit()
                                                                    .putInt(Config.SP_LOGIN_STATUS, 0).commit();
                                                        }
                                                    }
                                                )
                                                .setCancelable(false);
                                    } else{

                                        new AlertDialog.Builder(PostCommentActivity.this,
                                                android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar)
                                                .setIcon(R.mipmap.ic_sentiment_dissatisfied)
                                                .setTitle("Logout")
                                                .setMessage("Logout failed.")
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit()
                                                                    .putInt(Config.SP_LOGIN_STATUS, 1).commit();
                                                        }
                                                    }
                                                )
                                                .setCancelable(false);
                                    }
                                    setViewAsPerLoginStatus();
                                }

                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .show();
                break;
            default:
                break;
        }
    }

    private void setToolBar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationIcon(R.mipmap.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_citizen_journalist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private class PostCommentAsyncTask extends AsyncTask<String, Void, Void> {

        String TAG = "PostCommentAsyncTask";
        private boolean is_success = false;
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(PostCommentActivity.this, R.style.MyTheme);
                progressDialog.setCancelable(false);
                progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
                progressDialog.show();
            }
        }

        @Override
        protected Void doInBackground(String... params) {
            String charset = "UTF-8";
            String requestURL = "";
            if(is_video_upload == false && mNews != null) {
                requestURL = Config.API_BASE_URL + Config.POST_COMMENT;
            } else {
                requestURL = Config.API_BASE_URL + Config.POST_CITIZEN_NEWS;
            }
            try {
                String name = params[0];
                String email = params[1];
                String comment_text = params[2];
                String is_anonymous = params[3];
                MultipartUtility multipart = new MultipartUtility(requestURL, charset);
                if(is_video_upload == false && mNews != null) {
                    multipart.addFormField("news_id", mNews.getId());
                    multipart.addFormField("comment", comment_text);
                } else{
                    String long_description = params[4];
                    String title = params[5];
                    multipart.addFormField("description", comment_text);
                    multipart.addFormField("long_description", long_description);
                    multipart.addFormField("title", title);
                }
                multipart.addFormField("is_anonymous", is_anonymous);
                multipart.addFormField("name", name);
                multipart.addFormField("email", email);
                if(file_to_post != null && file_to_post.exists())
                    multipart.addFilePart("file_path", file_to_post);
                List<String> response = multipart.finish();
                System.out.println("SERVER REPLIED:");
                String res = "";
                for (String line : response) {
                    res = res + line + "\n";
                }
                Log.i(TAG, res);

                if(res!= null && res.trim().length() > 0 && res.trim().equalsIgnoreCase("1")){
                    is_success = true;
                } else{
                    is_success = false;
                }
            } catch(ConnectTimeoutException e){
                System.err.println(e);
            } catch(IOException ex) {
                System.err.println(ex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            super.onPostExecute(result);

            if(progressDialog != null){
                progressDialog.dismiss();
            }

            String title = "";
            String message = "";
            if(is_video_upload){
                title = "Upload file";
                message = "File";
            } else{
                title = "Post Comment";
                message = "Comment";
            }

            if(is_success){
                new AlertDialog.Builder(PostCommentActivity.this,
                        android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar)
                        .setIcon(R.mipmap.ic_done)
                        .setTitle(title)
                        .setMessage(message + " posted successfully and will be available after admin approves it.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .show();
            } else{
                new AlertDialog.Builder(PostCommentActivity.this,
                        android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar)
                        .setIcon(R.mipmap.ic_sentiment_dissatisfied)
                        .setTitle(title)
                        .setMessage(message + " post failed.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String realPath = null;
        if (requestCode == REQUESTCODE_PICK_VIDEO
                && resultCode == Activity.RESULT_OK) {
            if(data != null && data.getData() != null) {
                /*if (Build.VERSION.SDK_INT < 11)
                    realPath = RealPathOfVideoUtil.getRealPathFromURI_BelowAPI11(this, data.getData());
                else if (Build.VERSION.SDK_INT < 19)
                    realPath = RealPathOfVideoUtil.getRealPathFromURI_API11to18(this, data.getData());
                else
                    realPath = RealPathOfVideoUtil.getRealPathFromURI_API19(this, data.getData());*/
                if(realPath == null || realPath.trim().length() <= 0){
                    realPath = RealPathOfVideoUtil.getRealPathFromURI(data.getData(), PostCommentActivity.this);
                }
                if(realPath != null && realPath.length() > 0){
                    is_audio_file = false;
                    is_image_file = false;
                    is_video_file = true;
                }
                Log.i("CommentList", "File to post real path " + realPath);
            } else{
                new AlertDialog.Builder(PostCommentActivity.this,
                        android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar)
                        .setIcon(R.mipmap.ic_sentiment_dissatisfied)
                        .setTitle("Select File")
                        .setMessage("Something went wrong.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
        } else if (requestCode == REQUESTCODE_PICK_IMAGE
                && resultCode == Activity.RESULT_OK) {
            if(data != null && data.getData() != null) {
                /*if (Build.VERSION.SDK_INT < 11)
                    realPath = RealPathOfImageUtil.getRealPathFromURI_BelowAPI11(this, data.getData());
                else if (Build.VERSION.SDK_INT < 19)
                    realPath = RealPathOfImageUtil.getRealPathFromURI_API11to18(this, data.getData());
                else
                    realPath = RealPathOfImageUtil.getRealPathFromURI_API19(this, data.getData());*/
                if(realPath == null || realPath.trim().length() <= 0){
                    realPath = RealPathOfImageUtil.getRealPathFromURI(data.getData(), PostCommentActivity.this);
                }
                Log.i("CommentList", "File to post real path " + realPath);
                if(realPath != null && realPath.length() > 0){
                    is_audio_file = false;
                    is_image_file = true;
                    is_video_file = false;
                }
            } else{
                new AlertDialog.Builder(PostCommentActivity.this,
                        android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar)
                        .setIcon(R.mipmap.ic_sentiment_dissatisfied)
                        .setTitle("Select File")
                        .setMessage("Something went wrong.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
        } else if (requestCode == REQUESTCODE_PICK_AUDIO
                && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                if (Build.VERSION.SDK_INT < 11)
                    realPath = RealPathOfAudioUtil.getRealPathFromURI_BelowAPI11(this, data.getData());
                else if (Build.VERSION.SDK_INT < 19)
                    realPath = RealPathOfAudioUtil.getRealPathFromURI_API11to18(this, data.getData());
                else
                    realPath = RealPathOfAudioUtil.getRealPathFromURI_API19(this, data.getData());
                if(realPath == null || realPath.trim().length() <= 0){
                    realPath = RealPathOfAudioUtil.getRealPathFromURI(data.getData(), PostCommentActivity.this);
                }
                Log.i("CommentList", "File to post real path " + realPath);
                if (realPath != null && realPath.length() > 0) {
                    is_audio_file = true;
                    is_image_file = false;
                    is_video_file = false;
                }
            } else {
                new AlertDialog.Builder(PostCommentActivity.this,
                        android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar)
                        .setIcon(R.mipmap.ic_sentiment_dissatisfied)
                        .setTitle("Select File")
                        .setMessage("Something went wrong.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
        }
        if(realPath != null && realPath.trim().length() > 0){
            File file = new File(realPath);
            if(file != null && file.exists()){
                file_to_post = file;
                LinearLayout layoutFileToShow = (LinearLayout) findViewById(R.id.layoutFileToShow);
                layoutFileToShow.removeAllViews();
                if(is_video_file){
                    View view = LayoutInflater.from(PostCommentActivity.this).inflate(R.layout.layout_file_video, null);
                    ImageView imgPlay = (ImageView) view.findViewById(R.id.imgPlay);
                    imgPlay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(file_to_post != null) {
                                Uri myUri = Uri.fromFile(file_to_post);
                                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                                intent.setDataAndType(myUri, "video/*");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } else{
                                Toast.makeText(PostCommentActivity.this, "Some thing went wrong.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    layoutFileToShow.addView(view);
                } else if(is_image_file){
                    View view = LayoutInflater.from(PostCommentActivity.this).inflate(R.layout.layout_file_image, null);
                    ImageView image = (ImageView) view.findViewById(R.id.image);
                    Bitmap myBitmap = BitmapFactory.decodeFile(file_to_post.getAbsolutePath());
                    image.setImageBitmap(myBitmap);
                    layoutFileToShow.addView(view);
                } else if(is_audio_file){

                    View view = LayoutInflater.from(PostCommentActivity.this).inflate(R.layout.layout_file_audio, null);
                    ImageView imgPlay = (ImageView) view.findViewById(R.id.imgPlay);
                    imgPlay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(file_to_post != null) {
                                Uri myUri = Uri.fromFile(file_to_post);
                                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                                intent.setDataAndType(myUri, "audio/*");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } else{
                                Toast.makeText(PostCommentActivity.this, "Some thing went wrong.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    layoutFileToShow.addView(view);

                    /*View view = LayoutInflater.from(PostCommentActivity.this).inflate(R.layout.layout_file_audio, null);
                    initPlayAudio(view);
                    layoutFileToShow.addView(view);*/
                }
            }
        }
    }

    private void setViewAsPerLoginStatus(){
        int login_status = getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 1).getInt(Config.SP_LOGIN_STATUS, 0);
        Button btnPostComment = (Button) findViewById(R.id.btnPostComment);
        Button btnLogout = (Button) findViewById(R.id.btnLogout);
        MaterialEditText name = (MaterialEditText) findViewById(R.id.name);
        MaterialEditText email = (MaterialEditText) findViewById(R.id.email);

        ImageView btnPostCommentFB = (ImageView) findViewById(R.id.btnPostCommentFB);
        ImageView btnResetTwitter = (ImageView) findViewById(R.id.btnResetTwitter);
        ImageView btnResetGooglePlus = (ImageView) findViewById(R.id.btnResetGooglePlus);
        ImageView btnResetEmail = (ImageView) findViewById(R.id.btnResetEmail);

        name.setVisibility(View.VISIBLE);
        email.setVisibility(View.VISIBLE);

        if(login_status == 0) {
            btnPostComment.setText("Login & Post");
            btnLogout.setVisibility(View.GONE);
            name.setText("");
            email.setText("");

            btnPostCommentFB.setVisibility(View.VISIBLE);
            btnResetTwitter.setVisibility(View.VISIBLE);
            btnResetGooglePlus.setVisibility(View.VISIBLE);
            btnResetEmail.setVisibility(View.VISIBLE);
        } else {

            btnPostCommentFB.setVisibility(View.GONE);
            btnResetTwitter.setVisibility(View.GONE);
            btnResetGooglePlus.setVisibility(View.GONE);
            btnResetEmail.setVisibility(View.GONE);

            btnPostComment.setText("Post");
            btnLogout.setVisibility(View.VISIBLE);
            String _name = getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 1).getString(Config.SP_USER_NAME, "");
            String _email = getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 1).getString(Config.SP_USER_EMAIL, "");

            if(_name != null && _name.trim().length() > 0) {
                name.setText(_name);
            }
            if(_email != null && _email.trim().length() > 0) {
                email.setText(_email);
            }
        }
    }

    private void userLogin(int which){
        int login_status = getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 1).getInt(Config.SP_LOGIN_STATUS, 0);;
        if(login_status == 0){
            //Allow user to login using any social network

            //CharSequence colors[] = new CharSequence[]{"Facebook", "Twitter", "Google Plus"};

            //android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
            //builder.setTitle("Choose one");
            //builder.setItems(colors, new DialogInterface.OnClickListener() {
                //@Override
                //public void onClick(DialogInterface dialog, int which) {
                    // the user clicked on colors[which]
                    switch (which) {
                        case 0:
                            try {
                                initSocialAdapterForFacebook();
                            } catch(Exception exception){
                                Log.e("PostComment", "userLogin()", exception);
                            }
                            break;
                        case 1:
                            initSocialAdapter();
                            break;
                        case 2:
                            initSocialAdapterForGooglePlus();
                            break;
                        default:
                            break;
                    }
               // }
            //});
            //builder.show();
        }
    }

    private SocialAuthAdapter socialAuthAdapter;
    private ProgressDialog pd;

    private void initSocialAdapter() {
        // Utils.isOnline method check the internet connection
        if (Utils.isOnline(getApplicationContext())) {
            // Initialize the socialAuthAdapter with ResponseListener
            pd = ProgressDialog.show(PostCommentActivity.this, null, null);
            socialAuthAdapter = new SocialAuthAdapter(new ResponseListener("t"));
            // Add Twitter to set as provider to post on twitter
            socialAuthAdapter.addProvider(SocialAuthAdapter.Provider.TWITTER, R.drawable.twitter);
            // this line is for Authorize start
            socialAuthAdapter.authorize(PostCommentActivity.this, SocialAuthAdapter.Provider.TWITTER);
        } else {
            // showing message when internet connection is not available
            Toast.makeText(getApplicationContext(),
                    "Check your internet connection..", Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void initSocialAdapterForGooglePlus() {
        if (Utils.isOnline(getApplicationContext())) {
            pd = ProgressDialog.show(PostCommentActivity.this, null, null);
            socialAuthAdapter = new SocialAuthAdapter(new ResponseListener("g"));
            socialAuthAdapter.addProvider(SocialAuthAdapter.Provider.GOOGLEPLUS, R.drawable.googleplus);
            socialAuthAdapter.authorize(PostCommentActivity.this, SocialAuthAdapter.Provider.GOOGLEPLUS);
        } else {
            Toast.makeText(getApplicationContext(),
                    "Check your internet connection..", Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void initSocialAdapterForFacebook() throws Exception {
        // Utils.isOnline method check the internet connection
        if (Utils.isOnline(getApplicationContext())) {
            // Initialize the socialAuthAdapter with ResponseListener
            pd = ProgressDialog.show(PostCommentActivity.this, null, null);
            socialAuthAdapter = new SocialAuthAdapter(new ResponseListener("f"));
            // Add Twitter to set as provider to post on twitter
            socialAuthAdapter.addProvider(SocialAuthAdapter.Provider.FACEBOOK, R.drawable.facebook);
            // this line is for Authorize start
            //socialAuthAdapter.addConfig(SocialAuthAdapter.Provider.FACEBOOK,
              //      "197632853968695","12277e1c7260bc1850a821734af479c6", "publish_actions");
            socialAuthAdapter.authorize(PostCommentActivity.this, SocialAuthAdapter.Provider.FACEBOOK);
        } else {
            // showing message when internet connection is not available
            Toast.makeText(getApplicationContext(),
                    "Check your internet connection..", Toast.LENGTH_LONG)
                    .show();
        }
    }

    // this ResponseListener class is for getting responce of post uploading
    private class ResponseListener implements DialogListener {
        String social_site;
        public ResponseListener(String social_sites) {
            this.social_site = social_sites;
        }

        @Override
        public void onComplete(final Bundle values) {
            try {
                getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit()
                        .putInt(Config.SP_LOGIN_STATUS, 1).commit();
                if(social_site.equalsIgnoreCase("f")) {
                    getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit()
                            .putString(Config.SP_LOGGED_IN_SOCIAL_SITE, "f").commit();
                } else if(social_site.equalsIgnoreCase("t")) {
                    getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit()
                            .putString(Config.SP_LOGGED_IN_SOCIAL_SITE, "t").commit();
                } else if(social_site.equalsIgnoreCase("g")) {
                    getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit()
                            .putString(Config.SP_LOGGED_IN_SOCIAL_SITE, "g").commit();
                }
                setViewAsPerLoginStatus();
                socialAuthAdapter.getUserProfileAsync(new ProfileDataListener(social_site));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(SocialAuthError error) {
            // this method is call when error is occured in authorization
            if (pd != null && pd.isShowing())
                pd.dismiss();
            if(social_site.equalsIgnoreCase("f")) {
                socialAuthAdapter.signOut(PostCommentActivity.this, SocialAuthAdapter.Provider.FACEBOOK.name());
            } else if(social_site.equalsIgnoreCase("t")) {
                socialAuthAdapter.signOut(PostCommentActivity.this, SocialAuthAdapter.Provider.TWITTER.name());
            } else if(social_site.equalsIgnoreCase("g")) {
                socialAuthAdapter.signOut(PostCommentActivity.this, SocialAuthAdapter.Provider.GOOGLEPLUS.name());
            }
            Log.d("ShareTwitter", "Authentication Error: " + error.getMessage());
        }

        @Override
        public void onCancel() {
            // this method is call when user cancel Authentication
            if (pd != null && pd.isShowing())
                pd.dismiss();
            Log.d("ShareTwitter", "Authentication Cancelled");
        }

        @Override
        public void onBack() {
            // this method is call when user backpressed from dialog
            if (pd != null && pd.isShowing())
                pd.dismiss();
            Log.d("ShareTwitter", "Dialog Closed by pressing Back Key");
        }
    }

    private class ProfileDataListener implements SocialAuthListener {

        private String url;

        public ProfileDataListener(String site_url){
            this.url = site_url;
        }

        @Override
        public void onError(SocialAuthError socialAuthError) {
            if (pd != null && pd.isShowing())
                pd.dismiss();
        }

        @Override
        public void onExecute(String s, Object t) {
            if (pd != null && pd.isShowing())
                pd.dismiss();

            if (t instanceof Profile == false) {
                return;
            }

            Profile profileMap = (Profile) t;
            String id = profileMap.getValidatedId();
            String name = profileMap.getFullName();//getFirstName() + profileMap.getLastName();
            String email = profileMap.getEmail();
            String profile_image_url = profileMap.getProfileImageURL();

            Log.i("Postcomment", "profile : "+ profileMap.toString());

            getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit().putString(Config.SP_USER_NAME, name).commit();
            getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit().putString(Config.SP_USER_EMAIL, email).commit();
            getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit().putString(Config.SP_USER_SOCIAL_SITE_ID, id).commit();
            getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit().putString(Config.SP_USER_SOCIAL_SITE, url).commit();
            getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit().putString(Config.SP_USER_AVATAR, profile_image_url).commit();
            getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit().putString(Config.SP_USER_AVATAR_ORIGINAL, profile_image_url).commit();

            if(Util.getNetworkConnectivityStatus(PostCommentActivity.this) == false){
                Util.showDialogToShutdownApp(PostCommentActivity.this);
                return;
            }

            PostUserDetailsAsyncTask postUserDetailsAsyncTask = new PostUserDetailsAsyncTask();
            postUserDetailsAsyncTask.execute(name, email, id, url, profile_image_url, profile_image_url);
        }
    }

    private class PostUserDetailsAsyncTask extends AsyncTask<String, Void, Void> {

        String TAG = "PostUserDetailsAsyncTask";
        private boolean is_success = false;
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(PostCommentActivity.this, R.style.MyTheme);
                progressDialog.setCancelable(false);
                progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
                progressDialog.show();
            }
        }

        @Override
        protected Void doInBackground(String... params) {
            String charset = "UTF-8";
            String requestURL = "";
            requestURL = Config.API_BASE_URL + Config.API_SEND_USER_DETAILS;

            try {
                String name = params[0];
                String email = params[1];
                String socialsite_id = params[2];
                String socialsite = params[3];
                String avatar = params[4];
                String avatar_original = params[5];

                String lat = getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 1).getString(Config.SP_LATITUDE, "");
                String lon = getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 1).getString(Config.SP_LONGITUDE, "");

                InputStream in = null;
                int resCode = -1;

                String link = requestURL;
                URL url = new URL(link);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setAllowUserInteraction(false);
                conn.setInstanceFollowRedirects(true);
                conn.setRequestMethod("POST");

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("name", name)
                .appendQueryParameter("email", email)
                        .appendQueryParameter("socialsite_id", socialsite_id)
                        .appendQueryParameter("socialsite", socialsite)
                        .appendQueryParameter("avatar", avatar)
                        .appendQueryParameter("latitude", lat)
                        .appendQueryParameter("longitude", lon)
                        .appendQueryParameter("avatar_original", avatar_original);
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
                BufferedReader reader =new BufferedReader(new InputStreamReader(in, "UTF-8"));
                String response = "",data="";

                while ((data = reader.readLine()) != null){
                    response += data + "\n";
                }

                Log.i(TAG, "Response : "+response);

                if(response!= null && response.trim().length() > 0 && response.trim().equalsIgnoreCase("1")){
                    is_success = true;
                } else{
                    is_success = false;
                }
            } catch(ConnectTimeoutException e){
                System.err.println(e);
            } catch(IOException ex) {
                System.err.println(ex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            super.onPostExecute(result);

            if(progressDialog != null)
                progressDialog.dismiss();

            if(is_success){
                getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit().putInt(Config.SP_LOGIN_STATUS, 1).commit();
            } else{
                getSharedPreferences(Config.SHARED_PREFERENCE_KEY, 2).edit().putInt(Config.SP_LOGIN_STATUS, 0).commit();
            }
        }
    }
}