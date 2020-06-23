package com.appbestsmile.voicelikeme.global;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class AppPreference {

    private static AppPreference mInstance;
    private Context mContext;

    private SharedPreferences mMyPreferences;
    private SharedPreferences.Editor editorPreference;

    private final String FIELD_NICKNAME = "Nickname";
    private final String FIELD_PROFILE_IMAGE = "ProfileImage";

    private AppPreference(){ }

    public static AppPreference getInstance(){
        if (mInstance == null) mInstance = new AppPreference();
        return mInstance;
    }

    public void Initialize(Context ctxt){
        mContext = ctxt;
        mMyPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        editorPreference = mMyPreferences.edit();
    }

    public void SetNickname(String nickname){
        editorPreference.putString(FIELD_NICKNAME, nickname);
        editorPreference.commit();
    }

    public String GetNickname(){
        return mMyPreferences.getString(FIELD_NICKNAME, "");
    }

    public void SetProfileImage(String profileImage){
        editorPreference.putString(FIELD_PROFILE_IMAGE, profileImage);
        editorPreference.commit();
    }

    public String GetProfileImage(){
        return mMyPreferences.getString(FIELD_PROFILE_IMAGE, "");
    }
}
