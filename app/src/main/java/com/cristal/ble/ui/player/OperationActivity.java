package com.cristal.ble.ui.player;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
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
import com.cristal.ble.MainActivity;
import com.cristal.ble.MapFragment;
import com.cristal.ble.R;
import com.cristal.ble.comm.Observer;
import com.cristal.ble.comm.ObserverManager;
import com.cristal.ble.operation.CharacteristicListFragment;
import com.cristal.ble.operation.CharacteristicOperationFragment;
import com.cristal.ble.ui.CloudPlaylistFragment;
import com.cristal.ble.ui.ColudFragment;
import com.cristal.ble.ui.PlaylistFragment;
import com.cristal.ble.ui.imageList.ImageItemFragment;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


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

    private EditText wifi_ssid;           // store ssid
    private EditText wifi_password;       // store password


    private ImageView btnPlay;
    private ImageView btnNext;
    private ImageView btnPrevious;
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

        Glide.with(OperationActivity.this)
                .load("data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAoHCBIRFRgSEhIZGRgZGRgYGBoYGhgcGBgYGBkZHBgYGRocIS4lHB4rIRgZJjgmKy8xNTU1GiQ7QDs0Py40NTEBDAwMEA8QHxISHjErISs0NjQ0NDQ0NDQ0MTE3NDE0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ0NDQ2PzQ0NP/AABEIAJ8BPgMBIgACEQEDEQH/xAAcAAABBQEBAQAAAAAAAAAAAAABAAIDBAYFBwj/xABEEAACAQIEAwUFBgMFBgcAAAABAgADEQQSITEFQVEGImFxgRMyQpGhB1JiscHRI3LwFIKi4fEWQ5KywtIkMzRTY3PD/8QAGQEAAwEBAQAAAAAAAAAAAAAAAAECAwQF/8QAJREAAgICAgICAgMBAAAAAAAAAAECEQMhEjFBUQRhQqETIpEy/9oADAMBAAIRAxEAPwDx47RklAjCJQgCPQRsesYBitDBGSK0FoYrQAFobRAR1oUA20cBCBCBALBaK0dFaIdjYoYoBYLQWjoohjbQR06lDBotPO5s7WK392mh/wB4w+Jm2VfXpBRsTdHKRGY5VBJPISwuGRTZ2zN9xLG38z7D0vJkIYFKfcp27zHVmt94jc/hGmsr1GUDKgPiTvYbjpvHWrC90iRcUE9wIh5WGZvVmv8ALSQ18QW1ZnPS5J+hMalE6evyjWpkkm2n7chJ5FcRorWOg/167R6FCNWsfEG3zF5CyxpEVhRcyOASrHLpcqe7rqM1tvIwnEs2jjP4t748n39DceErUajIbqxB6jp0PUeEl9rn6Bv8Lf8Aafp5R2FEtPDh75GF/utZT5X2P9baCQOhUkEEEbgixHmDJAvhaSirnsj8tFPMdAOo/D8rHcEVIpI6FTY/5EdRGEQGCKG0FoACAiOtFaADLRpkkaRABkUMEAJA0YTAYoxDhHiMWOMYDgYoAIhGSGGCKADhDGiEQAcI4GMhgxDoLwXgiGG8UEUCgxQCEC8ALnD6Sa1KmqJ8O2d/hXwHMnoPGPw61MZUSnfckmwsFG7MfJR8gAJAqvUK0kBY3soHMnUn9bnYATcdmeBKtPOpuzXUvrYgHUJ1W/PnYcrS9cbfS2/sz23Xl6RnsfhmqZKVFCFUa76k7eWgF/5rcp0eCdly7fxAbAE7c+XrztPQcFwmmtjlF5fTCgbCcGTO5Oz0cXxVFUecYzgGW4VLXNhbmenlbcznYngzIp5AbnmT0HTy/wBJ6m+CubgevTylarwkWva5G1+R6yFkKlhPGsbgfZjbvdBso5Xvsdzb520vzmpkT0nifZ43Jtrvpy6sSdz0/wAhbP4nhIp307w5nXLv7qj3j/qdLX3jkRhLGzKMv0jbTqVsE2pIsPG1ztqZSelrb+jLUkzNxaH0HBABOutvEC1h5728rdIqqjcbGVSNZbVrqSfDN4E7N6/n5iUiGPp/xFKk94XIvzAGp9La+Gvw61zCjspDKbEG4PQjaWcfTBy1EFlcXt91hoyenLwsecYipeC8VorQGKKK0VoACAwwWgA0wQmKAgkRsLGASgHrHARqxwgSOtABDAIwFFDGwAcI4RgjxAAxRRQAUFoYogFaCGKBQpePDKq0lrhCyGwJBHdLGwBG4JlJVJ0AjaruoKhjY2zanLe23Tn9YAW6ZAb2dNszNZSRorFrXW/3Qdzzt0nsWAw2RUpi1kVUFgB7oAJsPG88i7L4bPiKX/2Jp4A3P0BntOGTQTDPNqPH2dHxsacuXo6eHpi0nRBIqW0sINJxncNZIwpJrxpIiaGmc/E4UONRM5j+HKNkv4X09RfXXrNm1rTjcQW9/wCrx9EumYDiHCi176cyRYgdFUcz9JkuJEKStNbab7m3PyGu/O/jPQONmwI1y9AL/T95heJnMCqoUW97E5mffvO3ToNhrvN8bObLGjhsLaXljhhHtArahwVb1/zAkLpaCibMpH3hOjwc3ktYqgablDy2PUcj/XQ9JJh+9Ten921VfDKQrj1Vgf7gljiDZ2KnfKHTzyjMvra48RbnIeE6VlU7OTTPT+ICl/IFgfSNO1ZL0UooXWxI6Ej5QRgCAwwGACiiigAwxQmCAByxoEcWjQZQh4hEAhEZI6IRXgEYBMbHGNiAIkgkYjxABxnQwfA8VWX2lOg5T77WSnpvZ3IU/Oc8VLbb/X06RVKzvbMS1hYZiWsBsBe9h4R0J2TVsI6Gxyk/hdH+qsRISp6QAN92SJVZeRHl+0GvQJ+yMxS+uJSoMrjXkwGvqP2+RlStSKHqORGx8jJ35LDhXs4ubC9mPMKdGtfS9ry3xniRxLKtNFp0lASlTUaBVvYsd2YkkljqST0EoV6RVVYle8GNgbsADYZhyudvKdfheBD4lKZ2RVzW/CoZv8RMdtKiaTdnS7N4b2dcP7N39iuZlQDNncFQbEjQDN8p6PgeI0ay3pvc81OjL4ETE9n6LqXxA+OqVA6ql9fmSPSdHjPAqjWxFBsj7nkD5HcHynJkSlI7cTcY6N9hnBA+UtqbzIdluKM4SnVBDp7xPxcrgjQzt4viKUAzs9lHePkT+852qdHUnas6xjCoM5uF4rRxADpWUjwIuPMS6jqfdYHyIiAVU2nIx1TS06WOYgbTgYkkxgcTHlTdWU+gLfSZTiaakkW8ALnzZtr+pPlN22HJFjr5zjcUphdCmnUWOv6SoOmRONo88rjfu2/P5fCPDcyHCJmdQdr3+Wv6Tp4srWchBZeRUX87LcE+J152EgxGGSmoYsA3LKbk+I6etuY5TrSbRwtpMdxO4ZWGhtceBBuJA5AYMuh99dL2K6gadCCv92RVMWXyhtxcX6+cm/tNQKHViGT+GCLXyOrd3x2b5yoqlTJbt2ScWsaruoIV2Z0uCDkLEAkHb3TKUv8AFS4ZUqWLqoBIvqDdlsSNrNbzWUIwdeARGKKAgRRRQAaYojFAAEQAR5MjvKESCGBTDAkIiEEIjARghMEAHCJjyG8KkDUyXCU8xzGNKwbodRw/MywEAkpjbzQz7GgRMgjo68AKr0o1WPumWGldxJZSIHW30/O87vAMRfEu3Ns+X1Jt+k4176GdDs1S/wDF0h1a/mApb/pmUnSs0iraRusfwCqaaLRqsMmXKuwuBZjmFjrcn1PWR0aHEsuR6r8+SH67n5TZYNgBI8VWGyjUzhU35PS/ij2ivwThxBXMbkbnS587aR3HuCLX+Ig25G1/OdXBgKuv+sdfvA8pF27NeNKjzP8A2VIcqlYA7kaEjobAzQYHg1OmLVAHPU3Euce4A7sKlJirr7jAkEDpf9Jm8RxHiSHJUoh7fFkN9+ZQ29bTZO12c8lT6NUzWFkquOgLZ18u9f8AORrdtJU4Jhq1Vc9RQnhclvmQJ1XohTpM5dmkdoh2EwfaWt/aKnsUY2B7+pA/CD6an06za8VxS0ab1G2UXt1OyjzJIHrMTRwtkzu5V6jFyV3ym5Y2OhBJ0U+E0wxt2Y/InxVHMrqKCCpa9zlXYgkciNwdyCOnLecb2FSvUA3ZidzYAbkk8gACSfAzTYHANi6gdyMtjlIXL/CQ5c9uRYkBQebX20kuM4eqJUdBZfdY/hHwL4GwJ6gKORv1M5Er2+jJ4moid2lqNi5Gr9bA+6vhv1io1ENMhiwbOt7WtlAa1uea5P0lau9zpJuHYb2lRKbbMwBPRb94+guYkqBtsscRZSyFSzA01N2N2JJbfy29BKkn4hb2rgbB2HLTvG4FuV728JXMaBuxRRQQECIxRQAEUUUAGGNElMjjESCIxKYjGAhCI0RwgATBCYIEifkJ0qa5QBOcurL5idEmXEmRYw6K7BWbKOtr/IdZ38L2fo1nSnTrPmfQDIpNzsNwPrOFgVvmNvhI+A7g8m38hNP2SwneLFTf2lBRanUB1ck6oS493dfWTKTvRUYo6PGfs4OHXMtaowBCk+zWwJAJ1D7eNph8fhTRcoST0JFiR1tc2nuVbUOozd5qpN3xTbFR7tUZR5j03M8f7b0vZ1x4qOTch1Oh9JMZNvY3FUcS8Y4jFqSQG80IKzLJsLWZCHQ2dDnXxt7w8ufzicSJXykH+vL85MkXF0esYbjKvSWop0ZQ3l1HmDp6R/CuIo1TLUNiRmF+YvbSYPs5iu61InRGuv8AK3+Y+s1AwhxACjS2oI0ynrOCUeLaPShPlFM2VWvTscri/TnDhq9rZpiW4Vi/aArVysALG2ZGH4lO3pNHRpVyo9oVvzy3/WRxNuV9mpVgYHpKdwJy8Bijax3Gnylx8TEHEhxLgaKLTn1Xk9aqJyq+KGfJfW14ilGjhdq8SGanRvpc1H/lXQA+ZJ9VEzWMx5qHJewqXVsw1SkupKm1iMoPQ3A3vNZ2m7MMcOcdTLM5AD02IAFIZj3MozA/FYk3DEW2E80xtfKzBcw0C2bcDcrsOduQ2nbipR0ebntzbZ6B2R4bWx7MUX2dD3c9gR3FIp01W4LBQdehdteU5XbLHCmqYGmblC3tG6tc6/rN19mxZOHUSN71D6NUb9hPK+1gZsdWW3ed1H/Eq/vFGdyaKlj4wX2bHsj9nlGrR/tOLYstSlmRFJXIGIyOWB1NgdLW73OYLgtVKbtUcjuoxUH4ja1h4lS3qRPc+MVDRwoVCBamFt4Kulp4WOGs2RUuzsoOUb3Y2VfMgFvIrFjndthlxpUkimSTqTc8z1PMxTu8b7MVcHRpVqrC7sylRrlK6qc3O4B8tN5wjNU01aMHFxdMEUUEYgRRRGAAiiigAxjAI8iNEYhwEJiERgA0RwjBJFjAUUJgvAAIe8DL4ac88j0l9RKiRI63CqRZHst7gD3Ub4hyJudtvWbLsthcuTuEXrg92lXU9xBr/BYsPeOu3ymR4el6RGXMSy6ZUfbN8JIPP0v4zadmaWU4dcpGtR+7SxAOrAf7hiVPd577zOT2XHo2C1Luo7+oq+8cZb3h8Nbuj8+k8v8AtIpd9XtyUXyPzB+M9w+Q16z0Sk59og7/ALr6MMcBqb7V+4JhftEoMwL5T3QuoRzbVhq+bKPK15Ef+in0eehpYovKslotNjMsuJXaWWldxvGwLHC2bO2Q6lGt4lRcflOhwDEYhnK0mOcXOW5BIG+ux8vGUOCVAlZGOwbXyII/Wah+ygqP7Wm5F3JIHwqVBDKRrvec82r2duGMuNx9naodoaqWNekVHMlbc7e8NBNNheJ06qBkYEfUec8+xfD+If8AlJiWNNbHvam/S5FyNdibfKUuzOFxvthluqLcOxFlIuTp1PiJjKCq0zfnK6kv8PSs9nJGxj3ryBBzhtMDaIHqHUzI8OxhqYis5PdW4+V/2mg4viPZ0mPgZiMJX9jhatQ+8+YL1u+g/eVFWKcqR6xgqv8AaMLTYkWemjEdSUH6GfP+PwxpValJt0dlPjZiL/rPbuwFVauBok62XLr+FmX/AKZ5v9ovDTSxrOB3aqhx0uBlYeegP96a4nUnE5s65RUj0D7MWFXAKhPutUXy7xa3yYTGdvOGewx9KodVqMhN+qMqt/hy/Mzv/Y7ibJXo291w/h31A/8Az+sufaNw8Vqasb3R1YW3IYhSB81PpJvjkY6csa+jXcRwqVaBBFxbSeZ9g+FBa1R31FJmpqSNSwNmb0UAepnonBajVMGhbQldb8jOTgkVFcgAd9ybc2ZizH1Jv6yFKk17NXG2n6MD9ovFmrVUoWAWkpIt8RY7noQB9TMdLnFsSKtapUBuGc5f5Rov0AlImdsFUUjz8kuUmxRRRSiBBY0iXFItKjnWADYoooANJjY60bGIcsJiERgA0R6yOSLAAmCEwQAIHWXKR0sdxofSU5NTf9j4jkf0/wBY06E0d/AAumU00Ycs3UaX1G9psOy7FaiBsOSFUhfZvlK6k8nXmTMrwvDN7NHFRVLu6KpVjcoEZjcaDRx8psOxdVjXKMyNlDXy30IGoboZnLspGkxFenTWlU9jWzBTq9RnH/CahHPpMT2vro9FqlSkMx7qMSCV1JGnLebjjZ7tMfg/MmYf7RVC0FH44LsZ5vHIdYyFZoZl0HSRO28IbSQOY2wJqFwpYbgi3mJseGcdVwPhbnY2PoZnOCIjOEfYg/O4mtTgNLQqLHwnLkkrpnf8dyitdHZwzioovmPizEj0G0vLlAsBYSlhsOVUC95aVbTnlI6rssK2lomqSq9a05HE+MpSG925AbmQrYWkit2txlwKS7sQP3nG4rRtSVOg+vOW8EhqP7Z9W5DkolXir3a00TrRnLa2a37NKjrhnT7tRgPIqjfmxh7Y8HfGmnZspQvra5ysF0/wiL7PUcU6jZbKX7p+9ZVDW8iLehmiqpncjw085Lk1KyoxTjRV+z/hX9loMGN2Luxbroqj6LGdoyCVXfMw0/lOYfVRNBhMOaVFUYjNa7EaDMdTbw1nHTCCs4qNyY5fIafnFJ27HFJKi9hl9nhsvhMn2nxH9nwba2ZlY+OZ9F9dV+U02NqZiKfwjVvIcvXb5zzr7ScWXKINrlj0vsB9THBXNIzyS4xbPP8APFnh9lHeznoHnDQ8cIQkcukAFka0ivL2Zbbym4iAV4oLRRgWHVZXdY3MYM0tyTIUWhwiMQhaSUMkiyKSrAAmCIy1VKMgZaYBBGaxY2GUADvMdCQTfqbbaQAqiOBtqI0RwgB6B2Vq0nwiUy4DrVqNZr6BggBBt4MJqezmFVMSXW2qNmI5kbH5TzvgWIWnRLsdFLGbTsJxinXdwujBCcp3t1EzZRqONDvIOiIJgftIf+Gg/EZveNWzi/JU/IGeb/aHVuKa+JP9fKNdh4MNEBFJ6BCZSfiPS9lHO3WaGYFa8iY3MalzpJqNIuQqAu3RQSfkImykh9CoUIN7WN79D1mw4Tx69g5EzZ4Jih71F1Gl2YWUX/Ft8pMcLRoAtUbOQDYDRS3IX3bx5RPC5d6+xx+RwdLf0b48YpAXLgeZnNxXaiiuitmPhrMW4PdzbkXPmdbfWOq0QNZyOEUzt/kk10drEcYqVPd7o+s5BcB9T5k7yJamXnKrvc3jUROZqsNjFtvKNctVqBKYzMxyqBzP7fpOVTrkT0X7OeCH/wBVUGrjuDoh3bzbT085Ljx2WpctG04XgVw1JKQGiqLHr94nxJ19Y1XAqhvCdOuRaw5CciwDEzCXZvF6L+NxACMxPIyDhiBaYPhORi6hYhb6XufSV8dxBwBTp7nnyXxiHQ+tij/EqcszW8lFv3nmvaDFNUVWYaln/O4+hE9M4Zwupiv4YutMAh35DTUA82PTle5nA7TdjaNKmUXHpnU3Cutr3sLEqSV0A1tOjDF3y8HJ8iSf9V2eaNYeJ/L/ADkc6+F7P16rFUyEA2zAnL5jS/5Tsf7CVbX9st+mU2/5p0PLFabOdYZy2kZCAzq8T4BiMPq6Zl+8utvMbicq8qMlJWiZRcXTQ68YYbwSyARRGKAEYgMUEQDxEYhCYwGSRZHJFiAMfTcpYjW9wRyI00+v0jIj7v8AeP5CMCSog3XY6xgnd7LYuhd8NikvSr5V9oBd6FQEinVXmRc2ZRuDzsBOXxDCGjVqUiQTTdkJF7EqbG19bRDLOAT2iijewYsT5AafUiaXsPw5ytV0a1RCSltNV0KN+FtvrM7wchSWO+gH5zW9hq+UYkf/ACf8xETA1uP4itZfaJzUCx3VlFmVhyIII9J5124rG6f1y/zmv7Q1RQxbEe5WIVh92plADjzAsfITE9tSS6+BIPmQLfQGR5KRw8DiCDoinxZc1v7rXUnpcGW6/DcQjB6qFAx0BsCAdPdHugZhppLHZHDhsTQQi92LW/lRmX5EAzWdrKeb2h/9taA9XrIfyEJ5GmolQxpps6fZLsThfaA1EzhBmYsdGPIFdreHhO7x7F4bCBf4aqDeyooDEL5bAmWcHiBhqNWq23huQvL1JnmOP4jUxLtWc6k6DoOQHhNMUnxVmeWK5OvBLxvi9TEteqe4L5EGiqOV7TLNUbEVNT3V+Vh4TuNw810vmyg6abm36RuH4UEFgdOfU+cnLn/ErFg/I5dcG1/GWA2ZJdxOEAUic2nohE57tHVVMpVXlcNH1jrIhNktGLey7w3DGvUSkPiYL5D4j6C59J73wpAigAWsAAOQA5TxjsVb+1Lf7rW89P0vPb8KndEwys3xLRJXewnJd7Aky5jHsJw8ViNLdZzM6YogetbMxkfDMKazlmOVFuzuTYAAXIB5abnkPMA8riuO9mvmbDzP+kq4riTJg6NAE5q2atVPVPaMtNR4XQkj8KzXHC1yfS/ZllyV/Vdv9Ha492zOX2OE7lNRlDDRmHgPhX6nnMngcHVxb94nLfWWcHw/2pA+c23CuHrSUACOWRsIYkiThvDUpIFVbWl72AkyDwkgE52dPRxsfgwRtPMe1nBhRPtEFlJ7wGwJ2I8569iRpMpx/DLUVkOzAj5ia45uMrMc0VKJ5PFHFSLg7i4PpGz0jywGKAw3gI//2Q==")
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE).placeholder(R.drawable.song_logo)
                        .error(R.drawable.song_logo))
                .into(((ImageView) findViewById(R.id.im_music)));

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



    private void showCloud(){

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, ColudFragment.newInstance("",""), "cloud")
                .commit();

    }

    private void showcloudplaylist(){
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, CloudPlaylistFragment.newInstance("",""), "cloud")
                .commit();
    }

    private void showPlaylist(View view) {

        System.out.println("---> showPlaylist\n");
        sendMusicControlCmd(CMD.getSDSONGLIST());
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, PlaylistFragment.newInstance(playList), "playList")
                .addToBackStack("playList")
                .commit();

    }

    private void showImageList() {

        System.out.println("---> showImageList\n");
        sendMusicControlCmd(CMD.getSDSONGLIST());
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

                        showImageList();
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
    private void selectMusicSource() {
        final Dialog dialog = new Dialog(OperationActivity.this);
        dialog.setContentView(R.layout.layout_select_source);

        dialog.findViewById(R.id.bt_wifi_radio).setOnClickListener(v -> {
            playList = new ArrayList<>();
            showMapView();

            dialog.dismiss();
        });

        dialog.findViewById(R.id.bt_sdcard).setOnClickListener(v -> {
            sendMusicControlCmd(CMD.getCMD_SRC_SD());
            playList = new ArrayList<>();
            dialog.dismiss();
        });

        dialog.findViewById(R.id.bt_bluetooth).setOnClickListener(v -> {
            sendMusicControlCmd(CMD.getCMD_SRC_BLUTOOTH_STREAM());
            playList = new ArrayList<>();
            dialog.dismiss();
        });

        dialog.findViewById(R.id.bt_wifi_cloud).setOnClickListener(v -> {
            playList = new ArrayList<>();
            sendMusicControlCmd(CMD.getCMD_SRC_CRISTAL_CLOUD());
//          showCloud();
//          showcloudplaylist();

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
//                           sendMusicControlCmd(CMD.getSDSONGURL());
//
//                        }
                        else if (data[0] == CMD.getSDSONGLIST()[1]){
                            System.out.println("getSDSONGLIST    -----> "+Integer.toHexString(data[0]));

                            playList.add(new String(data).substring(1));
                            System.out.println("playList 3  =====> "+ Arrays.deepToString(playList.toArray()));

                            ((PlaylistFragment) getSupportFragmentManager().findFragmentByTag("playList")).update(playList);
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
        } else {

            if (doubleTapToExit) {
                OperationActivity.this.finish();
            } else {
                Toast.makeText(OperationActivity.this, "Press again to exit", Toast.LENGTH_LONG).show();
                doubleTapToExit = true;
                doubleTapToExitHandler.postDelayed(() -> {
                    doubleTapToExit = false;
                }, 2000);
            }
        }
    }
}