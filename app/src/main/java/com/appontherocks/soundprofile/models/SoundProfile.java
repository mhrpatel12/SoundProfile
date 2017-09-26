package com.appontherocks.soundprofile.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mihir on 3/13/2017.
 */

public class SoundProfile {
    public String profileName;
    public boolean chkDefaultProfile;
    public boolean chkRinger;
    public boolean chkMedia;
    public boolean chkAlarm;
    public boolean chkCall;
    public boolean chkNotification;
    public boolean chkSystem;
    public String ringtoneVolume;
    public String musicVolume;
    public String alarmVolume;
    public String callVolume;
    public String notificationVolume;
    public String systemVolume;

    public String wifiSetting;
    public String bluetoothSetting;

    public String latitude;
    public String longitude;
    public String ringToneURI;
    public String notificationToneURI;
    public String mKey;
    private Integer mainBackgroundResource;
    private Integer headBackgroundResource;
    private List<SoundProfile> listItems;

    public SoundProfile() {
    }

    public SoundProfile(String profileName, String ringtoneVolume, String musicVolume, String alarmVolume) {
        this.profileName = profileName;
        this.ringtoneVolume = ringtoneVolume;
        this.musicVolume = musicVolume;
        this.alarmVolume = alarmVolume;
    }

    public SoundProfile(
            String profileName,
            boolean chkDefaultProfile, boolean chkRinger, boolean chkMedia, boolean chkAlarm, boolean chkCall, boolean chkNotification, boolean chkSystem,
            String ringtoneVolume, String musicVolume, String alarmVolume, String callVolume, String notificationVolume, String systemVolume,
            String wifiSetting, String bluetoothSetting,
            String latitude, String longitude,
            String ringToneURI, String notificationToneURI) {
        this.profileName = profileName;
        this.chkDefaultProfile = chkDefaultProfile;
        this.chkRinger = chkRinger;
        this.chkMedia = chkMedia;
        this.chkAlarm = chkAlarm;
        this.chkCall = chkCall;
        this.chkNotification = chkNotification;
        this.chkSystem = chkSystem;
        this.wifiSetting = wifiSetting;
        this.bluetoothSetting = bluetoothSetting;

        this.ringtoneVolume = ringtoneVolume;
        this.musicVolume = musicVolume;
        this.alarmVolume = alarmVolume;
        this.callVolume = callVolume;
        this.notificationVolume = notificationVolume;
        this.systemVolume = systemVolume;

        this.latitude = latitude;
        this.longitude = longitude;
        this.ringToneURI = ringToneURI;
        this.notificationToneURI = notificationToneURI;
    }

    public SoundProfile(String cardTitle, Integer mainBackgroundResource, Integer headBackgroundResource, List<SoundProfile> listItems) {
        this.mainBackgroundResource = mainBackgroundResource;
        this.headBackgroundResource = headBackgroundResource;
        this.listItems = listItems;
    }

    private static List<SoundProfile> createItemsList(String cardName) {

        List<SoundProfile> list = new ArrayList<>();
        SoundProfile profile = new SoundProfile();
        profile.profileName = cardName;
        list.add(profile);

        return list;
    }
}
