package com.google.sdl.decisionhelper;

import android.media.Image;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aditya on 22/9/17.
 */

public class UserObj {


    String uid;
    String name;
    String profilepickUrl;
    String userAuthType;

    ArrayList<String> groupid;

    public UserObj() {
        groupid=new ArrayList<>();
        userAuthType="Fuck offf";
    }

    public String getUserAuthType() { return userAuthType; }

    public void setUserAuthType(String userAuthType) { this.userAuthType = userAuthType; }

    public ArrayList<String> getGroupid() {
        return groupid;
    }

    public void setGroupid(ArrayList<String> groupid) {
        this.groupid = groupid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilepickUrl() {
        return profilepickUrl;
    }

    public void setProfilepickUrl(String profilepickUrl) {
        this.profilepickUrl = profilepickUrl;
    }
}
