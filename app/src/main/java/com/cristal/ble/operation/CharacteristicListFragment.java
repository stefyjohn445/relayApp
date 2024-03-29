package com.cristal.ble.operation;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cristal.ble.R;
import com.cristal.ble.tool.SampleGattAttributes;
import com.cristal.ble.ui.player.OperationActivity;

import java.util.ArrayList;
import java.util.List;

import static com.cristal.ble.ui.player.OperationActivity.CHAR_OPERATION_PAGE;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class CharacteristicListFragment extends Fragment{

    private ResultAdapter mResultAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_characteristic_list, null);
//        View view = inflater.inflate(R.layout.wifi_connection, null);
//          View view = inflater.inflate(R.layout.player, null);



        initView(view);
        return view;
    }

    private void initView(View view) {
        mResultAdapter = new ResultAdapter(getActivity());

        ListView listViewChar = (ListView) view.findViewById(R.id.list_char);
        listViewChar.setAdapter(mResultAdapter);
        listViewChar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BluetoothGattCharacteristic characteristic = mResultAdapter.getItem(position);
                final List<Integer> propList = new ArrayList<>();
                List<String> propNameList = new ArrayList<>();

                int charaProp = characteristic.getProperties();
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    propList.add(CharacteristicOperationFragment.PROPERTY_READ);
                    propNameList.add("Read");
                }
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                    propList.add(CharacteristicOperationFragment.PROPERTY_WRITE);
                    propNameList.add("Write");
                }
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
                    propList.add(CharacteristicOperationFragment.PROPERTY_WRITE_NO_RESPONSE);
                    propNameList.add("Write No Response");
                }
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    propList.add(CharacteristicOperationFragment.PROPERTY_NOTIFY);
                    propNameList.add("Notify");
                    System.out.println("Notify ------------------->");
                }
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                    propList.add(CharacteristicOperationFragment.PROPERTY_INDICATE);
                    propNameList.add("Indicate");
                }

//                if (propList.size() > 1) {
//                    new AlertDialog.Builder(getActivity())
//                            .setTitle(getActivity().getString(R.string.select_operation_type)) // 选择操作类型
//                            .setItems(propNameList.toArray(new String[propNameList.size()]), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int which) {
//                                    ((OperationActivity) getActivity()).setBluetoothGattCharacteristic(characteristic);
//                                    ((OperationActivity) getActivity()).setCharaProp(propList.get(which));
//                                    ((OperationActivity) getActivity()).changePage(CHAR_OPERATION_PAGE);
//                                }
//                            })
//                            .show();
//                } else if (propList.size() > 0) {
//                    ((OperationActivity) getActivity()).setBluetoothGattCharacteristic(characteristic);
//                    ((OperationActivity) getActivity()).setCharaProp(propList.get(0));
//                    ((OperationActivity) getActivity()).changePage(CHAR_OPERATION_PAGE);
//                }
                ((OperationActivity) getActivity()).setBluetoothGattCharacteristic(characteristic);
                ((OperationActivity) getActivity()).setCharaProp(propList.get(0));
                ((OperationActivity) getActivity()).changePage(CHAR_OPERATION_PAGE);

            }
        });
    }

    public void showData() {
        BluetoothGattService service = ((OperationActivity) getActivity()).getBluetoothGattService();
        mResultAdapter.clear();
        for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
            mResultAdapter.addResult(characteristic);
        }
        mResultAdapter.notifyDataSetChanged();
    }

    private class ResultAdapter extends BaseAdapter {

        private Context context;
        private List<BluetoothGattCharacteristic> characteristicList;

        ResultAdapter(Context context) {
            this.context = context;
            characteristicList = new ArrayList<>();
        }

        void addResult(BluetoothGattCharacteristic characteristic) {
            characteristicList.add(characteristic);
        }

        void clear() {
            characteristicList.clear();
        }

        @Override
        public int getCount() {
            return characteristicList.size();
        }

        @Override
        public BluetoothGattCharacteristic getItem(int position) {
            if (position > characteristicList.size()) {
                return null;
            }
            return characteristicList.get(position);
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
                view = View.inflate(context, R.layout.adapter_characteristic, null);
                holder = new ViewHolder();
                view.setTag(holder);

                holder.txt_char_name = (TextView) view.findViewById(R.id.txt_char_name);
                holder.txt_char_uuid = (TextView) view.findViewById(R.id.txt_char_uuid);
                holder.txt_char_type = (TextView) view.findViewById(R.id.txt_char_type);
                holder.img_char_enter = (ImageView) view.findViewById(R.id.img_char_enter);
            }

            BluetoothGattCharacteristic characteristic = characteristicList.get(position);
            String uuid = characteristic.getUuid().toString();

            //holder.txt_char_name.setText(String.valueOf(getActivity().getString(R.string.characteristic) + "(" + position + ")"));
            holder.txt_char_name.setText(SampleGattAttributes.lookup(uuid, "Unknown"));
            holder.txt_char_uuid.setText(uuid);

            StringBuilder property = new StringBuilder();
            int charaProp = characteristic.getProperties();
            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                property.append("Read");
                property.append(" , ");
            }
            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                property.append("Write");
                property.append(" , ");
            }
            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
                property.append("Write No Response");
                property.append(" , ");
            }
            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                property.append("Notify");
                property.append(" , ");
            }
            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                property.append("Indicate");
                property.append(" , ");
            }

            if (property.length() > 1) {
                property.delete(property.length() - 2, property.length() - 1);
            }
            if (property.length() > 0) {
                holder.txt_char_type.setText(String.valueOf(getActivity().getString(R.string.property) + "(" +property.toString() + ")"));
                holder.img_char_enter.setVisibility(View.VISIBLE);
            } else {
                holder.img_char_enter.setVisibility(View.INVISIBLE);
            }

            return view;
        }

        class ViewHolder {
            TextView txt_char_name;
            TextView txt_char_uuid;
            TextView txt_char_type;

            ImageView img_char_enter;
        }

    }
}
