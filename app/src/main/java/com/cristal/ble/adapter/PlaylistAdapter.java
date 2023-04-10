package com.cristal.ble.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;
import com.cristal.ble.R;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder>{

    private ArrayList<String> mBleDeviceList;

    public void update(@NotNull ArrayList<String> playList) {
        mBleDeviceList = playList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        View deviceView;
        TextView deviceNameTxt;
        TextView connectBtn;

        ViewHolder(View view) {
            super(view);

            deviceView = view;
            deviceNameTxt = (TextView) view.findViewById(R.id.device_name_txt);
            connectBtn = (TextView) view.findViewById(R.id.connect_btn);
        }
    }

    public PlaylistAdapter(ArrayList<String> bleDeviceList) {
        mBleDeviceList = bleDeviceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.playlist_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.deviceView.setOnClickListener(view12 -> {
            int position = holder.getAdapterPosition();
            if (mListener != null) {
                mListener.onSelect(mBleDeviceList.get(position));
            }
        });

        holder.connectBtn.setOnClickListener(view1 -> {
            int position = holder.getAdapterPosition();
            if (mListener != null) {
                mListener.onSelect(mBleDeviceList.get(position));
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String[] separated = mBleDeviceList.get(position).split("/");
        if (separated.length > 2){
            holder.deviceNameTxt.setText(separated[separated.length-1]);
        }else {
            holder.deviceNameTxt.setText(mBleDeviceList.get(position));

        }

    }

    @Override
    public int getItemCount() {
        return mBleDeviceList.size();
    }

//    public void addDevice(BleDevice bleDevice) {
//        removeDevice(bleDevice);
//        mBleDeviceList.add(bleDevice);
//    }

//    public void removeDevice(BleDevice bleDevice) {
//        for (int i = 0; i < mBleDeviceList.size(); i++) {
//            BleDevice device = mBleDeviceList.get(i);
//            if (bleDevice.getKey().equals(device.getKey())) {
//                mBleDeviceList.remove(i);
//            }
//        }
//    }
//
//    public void clearConnectedDevice() {
//        for (int i = 0; i < mBleDeviceList.size(); i++) {
//            BleDevice device = mBleDeviceList.get(i);
//            if (BleManager.getInstance().isConnected(device)) {
//                mBleDeviceList.remove(i);
//            }
//        }
//    }

    public void clearScanDevice() {
        for (int i = 0; i < mBleDeviceList.size(); i++) {
            mBleDeviceList.remove(i);
        }
    }

    public void clear() {
        clearScanDevice();
    }

    public interface OnPlaylistClickListener {
        void onSelect(String song);
    }

    private OnPlaylistClickListener mListener;

    public void setOnDeviceClickListener(OnPlaylistClickListener listener) {
        this.mListener = listener;
    }
}
