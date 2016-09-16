package com.aykow.aube.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

/**
 * Created by sarah on 07/04/2016.
 */
public class RecordedDeviceListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<UserDevicesDB> mData;
    private OnDeleteButtonClickListener mDeleteListener;
    private OnModifyButtonClickListener mModifyListener;

    public RecordedDeviceListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public void setData(List<UserDevicesDB> data) {
        mData = data;
    }

    public void setDeleteListener(OnDeleteButtonClickListener listener) {
        mDeleteListener = listener;
    }

    public void setModifyListener(OnModifyButtonClickListener listener) {
        mModifyListener = listener;
    }

    public int getCount() {
        return (mData == null) ? 0 : mData.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView			=  mInflater.inflate(R.layout.list_recorded_device, null);

            holder 				= new ViewHolder();

            holder.nameTv		= (TextView) convertView.findViewById(R.id.tv_name_recordedDevice);
            holder.addressTv 	= (TextView) convertView.findViewById(R.id.tv_address_recordedDevice);
            holder.modifyBtn		= (ImageButton) convertView.findViewById(R.id.btn_modify_device);
            holder.deleteBtn = (ImageButton) convertView.findViewById(R.id.btn_delete_device);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        UserDevicesDB userDevicesDB	= mData.get(position);

        holder.nameTv.setText(userDevicesDB.getNameDev());
        holder.addressTv.setText(userDevicesDB.getAddr());
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDeleteListener != null) {
                    mDeleteListener.onDeleteButtonClick(position);
                }
            }
        });

        holder.modifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mModifyListener != null) {
                    mModifyListener.onModifyButtonClick(position);
                }
            }
        });

        return convertView;
    }

    static class ViewHolder {
        TextView nameTv;
        TextView addressTv;
        ImageButton modifyBtn;
        ImageButton deleteBtn;
    }

    public interface OnModifyButtonClickListener {
        public abstract void onModifyButtonClick(int position);
    }

    public interface OnDeleteButtonClickListener {
        public abstract void onDeleteButtonClick(int position);
    }

}
