package com.cristal.ble.ui.player

class CommandEnum {

    private val http :String   = "http://";
    private val suburl:String = "/P?devid=abcd&src=1&flag=1&NP=>";

    public val ERROR: Byte = 0x0;
    public val CONTROLCMD:Byte = 0x1;
    public val CONTROLCMDSTATUS: Byte = 0xB;


    public val WIFIUSER: Byte = 0x2;
    public val WIFIPASS: Byte = 0x3;
    public val WIFIERROR: Byte = 0x4;
    public val PROGRESSBAR: Byte = 0x5;
    public val SDSONGLIST= byteArrayOf(CONTROLCMDSTATUS, 0x6); //: Byte = 0x6;
//    public val SDSONGURL = 0x7;
    public val SDSONGURL = byteArrayOf(CONTROLCMDSTATUS, 0x7);

    public val CRISTALCLOUDURL: Byte = 0x8;
    public val WIFIFMURL: Byte = 0x9;
    public val CMDINITSTATE: Byte = 0xA;

    public val PLAYBUTTONCURRENTSTATUS: Byte = 0xc;
    public val CURRENTSRCSTATUS:    Byte = 0xd;


    public val CMD_NONE = byteArrayOf(CONTROLCMD,0x0);
    public val CMD_START = byteArrayOf(CONTROLCMD,0x1);
    public val CMD_STOP = byteArrayOf(CONTROLCMD,0x2);
    public val CMD_POUSE = byteArrayOf(CONTROLCMD,0x3);
    public val CMD_PLAY = byteArrayOf(CONTROLCMD,0x4);
    public val CMD_NEXT = byteArrayOf(CONTROLCMD,0x5);
    public val CMD_PREV = byteArrayOf(CONTROLCMD,0x6);
    public val CMD_SRC_SWITCH = byteArrayOf(CONTROLCMD,0x7);
    public val CMD_VOL_UP = byteArrayOf(CONTROLCMD,0x8);
    public val CMD_VOL_DOWN = byteArrayOf(CONTROLCMD,0x9);
    public val CMD_SRC_WIFI = byteArrayOf(CONTROLCMD,0x0A)
    public val CMD_SRC_SD = byteArrayOf(CONTROLCMD,0xB);
    public val CMD_SRC_BLUTOOTH_STREAM = byteArrayOf(CONTROLCMD,0xC);
    public val CMD_SRC_WIFI_FM = byteArrayOf(CONTROLCMD,0xD);
    public val CMD_SEND_SRC_SOURS_STATUS = byteArrayOf(CONTROLCMDSTATUS,0xE);
    public val CMD_HET_SONG_FROM_NVS_MEMORY = byteArrayOf(CONTROLCMD,0xF);
    public val CMD_PLAYBUTTON_CURRENT_STATUS = byteArrayOf(CONTROLCMDSTATUS, 0x10);
    public val CMD_NEXT_WIFI_RADIO_URL = byteArrayOf(CONTROLCMD,0x11);
    public val CMD_SRC_CRISTAL_CLOUD   = byteArrayOf(CONTROLCMD,0x12);


    // device source enums
    public val SRC_NONE: Byte =  0x0;
//    public val SRC_NONE : Byte = 0x0;
    public val SDCARD: Byte = 0x1;
    public val WIFI: Byte = 0x2;
    public val BT_STREAM: Byte = 0x3;
    public val WIFI_RADIO: Byte = 0x4;
    public val WIFI_CRISTATL_CLOUD: Byte = 0x5;
    public val WIFI_AUDIO_BOOK: Byte = 0x6;
    public val WIFI_SPOTIFY: Byte = 0x7;
    /*
    add here
    */
    public val SRC_END: Byte = 0x8;



    fun buildCristalCloudUrl(srcip:String ): String {

        return http+srcip+suburl;

    }

}