package com.application.material.takeacoffee.app.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.application.material.takeacoffee.app.CoffeeMachineActivity;
import com.application.material.takeacoffee.app.R;
import com.application.material.takeacoffee.app.Utils;
import com.application.material.takeacoffee.app.VolleyImageRequestWrapper;
import com.application.material.takeacoffee.app.application.DataApplication;
import com.application.material.takeacoffee.app.fragments.interfaces.OnChangeFragmentWrapperInterface;
import com.application.material.takeacoffee.app.fragments.interfaces.OnLoadViewHandlerInterface;
import com.application.material.takeacoffee.app.fragments.interfaces.SetActionBarInterface;
import com.application.material.takeacoffee.app.models.User;
import com.application.material.takeacoffee.app.services.HttpIntentService;
import com.application.material.takeacoffee.app.singletons.BusSingleton;
import com.squareup.otto.Subscribe;

/**
 * Created by davide on 27/12/14.
 */
public class LoggedUserFragment extends Fragment
        implements View.OnClickListener {

    public static String LOGGED_USER_FRAG_TAG = "LOGGED_USER_FRAG_TAG";

    private static final String TAG = "LoggedUserFragment";
    private static FragmentActivity mainActivityRef = null;

    private View settingListView;
    private String coffeeMachineId;
    private Bundle bundle;
    @InjectView(R.id.loginUsernameEditId)
    EditText loginUsernameEdit;
    @InjectView(R.id.deleteUserButtonId)
    LinearLayout deleteUserButton;
    @InjectView(R.id.profilePictureViewId)
    ImageView profilePictureView;

    private DataApplication dataApplication;
    private String meUserId;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof OnLoadViewHandlerInterface)) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnLoadViewHandlerInterface");
        }
        if (!(activity instanceof OnChangeFragmentWrapperInterface)) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnLoadViewHandlerInterface");
        }
        mainActivityRef =  (CoffeeMachineActivity) activity;
        dataApplication = ((DataApplication) mainActivityRef.getApplication());
        meUserId = dataApplication.getUserId();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        settingListView = inflater.inflate(R.layout.fragment_logged_user, container, false);
        ButterKnife.inject(this, settingListView);
        setHasOptionsMenu(true);
        initOnLoadView();
        return settingListView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstance) {
        super.onActivityCreated(savedInstance);
        //get all bundle
        bundle = getArguments();
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

    private void initOnLoadView() {
        initView();
    }

    public void initView() {
        ((OnLoadViewHandlerInterface) mainActivityRef).hideOnLoadView(null);

        //action bar
        ((SetActionBarInterface) mainActivityRef)
                .setActionBarCustomViewById(R.id.customActionLoggedUserLayoutId, null);
        ((SetActionBarInterface) mainActivityRef)
                .setCustomNavigation(LoggedUserFragment.class);

        try {
            int defaultIconId = R.id.userIconId;
            ((VolleyImageRequestWrapper) mainActivityRef).volleyImageRequest(
                    dataApplication.getProfilePicturePath(), profilePictureView,
                    defaultIconId);

        } catch (Exception e) {
            e.printStackTrace();
        }

        loginUsernameEdit.setText(dataApplication.getUsername());
        deleteUserButton.setVisibility(View.VISIBLE);
        deleteUserButton.setOnClickListener(this);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.logged_user, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_logged_user:
//                save user
                boolean isUserUpdated = updateUser();
                if(! isUserUpdated) {
                    Log.e(TAG, "error on save user");
                    break;
                }

                HttpIntentService.updateUserRequest(mainActivityRef, dataApplication.getUser());
                //TODO on callback
                mainActivityRef.onBackPressed();
                break;
        }
        return true;
    }

    private boolean updateUser() {
        String username = loginUsernameEdit.getText().toString();
        if(username.compareTo("") == 0) {
            return false;
        }

        Utils.hideKeyboard(mainActivityRef, loginUsernameEdit);

        dataApplication.setUsername(username);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.deleteUserButtonId:
//                HttpIntentService.deleteUserRequest(mainActivityRef, meUserId);
                break;
        }
    }

    @Subscribe
    public void onNetworkRespose(User.DeletedResponse deleteUserResponse) {
        Log.d(TAG, "get response from bus - DELETE_REVIEW_REQ");
        ((OnLoadViewHandlerInterface) mainActivityRef).hideOnLoadView(null);

        if(deleteUserResponse == null) {
            //TODO handle adapter with empty data
            return;
        }
        //TODO handle this;

    }

}
