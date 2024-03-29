package com.lipl.ommcom.pojo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by luminousinfoways on 09/10/15.
 */
public class Comment implements Parcelable {
    private String name;
    private String comment;
    private String user_id;
    private String verified_date;
    private String file_path;
    private String is_image;
    private String is_video;
    private String is_audio;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getVerified_date() {
        return verified_date;
    }

    public void setVerified_date(String verified_date) {
        this.verified_date = verified_date;
    }

    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    public String getIs_image() {
        return is_image;
    }

    public void setIs_image(String is_image) {
        this.is_image = is_image;
    }

    public String getIs_video() {
        return is_video;
    }

    public void setIs_video(String is_video) {
        this.is_video = is_video;
    }

    public String getIs_audio() {
        return is_audio;
    }

    public void setIs_audio(String is_audio) {
        this.is_audio = is_audio;
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {

        @Override
        public Comment createFromParcel(Parcel source) {
            return new Comment(source);
        }

        @Override
        public Comment[] newArray(int size) {
            Comment[] currentLocations = new Comment[size];
            return currentLocations;
        }
    };

    public Comment(Parcel in){
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(comment);
        dest.writeString(user_id);
        dest.writeString(verified_date);
        dest.writeString(file_path);
        dest.writeString(is_image);
        dest.writeString(is_video);
        dest.writeString(is_audio);
    }

    private void readFromParcel(Parcel in){
        name = in.readString();
        comment = in.readString();
        user_id = in.readString();
        verified_date = in.readString();
        file_path = in.readString();
        is_image = in.readString();
        is_video = in.readString();
        is_audio = in.readString();
    }
}