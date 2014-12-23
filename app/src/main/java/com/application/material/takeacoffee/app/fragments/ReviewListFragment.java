package com.application.material.takeacoffee.app.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.internal.widget.ListViewCompat;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.application.material.takeacoffee.app.AddReviewActivity;
import com.application.material.takeacoffee.app.CoffeeMachineActivity;
import com.application.material.takeacoffee.app.EditReviewActivity;
import com.application.material.takeacoffee.app.R;
import com.application.material.takeacoffee.app.adapters.ReviewListAdapter;
import com.application.material.takeacoffee.app.fragments.interfaces.OnChangeFragmentWrapperInterface;
import com.application.material.takeacoffee.app.fragments.interfaces.OnLoadViewHandlerInterface;
import com.application.material.takeacoffee.app.fragments.interfaces.SetActionBarInterface;
import com.application.material.takeacoffee.app.loaders.RestResponse;
import com.application.material.takeacoffee.app.loaders.RetrofitLoader;
import com.application.material.takeacoffee.app.models.*;
import com.application.material.takeacoffee.app.parsers.ParserToJavaObject;
import com.neopixl.pixlui.components.textview.TextView;
import com.shamanland.fab.ShowHideOnScroll;

import java.util.ArrayList;
import java.util.Iterator;

import static com.application.material.takeacoffee.app.loaders.RetrofitLoader.HTTPActionRequestEnum.COFFEE_MACHINE_STATUS_REQUEST;
import static com.application.material.takeacoffee.app.models.Review.REVIEW_KEY;
import static com.application.material.takeacoffee.app.loaders.RetrofitLoader.HTTPActionRequestEnum.*;


/**
 * Created by davide on 08/04/14.
 */
public class ReviewListFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<RestResponse>,
        AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener,
        DialogInterface.OnClickListener, View.OnClickListener,
        OnRefreshListener, AbsListView.OnScrollListener {
    private static final String TAG = "ReviewListFragment";
    public static final String REVIEW_LIST_FRAG_TAG = "REVIEW_LIST_FRAG_TAG";
    private static FragmentActivity mainActivityRef = null;
    public static String EDIT_REVIEW_STRING = "Edit";

    private View reviewListView;
    private String coffeeMachineId;
    private Bundle bundle;
    private Bundle bundle2;
    private CoffeeMachineStatus coffeeMachineStatus;
    private String meUserId = "4nmvMJNk1R";

    @InjectView(R.id.reviewsContainerListViewId) ListView listView;
    @InjectView(R.id.addReviewFabId) View addReviewFabButton;
    @InjectView(R.id.goodReviewPercentageTextId) TextView goodReviewPercentageView;
    @InjectView(R.id.statusCoffeeIconId) ImageView statusCoffeeIcon;
    @InjectView(R.id.leftArrowIconId) ImageView leftArrowIcon;
    @InjectView(R.id.swipeRefreshLayoutId) SwipeRefreshLayout swipeRefreshLayout;
    @InjectView(R.id.dashboardStatusLayoutId) View dashboardStatusLayout;

//    @InjectView(R.id.moreReviewTemplateId) View moreReviewTemplate;

    private View moreReviewLoaderView;
    private View emptyView;
    private AlertDialog customDialog;
    private View addReviewDialogTemplate;
    private ArrayList<Review> reviewList;
    private ArrayList<User> userList;
    private CoffeeMachine coffeeMachine;
    private boolean hasMoreReviews;
    private View oldReviewView;
    private boolean isAllowToEdit = false;
    private ArrayList<Review> prevReviewList;
    private View headerView;
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
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        reviewListView = inflater.inflate(R.layout.fragment_review_list, container, false);
        ButterKnife.inject(this, reviewListView);
        emptyView = inflater.inflate(R.layout.empty_data_status_layout, listView, false);
        headerView = inflater.inflate(R.layout.header_listview_template, listView, false);
        oldReviewView = headerView.findViewById(R.id.oldReviewTemplateId);
        moreReviewLoaderView = headerView.findViewById(R.id.moreReviewTemplateId);

        bundle = getArguments();
        bundle2 = new Bundle(); //TODO please implement parcelable in coffeeMachine
        coffeeMachineId = bundle.getString(CoffeeMachine.COFFEE_MACHINE_ID_KEY); //TODO fix it this crash app
        coffeeMachine = bundle.getParcelable(CoffeeMachine.COFFEE_MACHINE_OBJ_KEY);
//        reviewStatus = ReviewStatus.ReviewStatusEnum.valueOf(bundle
//                .getString(ReviewStatus.REVIEW_STATUS_KEY));
        restoreSavedInstance(savedInstance);
        setHasOptionsMenu(true);
        initOnLoadView();

        return reviewListView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstance) {
        super.onActivityCreated(savedInstance);
    }


    private void initOnLoadView() {
        ((OnLoadViewHandlerInterface) mainActivityRef).initOnLoadView();
        Log.w(TAG, "call initLoader getCoffeeMachineStatus");

        if(reviewList != null) {
            initView();
            return;
        }

        getLoaderManager().initLoader(COFFEE_MACHINE_STATUS_REQUEST.ordinal(), null, this)
                .forceLoad();

        //TODO REFACTORING
/*        long fromTimestamp = bundle.getLong(Common.FROM_TIMESTAMP_KEY);
        long toTimestamp = bundle.getLong(Common.TO_TIMESTAMP_KEY);
        Bundle params = RestResponse.createBundleReview(coffeeMachineId, fromTimestamp, toTimestamp);
        if (getLoaderManager().getLoader(RestLoader.HTTPVerb.POST) == null) {
            getLoaderManager().initLoader(RestLoader.HTTPVerb.POST, params, this)
                    .forceLoad();
        } else {
            initView(reviewListDataStorage, coffeeMachineId);
        }*/

    }

    public void initView() {
        ((OnLoadViewHandlerInterface) mainActivityRef).hideOnLoadView();

        if(coffeeMachineStatus != null) {
            goodReviewPercentageView.setText(coffeeMachineStatus.getGoodReviewPercentage() + " %");
//            statusCoffeeIcon.setImageDrawable();
        }
        //action bar
        setActionBarData();

        if (reviewList == null) {
            Log.w(TAG, "empty review list");
            listView.setEmptyView(emptyView);
            return;
        }

        leftArrowIcon.setOnClickListener(this);

        ReviewListAdapter reviewListenerAdapter = new ReviewListAdapter(mainActivityRef,
                R.layout.review_template, reviewList, coffeeMachineId);

        listView.addHeaderView(headerView);
        setHasMoreReviewView();
        listView.setAdapter(reviewListenerAdapter);

        listView.setOnItemLongClickListener(this);
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(this);
        listView.setOnTouchListener(new ShowHideOnScroll(addReviewFabButton));
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setProgressViewOffset(true, 100, 200); //TODO replace please with dimen size
        addReviewFabButton.setOnClickListener(this);
    }

    private void setActionBarData() {
        Bundle actionbarBundle = new Bundle();
        actionbarBundle.putString(CoffeeMachineStatus.COFFEE_MACHINE_STATUS_STRING_KEY,
                "01.12 - today");
        actionbarBundle.putString(CoffeeMachine.COFFEE_MACHINE_STRING_KEY,
                coffeeMachine.getName());
        ((SetActionBarInterface) mainActivityRef)
                .setActionBarCustomViewById(R.id.customActionBarReviewListLayoutId,
                        actionbarBundle);
        ((SetActionBarInterface) mainActivityRef)
                .setCustomNavigation(ReviewListFragment.class);
    }

    private ReviewListAdapter getAdapterWrapper() {
        return listView.getAdapter().getClass() == ReviewListAdapter.class ?
                ((ReviewListAdapter) listView.getAdapter()) :
                ((ReviewListAdapter) ((HeaderViewListAdapter) listView.getAdapter())
                    .getWrappedAdapter());
    }

    public ListView getListView() {
        return listView;
    }

    @Override
    public Loader<RestResponse> onCreateLoader(int id, Bundle params) {
        try {
            String action = RetrofitLoader.getActionByActionRequestEnum(id);
            return new RetrofitLoader(this.getActivity(), action, params);
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<RestResponse> loader,
                               RestResponse restResponse) {
        //TODO REFACTORIZE IT - add a new class with an interface
        //TODO FIX IT
        final int REVIEW_REQ = 0; //REVIEW_REQUEST.ordinal();
        final int MORE_REVIEW_REQ = 1; //MORE_REVIEW_REQUEST.ordinal();
        final int USER_REQ = 6; //USER_REQUEST.ordinal();
        final int COFFEE_MACHINE_STATUS_REQ = 5; //.ordinal();
        Log.i(TAG, "request id - " + loader.getId());
        try {
            switch (loader.getId()) {
                case COFFEE_MACHINE_STATUS_REQ:
                    Log.i(TAG, "STATUS_REQ");

                    ((OnLoadViewHandlerInterface) mainActivityRef).hideOnLoadView();
//                    reviewStatus = (ReviewStatus) restResponse.getParsedData();

                    //MOCKUP
                    String filename = "review_status.json";
                    String data = RetrofitLoader.getJSONDataMockup(this.getActivity(), filename);
                    coffeeMachineStatus = ParserToJavaObject.coffeeMachineStatusParser(data);

                    //real json data
//                    coffeeMachineStatus = (CoffeeMachineStatus) restResponse.getParsedData();

                    if(getLoaderManager().getLoader(REVIEW_REQUEST.ordinal()) != null) {
                        getLoaderManager().restartLoader(REVIEW_REQUEST.ordinal(), null, this).forceLoad();
                        goodReviewPercentageView.setText(coffeeMachineStatus.getGoodReviewPercentage() + " %");
                        return;
                    }

                    getLoaderManager().initLoader(REVIEW_REQUEST.ordinal(), null, this)
                            .forceLoad();
                    break;
                case REVIEW_REQ:
                    Log.i(TAG, "REVIEW_REQ");

                    ((OnLoadViewHandlerInterface) mainActivityRef).hideOnLoadView();

                    filename = "reviews.json";
                    data = RetrofitLoader.getJSONDataMockup(this.getActivity(), filename);
                    ReviewDataContainer reviewDataContainer = ParserToJavaObject.getReviewListParser(data);
                    //real json data
//                    reviewList = (ArrayList<Review>) restResponse.getParsedData();

                    reviewList = reviewDataContainer.getReviewList();
                    hasMoreReviews = reviewDataContainer.getHasMoreReviews();

                    Bundle params = new Bundle();
                    if(getLoaderManager().getLoader(USER_REQUEST.ordinal()) != null) {
                        setHasMoreReviewView();
                        getLoaderManager().restartLoader(USER_REQUEST.ordinal(), params, this).forceLoad();

                        getAdapterWrapper().setReviewList(reviewList);
                        getAdapterWrapper().notifyDataSetChanged();
                        return;
                    }

                    getLoaderManager().initLoader(USER_REQUEST.ordinal(), params, this).forceLoad();
                    initView();
                    break;
                case MORE_REVIEW_REQ:
                    Log.i(TAG, "MORE_REVIEW_REQ");
                    swipeRefreshLayout.setRefreshing(false);

                    filename = "prev_reviews.json";
                    data = RetrofitLoader.getJSONDataMockup(this.getActivity(), filename);
                    reviewDataContainer = ParserToJavaObject.getReviewListParser(data);
                    prevReviewList = reviewDataContainer.getReviewList();
                    hasMoreReviews = reviewDataContainer.getHasMoreReviews();
                    setHasMoreReviewView();
//                    prevReviewList = (ArrayList<Review>) restResponse.getParsedData();

                    //TODO add review to adapter
                    getAdapterWrapper().setPrevReview(prevReviewList);
                    //create new loader for user
                    params = new Bundle();
                    if(getLoaderManager().getLoader(USER_REQUEST.ordinal()) != null) {
                        getLoaderManager().restartLoader(USER_REQUEST.ordinal(), params, this).forceLoad();
                        getAdapterWrapper().notifyDataSetChanged();
                        return;
                    }

                    getLoaderManager().initLoader(USER_REQUEST.ordinal(), params, this).forceLoad();
                    break;
                case USER_REQ:
                    Log.i(TAG, "USER_REQ");

                    //TODO TEST
                    filename = "user.json";
                    data = RetrofitLoader.getJSONDataMockup(this.getActivity(), filename);
                    userList = ParserToJavaObject.getUserListParser(data);

//                    ArrayList<User> userList = (ArrayList<User>) restResponse.getParsedData();
                    getAdapterWrapper().setUserList(userList);
                    getAdapterWrapper().notifyDataSetChanged();
//                    swipeRefreshLayout.setRefreshing(false);
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setHasMoreReviewView() {
        if(hasMoreReviews) {
            oldReviewView.setVisibility(View.GONE);
            moreReviewLoaderView.setVisibility(View.VISIBLE);
            return;
        }
        moreReviewLoaderView.setVisibility(View.GONE);
        oldReviewView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<RestResponse> restResponseLoader) {
        //called on release resources - on back press and when loader is deleted/abandoned
        Log.e(TAG, "reset loader");
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        //get more review
        switch (view.getId()) {
            case R.id.headerTemplateId :
                if(view.findViewById(R.id.moreReviewTemplateId).getVisibility() == View.VISIBLE) {
                    Log.e(TAG, "get previous review");
                    swipeRefreshLayout.setRefreshing(true); //show dialog spinner (but doesnt refresh I hope)

                    if(getLoaderManager().getLoader(MORE_REVIEW_REQUEST.ordinal()) != null) {
                        getLoaderManager().restartLoader(MORE_REVIEW_REQUEST.ordinal(), null, this)
                                .forceLoad();
                        return;
                    }
                    getLoaderManager().initLoader(MORE_REVIEW_REQUEST.ordinal(), null, this)
                            .forceLoad();
                    return;
                }

                if(view.findViewById(R.id.oldReviewTemplateId).getVisibility() == View.VISIBLE) {
                    Log.e(TAG, "get old review");
                    Toast.makeText(mainActivityRef, "load old review", Toast.LENGTH_SHORT).show();
                }
                break;
        }


        //expand listview
//        View reviewDialogView = View.inflate(mainActivityRef,
//                R.layout.review_dialog_template, null);
//        Review review = (Review) adapterView.getItemAtPosition(position);
//        User user = getAdapterWrapper().getUserByUserId(review.getUserId());
//
//        ((TextView) reviewDialogView
//                .findViewById(R.id.reviewUsernameDialogId)).setText(user.getUsername());
//        ((TextView) reviewDialogView
//                .findViewById(R.id.reviewDialogCommentTextId)).setText(review.getComment());
//
//        View doneDialogButton = reviewDialogView
//                .findViewById(R.id.doneDialogIconId);
//
//        doneDialogButton.setOnClickListener(this);
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivityRef)
//                .setView(reviewDialogView);
//        customDialog = builder.create();
//        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        customDialog.show();

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        if(view.getId() == R.id.headerTemplateId) {
            Log.e(TAG, "moreReviewTemplateId");
            return true;
        }
        try {
            Review review = (Review) adapterView.getItemAtPosition(position);
            User user = getAdapterWrapper().getUserByUserId(review.getUserId());

            //clear prev bundle
            bundle2.putParcelable(Review.REVIEW_OBJ_KEY, null);
            bundle2.putParcelable(User.USER_OBJ_KEY, null);
            isAllowToEdit = false;
            //set
            if (user != null &&
                    user.getId().equals(meUserId)) {
                bundle2.putParcelable(Review.REVIEW_OBJ_KEY, review);
                bundle2.putParcelable(User.USER_OBJ_KEY, user);
                isAllowToEdit = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //disable onItemLongClick (only one edit per time)
        ((SetActionBarInterface) mainActivityRef)
                .updateSelectedItem(this, listView, view, position);
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        Log.e(TAG, "create option menu");
        if(((SetActionBarInterface) mainActivityRef).isItemSelected()) {
            menuInflater.inflate(isAllowToEdit ? R.menu.edit_review : R.menu.clipboard_review,
                    menu);
            return;
        }
        menuInflater.inflate(R.menu.review_list, menu);
//        listView.setOnItemLongClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_coffee_machine_position:
                    Toast.makeText(mainActivityRef, "get machine pos", Toast.LENGTH_SHORT).show();
                ((OnChangeFragmentWrapperInterface) mainActivityRef)
                        .changeFragment(new MapFragment(),
                                bundle, MapFragment.MAP_FRAG_TAG);
                break;

            case R.id.action_edit_icon:
                Toast.makeText(mainActivityRef, "change", Toast.LENGTH_SHORT).show();
                ((OnChangeFragmentWrapperInterface) mainActivityRef)
                        .startActivityWrapper(EditReviewActivity.class,
                                CoffeeMachineActivity.ACTION_EDIT_REVIEW, bundle2);
                //deselect Item
                ((SetActionBarInterface) mainActivityRef)
                        .updateSelectedItem(this, listView, null, -1);
                break;
            case R.id.action_delete:
                Toast.makeText(mainActivityRef, "change", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(mainActivityRef)
                        .setMessage("Sure to delete this review?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
//                                getLoaderManager().initLoader(DELETE_REVIEW.ordinal(), null, this)
//                                        .forceLoad();
                                Review review = (Review) bundle2.get(Review.REVIEW_OBJ_KEY);
                                getAdapterWrapper().deleteReview(review.getId());
                                getAdapterWrapper().notifyDataSetChanged();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                Dialog dialog = builder.create();
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.show();


                //deselect Item
                ((SetActionBarInterface) mainActivityRef)
                        .updateSelectedItem(this, listView, null, -1);
                break;

        }
        return true;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        Log.e(TAG, "dismiss");
    }

    @Override
    public void onClick(View v) {
        //dialog button
        switch (v.getId()) {
            case R.id.doneDialogIconId:
                customDialog.dismiss();
                break;
            case R.id.addReviewFabId:
                ((OnChangeFragmentWrapperInterface) mainActivityRef)
                        .startActivityWrapper(AddReviewActivity.class,
                                CoffeeMachineActivity.ACTION_ADD_REVIEW, null);
                break;
            case R.id.leftArrowIconId:
                Toast.makeText(mainActivityRef, "got statistics on machine",
                        Toast.LENGTH_SHORT).show();
//                ((OnChangeFragmentWrapperInterface) mainActivityRef)
//                        .startActivityWrapper(AddReviewActivity.class,
//                                CoffeeMachineActivity.ACTION_ADD_REVIEW, null);
                break;

        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstance) {
        super.onSaveInstanceState(savedInstance);

        //Status
        savedInstance.putParcelable(CoffeeMachineStatus.COFFEE_MACHINE_STATUS, coffeeMachineStatus);

        //review
        if(reviewList == null) {
            return;
        }

        Parcelable[] parcelableArray = new Parcelable[reviewList.size()];
        Iterator iterator = reviewList.iterator();
        int i = 0;
        while (iterator.hasNext()) {
                parcelableArray[i] = (Parcelable) iterator.next();
            i ++;
        }
        savedInstance.putParcelableArray(REVIEW_KEY, parcelableArray);

        //user
        if(userList == null) {
            return;
        }

        parcelableArray = new Parcelable[userList.size()];
        iterator = userList.iterator();
        i = 0;
        while (iterator.hasNext()) {
            parcelableArray[i] = (Parcelable) iterator.next();
            i ++;
        }
        savedInstance.putParcelableArray(User.USER_OBJ_KEY, parcelableArray);

    }

    private boolean restoreSavedInstance(Bundle savedInstance) {
        if(savedInstance == null ||
                savedInstance.getParcelableArray(REVIEW_KEY) == null) {
            return false;
        }

        //restore data from savedInstance
        reviewList = new ArrayList<Review>();
        Parcelable[] temp = savedInstance.getParcelableArray(REVIEW_KEY);
        for(int i = 0; i < temp.length; i ++) {
            reviewList.add((Review) temp[i]);
        }

        userList = new ArrayList<User>();
        temp = savedInstance.getParcelableArray(User.USER_OBJ_KEY);
        for(int i = 0; i < temp.length; i ++) {
            userList.add((User) temp[i]);
        }

        return true;
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(false);
        Toast.makeText(mainActivityRef, "refresh", Toast.LENGTH_SHORT).show();
        getLoaderManager().restartLoader(COFFEE_MACHINE_STATUS_REQUEST.ordinal(), null, this)
                .forceLoad();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        try {
            if(firstVisibleItem == 0) {
//                Animation jumpUp = AnimationUtils.loadAnimation(mainActivityRef, R.anim.slide_up);
//                Animation jumpDown =AnimationUtils.loadAnimation(mainActivityRef, R.anim.slide_down);
                if(view.getChildAt(firstVisibleItem) == null) {
                    return;
                }
                int viewHeight = view.getChildAt(firstVisibleItem).getHeight();
                int offset = viewHeight * 30 / 100;

                //hide and show element
                if(Math.abs(view.getChildAt(firstVisibleItem).getY()) > offset) {
                    dashboardStatusLayout.setVisibility(View.GONE);
                    return;
                }
                dashboardStatusLayout.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



/*
    private void moreReviewResponse(RestResponse restResponse) {
        if(! restResponse.getHasMoreReviews()) {
            listView.removeHeaderView(moreReviewLoaderView);
        }

        String filename = "reviews.json";
        String data = RestResponse.getJSONDataMockup(this.getActivity(), filename);
        ArrayList<Review> reviewList = restResponse.getReviewListParser(data);

        Collections.reverse(reviewList);

        reviewListDataStorage.addAll(0, reviewList);

        ArrayList<String> userIdList = new ArrayList<>();
        for (Review review : reviewList) {
            userIdList.add(review.getUserId());
        }
        Bundle bundle = RestResponse.createBundleUser(userIdList);
        Log.d(TAG, "hey " + bundle.getString("requestType"));
        getLoaderManager().restartLoader(RestLoader.HTTPVerb.POST, bundle, this).forceLoad();

        if (listView.getAdapter() != null) {
            try {
                ((ReviewListAdapter) ((WrapperListAdapter) listView.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();
            } catch (Exception e) {
                ((ReviewListAdapter) listView.getAdapter()).notifyDataSetChanged();
            }
        }

    }

    private void userResponse(RestResponse restResponse) {
        ArrayList<User> userList = restResponse.getUserListParser();
        //TODO move in coffeeAppLogic
        coffeeAppController.addUserOnLocalListByList(userList);
        if (listView.getAdapter() != null) {
            try {
                ((ReviewListAdapter) ((WrapperListAdapter) listView.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();
            } catch (Exception e) {
                ((ReviewListAdapter) listView.getAdapter()).notifyDataSetChanged();
            }
        }
    }

    private void reviewResponse(RestResponse restResponse) {
        if(restResponse.getHasMoreReviews()) {
            listView.addHeaderView(moreReviewLoaderView);
        }


        String filename = "reviews.json";
        String data = RestResponse.getJSONDataMockup(this.getActivity(), filename);

        ArrayList<Review> reviewList = RestResponse.getReviewListParser(data);

        reviewListDataStorage = reviewList;

        ArrayList<String> userIdList = new ArrayList<>();
        for (Review review : reviewList) {
            userIdList.add(review.getUserId());
        }
        Bundle bundle = RestResponse.createBundleUser(userIdList);
        Log.d(TAG, "hey " + bundle.getString("requestType"));
        getLoaderManager().restartLoader(RestLoader.HTTPVerb.POST, bundle, this).forceLoad();

        initView(reviewList, coffeeMachineId);
    }









    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        //USEFUL
        Log.e(TAG, "on item click " + position + "id - " + view.getId());
        if(view.getId() == R.id.linearLayout) {
            Log.e(TAG, " id: linearLayout");
        }
        //more review click
        if(view.getId() == R.id.loadOlderReviewLayoutId) {
            try{
                Review firstReview = reviewListDataStorage.get(0);
                String latestReviewId = firstReview.getId();
                DateTime dateTime = new DateTime(firstReview.getTimestamp());
                long fromTimestamp = TimestampHandler.getOneWeekAgoTimestamp(dateTime);
                Bundle bundle = RestResponse.createBundleMoreReview(coffeeMachineId, latestReviewId, fromTimestamp);
                Log.d(TAG, "hey " + bundle.getString("requestType"));
                getLoaderManager().restartLoader(RestLoader.HTTPVerb.POST, bundle, this).forceLoad();
            } catch (Exception e) {
                Log.e(TAG, "failed to load more review");
            }
        }
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
        if(view.getId() == R.id.loadOlderReviewLayoutId) {
            return true;
        }

        Review reviewObj = (Review) adapterView.getItemAtPosition(position);
        final View mainItemView = view.findViewById(R.id.mainItemViewId);

        //TODO CHECK THIS STATEMENT
        if (mainItemView != null &&
                mainItemView.getVisibility() == View.VISIBLE &&
                coffeeAppController.checkIsMe(reviewObj.getUserId())) {
            try {
                final View extraMenuItemView = view.findViewById(R.id.extraMenuItemViewId);
                ReviewListAdapter adapter = ((ReviewListAdapter) adapterView.getAdapter());

                mainItemView.setVisibility(View.GONE);
                extraMenuItemView.setVisibility(View.VISIBLE);
                setReviewListHeaderBackgroundLabel(extraMenuItemView, false);

                int prevSelectedItemPosition = adapter
                        .getSelectedItemIndex();

                //DESELECT prev item
                if (prevSelectedItemPosition != Common.ITEM_NOT_SELECTED) {
                    int index = prevSelectedItemPosition - adapterView.getFirstVisiblePosition();
                    View v = adapterView.getChildAt(index);
                    v.findViewById(R.id.mainItemViewId).setVisibility(View.VISIBLE);
                    v.findViewById(R.id.extraMenuItemViewId).setVisibility(View.GONE);
                }

                adapter.setSelectedItemIndex(position);
                adapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }*/


}

