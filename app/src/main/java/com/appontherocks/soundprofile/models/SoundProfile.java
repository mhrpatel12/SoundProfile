package com.appontherocks.soundprofile.models;

import com.appontherocks.soundprofile.R;
import com.ramotion.expandingcollection.ECCardData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mihir on 3/13/2017.
 */

public class SoundProfile implements ECCardData<SoundProfile> {
    public String profileName;
    public boolean chkDefaultProfile;
    public boolean chkRinger;
    public boolean chkMedia;
    public boolean chkAlarm;
    public boolean chkCall;
    public String notificationVolume;
    public String musicVolume;
    public String alarmVolume;
    public String callVolume;
    public String latitude;
    public String longitude;
    public String ringToneURI;
    public String mKey;
    private Integer mainBackgroundResource;
    private Integer headBackgroundResource;
    private List<SoundProfile> listItems;

    public SoundProfile() {
    }
    public SoundProfile(String profileName, String notificationVolume, String musicVolume, String alarmVolume) {
        this.profileName = profileName;
        this.notificationVolume = notificationVolume;
        this.musicVolume = musicVolume;
        this.alarmVolume = alarmVolume;
    }

    public SoundProfile(String profileName, boolean chkDefaultProfile, boolean chkRinger, boolean chkMedia, boolean chkAlarm, boolean chkCall, String notificationVolume, String musicVolume, String alarmVolume, String callVolume, String latitude, String longitude, String ringToneURI) {
        this.profileName = profileName;
        this.chkDefaultProfile = chkDefaultProfile;
        this.chkRinger = chkRinger;
        this.chkMedia = chkMedia;
        this.chkAlarm = chkAlarm;
        this.chkCall = chkCall;
        this.notificationVolume = notificationVolume;
        this.musicVolume = musicVolume;
        this.alarmVolume = alarmVolume;
        this.callVolume = callVolume;
        this.latitude = latitude;
        this.longitude = longitude;
        this.ringToneURI = ringToneURI;
    }

    public SoundProfile(String cardTitle, Integer mainBackgroundResource, Integer headBackgroundResource, List<SoundProfile> listItems) {
        this.mainBackgroundResource = mainBackgroundResource;
        this.headBackgroundResource = headBackgroundResource;
        this.listItems = listItems;
    }

    public static ArrayList<ECCardData> generateListData() {
        ArrayList<ECCardData> list = new ArrayList<>();
        list.add(new SoundProfile("Profile 1", R.drawable.staticmap, R.drawable.staticmap, createItemsList("Profile 1")));
        list.add(new SoundProfile("Profile 2", R.drawable.staticmap, R.drawable.staticmap, createItemsList("Profile 2")));
        list.add(new SoundProfile("Profile 3", R.drawable.staticmap, R.drawable.staticmap, createItemsList("Profile 3")));
        list.add(new SoundProfile("Profile 4", R.drawable.staticmap, R.drawable.staticmap, createItemsList("Profile 4")));

        return list;
    }

    private static List<SoundProfile> createItemsList(String cardName) {

        List<SoundProfile> list = new ArrayList<>();
        SoundProfile profile = new SoundProfile();
        profile.profileName = cardName;
        list.add(profile);

        return list;
    }

    @Override
    public Integer getMainBackgroundResource() {
        return mainBackgroundResource;
    }

    @Override
    public Integer getHeadBackgroundResource() {
        return headBackgroundResource;
    }

    @Override
    public List<SoundProfile> getListItems() {
        return listItems;
    }

/*    public static List<ECCardData> generateListData(ArrayList<SoundProfile> listItems) {
        List<ECCardData> list = new ArrayList<>();
        for (int i = 0; i < listItems.size(); i++) {
            list.add(new SoundProfile(listItems.get(i).profileName, R.drawable.staticmap, R.drawable.staticmap, listItems));
        }
        return list;
    }*/
}
