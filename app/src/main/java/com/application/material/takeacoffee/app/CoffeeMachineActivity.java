package com.application.material.takeacoffee.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.widget.ImageView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.application.material.takeacoffee.app.adapters.ReviewListAdapter;
import com.application.material.takeacoffee.app.application.DataApplication;
import com.application.material.takeacoffee.app.fragments.CoffeeMachineFragment;
import com.application.material.takeacoffee.app.fragments.ReviewListFragment;
import com.application.material.takeacoffee.app.fragments.LoggedUserFragment;
import com.application.material.takeacoffee.app.fragments.interfaces.OnChangeFragmentWrapperInterface;
import com.application.material.takeacoffee.app.fragments.interfaces.OnLoadViewHandlerInterface;
import com.application.material.takeacoffee.app.fragments.interfaces.SetActionBarInterface;
import com.application.material.takeacoffee.app.models.CoffeeMachine;
import com.application.material.takeacoffee.app.models.CoffeeMachineStatus;
import com.application.material.takeacoffee.app.models.Review;
import com.application.material.takeacoffee.app.singletons.BusSingleton;
import com.application.material.takeacoffee.app.singletons.VolleySingleton;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;


public class CoffeeMachineActivity extends ActionBarActivity implements
        OnLoadViewHandlerInterface, OnChangeFragmentWrapperInterface,
        SetActionBarInterface, View.OnClickListener {
    private static final String TAG = "CoffeeMachineActivity";
    public static final int RESULT_FAILED = 111;
    public static String EXTRA_DATA = "EXTRA_DATA";
    @InjectView(R.id.onLoadLayoutId)
    View onLoadLayout;
    //Volley lib
    public static final String CURRENT_FRAGMENT_TAG = "CURRENT_FRAGMENT_TAG";
    private static ArrayList<String> currentFragTagList = new ArrayList<String>();
    public static String ACTION_EDIT_REVIEW_RESULT = "EDIT_RESULT";
    public static String ACTION_ADD_REVIEW_RESULT = "ADD_RESULT";
    public static final String ERROR_MESSAGE_KEY = "EMK";
    public static final int ACTION_EDIT_REVIEW = 1;
    public static final int ACTION_ADD_REVIEW = 2;
    private int selectedItemPosition = -1;
    private String tempActionBarTitle;
    private View selectedItemView;
    private ListView reviewListview;
    private DataApplication dataApplication;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coffee_machine);
        ButterKnife.inject(this);

        //custom actionBar
        getSupportActionBar().setCustomView(R.layout.action_bar_custom_template);
        getSupportActionBar().setDisplayShowCustomEnabled(true);


        dataApplication = ((DataApplication) this.getApplication());

        //INIT VIEW
        if(savedInstanceState != null) {
            String currentFragTag = getCurrentFragTag();

            if(currentFragTag == null) {
                Log.e(TAG, "BUG - this shouldnt happen!");
                return;
            }

            Fragment fragment = getSupportFragmentManager().findFragmentByTag(
                    currentFragTag);
            initView(fragment);
            return;
        }

        pushCurrentFragTag(CoffeeMachineFragment.COFFEE_MACHINE_FRAG_TAG);
        initView(new CoffeeMachineFragment());
    }

    @Override
    public void onResume(){
        BusSingleton.getInstance().register(this);
        super.onResume();
    }

    @Override
    public void onPause(){
        BusSingleton.getInstance().unregister(this);
        super.onPause();
    }


    private void initView(Fragment fragment) {
        String currentFragTag = getCurrentFragTag();
        if(fragment == null ||
                currentFragTag == null) {
            //if sm error init again app
            pushCurrentFragTag(CoffeeMachineFragment.COFFEE_MACHINE_FRAG_TAG);
            fragment = new CoffeeMachineFragment();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.coffeeAppFragmentContainerId, fragment, currentFragTag)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.take_a_coffee, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "OnBackPressed - ");

        String currentFragTag = getCurrentFragTag();
        if(currentFragTag.compareTo(ReviewListFragment.REVIEW_LIST_FRAG_TAG) == 0 &&
                isItemSelected()) {
            selectedItemPosition = -1; //false
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(
                    currentFragTag);
            //no item popped out
            updateSelectedItem((AdapterView.OnItemLongClickListener) fragment,
                    ((ReviewListFragment) fragment).getListView(), null, -1);
            return;
        }

        //popCurrentFragment :)
        if(popCurrentFragTag() == null) {
            Log.e(TAG, "BUG - this shouldnt happen!");//TODO HANDLE THIS
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void initOnLoadView(View view) {
    }

    @Override
    public void initOnLoadView() {
            onLoadLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideOnLoadView(View view) {
    }

    @Override
    public void hideOnLoadView() {
        onLoadLayout.setVisibility(View.GONE);
    }

    @Override
    public void changeFragment(Fragment fragment, Bundle bundle, String tag) {
        if (fragment == null) {
            Log.e(TAG, "cannot change fragment!");
            return;
        }

        pushCurrentFragTag(tag);
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.coffeeAppFragmentContainerId, fragment, tag)
                .addToBackStack("TAG")
                .commit();
    }

    @Override
    public void startActivityWrapper(Class activityClassName, int requestCode, Bundle bundle) {
        Intent intent = new Intent(this, activityClassName);
        //EditReviewActivity
        if(activityClassName.equals(EditReviewActivity.class)) {
            try {
                intent.putExtra(CoffeeMachineActivity.EXTRA_DATA, bundle);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        if(activityClassName.equals(AddReviewActivity.class)) {
            try {
                CoffeeMachine coffeeMachine = (CoffeeMachine) bundle.get(CoffeeMachine.COFFEE_MACHINE_OBJ_KEY);
                intent.putExtra(CoffeeMachineActivity.EXTRA_DATA, coffeeMachine.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case RESULT_OK:
                Bundle bundle;
                ReviewListAdapter adapter;
                try {
                    String currentFragTag = getCurrentFragTag();
                    if(currentFragTag == null) {
                        Log.e(TAG, "BUG - this shouldnt happen");
                        return;
                    }

                    Fragment fragment = getSupportFragmentManager().findFragmentByTag(
                            currentFragTag);
                    adapter = getAdapterByFragment(fragment);
                    //get data
                    bundle = data.getExtras();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                switch (requestCode) {
                    case CoffeeMachineActivity.ACTION_EDIT_REVIEW:
                        String action = bundle.getString(
                                CoffeeMachineActivity.ACTION_EDIT_REVIEW_RESULT);
                        Review review = (Review) bundle.get(
                                Review.REVIEW_OBJ_KEY);
                        if (action == null || review == null) {
                            Log.e(TAG, "error onActivityResult - no action or empty data");
                            break;
                        }

                        if (action.equals("SAVE")) {
                            Log.e(TAG, "save " + review.getId());
                            adapter.updateReview(review);
                            break;
                        }

                        Log.e(TAG, "hey return form edit review");
                        break;
                    case CoffeeMachineActivity.ACTION_ADD_REVIEW:
                        //get current frag
                        review = (Review) bundle.get(
                                Review.REVIEW_OBJ_KEY);
                        if (review == null) {
                            Log.e(TAG, "error onActivityResult - no action or empty data");
                            break;
                        }

                        Log.e(TAG, "add " + review.getId());
                        adapter.addReview(review);
                        break;
                }
                break;
            case RESULT_CANCELED:
//                String message =  data.getExtras().getString(ERROR_MESSAGE_KEY);
//                String message = "error";
//                Log.e(TAG, "error got from above activity" + message);
//                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                break;
            case CoffeeMachineActivity.RESULT_FAILED:
                String message =  data.getExtras().getString(ERROR_MESSAGE_KEY);
                Log.e(TAG, "error got from above activity" + message);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private ReviewListAdapter getAdapterByFragment(Fragment fragment) throws Exception {
        ListView listView = ((ListView) fragment
                .getView().findViewById(R.id.reviewsContainerListViewId));
        return listView.getAdapter().getClass() == ReviewListAdapter.class ?
                ((ReviewListAdapter) listView.getAdapter()) :
                ((ReviewListAdapter) ((HeaderViewListAdapter) listView.getAdapter())
                        .getWrappedAdapter());
    }

    @Override
    public void pushCurrentFragTag(String tag) {
        currentFragTagList.add(tag);
    }

    @Override
    public void setCurrentFragTag(String tag) {
    }

    @Override
    public String getCurrentFragTag() {
        return currentFragTagList.size() == 0 ?
                null : currentFragTagList.get(currentFragTagList.size() -1);
    }

    @Override
    public String popCurrentFragTag() {
        if(currentFragTagList.size() == 0) {
            return null;
        }

        if(currentFragTagList.size() == 1) {
            return currentFragTagList.get(0);
        }

        currentFragTagList.remove(currentFragTagList.size() - 1);
        return currentFragTagList.get(currentFragTagList.size() -1);
    }


    @Override
    public boolean isItemSelected() {
        return selectedItemPosition != -1;
    }

    @Override
    public int getSelectedItemPosition() {
        return selectedItemPosition;
    }

    @Override
    public void setSelectedItemView(View view) {
        selectedItemView = view;
        selectedItemView.setBackgroundColor(isItemSelected() ?
                getResources().getColor(R.color.material_amber_200) : 0x00000000);
    }


    @Override
    public void updateSelectedItem(AdapterView.OnItemLongClickListener listener,
                                   ListView listView, View view, int itemPos) {
        try {
            selectedItemPosition = itemPos; // due to header on listview
            reviewListview = listView;
            selectedItemView = isItemSelected() ? view : selectedItemView;

            setActionBarSelected();
            reviewListview.setOnItemLongClickListener(isItemSelected() ? null : listener);
            if(selectedItemView != null) {
                selectedItemView.setBackgroundColor(isItemSelected() ?
                        getResources().getColor(R.color.material_amber_200) : 0x00000000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setActionBarSelected() {
        getSupportActionBar().setBackgroundDrawable(
                new ColorDrawable(getResources().getColor(
                        isItemSelected() ? R.color.material_grey : R.color.action_bar)));
        invalidateOptionsMenu();
        //set new title on actionBar
        try {
            if(isItemSelected()) {
                tempActionBarTitle = ((TextView) getSupportActionBar()
                        .getCustomView().findViewById(R.id.cActBarCoffeeMachineNameId)).getText().toString();
                ((TextView) getSupportActionBar()
                        .getCustomView().findViewById(R.id.cActBarCoffeeMachineNameId))
                        .setText(ReviewListFragment.EDIT_REVIEW_STRING);

                return;
            }

            if(tempActionBarTitle != null) {
                ((TextView) getSupportActionBar()
                        .getCustomView().findViewById(R.id.cActBarCoffeeMachineNameId))
                        .setText(tempActionBarTitle);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void setActionBarCustomViewById(int id, Object data) {
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar == null) {
            return;
        }

        //HIDE all template - TODO refactorize it
        View userView = actionBar.getCustomView().findViewById(R.id.customActionBarUserLayoutId);
        userView.setVisibility(View.GONE);
        userView.setOnClickListener(null);
        actionBar.getCustomView().findViewById(R.id.customActionBarCoffeeMachineLayoutId).setVisibility(View.GONE);
        actionBar.getCustomView().findViewById(R.id.customActionBarReviewListLayoutId).setVisibility(View.GONE);
        actionBar.getCustomView().findViewById(R.id.customActionBarMapLayoutId).setVisibility(View.GONE);
        actionBar.getCustomView().findViewById(R.id.customActionSettingsLayoutId).setVisibility(View.GONE);
        actionBar.getCustomView().findViewById(R.id.customActionLoggedUserLayoutId).setVisibility(View.GONE);

        //current view
        View currentView = actionBar.getCustomView().findViewById(id);
        currentView.setVisibility(View.VISIBLE);
        switch (id) {
            case R.id.customActionBarCoffeeMachineLayoutId:
                if(data == null) {
                    return;
                }
                ((TextView) actionBar.getCustomView().findViewById(R.id.cActBarTitleId))
                        .setVisibility(View.VISIBLE);
                ((TextView) actionBar.getCustomView().findViewById(R.id.cActBarTitleId))
                    .setText(((CoffeeMachine) data).getName());
                ((TextView) actionBar.getCustomView().findViewById(R.id.cActBarCoffeeMachineLocationId))
                        .setText(((CoffeeMachine) data).getAddress());
                break;
            case R.id.customActionBarUserLayoutId:
                userView.setOnClickListener(this);
                ((TextView) actionBar.getCustomView().findViewById(R.id.cActBarTitleId))
                        .setVisibility(View.GONE);

                ((TextView) actionBar.getCustomView().findViewById(R.id.usernameId))
                        .setText(dataApplication.getUsername());
                ((ImageView) actionBar.getCustomView().findViewById(R.id.userIconId))
                        .setVisibility(View.VISIBLE);

                try {
                    int defaultIconId = R.drawable.user_icon;
                    VolleySingleton volleySingleton = VolleySingleton.getInstance(this);
                    volleySingleton.imageRequest(dataApplication.getProfilePicturePath(),
                            (ImageView) actionBar.getCustomView().findViewById(R.id.userIconId),
                            defaultIconId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.customActionBarReviewListLayoutId:
                ((TextView) actionBar.getCustomView().findViewById(R.id.cActBarTitleId))
                        .setVisibility(View.GONE);
                ((TextView) actionBar.getCustomView().findViewById(R.id.cActBarPeriodTextId))
                        .setText(((Bundle) data)
                                .getString(CoffeeMachineStatus.COFFEE_MACHINE_STATUS_STRING_KEY));
                ((TextView) actionBar.getCustomView().findViewById(R.id.cActBarCoffeeMachineNameId))
                        .setText(((Bundle) data)
                                .getString(CoffeeMachine.COFFEE_MACHINE_STRING_KEY));
                break;
            case R.id.customActionBarMapLayoutId:
                ((TextView) actionBar.getCustomView().findViewById(R.id.cActBarTitleId))
                        .setVisibility(View.VISIBLE);
                ((TextView) actionBar.getCustomView().findViewById(R.id.cActBarTitleId))
                        .setText("Politecnico di Torino");
                break;
            case R.id.customActionSettingsLayoutId:
                ((TextView) actionBar.getCustomView().findViewById(R.id.cActBarTitleId))
                        .setVisibility(View.VISIBLE);
                ((TextView) actionBar.getCustomView().findViewById(R.id.cActBarTitleId))
                        .setText("Settings");
                break;
            case R.id.customActionLoggedUserLayoutId:
                ((TextView) actionBar.getCustomView().findViewById(R.id.cActBarTitleId))
                        .setVisibility(View.VISIBLE);
                ((TextView) actionBar.getCustomView().findViewById(R.id.cActBarTitleId))
                        .setText("Account settings");
                break;


        }
    }

    @Override
    public void setCustomNavigation(Class<?> id) {
        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar == null) {
            return;
        }

        if(id.equals(CoffeeMachineFragment.class)) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            return;
        }
        actionBar.setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(CURRENT_FRAGMENT_TAG, getCurrentFragTag());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
//        currentFragTag = savedInstanceState.getString(CURRENT_FRAGMENT_TAG);
        pushCurrentFragTag(savedInstanceState.getString(CURRENT_FRAGMENT_TAG));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.customActionBarUserLayoutId:
                changeFragment(new LoggedUserFragment(), null, LoggedUserFragment.LOGGED_USER_FRAG_TAG);
                break;
        }

    }

    @Subscribe
    public void onHandlingError(Throwable cause) {
        String message = cause.getMessage();
        int code = Integer.parseInt(cause.getCause().getMessage());

        Log.e(TAG, "error - " + message + code);
        switch (code) {
            case 500:
                Toast.makeText(this.getApplicationContext(),
                        getResources().getString(R.string.HTTP_generic_error),
                        Toast.LENGTH_LONG).show();
                break;
            default:
                Toast.makeText(this.getApplicationContext(),
                        getResources().getString(R.string.generic_error),
                        Toast.LENGTH_LONG).show();
                break;
        }
        hideOnLoadView();
    }


}