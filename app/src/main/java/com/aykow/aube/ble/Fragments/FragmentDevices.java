package com.aykow.aube.ble.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.aykow.aube.ble.DatabaseHelper;
import com.aykow.aube.ble.activities.MainActivity;
import com.aykow.aube.ble.R;
import com.aykow.aube.ble.RecordedDeviceListAdapter;
import com.aykow.aube.ble.activities.SearchNewDevicesActivity;
import com.aykow.aube.ble.UserDevicesDB;
import com.aykow.aube.ble.UserDevicesDBDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sarah on 23/03/2016.
 */
public class FragmentDevices extends Fragment {

    private UserDevicesDBDataSource dataSource;
    DatabaseHelper dbHelper;
    private List<UserDevicesDB> userDevicesDBList = new ArrayList<UserDevicesDB>();

    ListView lvDevices;
    private RecordedDeviceListAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d("FRAGMENT_DEVICES", "--------> onCreateView");
        View view = inflater.inflate(R.layout.fragment_devices, container, false);
        lvDevices = (ListView) view.findViewById(R.id.listDevices);

        /*dataSource = new UserDevicesDBDataSource(getContext());
        userDevicesDBList = dataSource.getAllDevicesInDB();*/

        dbHelper = DatabaseHelper.getDbHelperInstance(getContext());
        userDevicesDBList = dbHelper.getAllDevicesInDbSQLite();

        Log.d("FRAGMENT_DEVICES", "--------> userDevicesDBList size: "+ userDevicesDBList.size());
        if(userDevicesDBList.size()>0)
        {
            Log.d("FRAGMENT DEVICES", " ---> Some devices founded");

            //ArrayAdapter<UserDevicesDB> objAdapter = new ArrayAdapter<UserDevicesDB>(this.getActivity(),android.R.layout.simple_list_item_1, userDevicesDBList);
            mAdapter = new RecordedDeviceListAdapter(getContext());

            mAdapter.setData(userDevicesDBList);
            mAdapter.setModifyListener(new RecordedDeviceListAdapter.OnModifyButtonClickListener() {
                @Override
                public void onModifyButtonClick(int position) {
                    //Toast.makeText(getApplicationContext(),"oki, we will add it soon", Toast.LENGTH_SHORT).show();
                    UserDevicesDB selected_aube = userDevicesDBList.get(position);

                    // TO DO --> action to modify data on this device by device address
                    Toast.makeText(getContext(), R.string.coming_soon, Toast.LENGTH_SHORT ).show();
                }
            });

            mAdapter.setDeleteListener(new RecordedDeviceListAdapter.OnDeleteButtonClickListener() {
                @Override
                public void onDeleteButtonClick(int position) {
                    //Toast.makeText(getApplicationContext(),"oki, we will add it soon", Toast.LENGTH_SHORT).show();
                    UserDevicesDB selected_aube = userDevicesDBList.get(position);

                    // TO DO --> action to delete this device by device address
                    Toast.makeText(getContext(), R.string.coming_soon, Toast.LENGTH_SHORT ).show();
                }
            });

            if(lvDevices ==null)
            {
                Log.d("FRAGMENT DEVICES", "----->LvDEVICES is NULL");
            }
            else {
                Log.d("FRAGMENT DEVICES", "----->LvDEVICES is NOT NULL");
                lvDevices.setAdapter(mAdapter);
                lvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ImageButton btnModify = (ImageButton) view.findViewById(R.id.btn_modify_device);
                    ImageButton btnDelete = (ImageButton) view.findViewById(R.id.btn_delete_device);

                    }


                });

            }

        }
        else
        {
            Log.d("FRAGMENT DEVICES", " ---> Nothing founded");
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("FRAGMENT DEVICES", "---->onResume");
        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.devices);
        //getActivity().setTitle(R.string.devices);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);// <----------
        menu.clear();
        inflater.inflate(R.menu.add_device,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add_device:
                //Toast.makeText(getContext(),"add device action",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getContext(),SearchNewDevicesActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
