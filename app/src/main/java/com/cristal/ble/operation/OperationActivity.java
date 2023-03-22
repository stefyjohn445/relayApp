package com.cristal.ble.operation;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.cristal.ble.PlayerUi;
import com.cristal.ble.R;
import com.cristal.ble.comm.Observer;
import com.cristal.ble.comm.ObserverManager;

import java.util.ArrayList;
import java.util.List;

import static com.cristal.ble.operation.CharacteristicOperationFragment.*;

public class OperationActivity extends AppCompatActivity implements Observer {

    private static final String TAG = "OperationActivity";

    public static final int SERVICE_LIST_PAGE = 0;
    public static final int CHAR_LIST_PAGE = 1;
    public static final int CHAR_OPERATION_PAGE = 2;

    public static final String KEY_DATA = "key_data";

    private BleDevice bleDevice;
    private String uuid_service = "0000abf0-0000-1000-8000-00805f9b34fb";
    private String uuid_write = "0000abf3-0000-1000-8000-00805f9b34fb";
    private String uuid_notify = "0000abf2-0000-1000-8000-00805f9b34fb";  //for receiving the data from device

    private BluetoothGattService bluetoothGattService;
    private BluetoothGattCharacteristic bluetoothGattCharacteristic;
    private int charaProp;

    private MenuItem connectMenuItem;       // Menu in the toolbar

    private Button wificonfigdone;                // send wifi config to the device
    private Button wificonfigCancel;                // close wifi condif
    private EditText wifi_ssid;           // store ssid
    private EditText wifi_password;       // store password


    private ImageButton btnPlay;
    private ImageButton btnForward;
    private ImageButton btnBackward;
    private ImageButton btnNext;
    private ImageButton btnPrevious;
    private ImageButton btnPlaylist;
    private ImageButton btnRepeat;
    private ImageButton btnShuffle;
    private SeekBar songProgressBar;

    private  byte PROGRESSBAR = 0x5;
    private  byte CMDINITSTATE = 0x7;
    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();;

    private TextView songTitleLabel;
    private TextView songCurrentDurationLabel;
    private TextView songTotalDurationLabel;

    private boolean playpuseflag = true;

    private android.support.v7.widget.Toolbar toolbar;
    private List<Fragment> fragmentList = new ArrayList<>();
    private int currentPage = SERVICE_LIST_PAGE;
    private final String[] titles = {"Service List","Char List","Char Operation"};
    private AlertDialog src_alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_operation);
      setContentView(R.layout.player);
      initplayViw();
        initData();
        initView();

//        initPage();
        getNotification();
        ObserverManager.getInstance().addObserver(this);
//      getSupportFragmentManager().beginTransaction().add(R.id.fragment_con, PlayerUi.newInstance(),"music player").commit();

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

        public void onRadioButtonClicked(View view) {
            // Is the button now checked?
            byte [] cmd = new byte[2];
            cmd[0] = 0x1; //CONTROLCMD enum in firmwire


            boolean checked = ((RadioButton) view).isChecked();

            // Check which radio button was clicked
            switch(view.getId()) {
                case R.id.radio_wifi:
                    if (checked)
                        {
                            cmd[1] = 0x0A;
                            // wifi are the best
                            System.out.println("########### radio_wifi");
                        }
                    break;
                case R.id.radio_sdcard:
                    if (checked)
                    {// sd card rule
                        cmd[1] = 0x0B;
                        System.out.println("########### radio_sdcard");
                    }
                    break;


                case R.id.radio_blutooth:
                    if (checked)
                        {
                            cmd[1] = 0x0C;
                            // bluetooth rule
                            System.out.println("########### radio_blutooth");
                        }
                    break;

                case R.id.radio_wifiradio:
                    if (checked)
                    {// sd card rule
                        cmd[1] = 0x0D;
                        System.out.println("########### radio_sdcard");
                    }

                    break;


            }
            sendMusicControlCmd(cmd);
            src_alertDialog.cancel();

        }


    @SuppressLint("ClickableViewAccessibility")
    private void  initplayViw(){

        // All player buttons
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        btnForward = (ImageButton) findViewById(R.id.btnForward);
        btnBackward = (ImageButton) findViewById(R.id.btnBackward);
        btnNext = (ImageButton) findViewById(R.id.btnNext);
        btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
        btnPlaylist = (ImageButton) findViewById(R.id.btnPlaylist);
        btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
        btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
        songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
        songTitleLabel = (TextView) findViewById(R.id.songTitle);
        songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
        songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);

        /**
         * Play button click event
         * plays a song and changes button to pause image
         * pauses a song and changes button to play image
         * */

//        songTitleLabel.setText("amma");

        // Listeners
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

//        playSong(0);

        // set Progress bar values
        songProgressBar.setProgress(0);
        songProgressBar.setMax(100);


        btnPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                byte [] cmd = new byte[2];
                cmd[0] = 0x1; //CONTROLCMD enum in firmwire
                // check for already playing
                if(!playpuseflag){
                        playpuseflag = true;

                        // Changing button image to play button
                        cmd[1] = 0x3;

                        btnPlay.setImageResource(R.drawable.btn_play);

                }else{
                    // Resume song
                        playpuseflag = false;
                        cmd[1] = 0x4;

                        // Changing button image to pause button
                        btnPlay.setImageResource(R.drawable.btn_pause);

                }
                sendMusicControlCmd(cmd);


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
                byte [] cmd = new byte[2];
                cmd[0] = 0x1; //CONTROLCMD enum in firmwire
                cmd[1] = 0x5;
                sendMusicControlCmd(cmd);


            }
        });

        /**
         * Back button click event
         * Plays previous song by currentSongIndex - 1
         * */
        btnPrevious.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                byte [] cmd = new byte[2];
                cmd[0] = 0x1; //CONTROLCMD enum in firmwire
                cmd[1] = 0x6;
                sendMusicControlCmd(cmd);

            }
        });

        /**
         * Forward button click event
         * Forwards song specified seconds
         * */

        btnForward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN){
                    System.out.println( "you just touch the screen down :-)"+ Toast.LENGTH_SHORT);
                    System.out.println(Toast.LENGTH_SHORT);


                }
                if (event.getActionMasked() == MotionEvent.ACTION_UP){
                    System.out.println( "you just touch the screen up :-)"+ Toast.LENGTH_SHORT);
                    System.out.println(Toast.LENGTH_SHORT);

                }
                System.out.println("btnForward  --------->");

                return false;
            }


        });


        /**
         * Backward button click event
         * Backward song to specified seconds
         * */
        btnBackward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN){
                    System.out.println( "you just touch the screen down :-)"+ Toast.LENGTH_SHORT);
                    System.out.println(Toast.LENGTH_SHORT);


                }
                if (event.getActionMasked() == MotionEvent.ACTION_UP){
                    System.out.println( "you just touch the screen up :-)"+ Toast.LENGTH_SHORT);
                    System.out.println(Toast.LENGTH_SHORT);

                }
                System.out.println("btnForward  --------->");

                return false;
            }


        });

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
     * Function to play a song
     * @param songIndex - index of song
     * */
    public void  playSong(int songIndex){
        // Play song
        try {

            System.out.println("Displaying Song title");
            // Displaying Song title
            String songTitle = "arunjith is good boy";
            songTitleLabel.setText(songTitle);

            // Changing Button Image to pause image
//            btnPlay.setImageResource(R.drawable.btn_pause);

            // set Progress bar values
//            songProgressBar.setProgress(0);
//            songProgressBar.setMax(100);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }


    /**
     * send config to the gatts device
     */
    private void send_wifi_config_to_device() {

                String ssid =  wifi_ssid.getText().toString();
                String passwd =  wifi_password.getText().toString();
                send_wifi_config_to_device((byte)0x2,ssid.getBytes());
                send_wifi_config_to_device((byte)0x3,passwd.getBytes());

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
    private void getNotification(){

        System.out.println("Notify 11---->: ");
        System.out.println("Notify 22---->: ");

        BleManager.getInstance().notify(
                bleDevice,
//                                characteristic.getService().getUuid().toString(),
//                                characteristic.getUuid().toString(),
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
                        if(data[0] == PROGRESSBAR) {
                            float proces1 = Float.valueOf(tmp.substring(1,6));
                            songProgressBar.setProgress((int) proces1);
                        }
                        else if(data[0] == CMDINITSTATE) {

                            if(data[1] == 0 | data[1] == 0x3 ){
                                playpuseflag = true;
                                btnPlay.setImageResource(R.drawable.btn_play);
                                System.out.println("btnPlay.setImageResource(R.drawable.btn_play)");

                            }else if (data[1] == 0x4){
                                playpuseflag = false;
                                btnPlay.setImageResource(R.drawable.btn_pause);
                                System.out.println("btnPlay.setImageResource(R.drawable.btn_pause)");


                            }


                        }

                        System.out.println("charDisplayRecvData    -----> "+tmp);
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
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(titles[0]);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (currentPage != SERVICE_LIST_PAGE) {
                        currentPage--;
                        changePage(currentPage);
                    } else {
                        finish();
                    }
                }
            });
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

}
