package com.cristal.ble.adapter;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;
import com.cristal.ble.R;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder>{

    private List<BleDevice> mBleDeviceList;

    static class ViewHolder extends RecyclerView.ViewHolder {

        View deviceView;

        ImageView bluetoothImg;
        ImageView rssiLogoImg;

        TextView deviceNameTxt;
        TextView macTxt;
        TextView rssiTxt;

        TextView connectBtn;

        ViewHolder(View view) {
            super(view);

            deviceView = view;

            bluetoothImg = (ImageView) view.findViewById(R.id.bluetooth_img);
            rssiLogoImg = (ImageView) view.findViewById(R.id.rssi_logo_img);
            deviceNameTxt = (TextView) view.findViewById(R.id.device_name_txt);
            macTxt = (TextView) view.findViewById(R.id.mac_txt);
            rssiTxt = (TextView) view.findViewById(R.id.rssi_txt);
            connectBtn = (TextView) view.findViewById(R.id.connect_btn);
        }
    }

    public DeviceAdapter(List<BleDevice> bleDeviceList) {
        mBleDeviceList = bleDeviceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.deviceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                BleDevice bleDevice = mBleDeviceList.get(position);

                if (mListener != null) {
                    mListener.onDetail(bleDevice);
                }
            }
        });

        holder.connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                BleDevice bleDevice = mBleDeviceList.get(position);

                if (mListener != null) {
                    mListener.onConnect(bleDevice);
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        BleDevice bleDevice = mBleDeviceList.get(position);

        String name = bleDevice.getName();
        String mac = bleDevice.getMac();
        int rssi = bleDevice.getRssi();

        holder.bluetoothImg.setImageResource(R.mipmap.ic_blue_remote);
        //When the device name is empty, it is mandatory to set the device name to N/A
        holder.deviceNameTxt.setText((name == null) || (name.length() == 0) ? "N/A" : name);
        holder.macTxt.setText(mac);
        holder.rssiTxt.setText(String.valueOf(rssi));

        // Display different RSSI Logos according to different RSSI levels
        final int rssiPercent = (int) (100.0f * (127.0f + rssi) / (127.0f + 20.0f));
        holder.rssiLogoImg.setImageLevel(rssiPercent);
    }

    @Override
    public int getItemCount() {
        return mBleDeviceList.size();
    }

    public void addDevice(BleDevice bleDevice) {
        removeDevice(bleDevice);
        mBleDeviceList.add(bleDevice);
        notifyDataSetChanged();
    }

    public void removeDevice(BleDevice bleDevice) {
        for (int i = 0; i < mBleDeviceList.size(); i++) {
            BleDevice device = mBleDeviceList.get(i);
            if (bleDevice.getKey().equals(device.getKey())) {
                mBleDeviceList.remove(i);
            }
        }
    }

    public void clearConnectedDevice() {
        for (int i = 0; i < mBleDeviceList.size(); i++) {
            BleDevice device = mBleDeviceList.get(i);
            if (BleManager.getInstance().isConnected(device)) {
                mBleDeviceList.remove(i);
            }
        }
    }

    public void clearScanDevice() {
        for (int i = 0; i < mBleDeviceList.size(); i++) {
            BleDevice device = mBleDeviceList.get(i);
            if (!BleManager.getInstance().isConnected(device)) {
                mBleDeviceList.remove(i);
            }
        }
    }

    public void clear() {
        clearConnectedDevice();
        clearScanDevice();
        notifyDataSetChanged();
    }

    public interface OnDeviceClickListener {
        void onConnect(BleDevice bleDevice);
        void onDetail(BleDevice bleDevice);
    }

    private OnDeviceClickListener mListener;

    public void setOnDeviceClickListener(OnDeviceClickListener listener) {
        this.mListener = listener;
    }
}
