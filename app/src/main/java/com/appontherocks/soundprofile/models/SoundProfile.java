package com.appontherocks.soundprofile.models;

/**
 * Created by Mihir on 3/13/2017.
 */

public class SoundProfile {
    public String profileName;
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

    public SoundProfile() {
    }

    public SoundProfile(String profileName, String notificationVolume, String musicVolume, String alarmVolume) {
        this.profileName = profileName;
        this.notificationVolume = notificationVolume;
        this.musicVolume = musicVolume;
        this.alarmVolume = alarmVolume;
    }

    public SoundProfile(String profileName, boolean chkRinger, boolean chkMedia, boolean chkAlarm, boolean chkCall, String notificationVolume, String musicVolume, String alarmVolume, String callVolume, String latitude, String longitude, String ringToneURI) {
        this.profileName = profileName;
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
}
