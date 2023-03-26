package com.cristal.ble.operation;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleIndicateCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import com.cristal.ble.ChatMsg;
import com.cristal.ble.R;
import com.cristal.ble.adapter.ChatMsgAdapter;
import com.cristal.ble.tool.Utils;
import com.cristal.ble.ui.player.OperationActivity;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class CharacteristicOperationFragment extends Fragment {

    private static final String TAG = "CharacteristicOperation";

    // Property
    public static final int PROPERTY_READ = 1;
    public static final int PROPERTY_WRITE = 2;
    public static final int PROPERTY_WRITE_NO_RESPONSE = 3;
    public static final int PROPERTY_NOTIFY = 4;
    public static final int PROPERTY_INDICATE = 5;

    // Notify and Indicate Property
    private static final int CHAR_PROPERTY_NO_NOTIFY_OR_INDICATE_SELECTED = 0;
    private static final int CHAR_PROPERTY_NOTIFY_SELECTED = 1;
    private static final int CHAR_PROPERTY_INDICATE_SELECTED = 2;

    private static final int CHAR_PROPERTY_NO_READ_SELECTED = 0;
    private static final int CHAR_PROPERTY_READ_SELECTED = 1;

    //
    //Notify or Indicate select (If both exist, then Indicate)
    private static int charaPropNotifyOrIndicateSelect;
    private static int charaPropReadSelect;

    // Date Format
    private static final int DATA_FMT_STR = 0;  // UTF-8 string format

    private static final int DATA_FMT_HEX = 1;  // Hexadecimal format

    private static final int DATA_FMT_DEC = 2;  // Decimal format


    // Initialize the control
    private List<ChatMsg> mChatMsgList = new ArrayList<>();
    private RecyclerView msgRecyclerView;
    private ChatMsgAdapter adapter;
    private LinearLayoutManager layoutManager;

    private ArrayAdapter<String> fmtAdapter;    // Data format Adapter

    private static final String FMT_SELECT[] = { "Str", "Hex", "Dec" };
    private int charRecvFmtInt; // Receive data format
    private int charSendFmtInt; // Send data format


    private RelativeLayout charReceivableLayout;// Receiving area Layout

    private Spinner charRecvFmtSelect;          // Read data format selection
    private ToggleButton charNotifyIndicateEnableBtn; // Notification/instruction enable button
    private Button charClearBtn;                // Clear screen button

    private Button charReadBtn;                 // Read data button


    private LinearLayout charSendableLayout;    // Sending area Layout
    private CheckBox charSendOnTimeCheckbox;    // Timed send check box
    private EditText charSendOnTimeEdit;        // Timed sending time
    private Spinner charSendFmtSelect;          // Write data format selection
    private EditText charWriteStringEdit;       // Write data input box (UTF-8 format)
    private EditText charWriteHexEdit;          // Write data input box (hexadecimal format)
    private EditText charWriteDecEdit;          // Write data input box (decimal format)
    private Button charWriteBtn;                // Write data button


    private AlertDialog.Builder builder = null;
    private AlertDialog dialog = null;                 // Notify data bullet box
    private String uuid_service = "0000abf0-0000-1000-8000-00805f9b34fb";   //this is the characters uuid
    private String uuid_write = "0000abf3-0000-1000-8000-00805f9b34fb";   //for sending the command to device
    private String uuid_notify = "0000abf2-0000-1000-8000-00805f9b34fb";  //for receiving the data from device




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_characteristic_operation, container, false);
        initView(view);

        return view;
    }

    private void initView(View view) {

        //Receive area, including data format selection, Notify/Indicate enable, clear screen, read
        charReceivableLayout = (RelativeLayout) view.findViewById(R.id.char_receivable_layout);
        charRecvFmtSelect = (Spinner) view.findViewById(R.id.char_recv_fmt_select);
        charNotifyIndicateEnableBtn = (ToggleButton) view.findViewById(R.id.char_notify_indicate_enable_btn);
        charClearBtn = (Button) view.findViewById(R.id.char_clear_btn);
        charReadBtn = (Button) view.findViewById(R.id.char_read_btn);

        // Chat area
        msgRecyclerView = (RecyclerView) view.findViewById(R.id.char_msg_recycler_view);
        layoutManager = new LinearLayoutManager(getActivity());
        msgRecyclerView.setLayoutManager(layoutManager);
        adapter = new ChatMsgAdapter(mChatMsgList);
        msgRecyclerView.setAdapter(adapter);

        // Send area, including sending timing and sending button
        charSendableLayout = (LinearLayout) view.findViewById(R.id.char_sendable_layout);
        charSendOnTimeCheckbox = (CheckBox) view.findViewById(R.id.char_send_onTime_checkbox);
        charSendOnTimeEdit = (EditText) view.findViewById(R.id.char_send_onTime_et);
        charSendFmtSelect = (Spinner) view.findViewById(R.id.char_send_fmt_select);
        charWriteStringEdit = (EditText) view.findViewById(R.id.char_write_string_et);
        charWriteHexEdit = (EditText) view.findViewById(R.id.char_write_hex_et);
        charWriteDecEdit = (EditText) view.findViewById(R.id.char_write_dec_et);
        charWriteBtn = (Button) view.findViewById(R.id.char_write_btn);

        // Initialize the received data format
        fmtAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, FMT_SELECT);
        fmtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        charRecvFmtSelect.setAdapter(fmtAdapter);
        charRecvFmtSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                charRecvFmtInt = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Initialize sending data format
        charSendFmtSelect.setAdapter(fmtAdapter);
        charSendFmtSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                charSendFmtInt = i;

                switch (charSendFmtInt) {
                    case DATA_FMT_STR:
                        charWriteStringEdit.setVisibility(View.VISIBLE);
                        charWriteHexEdit.setVisibility(View.GONE);
                        charWriteDecEdit.setVisibility(View.GONE);

                        charWriteStringEdit.setFocusable(true);
                        charWriteStringEdit.setFocusableInTouchMode(true);
                        charWriteStringEdit.requestFocus();
                        break;

                    case DATA_FMT_HEX:
                        charWriteStringEdit.setVisibility(View.GONE);
                        charWriteHexEdit.setVisibility(View.VISIBLE);
                        charWriteDecEdit.setVisibility(View.GONE);

                        charWriteHexEdit.setFocusable(true);
                        charWriteHexEdit.setFocusableInTouchMode(true);
                        charWriteHexEdit.requestFocus();
                        break;

                    case DATA_FMT_DEC:
                        charWriteStringEdit.setVisibility(View.GONE);
                        charWriteHexEdit.setVisibility(View.GONE);
                        charWriteDecEdit.setVisibility(View.VISIBLE);

                        charWriteDecEdit.setFocusable(true);
                        charWriteDecEdit.setFocusableInTouchMode(true);
                        charWriteDecEdit.requestFocus();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public void showData() {
        final BleDevice bleDevice = ((OperationActivity) getActivity()).getBleDevice();
        final BluetoothGattCharacteristic characteristic = ((OperationActivity) getActivity()).getBluetoothGattCharacteristic();

        // Property
        int charaProp = characteristic.getProperties();
        charaPropNotifyOrIndicateSelect = CHAR_PROPERTY_NO_NOTIFY_OR_INDICATE_SELECTED;
        charaPropReadSelect = CHAR_PROPERTY_NO_READ_SELECTED;

        // Determine the functions that need to be displayed on the UI according to the Property
        // Write or Write_no_response
        if (((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0)
                || ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0)) {
            charSendableLayout.setVisibility(View.VISIBLE);
        } else {
            charSendableLayout.setVisibility(View.GONE);
        }

        // Read
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            charaPropReadSelect = CHAR_PROPERTY_READ_SELECTED;
        }

        // Notify or Indicate
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            charaPropNotifyOrIndicateSelect = CHAR_PROPERTY_NOTIFY_SELECTED;
        }
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
            charaPropNotifyOrIndicateSelect = CHAR_PROPERTY_INDICATE_SELECTED;
        }
        if ((charaPropReadSelect > 0) || (charaPropNotifyOrIndicateSelect > 0)) {
            // Initialize the receiving area
            charReceivableLayout.setVisibility(View.VISIBLE);

            // Initialize Read Button
            if (charaPropReadSelect > 0) {
                charReadBtn.setVisibility(View.VISIBLE);
            } else {
                charReadBtn.setVisibility(View.GONE);
            }

            // Initialize Notify or Indicate Enable/Disable ToggleButton
            switch (charaPropNotifyOrIndicateSelect) {
                case CHAR_PROPERTY_NO_NOTIFY_OR_INDICATE_SELECTED:
                    charNotifyIndicateEnableBtn.setVisibility(View.GONE);
                    break;
                case CHAR_PROPERTY_NOTIFY_SELECTED:
                    charNotifyIndicateEnableBtn.setVisibility(View.VISIBLE);
                    charNotifyIndicateEnableBtn.setText(getActivity().getString(R.string.notify));
                    break;
                case CHAR_PROPERTY_INDICATE_SELECTED:
                    charNotifyIndicateEnableBtn.setVisibility(View.VISIBLE);
                    charNotifyIndicateEnableBtn.setText(getActivity().getString(R.string.indicate));
                    break;
                default:
                    break;
            }
        } else {
            // Initialize the receiving area
            charReceivableLayout.setVisibility(View.GONE);
        }

        // Clear screen during initialization
        charDisplayClear();

        //Send (Write) Data Button
        charWriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final byte[] sendMsg = getSendData();
                final byte[] tag_LEN = new byte[2];
                tag_LEN[0] = 0x2;
                tag_LEN[1] = (byte) sendMsg.length;
                final byte[] sendMsg_L = concatenateByteArrays(tag_LEN,sendMsg);


                if (sendMsg == null) {
                    return;
                }
                System.out.println("TEST LOG 111 : "+characteristic.getService().getUuid().toString());
                System.out.println("bleDevice name : "+bleDevice.getName());
                System.out.println("bleDevice mac : "+bleDevice.getMac());
                System.out.println("TEST LOG 222 : "+characteristic.getUuid().toString());
                System.out.println("data in hex: "+sendMsg_L.toString());

                //uuid_service
                //uuid_write


//                for (byte i : sendMsg_L) {
//                    String hex = String.format("%02X", i);
//                    System.out.print(hex);
//                }
                System.out.println("\n=============================="+uuid_write);

                send_wifi_config_to_device((byte) 2,sendMsg_L);
                send_wifi_config_to_device((byte) 3,sendMsg_L);


                BleManager.getInstance().write(
                        bleDevice,
//                        characteristic.getService().getUuid().toString(),
                        uuid_service,
//                        characteristic.getUuid().toString(),
                        uuid_write,
                        sendMsg_L,
                        new BleWriteCallback() {
                            @Override
                            public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        charDisplaySendData(sendMsg);
                                    }
                                });
                            }

                            @Override
                            public void onWriteFailure(final BleException exception) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                    }
                                });
                            }
                        }
                );
            }
        });

        // Read data Button
        charReadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BleManager.getInstance().read(
                        bleDevice,
                        characteristic.getService().getUuid().toString(),
                        characteristic.getUuid().toString(),
                        new BleReadCallback() {
                            @Override
                            public void onReadSuccess(final byte[] data) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        charDisplayRecvData(data, false, characteristic.getUuid().toString());
                                    }
                                });
                            }

                            @Override
                            public void onReadFailure(final BleException exception) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                    }
                                });
                            }
                        });
            }
        });

        // Notify or Indicate ToggleButton
        charNotifyIndicateEnableBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    if (charaPropNotifyOrIndicateSelect == CHAR_PROPERTY_NOTIFY_SELECTED) {
                        // Notify

                        System.out.println("Notify 11---->: "+characteristic.getService().getUuid().toString());
                        System.out.println("Notify 22---->: "+characteristic.getUuid().toString());

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
                                        charDisplayRecvData(data, true, characteristic.getUuid().toString());
                                    }
                                }
                        );
                    } else if (charaPropNotifyOrIndicateSelect == CHAR_PROPERTY_INDICATE_SELECTED){
                        // Indicate
                        System.out.println("Indicate ------------>");
                        BleManager.getInstance().indicate(
                                bleDevice,
                                characteristic.getService().getUuid().toString(),
                                characteristic.getUuid().toString(),
                                new BleIndicateCallback() {
                                    @Override
                                    public void onIndicateSuccess() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                            }
                                        });
                                    }

                                    @Override
                                    public void onIndicateFailure(final BleException exception) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCharacteristicChanged(byte[] data) {
                                        charDisplayRecvData(data, true, characteristic.getUuid().toString());
                                    }

                                }
                        );
                    }

                } else {
                    if (charaPropNotifyOrIndicateSelect == CHAR_PROPERTY_NOTIFY_SELECTED) {
                        // Stop Notify
                        System.out.println(" Stop Notify ------------->");
                        BleManager.getInstance().stopNotify(
                                bleDevice,
                                characteristic.getService().getUuid().toString(),
                                characteristic.getUuid().toString());
                    } else if (charaPropNotifyOrIndicateSelect == CHAR_PROPERTY_INDICATE_SELECTED) {
                        // Stop Indicate
                        System.out.println(" Stop Indicate ------------->");

                        BleManager.getInstance().stopIndicate(
                                bleDevice,
                                characteristic.getService().getUuid().toString(),
                                characteristic.getUuid().toString());
                    }

                } // end if (isChecked)
            }
        });

        // Clear screen
        charClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                charDisplayClear();
            }
        });
    }


    public void  send_wifi_config_to_device(byte action,byte [] sendMsg){

        final BleDevice bleDevice = ((OperationActivity) getActivity()).getBleDevice();
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
    private void runOnUiThread(Runnable runnable) {
        if (isAdded() && (getActivity() != null)) {
            getActivity().runOnUiThread(runnable);
        }
    }

    /**
     * Clear screen in RecyclerView
     */
    private void charDisplayClear() {
        mChatMsgList.clear();
        adapter.notifyDataSetChanged();
        msgRecyclerView.removeAllViews();
    }

    /**
     * Get the data to be sent from EditText
     * @return
     */
    private byte[] getSendData() {
        String content = "";
        byte[] sendMsgByte = null;

        switch (charSendFmtInt) {
            case DATA_FMT_STR:  // String
                content = charWriteStringEdit.getText().toString();
                if (TextUtils.isEmpty(content)) {
                    return null;
                }

                sendMsgByte = content.getBytes();
                break;
            case DATA_FMT_HEX:  // Hexadecimal
                content = charWriteHexEdit.getText().toString();
                if (TextUtils.isEmpty(content)) {
                    return null;
                }

                sendMsgByte = HexUtil.hexStringToBytes(content);
                break;
            case DATA_FMT_DEC:  // 10 hex

                content = charWriteDecEdit.getText().toString();
                if (TextUtils.isEmpty(content)) {
                    return null;
                }

                int dataInteger = Integer.parseInt(content);
                int byteSize;
                for (byteSize = 0; dataInteger != 0; byteSize++) {  // Calculate the number of bytes occupied

                    dataInteger /= 256;
                }
                sendMsgByte = new byte[byteSize];

                dataInteger = Integer.parseInt(content);
                for (int i = 0; i < byteSize; i++) {
                sendMsgByte[i] = (byte) (0xFF & (dataInteger % 256));
                dataInteger /= 256;
            }
                break;
        }

        return sendMsgByte;
    }

    /**
     * Display the sent data in RecyclerView
     * @param data  The value to be displayed
     */
    private void charDisplaySendData(final byte[] data) {

        String content = "";

        switch (charSendFmtInt) {
            case DATA_FMT_STR:  // string
                content = charWriteStringEdit.getText().toString();
                charWriteStringEdit.setText("");  // Clear the content in the input box
                break;
            case DATA_FMT_HEX:  // 16hex
                content = charWriteHexEdit.getText().toString();
                charWriteHexEdit.setText("");
                break;
            case DATA_FMT_DEC:  // 10hex
                content = charWriteDecEdit.getText().toString();
                charWriteDecEdit.setText("");
                break;
        }

        ChatMsg chatMsg = new ChatMsg(content, ChatMsg.TYPE_SENT);
        mChatMsgList.add(chatMsg);
        int position = mChatMsgList.size() - 1;     // Get the coordinates of the last line of mChatMsgList
        adapter.notifyItemInserted(position);       // When there is a new message, refresh the display in RecyclerView
        msgRecyclerView.scrollToPosition(position); // Position RecyclerView to the last row

    }

    /**
     * RecyclerView displays the received data
     * @param data  The value to be displayed
     * @param toastEnable
     * Toast enable
     */
    private void charDisplayRecvData(byte[] data, boolean toastEnable, String uuid) {
        String tmp = "";

        switch (charRecvFmtInt) {
            case DATA_FMT_STR:  // string
                tmp = new String(data);
                break;
            case DATA_FMT_HEX:  // 16hex
                tmp = HexUtil.formatHexString(data, true);
                break;
            case DATA_FMT_DEC:  // 10hex
                int count = 0;
                for (int i = 0; i < data.length; i++) {
                    count *= 256;
                    count += (data[data.length - 1 - i] & 0xFF);
                }
                tmp = Integer.toString(count);
                break;
        }

        System.out.println("charDisplayRecvData----->");
        ChatMsg chatMsg = new ChatMsg(tmp, ChatMsg.TYPE_RECEIVED);
        mChatMsgList.add(chatMsg);
        int position = mChatMsgList.size() - 1;     // Get the coordinates of the last line of mChatMsgList
        adapter.notifyItemInserted(position);       // When there is a new message, refresh the display in RecyclerView
        msgRecyclerView.scrollToPosition(position); // Position RecyclerView to the last row


        // Toast Display the received value and the UUID of the received value
        if (toastEnable) {
            System.out.println("NOtifi the charatctor\n");
            Utils.showToast(getActivity(), uuid + ":\n" + tmp);
        }
    }

}
