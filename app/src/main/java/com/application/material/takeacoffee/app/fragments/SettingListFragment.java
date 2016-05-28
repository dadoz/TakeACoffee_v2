package com.application.material.takeacoffee.app.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;

import butterknife.Bind;
import butterknife.ButterKnife;
import com.application.material.takeacoffee.app.PlacesActivity;
import com.application.material.takeacoffee.app.R;
import com.application.material.takeacoffee.app.adapters.SettingListAdapter;
import com.application.material.takeacoffee.app.models.CoffeePlace;
import com.application.material.takeacoffee.app.models.Setting;

import java.util.ArrayList;


/**
 * Created by davide on 08/04/14.
 */
public class SettingListFragment extends Fragment
        implements AdapterView.OnItemClickListener {
    public static final String SETTING_LIST_FRAG_TAG = "SETTING_LIST_FRAG_TAG";
    @Bind(R.id.settingsContainerListViewId)
    ListView listView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        View settingListView = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, settingListView);

        ((PlacesActivity) getActivity()).setCurrentFragmentTag(SETTING_LIST_FRAG_TAG);
        initView();
        return settingListView;
    }

    /**
     *
     */
    public void initView() {
        ActionBar actionbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setTitle("Settings");
        }

        ArrayList<Setting> settingList = getData();
        initListView(settingList);
    }

    /**
     * init data
     */
    public ArrayList<Setting> getData() {
        ArrayList<Setting> settingList = new ArrayList<Setting>();
        settingList.add(new Setting("ID", 0, R.drawable.ic_check_white_48dp, "User"));
        settingList.add(new Setting("ID", 1, R.drawable.ic_check_white_48dp, "Enable auto save"));
        settingList.add(new Setting("ID", 2, R.drawable.ic_check_white_48dp, "Version 0.3"));
        return settingList;
    }

    /**
     * init list view
     * @param settingList
     */
    public void initListView(ArrayList<Setting> settingList) {
        SettingListAdapter settingListAdapter = new SettingListAdapter(getActivity(),
                R.layout.item_review, settingList);
        listView.setAdapter(settingListAdapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.e("SETTING", "hey click item");
    }

}

