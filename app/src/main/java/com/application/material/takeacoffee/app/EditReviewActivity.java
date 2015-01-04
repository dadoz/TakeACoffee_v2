package com.application.material.takeacoffee.app;

import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.application.material.takeacoffee.app.application.DataApplication;
import com.application.material.takeacoffee.app.fragments.EditReviewFragment;
import com.application.material.takeacoffee.app.fragments.interfaces.OnChangeFragmentWrapperInterface;
import com.application.material.takeacoffee.app.fragments.interfaces.OnLoadViewHandlerInterface;
import com.application.material.takeacoffee.app.fragments.interfaces.SetActionBarInterface;
import com.application.material.takeacoffee.app.models.User;
import com.application.material.takeacoffee.app.singletons.ImagePickerSingleton;

import java.io.IOException;


public class EditReviewActivity extends ActionBarActivity implements
        OnLoadViewHandlerInterface, SetActionBarInterface, OnChangeFragmentWrapperInterface {
    private static final String TAG = "CoffeeMachineActivity";
    private static String EDIT_REVIEW_FRAG_TAG = "EDIT_REVIEW_FRAG_TAG";
    @InjectView(R.id.onLoadLayoutId) View onLoadLayout;
    //Volley lib
    private RequestQueue requestQueue;
    private ImageLoader imageLoader;
    private final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(20);
    public static final String CURRENT_FRAGMENT_TAG = "CURRENT_FRAGMENT_TAG";
    private static String currentFragTag = null;
    private Bundle bundle;
    private DataApplication dataApplication;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_review);
        ButterKnife.inject(this);

        bundle = getIntent().getBundleExtra(CoffeeMachineActivity.EXTRA_DATA);

        //custom actionBar
        getSupportActionBar().setCustomView(R.layout.action_bar_custom_template);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setActionBarCustomViewById(-1, "Edit review");

        dataApplication = ((DataApplication) this.getApplication());
        //INIT VIEW
        if(savedInstanceState != null) {
            //already init app - try retrieve frag from manager
            Fragment fragment = getSupportFragmentManager()
                    .findFragmentByTag(getCurrentFragTag());
            initView(fragment, savedInstanceState);
            return;
        }

        setCurrentFragTag(EditReviewActivity.EDIT_REVIEW_FRAG_TAG);
        initView(new EditReviewFragment(), null);
    }

    private void initView(Fragment fragment, Bundle savedInstanceState) {
        if(savedInstanceState == null) {
            fragment.setArguments(bundle);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.editReviewFragmentContainerId, fragment, getCurrentFragTag())
                .commit();
    }

    public void setActionBarCustomViewById(int id) {
        //        invalidateOptionsMenu();
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar == null) {
            return;
        }
        ((TextView) actionBar.getCustomView().findViewById(R.id.cActBarTitleId))
                .setText("Edit mode");

        //current view
        View currentView = actionBar.getCustomView().findViewById(id);

        currentView.setVisibility(View.VISIBLE);
        ((TextView) currentView.findViewById(R.id.usernameTextId))
                .setText("fake david");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.take_a_coffee, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                Log.d(TAG, "hey home button");
                finish();
                return true;
//            case R.id.action_settings:
//                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setCurrentFragTag(String tag) {
        currentFragTag = tag;
    }
    @Override
    public String getCurrentFragTag() {
        return currentFragTag;
    }

    @Override
    public void startActivityWrapper(Class activityClassName, int requestCode, Bundle bundle) {

    }

    @Override
    public void changeFragment(Fragment fragment, Bundle bundle, String tag) {

    }

    @Override
    public void pushCurrentFragTag(String tag) {
    }

    @Override
    public String popCurrentFragTag() {
        return null;
    }


    @Override
    public void initOnLoadView(View view) {
        onLoadLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideOnLoadView(View view) {
        onLoadLayout.setVisibility(View.GONE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            Bitmap picture = ImagePickerSingleton.getInstance(this.getApplicationContext())
                    .onActivityResultWrapped(requestCode, resultCode, data);
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(getCurrentFragTag());
            ((ImageView) fragment.getView().findViewById(R.id.imagePreviewViewId))
                    .setImageBitmap(picture);
            dataApplication.setReviewPictureTemp(picture);
            picture = null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(CURRENT_FRAGMENT_TAG, getCurrentFragTag());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        setCurrentFragTag(savedInstanceState.getString(CURRENT_FRAGMENT_TAG));
    }


    @Override
    public void setActionBarCustomViewById(int id, Object data) {
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar == null) {
            return;
        }

        View currentView = actionBar.getCustomView();

        currentView.findViewById(R.id.customActionBarReviewListLayoutId).setVisibility(View.GONE);
        currentView.findViewById(R.id.cActBarTitleId).setVisibility(View.VISIBLE);
        ((TextView) currentView.findViewById(R.id.cActBarTitleId)).setText((String) data);

    }

    @Override
    public void setCustomNavigation(Class<?> id) {

    }

    @Override
    public boolean isItemSelected() {
        return false;
    }

    @Override
    public int getSelectedItemPosition() {
        return 0;
    }

    @Override
    public void setSelectedItemView(View selectedItemView) {

    }

    @Override
    public void updateSelectedItem(AdapterView.OnItemLongClickListener listener, ListView listView, View view, int itemPos) {

    }
}