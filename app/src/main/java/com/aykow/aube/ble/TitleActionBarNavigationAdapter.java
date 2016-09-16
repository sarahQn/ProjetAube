package com.aykow.aube.ble;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by sarah on 06/05/2016.
 */
public class TitleActionBarNavigationAdapter extends BaseAdapter {

    private TextView txtTitle;
    private ArrayList<SpinnerActionBarNavItem> spinnerNavItem;
    private Context context;

    public TitleActionBarNavigationAdapter(Context context, ArrayList<SpinnerActionBarNavItem> spinnerNavItem){
        this.spinnerNavItem = spinnerNavItem;
        this.context = context;
    }

    @Override
    public int getCount() {
        return spinnerNavItem.size();
    }

    @Override
    public Object getItem(int position) {
        return spinnerNavItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.list_item_title_actionbar_navigation, null);
        }

        txtTitle = (TextView) convertView.findViewById(R.id.txtTitle);
        txtTitle.setText(spinnerNavItem.get(position).getTitle());
        txtTitle.setTextAppearance(context, android.R.style.Widget_Holo_ActionBar_Solid);// AppCompat.Light.ActionBar.Solid;//.TextAppearance_Holo_Widget_ActionBar_Title);
        txtTitle.setTextSize(19);
        txtTitle.setTextColor(Color.parseColor("#ffffff"));
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.list_item_title_actionbar_navigation, null);
        }

        txtTitle = (TextView) convertView.findViewById(R.id.txtTitle);

        txtTitle.setText(spinnerNavItem.get(position).getTitle());
        txtTitle.setTypeface(null, Typeface.BOLD);
       // txtTitle.setTextAppearance(context, android.R.style.TextAppearance_Holo_Widget_ActionBar_Title);
        txtTitle.setTextAppearance(context,android.R.style.Widget_Holo_ActionBar_Solid);
        txtTitle.setTextSize(19);
        txtTitle.setTextColor(Color.parseColor("#ffffff"));
        return convertView;
    }
}
