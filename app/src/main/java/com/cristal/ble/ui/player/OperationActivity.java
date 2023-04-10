package com.cristal.ble.ui.player;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.cristal.ble.AppPreference;
import com.cristal.ble.MapFragment;
import com.cristal.ble.R;
import com.cristal.ble.api.ApiRepository;
import com.cristal.ble.api.CloudStreamResponse;
import com.cristal.ble.api.CristalCloudSongListResponse;
import com.cristal.ble.api.CristalNextAudioBookFromAppResponce;
import com.cristal.ble.api.CristalSetNextSongfromAppResponce;
import com.cristal.ble.api.CristallGetCurrentSongNameResponce;
import com.cristal.ble.api.GetCurrentAudiobookResponce;
import com.cristal.ble.api.cristalcloudImgResponce;
import com.cristal.ble.comm.Observer;
import com.cristal.ble.comm.ObserverManager;
import com.cristal.ble.operation.CharacteristicListFragment;
import com.cristal.ble.operation.CharacteristicOperationFragment;
import com.cristal.ble.ui.PlaylistFragment;
import com.cristal.ble.ui.imageList.ImageItemFragment;

import com.cristal.ble.ui.imageList.audionbookInterface;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class OperationActivity extends AppCompatActivity implements Observer,
        PlaylistFragment.FragmentInteractionListener,MapFragment.FragmentInteractionListener, audionbookInterface {

    private static final String TAG = "OperationActivity";

    public static final int SERVICE_LIST_PAGE = 0;
    public static final int CHAR_LIST_PAGE = 1;
    public static final int CHAR_OPERATION_PAGE = 2;

    public static final String KEY_DATA = "key_data";

    private BleDevice bleDevice;
    private String uuid_service = "0000abf0-0000-1000-8000-00805f9b34fb";
    private String uuid_write   = "0000abf3-0000-1000-8000-00805f9b34fb";
    private String uuid_notify  = "0000abf2-0000-1000-8000-00805f9b34fb";  //for receiving the data from device

    private BluetoothGattService bluetoothGattService;
    private BluetoothGattCharacteristic bluetoothGattCharacteristic;
    private int charaProp;

    private MenuItem connectMenuItem;       // Menu in the toolbar

    private EditText wifi_ssid;           // store ssid
    private EditText wifi_password;       // store password


    private ImageView btnPlay;
    private ImageView btnNext;
    private ImageView btnPrevious;
    private ImageButton btnRepeat;
    private ImageButton btnShuffle;
    private SeekBar songProgressBar;
    private boolean setsongnameflasg = false;
    private boolean selectmusicsource = false;


    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();;

    private TextView songTitleLabel;
    private TextView songCurrentDurationLabel;
    private TextView songTotalDurationLabel;
    private TextView CurrentSong;
    private TextView CurrentSong2;

    private TextView ShowCloudPlayUrl;

    private boolean playpuseflag = true;

    private androidx.appcompat.widget.Toolbar toolbar;
    private List<Fragment> fragmentList = new ArrayList<>();
    private int currentPage = SERVICE_LIST_PAGE;
    private final String[] titles = {"Service List","Char List","Char Operation"};
    private AlertDialog src_alertDialog;

    private MenuPopupWindow mMenuPopupWindow;
    private CommandEnum CMD = new CommandEnum();

    private String deviceCurrentSource = "";
    private Byte deviceCurrentSourceBite = 0x0;

    private Dialog MusicSourcedialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_player);
        initplayViw();
        initData();
        initView();

        //  initPage();
        getNotification();
        ObserverManager.getInstance().addObserver(this);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Disconnect BLE connection
        if (BleManager.getInstance().isConnected(bleDevice)) {
            BleManager.getInstance().disconnect(bleDevice);
        }

        BleManager.getInstance().clearCharacterCallback(bleDevice);

        ObserverManager.getInstance().deleteObserver(this);
    }

    @Override
    public void disConnected(BleDevice device) {
        if ((device != null) && (bleDevice != null) && (device.getKey().equals(bleDevice.getKey()))) {
            finish();
        }
    }


//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            if (currentPage != SERVICE_LIST_PAGE) {
//                currentPage--;
//                changePage(currentPage);
//                return true;
//            } else {
////                finish();
//                Toast.makeText(this,"onBackPressed",Toast.LENGTH_SHORT).show();
//                return true;
//            }
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_toolbar, menu);
        connectMenuItem = menu.findItem(R.id.action_connect_item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_connect_item:  // Disconnect BLE connection

                if (bleDevice != null) {
                    finish();
                }
                break;

            default:
                break;
        }
        return true;
    }


    @SuppressLint("ClickableViewAccessibility")
    private void  initplayViw(){

        // All player buttons

        btnPlay = (ImageView) findViewById(R.id.btnPlay);
        btnNext = (ImageView) findViewById(R.id.btnNext);
        btnPrevious = (ImageView) findViewById(R.id.btnPrevious);
        btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
        btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
        songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
        songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
        songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);
        CurrentSong = (TextView) findViewById(R.id.tv_playlist);
        CurrentSong2 = (TextView) findViewById(R.id.tv_song);

        ShowCloudPlayUrl = (TextView) findViewById(R.id.tv_spotify_url);
        ShowCloudPlayUrl.setVisibility(View.GONE); //View.INVISIBLE


        btnRepeat.setOnClickListener((view) -> {


            System.out.println("btnRepeat ------>"+CurrentSong);
            getCurrentSongName();


        });

        CurrentSong.setOnClickListener((view) -> {


            System.out.println("song url ------>"+CurrentSong);


        });



        findViewById(R.id.btn_menu).setOnClickListener((view) -> {
            showMenu(view);
        });


        findViewById(R.id.tv_spotify_url).setOnClickListener((view) -> {
            getcloudplayurls("","get");
//            showcloudUrls();
        });



        findViewById(R.id.tvPlaylist).setOnClickListener((view) -> {

            if(deviceCurrentSourceBite == CMD.getSRC_NONE()){
                deviceCurrentSource = "Plsease select the source";
            }
            else if(deviceCurrentSourceBite == CMD.getSDCARD()){
                sendMusicControlCmd(CMD.getSDSONGLIST());
                showPlaylist();
            }
            else if(deviceCurrentSourceBite == CMD.getBT_STREAM()){
                deviceCurrentSource = "Bluetooth Sream";
            }
            else if(deviceCurrentSourceBite == CMD.getWIFI_RADIO()){
                deviceCurrentSource = "Wifi Radio";
            }
            else if(deviceCurrentSourceBite == CMD.getWIFI_CRISTATL_CLOUD()){
                showcloudplaylist();
                showPlaylist();

            }
            else if(deviceCurrentSourceBite == CMD.getWIFI_AUDIO_BOOK()){
                deviceCurrentSource = "Wifi Audio Book";
                showPlaylist();
            }
            else if(deviceCurrentSourceBite == CMD.getWIFI_SPOTIFY()){
                deviceCurrentSource = "Wifi Spotify";
            }
            else if(deviceCurrentSourceBite == CMD.getSRC_END()){
                deviceCurrentSource = "ERORR";
            }

        });

        /**
         * Play button click event
         * plays a song and changes button to pause image
         * pauses a song and changes button to play image
         * */

        sendMusicControlCmd(CMD.getCMD_PLAYBUTTON_CURRENT_STATUS());


        // ListenerssendMusicControlCmd
        songProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        }); // Important


        // set Progress bar values
        songProgressBar.setProgress(0);
        songProgressBar.setMax(100);


        btnPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                // check for already playing

                if(!playpuseflag){
                    // Changing button image to play button
                    sendMusicControlCmd(CMD.getCMD_PLAY());
//                    btnPlay.setImageResource(R.drawable.btn_pause);


                }else{
                    // Resume song
                    sendMusicControlCmd(CMD.getCMD_POUSE());
//                    btnPlay.setImageResource(R.drawable.btn_play);

                    // Changing button image to pause button

                }


            }
        });

        /**
         * Next button click event
         * Plays next song by taking currentSongIndex + 1
         * */
        btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // check if next song is there or not
                sendMusicControlCmd(CMD.getCMD_NEXT());

            }
        });

        /**
         * Back button click event
         * Plays previous song by currentSongIndex - 1
         * */
        btnPrevious.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                sendMusicControlCmd(CMD.getCMD_PREV());

            }
        });

    }



    private void getcurrentruningsong(){

        System.out.println("--java-> getcurrentruningsong");


        ApiRepository.getcurrentsong("abcd", AppPreference.preference.getLoginResponse().getUser().getEmail(),AppPreference.preference.getLoginResponse().getToken(), new retrofit2.Callback<CristallGetCurrentSongNameResponce>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(retrofit2.Call<CristallGetCurrentSongNameResponce> call, retrofit2.Response<CristallGetCurrentSongNameResponce> response) {

                System.out.println("--java-> ApiRepository.GetCristalImg 3333" + response.body());

                if (response.body() != null) {
                    CristallGetCurrentSongNameResponce body = response.body();
                    System.out.println("--java-> ApiRepository.GetCristalImg 3333" + body.getData());
                    System.out.println("---> " + body);
                    if(body.getSuccess()) {
                        CurrentSong.setText(body.getData().getMusic_name());
                        CurrentSong2.setText(body.getData().getMusic_name());

                        setsongnameflasg = true;
                    }else{
                        CurrentSong.setText("No Name");
                        CurrentSong2.setText("Server error");
                        setsongnameflasg = false;
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<CristallGetCurrentSongNameResponce> call, Throwable t) {
                System.out.println("---> GeoWifiRadio 444 ERROR" + t);
            }
        });

    }


    private void showSongImage(){

        ApiRepository.GetCristalImg("deviceId", AppPreference.preference.getLoginResponse().getUser().getEmail(),AppPreference.preference.getLoginResponse().getToken(),"test.jpg", new retrofit2.Callback<cristalcloudImgResponce>() {
            @Override
            public void onResponse(retrofit2.Call<cristalcloudImgResponce> call, retrofit2.Response<cristalcloudImgResponce> response) {
                if (response.body() != null) {
                    cristalcloudImgResponce body = response.body();
                    System.out.println("--java-> ApiRepository.GetCristalImg 3333" + body.getData().getImg());
                    System.out.println("---> " + body);
                    if (body.success) {
                        System.out.println("---> Base64 " + Base64.decode(body.getData().getImg(), Base64.DEFAULT));
                        Glide.with(OperationActivity.this)
                            .load( Base64.decode(body.getData().getImg(), Base64.DEFAULT))

                            .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE).placeholder(R.drawable.song_logo)
                                    .error(R.drawable.song_logo))
                            .into(((ImageView) findViewById(R.id.im_music)));


                    } else {
                            System.out.println("---> get GeoWifiRadio failed");
                        }
                    }
            }

            @Override
            public void onFailure(retrofit2.Call<cristalcloudImgResponce> call, Throwable t) {
                System.out.println("---> GeoWifiRadio 444 ERROR" + t);
            }
        });


    }

    private void getcurrentaudiobook(){

        playList = new ArrayList<>();
        ApiRepository.Getcurrentaudiobookfromapp("abcd", AppPreference.preference.getLoginResponse().getUser().getEmail(),AppPreference.preference.getLoginResponse().getToken(), new retrofit2.Callback<GetCurrentAudiobookResponce>() {
            @Override
            public void onResponse(retrofit2.Call<GetCurrentAudiobookResponce> call, retrofit2.Response<GetCurrentAudiobookResponce> response) {
                if (response.body() != null) {
                    GetCurrentAudiobookResponce body = response.body();
                    System.out.println("---> getcurrentaudiobook:  " + body.getData().getAudio_name());
                    System.out.println("---> getcurrentaudiobook:  " + body.getData().getBook_name());

                    if (body.getSuccess()) {
//                        System.out.println("---> getcurrentaudiobook " + Base64.decode(body.getData().getImg(), Base64.DEFAULT));
                        for(String songs: body.getData().getAudios()) {
                            playList.add(songs);
//                          System.out.println("---> getcurrentaudiobook playlsit "+playList);

                        }

//                        imageByteArray = body.getData().getImg();
                        Glide.with(OperationActivity.this)
                                .load( Base64.decode(body.getData().getImg(), Base64.DEFAULT))
                                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE).placeholder(R.drawable.song_logo)
                                        .error(R.drawable.song_logo))
                                .into(((ImageView) findViewById(R.id.im_music)));

                        CurrentSong.setText(body.getData().getAudio_name());
                        CurrentSong2.setText(body.getData().getAudio_name());

                    } else {
                        CurrentSong.setText("NO name");
                        CurrentSong2.setText("NO name");


                        System.out.println("---> get getcurrentaudiobook failed");
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<GetCurrentAudiobookResponce> call, Throwable t) {
                CurrentSong.setText("Get Error");
                CurrentSong2.setText("Get Error");
                System.out.println("---> getcurrentaudiobook 444 ERROR" + t);
            }
        });


    }


    private void showcloudplaylist(){

        ApiRepository.CristalCloudSongList("abcd", AppPreference.preference.getLoginResponse().getUser().getEmail(),AppPreference.preference.getLoginResponse().getToken(),1,10, new retrofit2.Callback<CristalCloudSongListResponse>() {
            @Override
            public void onResponse(retrofit2.Call<CristalCloudSongListResponse> call, retrofit2.Response<CristalCloudSongListResponse> response) {
                if (response.body() != null) {
                    CristalCloudSongListResponse body = response.body();
                    System.out.println("--java-> CristalCloudSongListResponse.GetCristalImg 3333" + body);
                    System.out.println("---> " + body);
                    if (body.getSuccess()) {
                        System.out.println("---> Base64 "+body.getData());
                        for(String songs: body.getData().getSonglist()) {
                            playList.add(songs);
                            System.out.println("---> playlsit "+playList);

                        }
                        showPlaylist();
//                       imageByteArray = body.getData().getImg();

                    } else {
                        System.out.println("---> get CristalCloudSongListResponse failed");
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<CristalCloudSongListResponse> call, Throwable t) {
                System.out.println("---> CristalCloudSongListResponse 444 ERROR" + t);
            }
        });

    }


    private void getcloudplayurls(String coludUrl,String SetGet){

        showCloudUrls = new ArrayList<>();
        playList   = new ArrayList<>();
        String coludSorce = "soundcloud";
        ApiRepository.cloudStream("abcd", AppPreference.preference.getLoginResponse().getUser().getEmail(),coludSorce,coludUrl,SetGet,
                AppPreference.preference.getLoginResponse().getToken(), new retrofit2.Callback<CloudStreamResponse>() {
            @Override
            public void onResponse(retrofit2.Call<CloudStreamResponse> call, retrofit2.Response<CloudStreamResponse> response) {
                if (response.body() != null) {
                    CloudStreamResponse body = response.body();
                    System.out.println("--java-> getcloudplayurls 3333" + body);
                    System.out.println("---> " + body);
                    if (body.getSuccess()) {
//                      playList.add(songs);
                        for(String url : body.getData().getColudUrls()){
                            showCloudUrls.add(url);
                        }
                        if(body.getData().getColudUrls().length >0) {
                            showcloudUrls();
                        }

                        for(String url : body.getData().getSongs()){
                            playList.add(url);
                        }

                        if(body.getData().getSongs().length >0) {
                            showPlaylist();
                        }

                    } else {
                        System.out.println("---> get getcloudplayurls failed");
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<CloudStreamResponse> call, Throwable t) {
                System.out.println("---> getcloudplayurls 444 ERROR" + t);
            }
        });

    }


    private void setnextAudioBook(String audioname) {

        ApiRepository.setnextaudiobookfromapp("abcd", AppPreference.preference.getLoginResponse().getUser().getEmail(),"",0,0,audioname,AppPreference.preference.getLoginResponse().getToken(), new retrofit2.Callback<CristalNextAudioBookFromAppResponce>() {
        @Override
        public void onResponse(retrofit2.Call<CristalNextAudioBookFromAppResponce> call, retrofit2.Response<CristalNextAudioBookFromAppResponce> response) {
            if (response.body() != null) {
                CristalNextAudioBookFromAppResponce body = response.body();
                System.out.println("--java-> CristalCloudSongListResponse.GetCristalImg 3333" + body);
                System.out.println("---> " + body);
                if (body.getSuccess()) {
                    System.out.println("---> Base64 "+body.getData());
                    sendMusicControlCmd(CMD.getCMD_NEXT());
                    mHandler.postDelayed(mUpdateTimeTask, 100);
                    getcurrentaudiobook();

                } else {
                    System.out.println("---> get CristalCloudSongListResponse failed");
                }
            }
        }

        @Override
        public void onFailure(retrofit2.Call<CristalNextAudioBookFromAppResponce> call, Throwable t) {
            System.out.println("---> CristalCloudSongListResponse 444 ERROR" + t);
        }
    });



}
        private void showPlaylist() {

        System.out.println("---> showPlaylist\n");
        getSupportFragmentManager().beginTransaction()
//                .add(R.id.fragment_container, PlaylistFragment.newInstance(playList, imageSource), "playList")
                .add(R.id.fragment_container, PlaylistFragment.newInstance(playList, 0), "playList")

                .addToBackStack("playList")
                .commit();

    }

    private void showcloudUrls() {

        System.out.println("---> showPlaylist\n");
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, PlaylistFragment.newInstance(showCloudUrls, imageSource), "playList")
                .addToBackStack("playList")
                .commit();

    }

    private void showAudioBooks() {

        System.out.println("---> showImageList\n");
//        sendMusicControlCmd(CMD.getSDSONGLIST());
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, ImageItemFragment.newInstance(2), "ImageItemFragment")
                .addToBackStack("ImageItemFragment")
                .commit();

    }
    @SuppressLint("ResourceType")
    private void showMapView() {

        System.out.println("---> showMapView\n");
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container,new MapFragment(), "MapFragment")
                .addToBackStack("MapFragment")
                .commit();

    }

    private long lastClickTime = 0L;


    public void showMenu(View view) {

        if (SystemClock.elapsedRealtime() - lastClickTime < TimeUnit.SECONDS.toMillis(1)) {
            return;
        }
        lastClickTime = SystemClock.elapsedRealtime();

        if (mMenuPopupWindow != null && mMenuPopupWindow.isShowing()){
            mMenuPopupWindow.dismiss();
            return;
        }

        mMenuPopupWindow = new MenuPopupWindow(OperationActivity.this);
        mMenuPopupWindow.showAsDropDown(view);
        mMenuPopupWindow.setMenuListener(new MenuPopupWindow.MenuListener() {
            @Override
            public void onDismiss() {
                lastClickTime = 0;
            }

            @SuppressLint("NonConstantResourceId")
            @Override
            public void onClick(@NotNull View view) {
                switch (view.getId()) {
                    case R.id.menuWifiConfig:
                        Toast.makeText(OperationActivity.this, "menuWifiConfig", Toast.LENGTH_SHORT).show();
                        wifiConfig();
                        break;
                    case R.id.menuSelectSource:
                        Toast.makeText(OperationActivity.this, "please select music source", Toast.LENGTH_SHORT).show();
                        selectMusicSource();
                        break;
                    case R.id.menuAbout:
                        sendCriatalcloudUrl("192.168.29.220:3001");
                        Toast.makeText(OperationActivity.this, "menuAbout", Toast.LENGTH_SHORT).show();
//                        imaget.getCristalCloudImages();
//                        showSongImage();
//                        getcurrentruningsong();

                        break;
                    case R.id.menuLogout:
                        AppPreference.preference.logout();
//                        startActivity(new Intent(OperationActivity.this, MainActivity.class));
                        OperationActivity.this.finish();
                        Toast.makeText(OperationActivity.this, "User logged out", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }

                if (mMenuPopupWindow != null && mMenuPopupWindow.isShowing()) {
                    mMenuPopupWindow.dismiss();
                }
            }
        });

        mMenuPopupWindow.show();
    }

    private void setMusicPlayBotton(Byte acion){
        getCurrentSongName();
        // Changing button image to pause button
        if(acion == CMD.getCMD_PLAY()[1]){
            playpuseflag = true;
            btnPlay.setImageResource(R.drawable.btn_pause);


        }
        else if(acion == CMD.getCMD_POUSE()[1]){
            playpuseflag = false;
            btnPlay.setImageResource(R.drawable.btn_play);

        }
        else{
            System.out.println("playpuseflag 3   ----->"+Integer.toHexString(acion));
        }


    }

    private void wifiConfig() {
        final Dialog dialog = new Dialog(OperationActivity.this);
        dialog.setContentView(R.layout.wifi_config);
        wifi_ssid = (EditText) dialog.findViewById(R.id.wifi_ssid);
        wifi_password = (EditText) dialog.findViewById(R.id.wifi_password);
        dialog.findViewById(R.id.done).setOnClickListener(v -> {

            System.out.println("wifi config\n");
            if(get_wifi_config_to_device())
                dialog.dismiss();
            else{
                Toast.makeText(this, "please enter the ssid and password", Toast.LENGTH_SHORT).show();

            }
        });
        dialog.findViewById(R.id.cancel).setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();

    }

    int imageSource = 0;

    private void selectMusicSource() {
        MusicSourcedialog = new Dialog(OperationActivity.this);
        MusicSourcedialog.setContentView(R.layout.layout_select_source);

        MusicSourcedialog.findViewById(R.id.bt_wifi_radio).setOnClickListener(v -> {
            if(selectmusicsource){
                selectmusicsource = true;
                MusicSourcedialog.dismiss();

            }
            setsongnameflasg =false;
            playList = new ArrayList<>();
            showMapView();
            imageSource = 0;
            MusicSourcedialog.dismiss();
            selectmusicsource = false;

        });

        MusicSourcedialog.findViewById(R.id.bt_sdcard).setOnClickListener(v -> {
            sendMusicControlCmd(CMD.getCMD_SRC_SD());
            setsongnameflasg =false;
            playList = new ArrayList<>();
            imageSource = 1;
            MusicSourcedialog.dismiss();
            selectmusicsource = false;
        });

        MusicSourcedialog.findViewById(R.id.bt_bluetooth).setOnClickListener(v -> {
            setsongnameflasg =false;
            sendMusicControlCmd(CMD.getCMD_SRC_BLUTOOTH_STREAM());
            playList = new ArrayList<>();
            imageSource = 2;
            MusicSourcedialog.dismiss();
            selectmusicsource = false;
        });

        MusicSourcedialog.findViewById(R.id.bt_wifi_cloud).setOnClickListener(v -> {
            playList = new ArrayList<>();
            sendMusicControlCmd(CMD.getCMD_SRC_CRISTAL_CLOUD());
            getCurrentSongName();
//          showCloud();
            imageSource = 3;
            MusicSourcedialog.dismiss();
            selectmusicsource = false;
        });

        MusicSourcedialog.findViewById(R.id.bt_wifi_audion_book).setOnClickListener(v -> {
            setsongnameflasg =false;
            selectmusicsource = false;
            playList = new ArrayList<>();
            showAudioBooks();
            sendMusicControlCmd(CMD.builControllcomand(CMD.getCMD_SRC_CRISTAL_AUDIO_BOOK()));
            imageSource = 4;
            MusicSourcedialog.dismiss();
        });

        MusicSourcedialog.findViewById(R.id.bt_wfi_spotify).setOnClickListener(v -> {
            playList = new ArrayList<>();
//          showCloud();
            sendMusicControlCmd(CMD.builControllcomand(CMD.getCMD_SRC_CRISTAL_SPOTIFY()));
            ShowCloudPlayUrl.setVisibility(View.VISIBLE);

            imageSource = 5;
            MusicSourcedialog.dismiss();
            selectmusicsource = false;
        });

        MusicSourcedialog.show();

    }

    /**
     * Update timer on seekbar
     * */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     * */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            mHandler.postDelayed(this, 100);
        }
    };


    /**
     * send config to the gatts device
     */
    private boolean get_wifi_config_to_device() {

        String ssid =  wifi_ssid.getText().toString();
        String passwd =  wifi_password.getText().toString();
        if(ssid.length()>2 && passwd.length()>2) {
            send_wifi_config_to_device(CMD.getWIFIUSER(), ssid.getBytes());
            send_wifi_config_to_device(CMD.getWIFIPASS(), passwd.getBytes());
            System.out.println("###################################### "+ssid.length()+"->"+ssid+" -- "+passwd.length()+"-->"+ passwd);
            return true;
        }else{
            System.out.println("Please enter the password and  ssid");
            return false;
        }
    }



    byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
    private void getCurrentSongName(){

        if(deviceCurrentSourceBite == CMD.getSRC_NONE()){
            deviceCurrentSource = "ERORR";
        }
        else if(deviceCurrentSourceBite == CMD.getSDCARD()){
            sendMusicControlCmd(CMD.getSDSONGURL());
        }
        else if(deviceCurrentSourceBite == CMD.getBT_STREAM()){
            deviceCurrentSource = "Bluetooth Sream";
        }
        else if(deviceCurrentSourceBite == CMD.getWIFI_RADIO()){
            deviceCurrentSource = "Wifi Radio";
        }
        else if(deviceCurrentSourceBite == CMD.getWIFI_CRISTATL_CLOUD()){
            getcurrentruningsong();

        }
        else if(deviceCurrentSourceBite == CMD.getWIFI_AUDIO_BOOK()){
            deviceCurrentSource = "Wifi Audio Book";
            mHandler.postDelayed(mUpdateTimeTask, 100);
            getcurrentaudiobook();
        }
        else if(deviceCurrentSourceBite == CMD.getWIFI_SPOTIFY()){
            deviceCurrentSource = "Wifi Spotify";
        }
        else if(deviceCurrentSourceBite == CMD.getSRC_END()){
            deviceCurrentSource = "ERORR";
        }

    }

    public void  send_wifi_config_to_device(byte action,byte [] sendMsg){

        final byte[] tag_LEN = new byte[2];
        tag_LEN[0] = action;
        tag_LEN[1] = (byte) sendMsg.length;
        System.out.println("----->sendMsg.length "+sendMsg.length);
        final byte[] sendMsg_L = concatenateByteArrays(tag_LEN,sendMsg);

        BleManager.getInstance().write(
                bleDevice,
                uuid_service,
                uuid_write,
                sendMsg_L,
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                        System.out.println("wifi config sucess");
                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {
                        System.out.println("wifi config failure");

                    }
                }
        );
    }

    private void setdevicesrc(byte src){
        if (!setsongnameflasg){
            setsongnameflasg = true;
            getCurrentSongName();
        }

        deviceCurrentSourceBite = src;


        if(src == CMD.getSRC_NONE()){
            deviceCurrentSource = "please select music source";
//            Toast.makeText(OperationActivity.this, "please select music source", Toast.LENGTH_SHORT).show();
            if(!selectmusicsource){
                selectMusicSource();
                selectmusicsource = true;
            }
        }
        else if(src == CMD.getSDCARD()){
            deviceCurrentSource = "Memory Card";
            if(selectmusicsource){
                MusicSourcedialog.dismiss();

            }
        }
        else if(src == CMD.getBT_STREAM()){
            deviceCurrentSource = "Bluetooth Sream";
        }
        else if(src == CMD.getWIFI_RADIO()){
            deviceCurrentSource = "Wifi Radio";
            if(selectmusicsource){
                MusicSourcedialog.dismiss();

            }
        }
        else if(src == CMD.getWIFI_CRISTATL_CLOUD()){
            deviceCurrentSource = "Cristal Cloud";
            if(selectmusicsource){
                MusicSourcedialog.dismiss();

            }
        }
        else if(src == CMD.getWIFI_AUDIO_BOOK()){

            deviceCurrentSource = "Wifi Audio Book";
            if(selectmusicsource){
                MusicSourcedialog.dismiss();

            }

        }
        else if(src == CMD.getWIFI_SPOTIFY()){
            deviceCurrentSource = "Wifi Spotify";
        }
        else if(src == CMD.getSRC_END()){
            deviceCurrentSource = "ERORR";
        }

        TextView myAwesomeTextView = (TextView)findViewById(R.id.device_source);
        myAwesomeTextView.setText(deviceCurrentSource);
    }

    ArrayList<String> playList = new ArrayList<>();
    ArrayList<String> showCloudUrls = new ArrayList<>();

    private void getNotification(){


        BleManager.getInstance().notify(
                bleDevice,
                //  characteristic.getService().getUuid().toString(),
                //  characteristic.getUuid().toString(),
                uuid_service,
                uuid_notify,
                new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            }
                        });
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            }
                        });
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        String tmp = new String(data);

                        if(data[0] == CMD.getPROGRESSBAR()) {
                            float proces1 = Float.valueOf(tmp.substring(1,6));
                            if(proces1 <1){
                                getCurrentSongName();
                            }
                            songProgressBar.setProgress((int) proces1);
                        }
                        else if(data[0] == CMD.getCONTROLCMDSTATUS()) {

                            System.out.println("------>getCONTROLCMDSTATUS");
                            if (data[1] == CMD.getCMD_NEXT()[1] || data[1] == CMD.getCMD_PREV()[1])
                            {
                                getCurrentSongName();
                            }

                        }
                        else if(data[0] == CMD.getCURRENTSRCSTATUS()) {

                            System.out.println("------>getCURRENTSRCSTATUS: "+data[1]);

                            setdevicesrc(data[1]);


                        }
                        else if (data[0] == CMD.getSDSONGLIST()[1]){
                            System.out.println("getSDSONGLIST    -----> "+Integer.toHexString(data[0]));

                            playList.add(new String(data).substring(1));
                            System.out.println("playList 3  =====> "+ Arrays.deepToString(playList.toArray()));

                            Fragment playList = getSupportFragmentManager().findFragmentByTag("playList");
                            if (playList != null) {
                                ((PlaylistFragment) playList).update(OperationActivity.this.playList);
                            }
                        }
                        else if(data[0] == CMD.getSDSONGURL()[1])
                        {

                            System.out.println("getSDSONGURL    -----> "+tmp.substring(1));
                            CurrentSong.setText(tmp.substring(1));
                            CurrentSong2.setText(tmp.substring(1));
                            setsongnameflasg = true;


                        }
                        else if(data[0] == CMD.getCMD_PLAYBUTTON_CURRENT_STATUS()[1]){
                            setMusicPlayBotton(data[1]);
                        }
                        System.out.println("charDisplayRecvData 1   ----->"+tmp);
                        System.out.println("charDisplayRecvData 2   ----->"+data);

                    }
                }
        );
    }
    public void  sendMusicControlCmd(byte [] sendMsg){

        BleManager.getInstance().write(
                bleDevice,
                uuid_service,
                uuid_write,
                sendMsg,
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                        System.out.println("ble write sucess");
                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {
                        System.out.println("ble write failure");

                    }
                }
        );
    }

    private void initView() {
//        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        toolbar.setTitle(titles[0]);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
//            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (currentPage != SERVICE_LIST_PAGE) {
//                        currentPage--;
//                        changePage(currentPage);
//                    } else {
//                        finish();
//                    }
//                }
//            });
        }

    }

    private void initData() {
        bleDevice = getIntent().getParcelableExtra(KEY_DATA);
        if (bleDevice == null) {
            finish();
        }
    }


    public void changePage(int page) {
        currentPage = page;
        toolbar.setTitle(titles[page]);

        updateFragment(page);
        //arun for with will list all charasric
        switch (currentPage) {
            case CHAR_LIST_PAGE:
                ((CharacteristicListFragment) fragmentList.get(CHAR_LIST_PAGE)).showData();
                break;
            case CHAR_OPERATION_PAGE:
                ((CharacteristicOperationFragment) fragmentList.get(CHAR_OPERATION_PAGE)).showData();
                break;
            default:
                break;
        }
    }

    private void updateFragment(int position) {
        if (position > (fragmentList.size() - 1)) {
            return;
        }

        for (int i = 0; i < fragmentList.size(); i++) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            Fragment fragment = fragmentList.get(i);
            if (i == position) {
                transaction.show(fragment);
            } else {
                transaction.hide(fragment);
            }
            transaction.commit();
        }
    }

    public BleDevice getBleDevice() {
        return bleDevice;
    }

    public BluetoothGattService getBluetoothGattService() {
        return bluetoothGattService;
    }

    public void setBluetoothGattService(BluetoothGattService bluetoothGattService) {
        this.bluetoothGattService = bluetoothGattService;
    }

    public BluetoothGattCharacteristic getBluetoothGattCharacteristic() {
        return bluetoothGattCharacteristic;
    }

    public void setBluetoothGattCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        this.bluetoothGattCharacteristic = bluetoothGattCharacteristic;
    }

    public int getCharaProp() {
        return charaProp;
    }

    public void setCharaProp(int charaProp) {
        this.charaProp = charaProp;
    }


    public void setnextcristalcludsong(String url) {

        System.out.println("-----> setnextcristalcludsong "+url);

        ApiRepository.setnextsngfromapp("abcd",url, AppPreference.preference.getLoginResponse().getUser().getEmail(),AppPreference.preference.getLoginResponse().getToken(), new retrofit2.Callback<CristalSetNextSongfromAppResponce>() {
            @Override
            public void onResponse(retrofit2.Call<CristalSetNextSongfromAppResponce> call, retrofit2.Response<CristalSetNextSongfromAppResponce> response) {
                if (response.body() != null) {
                    CristalSetNextSongfromAppResponce body = response.body();
                    if (body.getSuccess()) {
                        System.out.println("---> Base64 "+body.getData());
                        sendMusicControlCmd(CMD.getCMD_NEXT());

                    } else {
                        System.out.println("---> get CristalCloudSongListResponse failed");
                        Toast.makeText(OperationActivity.this, "Server Error 400", Toast.LENGTH_SHORT).show();

                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<CristalSetNextSongfromAppResponce> call, Throwable t) {
                System.out.println("---> CristalCloudSongListResponse 444 ERROR" + t);
                Toast.makeText(OperationActivity.this, "Server Error 500", Toast.LENGTH_SHORT).show();

            }
        });
    }


    @Override
    public void sendCommand(String url) {

        if(deviceCurrentSourceBite == CMD.getSRC_NONE()){
            deviceCurrentSource = "please Select Sources";
        }
        else if(deviceCurrentSourceBite == CMD.getSDCARD()){

            byte [] cmd = new byte[2];
            cmd[0] = CMD.getSDSONGURL()[1]; //CONTROLCMD enum in firmwire for writing the larg string
//        String url = "Pavitram.mp3";
            System.out.println("--length --"+url.length());
            cmd[1] = (byte) url.length();
            System.out.println("The Value of Byte is: " +cmd[1]);
            sendMusicControlCmd(cmd);
            sendMusicControlCmd(url.getBytes());
        }
//        else if(deviceCurrentSourceBite == CMD.getBT_STREAM()){
//            deviceCurrentSource = "Bluetooth Sream";
//        }
//        else if(deviceCurrentSourceBite == CMD.getWIFI_RADIO()){
//            deviceCurrentSource = "Wifi Radio";
//        }
        else if(deviceCurrentSourceBite == CMD.getWIFI_CRISTATL_CLOUD()){
            setnextcristalcludsong(url);

        }
        else if(deviceCurrentSourceBite == CMD.getWIFI_AUDIO_BOOK()){
            System.out.println("---> next audion "+url);
            setnextAudioBook(url);
        }
        else if(deviceCurrentSourceBite == CMD.getWIFI_SPOTIFY()){

            getcloudplayurls(url,"get");
        }
//        else if(deviceCurrentSourceBite == CMD.getSRC_END()){
//            deviceCurrentSource = "ERORR";
//        }


    }

    private  void sendCriatalcloudUrl(String srcip){
        byte [] cmd = new byte[2];
        cmd[0] = CMD.getCRISTALCLOUDURL(); //CONTROLCMD enum in firmwire for writing the larg string
        String url = new String(CMD.buildCristalCloudUrl(srcip));
        cmd[1] = (byte) url.length();
        System.out.println("The Value of Byte is: " +cmd[1]);

        sendMusicControlCmd(cmd);
        sendMusicControlCmd(url.getBytes());
        System.out.println("fullurl --->"+ url);

    }
    @Override
    public void sendWifiRadioUrlToDevice(@NonNull String url) {
            System.out.println("sendWifiRadioUrlToDevice -> "+url);
        byte [] cmd = new byte[2];
        cmd[0] = CMD.getWIFIFMURL(); //CONTROLCMD enum in firmwire for writing the larg string
//        String url = "Pavitram.mp3";
        System.out.println("--length --"+url.length());
        cmd[1] = (byte) url.length();
        System.out.println("The Value of Byte is: " +cmd[1]);
        sendMusicControlCmd(cmd);
        sendMusicControlCmd(url.getBytes());

    }

    boolean doubleTapToExit = false;
    Handler doubleTapToExitHandler = null;

    @Override
    public void onBackPressed() {
        System.out.println("--> onBackPressed");
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (f instanceof MapFragment) {
            super.onBackPressed();
        } else if (f instanceof PlaylistFragment) {
            super.onBackPressed();
            getCurrentSongName();
        } else if (f instanceof ImageItemFragment) {
            super.onBackPressed();
        } else {

            if (doubleTapToExit) {
                OperationActivity.this.finish();
            } else {
                Toast.makeText(OperationActivity.this, "Press again to exit", Toast.LENGTH_LONG).show();
                doubleTapToExit = true;
                if (doubleTapToExitHandler == null) doubleTapToExitHandler = new Handler(Looper.getMainLooper());
                doubleTapToExitHandler.postDelayed(() -> {
                    doubleTapToExit = false;
                }, 2000);
            }
        }
    }

    @Override
    public void setdaudiobook(@NotNull String data, @NotNull Integer[] listofaudios) {
        System.out.println("---> setdaudiobook: "+data+" -- "+listofaudios);
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (f instanceof ImageItemFragment) {
            super.onBackPressed();

        }
        Toast.makeText(OperationActivity.this, "Operation activity: audio book: " + data, Toast.LENGTH_LONG).show();
        mHandler.postDelayed(mUpdateTimeTask, 100);
        sendMusicControlCmd(CMD.getCMD_NEXT());
        mHandler.postDelayed(mUpdateTimeTask, 300);
        getcurrentaudiobook();

    }
}