package com.cristal.ble.ui.player;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.cristal.ble.AppPreference;
import com.cristal.ble.MainActivity;
import com.cristal.ble.R;
import com.cristal.ble.api.LoginResponse;
import com.cristal.ble.comm.Observer;
import com.cristal.ble.comm.ObserverManager;
import com.cristal.ble.operation.CharacteristicListFragment;
import com.cristal.ble.operation.CharacteristicOperationFragment;
import com.cristal.ble.operation.ServiceListFragment;
import com.cristal.ble.MapFragment;
import com.cristal.ble.ui.LoginFragment;
import com.cristal.ble.ui.PlaylistFragment;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.google.gson.Gson;


public class OperationActivity extends AppCompatActivity implements Observer,
        PlaylistFragment.FragmentInteractionListener,MapFragment.FragmentInteractionListener{

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

    private Button wificonfigdone;                // send wifi config to the device
    private Button wificonfigCancel;                // close wifi condif
    private EditText wifi_ssid;           // store ssid
    private EditText wifi_password;       // store password


    private ImageView btnPlay;
    //    private ImageButton btnForward;
//    private ImageButton btnBackward;
    private ImageView btnNext;
    private ImageView btnPrevious;
    private ImageButton btnPlaylist;
    private ImageButton btnRepeat;
    private ImageButton btnShuffle;
    private SeekBar songProgressBar;


    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();;

    private TextView songTitleLabel;
    private TextView songCurrentDurationLabel;
    private TextView songTotalDurationLabel;
    private TextView CurrentSong;
    private TextView CurrentSong2;

    private boolean playpuseflag = true;

    private androidx.appcompat.widget.Toolbar toolbar;
    private List<Fragment> fragmentList = new ArrayList<>();
    private int currentPage = SERVICE_LIST_PAGE;
    private final String[] titles = {"Service List","Char List","Char Operation"};
    private AlertDialog src_alertDialog;

    private MenuPopupWindow mMenuPopupWindow;
    private CommandEnum CMD = new CommandEnum();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //  setContentView(R.layout.activity_operation);
        setContentView(R.layout.fragment_player);
        initplayViw();
        initData();
        initView();

        //  initPage();
        getNotification();
        ObserverManager.getInstance().addObserver(this);
        //  getSupportFragmentManager().beginTransaction().add(R.id.fragment_con, PlayerUi.newInstance(),"music player").commit();


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


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (currentPage != SERVICE_LIST_PAGE) {
                currentPage--;
                changePage(currentPage);
                return true;
            } else {
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

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
            case R.id.action_chat_show_log_item:
                Toast.makeText(this, "show log", Toast.LENGTH_SHORT).show();
                break;

            case R.id.action_wifi_config:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                LayoutInflater inflater = getLayoutInflater();

                View view = inflater.inflate(R.layout.dialog_wifi_signin, null);
                builder.setView(view);
                final AlertDialog alertDialog = builder.create();
                alertDialog.show();

                wificonfigdone = (Button) view.findViewById(R.id.done);
                wificonfigCancel = (Button) view.findViewById(R.id.cancel);
                wifi_ssid = (EditText) view.findViewById(R.id.wifi_ssid);
                wifi_password = (EditText) view.findViewById(R.id.wifi_password);
                Toast.makeText(this, "wifi config pressed", Toast.LENGTH_SHORT).show();
                wificonfigdone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        send_wifi_config_to_device();
                        alertDialog.cancel();
                    }
                });
                wificonfigCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.cancel();
                    }
                });

                break;
            case R.id.action_source_selection:
                System.out.println( "from source selector");
                AlertDialog.Builder src_builder = new AlertDialog.Builder(this);

                LayoutInflater src_inflater = getLayoutInflater();
                View src_view = src_inflater.inflate(R.layout.sourse_selector, null);
                src_builder.setView(src_view);
//                final AlertDialog src_alertDialog = src_builder.create();
                src_alertDialog = src_builder.create();

                src_alertDialog.show();

                break;
            default:
                break;
        }
        return true;
    }

    public void onRadioButtonClicked(int id) {

        // Check which radio button was clicked
        switch(id) {
            case R.id.bt_wifi:

                sendMusicControlCmd(CMD.getCMD_SRC_WIFI());

                // wifi are the best
                System.out.println("########### radio_wifi");
                break;
            case R.id.bt_sdcard:

                // sd card rule
                System.out.println("########### radio_sdcard");
                sendMusicControlCmd(CMD.getCMD_SRC_SD());

                break;
            case R.id.bt_bluetooth:
                // bluetooth rule
                System.out.println("########### radio_blutooth");
                sendMusicControlCmd(CMD.getCMD_SRC_BLUTOOTH_STREAM());
                break;

            case R.id.bt_wifi_cloud:
                // sd card rule
                System.out.println("########### radio_sdcard");
//                    sendMusicControlCmd(CMD.getCMD_SRC_WIFI_FM());
//                sendMusicControlCmd(CMD.getSDSONGURL());
                showMapView();



                break;
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private void  initplayViw(){

        // All player buttons



        btnPlay = (ImageView) findViewById(R.id.btnPlay);
//      btnForward = (ImageButton) findViewById(R.id.btnForward);
//      btnBackward = (ImageButton) findViewById(R.id.btnBackward);
        btnNext = (ImageView) findViewById(R.id.btnNext);
        btnPrevious = (ImageView) findViewById(R.id.btnPrevious);
        btnPlaylist = (ImageButton) findViewById(R.id.btnPlaylist);
        btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
        btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
        songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
//      songTitleLabel = (TextView) findViewById(R.id.songTitle);
        songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
        songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);
        CurrentSong = (TextView) findViewById(R.id.tv_playlist);
        CurrentSong2 = (TextView) findViewById(R.id.tv_song);
        findViewById(R.id.btn_menu).setOnClickListener((view) -> {
            showMenu(view);
        });

        findViewById(R.id.tvPlaylist).setOnClickListener((view) -> {
            showPlaylist(view);
        });

        /**
         * Play button click event
         * plays a song and changes button to pause image
         * pauses a song and changes button to play image
         * */

//        songTitleLabel.setText("amma");
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

//                System.out.println(btnPlay.);

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

        /**
         * Forward button click event
         * Forwards song specified seconds
         * */

//        btnForward.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getActionMasked() == MotionEvent.ACTION_DOWN){
//                    System.out.println( "you just touch the screen down :-)"+ Toast.LENGTH_SHORT);
//                    System.out.println(Toast.LENGTH_SHORT);
//
//
//                }
//                if (event.getActionMasked() == MotionEvent.ACTION_UP){
//                    System.out.println( "you just touch the screen up :-)"+ Toast.LENGTH_SHORT);
//                    System.out.println(Toast.LENGTH_SHORT);
//
//                }
//                System.out.println("btnForward  --------->");
//
//                return false;
//            }
//
//
//        });


        /**
         * Backward button click event
         * Backward song to specified seconds
         * */
//        btnBackward.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getActionMasked() == MotionEvent.ACTION_DOWN){
//                    System.out.println( "you just touch the screen down :-)"+ Toast.LENGTH_SHORT);
//                    System.out.println(Toast.LENGTH_SHORT);
//
//
//                }
//                if (event.getActionMasked() == MotionEvent.ACTION_UP){
//                    System.out.println( "you just touch the screen up :-)"+ Toast.LENGTH_SHORT);
//                    System.out.println(Toast.LENGTH_SHORT);
//
//                }
//                System.out.println("btnForward  --------->");
//
//                return false;
//            }
//
//
//        });

    }

    private void showPlaylist(View view) {

        System.out.println("---> showPlaylist\n");
        sendMusicControlCmd(CMD.getSDSONGLIST());
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, PlaylistFragment.newInstance(playList), "playList")
                .commit();

    }
    @SuppressLint("ResourceType")
    private void showMapView() {

        System.out.println("---> showMapView\n");
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container,new MapFragment())
                .commit();
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.fragment_container, new LoginFragment(), "LoginFragment")
//                .commit();

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

            @Override
            public void onClick(@NotNull View view) {
                switch (view.getId()) {
                    case R.id.menuWifiConfig:
                        Toast.makeText(OperationActivity.this, "menuWifiConfig", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.menuSelectSource:
                        Toast.makeText(OperationActivity.this, "please select music source", Toast.LENGTH_SHORT).show();
                        selectMusicSource();
                        break;
                    case R.id.menuAbout:

                        Toast.makeText(OperationActivity.this, "menuAbout", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.menuLogout:
                        AppPreference.preference.logout();
                        startActivity(new Intent(OperationActivity.this, MainActivity.class));
                        OperationActivity.this.finish();
                        Toast.makeText(OperationActivity.this, "User logged out", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }

                
                System.out.println("->>josn"+AppPreference.preference.getLoginResponse().getToken());

                if (mMenuPopupWindow != null && mMenuPopupWindow.isShowing()) {
                    mMenuPopupWindow.dismiss();
                }
            }
        });

        mMenuPopupWindow.show();
    }

    private void setMusicPlayBotton(Byte acion){

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
    private void selectMusicSource() {
        final Dialog dialog = new Dialog(OperationActivity.this);
        dialog.setContentView(R.layout.layout_select_source);

        dialog.findViewById(R.id.bt_wifi).setOnClickListener(v -> {
            onRadioButtonClicked(R.id.bt_wifi);
            playList = new ArrayList<>();
            dialog.dismiss();
        });

        dialog.findViewById(R.id.bt_sdcard).setOnClickListener(v -> {
            onRadioButtonClicked(R.id.bt_sdcard);
            playList = new ArrayList<>();
            dialog.dismiss();
        });

        dialog.findViewById(R.id.bt_bluetooth).setOnClickListener(v -> {
            onRadioButtonClicked(R.id.bt_bluetooth);
            playList = new ArrayList<>();
            dialog.dismiss();
        });

        dialog.findViewById(R.id.bt_wifi_cloud).setOnClickListener(v -> {
            onRadioButtonClicked(R.id.bt_wifi_cloud);
            playList = new ArrayList<>();
            dialog.dismiss();
        });

        dialog.show();
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
//            long totalDuration = mp.getDuration();
//            long currentDuration = mp.getCurrentPosition();
//
//            // Displaying Total Duration time
//            songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
//            // Displaying time completed playing
//            songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = 2;
            //Log.d("Progress", ""+progress);
            songProgressBar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    /**
     * send config to the gatts device
     */
    private void send_wifi_config_to_device() {

        String ssid =  wifi_ssid.getText().toString();
        String passwd =  wifi_password.getText().toString();
        send_wifi_config_to_device(CMD.getWIFIUSER(),ssid.getBytes());
        send_wifi_config_to_device(CMD.getWIFIPASS(),passwd.getBytes());

        System.out.println("###################################### "+ssid+" -- "+ passwd);
    }



    byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
    public void  send_wifi_config_to_device(byte action,byte [] sendMsg){

        final byte[] tag_LEN = new byte[2];
        tag_LEN[0] = action;
        tag_LEN[1] = (byte) sendMsg.length;
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

    ArrayList<String> playList = new ArrayList<>();
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
                            songProgressBar.setProgress((int) proces1);
                        }
                        else if(data[0] == CMD.getCONTROLCMDSTATUS()) {

                            System.out.println("------>getCONTROLCMDSTATUS");

                        }
//                        else if(data[0] == CMD.getCMDINITSTATE()) {
////                            sendMusicControlCmd(CMD.getSDSONGURL());
//
//                        }
                        else if (data[0] == CMD.getSDSONGLIST()[1]){
                            System.out.println("getSDSONGLIST    -----> "+Integer.toHexString(data[0]));

                            playList.add(new String(data).substring(1));
                            System.out.println("playList 3  =====> "+ Arrays.deepToString(playList.toArray()));

                        }
                        else if(data[0] == CMD.getSDSONGURL()[1])
                        {

                            System.out.println("getSDSONGURL    -----> "+data.toString());
                            CurrentSong.setText(tmp.substring(1));
                            CurrentSong2.setText(tmp.substring(1));


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

    private void initPage() {
        prepareFragment();
        changePage(SERVICE_LIST_PAGE);
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

    private void prepareFragment() {
        fragmentList.add(new ServiceListFragment());
        fragmentList.add(new CharacteristicListFragment());
        fragmentList.add(new CharacteristicOperationFragment());

        for (Fragment fragment : fragmentList) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment, fragment)
                    .hide(fragment)
                    .commit();
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

    @Override
    public void sendCommand(String url) {

        byte [] cmd = new byte[2];
        cmd[0] = CMD.getSDSONGURL()[1]; //CONTROLCMD enum in firmwire for writing the larg string
//        String url = "Pavitram.mp3";
        System.out.println("--length --"+url.length());
        cmd[1] = (byte) url.length();
        System.out.println("The Value of Byte is: " +cmd[1]);
        sendMusicControlCmd(cmd);
        sendMusicControlCmd(url.getBytes());


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


}