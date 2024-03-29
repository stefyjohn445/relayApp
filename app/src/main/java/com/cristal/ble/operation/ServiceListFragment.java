package com.cristal.ble.operation;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;
import com.cristal.ble.R;
import com.cristal.ble.tool.SampleGattAttributes;
import com.cristal.ble.ui.player.OperationActivity;

import java.util.ArrayList;
import java.util.List;

import static com.cristal.ble.ui.player.OperationActivity.CHAR_LIST_PAGE;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ServiceListFragment extends Fragment {

    private TextView txtServiceName;
    private TextView txtServiceMac;

    private ResultAdapter mResultAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_list, null);
        initView(view);
        showData();

        return view;
    }

    private void initView(View view) {
        txtServiceName = (TextView) view.findViewById(R.id.txt_service_device_name);
        txtServiceMac = (TextView)view.findViewById(R.id.txt_service_mac);

        mResultAdapter = new ResultAdapter(getActivity());

        ListView listViewDevice = (ListView) view.findViewById(R.id.list_service);
        listViewDevice.setAdapter(mResultAdapter);
        listViewDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                BluetoothGattService service = mResultAdapter.getItem(position);
                ((OperationActivity) getActivity()).setBluetoothGattService(service);
                ((OperationActivity) getActivity()).changePage(CHAR_LIST_PAGE);
            }
        });
    }

    private void showData() {
        BleDevice bleDevice = ((OperationActivity) getActivity()).getBleDevice();

        String name = bleDevice.getName();
        String mac = bleDevice.getMac();

        BluetoothGatt gatt = BleManager.getInstance().getBluetoothGatt(bleDevice);

        txtServiceName.setText(String.valueOf(getActivity().getString(R.string.device_name) + name));
        txtServiceMac.setText(String.valueOf(getActivity().getString(R.string.mac_address) + mac));

        mResultAdapter.clear();
        for (BluetoothGattService service : gatt.getServices()) {
            mResultAdapter.addResult(service);
        }
        mResultAdapter.notifyDataSetChanged();
    }

    private class ResultAdapter extends BaseAdapter {

        private Context context;
        private List<BluetoothGattService> bluetoothGattServiceList;

        private ResultAdapter(Context context) {
            this.context = context;
            bluetoothGattServiceList = new ArrayList<>();
        }

        private void addResult(BluetoothGattService service) {
            bluetoothGattServiceList.add(service);
        }

        private void clear() {
            bluetoothGattServiceList.clear();
        }

        @Override
        public int getCount() {
            return bluetoothGattServiceList.size();
        }

        @Override
        public BluetoothGattService getItem(int position) {
            if (position > bluetoothGattServiceList.size()) {
                return null;
            }
            return bluetoothGattServiceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            ViewHolder holder;

            if (view != null) {
                holder = (ViewHolder) view.getTag();
            } else {
                view = View.inflate(context, R.layout.service_item, null);
                holder = new ViewHolder();
                view.setTag(holder);

                holder.txt_service_name = (TextView) view.findViewById(R.id.txt_service_name);
                holder.txt_service_uuid = (TextView) view.findViewById(R.id.txt_service_uuid);
                holder.txt_service_type = (TextView) view.findViewById(R.id.txt_service_type);
            }

            BluetoothGattService service = bluetoothGattServiceList.get(position);
            String uuid = service.getUuid().toString();
            int type = service.getType();
            
            holder.txt_service_name.setText(SampleGattAttributes.lookup(uuid, "Unknown Service"));
            holder.txt_service_uuid.setText(uuid);
            holder.txt_service_type.setText(getActivity().getString(R.string.type));

            return view;
        }

        class ViewHolder {
            TextView txt_service_name;
            TextView txt_service_uuid;
            TextView txt_service_type;
        }
    }
}
