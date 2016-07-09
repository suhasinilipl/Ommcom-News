package com.lipl.ommcom.util;

/**
 * Created by Android Luminous on 5/13/2016.
 */
public class SingleTon {
    private static  SingleTon mInstance;
    private static CustomGCMService.OnConferenceStartListener mOnConferenceStartListener;
    private static CustomGCMService.OnConferenceStopListener mOnConferenceStopListener;
    private static CustomGCMService.OnConferenceFlashOutMessageListener mOnConferenceFlashOutMessageListener;
    private static CustomGCMService.OnBreakingNewsNotificationListener mOnBreakingNewsNotificationListener;
    public static SingleTon getInstance(){
        if(mInstance == null){
            mInstance = new SingleTon();
        }
        return mInstance;
    }

    public void setConferenceStartListener(CustomGCMService.OnConferenceStartListener mOnConferenceStartListener){
        this.mOnConferenceStartListener = mOnConferenceStartListener;
    }

    public CustomGCMService.OnConferenceStartListener getConferenceStartConference(){
        return mOnConferenceStartListener;
    }

    public void setConferenceStopListener(CustomGCMService.OnConferenceStopListener mOnConferenceStopListener){
        this.mOnConferenceStopListener = mOnConferenceStopListener;
    }

    public CustomGCMService.OnConferenceStopListener getConferenceStopConference(){
        return mOnConferenceStopListener;
    }

    public void setBreakingNewsNotificationListener(CustomGCMService.OnBreakingNewsNotificationListener mOnBreakingNewsNotificationListener){
        this.mOnBreakingNewsNotificationListener = mOnBreakingNewsNotificationListener;
    }

    public CustomGCMService.OnBreakingNewsNotificationListener getmOnBreakingNewsNotificationListener(){
        return mOnBreakingNewsNotificationListener;
    }

    public void setmOnConferenceFlashOutMessageListener(CustomGCMService.OnConferenceFlashOutMessageListener mOnConferenceFlashOutMessageListener){
        this.mOnConferenceFlashOutMessageListener = mOnConferenceFlashOutMessageListener;
    }

    public CustomGCMService.OnConferenceFlashOutMessageListener getmOnConferenceFlashOutMessageListener(){
        return mOnConferenceFlashOutMessageListener;
    }
}
