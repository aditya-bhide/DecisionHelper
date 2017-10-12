package com.google.sdl.decisionhelper;

import java.util.ArrayList;

/**
 * Created by aditya on 26/9/17.
 */

public class GroupObj {
    String gpName;
    String gpProfilePic;
    ArrayList<String> questionList;
    ArrayList<String> memberList;

    public GroupObj() {
        questionList=new ArrayList<>();
        memberList=new ArrayList<>();
    }

    public String getGpName() {
        return gpName;
    }

    public void setGpName(String gpName) {
        this.gpName = gpName;
    }

    public String getGpProfilePic() {
        return gpProfilePic;
    }

    public void setGpProfilePic(String gpProfilePic) {
        this.gpProfilePic = gpProfilePic;
    }

    public ArrayList<String> getQuestionList() {
        return questionList;
    }

    public void setQuestionList(ArrayList<String> questionList) {
        this.questionList = questionList;
    }

    public ArrayList<String> getMemberList() {
        return memberList;
    }

    public void setMemberList(ArrayList<String> memberList) {
        this.memberList = memberList;
    }
}
