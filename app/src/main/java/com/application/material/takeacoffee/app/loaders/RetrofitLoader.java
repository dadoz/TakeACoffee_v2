package com.application.material.takeacoffee.app.loaders;

import android.content.res.AssetManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import com.application.material.takeacoffee.app.models.CoffeeMachine;
import com.application.material.takeacoffee.app.models.Review;
import com.application.material.takeacoffee.app.models.User;
import com.application.material.takeacoffee.app.restServices.RetrofitServiceInterface;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import org.json.JSONArray;
import org.json.JSONObject;
import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/**
 * Created by davide on 15/09/14.
 */
public class RetrofitLoader extends AsyncTaskLoader<RestResponse> {
    private static final String TAG = "RESTLoader";
    private final HTTPActionRequestEnum mAction;
    private final RestAdapter restAdapter;
    private final RetrofitServiceInterface retrofitService;
    private final Object params;

    public RetrofitLoader(FragmentActivity activity, String action, Object params) {
        super(activity);
        Type typeOfListOfCategory = new com.google.gson.reflect.TypeToken<List<CoffeeMachine>>(){}.getType();
        Type typeOfListOfCategoryUser = new com.google.gson.reflect.TypeToken<List<User>>(){}.getType();


        mAction = HTTPActionRequestEnum.valueOf(action);
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(typeOfListOfCategory, new CustomDeserializer())
                .registerTypeAdapter(typeOfListOfCategoryUser, new CustomDeserializerUser())
                .create();

        restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://api.parse.com") // to baseUrl
//                .setEndpoint("https://192.168.130.112:8888") // to baseUrl
                .setRequestInterceptor(requestInterceptor)
                .setConverter(new GsonConverter(gson))
                .build();

        retrofitService = restAdapter.create(RetrofitServiceInterface.class);
        this.params = params;
    }

    @Override
    public RestResponse loadInBackground() {
        try{
            if (mAction == null) {
                Log.e(TAG, "You did not define an action. REST call canceled.");
                return null; //TODO HANDLE It
            }

            Object data = null;
            switch (mAction) {
                case COFFEE_MACHINE_REQUEST:
                    data = retrofitService.listCoffeeMachine();
                    break;
                case MORE_REVIEW_REQUEST:
                    User user = null;
                    data = retrofitService.listMoreReview(user);
                    break;
                case REVIEW_REQUEST:
                    data = retrofitService.listReview();
                    break;
                case REVIEW_COUNT_REQUEST:
                    data = retrofitService.mapReviewCount();
                    break;
                case ADD_REVIEW_BY_PARAMS:
                    Review review = (Review) params;
                    data = retrofitService.addReviewByParams(review);
                    break;
                case USER_REQUEST:
                    String [] array = {"4nmvMJNk1R","K8bwZOSmNo","8e2XwXZUKL"};
//                    ArrayList<String> userIdArrayLIst = new ArrayList<String>();
//                    userIdArrayLIst.addAll(Arrays.asList(array));
//                    JSONObject userIdList = new JSONObject().put("userIdList", new JSONArray(userIdArrayLIst));
                    UserParams userParams = new UserParams(array);
                    retrofitService.listUserByIdList(userParams);
                    break;
            }
            return new RestResponse(data, mAction); // We send a Response back
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    RequestInterceptor requestInterceptor = new RequestInterceptor() {
        @Override
        public void intercept(RequestFacade request) {
            request.addHeader("X-Parse-Application-Id", "61rFqlbDy0UWBfY56RcLdiJVB1EPe8ce1yUxdAEY");
            request.addHeader("X-Parse-REST-API-Key", "J37VkDdADU7jPfZSwLluAEixwJ3BmjPQJeuR1EzJ");
            request.addHeader("Content-Type", "application/json");
        }
    };

    public enum HTTPActionRequestEnum {
        ADD_REVIEW_REQUEST,
        REVIEW_REQUEST,
        MORE_REVIEW_REQUEST,
        USER_REQUEST,
        REVIEW_COUNT_REQUEST,
        ADD_REVIEW_BY_PARAMS,
        COFFEE_MACHINE_REQUEST
    }

//    public class UserCallback implements  Callback<List<User>>  {
//        @Override
//        public void success(List<User> s, Response response) {
//            Log.e(TAG, "success");
//        }
//
//        @Override
//        public void failure(RetrofitError retrofitError) {
//            Log.e(TAG, "success" + retrofitError);
//
//        }
//    }

    public static String getActionByActionRequestEnum(int ordinal) {
        try {
            return RetrofitLoader.HTTPActionRequestEnum.values()[ordinal].name();
        } catch (Exception e) {
            return null;
        }
    }

    public class ParseAction {
        public static final String CLASSES = "1/classes/";
        public static final String FUNCTIONS = "1/functions/";

        //action model
        public static final String COFFEE_MACHINE = "coffee_machines";
        public static final String REVIEW = "reviews";
        public static final String REVIEW_BY_TIMESTAMP_LIMIT = "getReviewByTimestampLimitOnResult";
        public static final String USER_BY_ID_LIST= "getUserListByUserIdList";
        public static final String MORE_REVIEW = "getMoreReview";
        public static final String REVIEW_COUNTER_TIMESTAMP = "countOnReviewsWithTimestamp";
    }

    //TODO TEST
    public static String getJSONDataMockup(FragmentActivity fragmentActivity, String filename) {
        AssetManager assetManager = fragmentActivity.getAssets();
        InputStream input;
        try {
            input = assetManager.open("data/" + filename);

            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();

            // byte buffer into a string
            return new String(buffer);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;

    }

    public class UserParams {
        List<String> userIdList;
        public UserParams(String [] array) {
//            userIdList = new ArrayList(Arrays.asList(array));
            userIdList = new ArrayList<String>();
            userIdList.add("4nmvMJNk1R");
            userIdList.add("K8bwZOSmNo");
            userIdList.add("8e2XwXZUKL");
        }
    }

    public static class CustomDeserializer implements JsonDeserializer<List<CoffeeMachine>> {

        @Override
        public List<CoffeeMachine> deserialize(JsonElement jsonElement, Type type,
                                  JsonDeserializationContext jsonDeserializationContext)
                throws JsonParseException {
//            Log.e(TAG, jsonElement.toString() + " - " + type.toString());
            Log.e(TAG, type.toString());
//            Class<? extends Type> classType = type.getClass();
            ArrayList<CoffeeMachine> coffeeMachineList = new ArrayList<CoffeeMachine>();
            JsonArray jsonArray = jsonElement.getAsJsonObject().get("results").getAsJsonArray();
            for(int i = 0; i < jsonArray.size(); i++) {
                coffeeMachineList.add(jsonDeserializationContext.<CoffeeMachine>deserialize(jsonArray.get(i), CoffeeMachine.class));
            }
            return coffeeMachineList;

        }
    }

    public static class CustomDeserializerUser implements JsonDeserializer<List<User>> {

        @Override
        public List<User> deserialize(JsonElement jsonElement, Type type,
                                               JsonDeserializationContext jsonDeserializationContext)
                throws JsonParseException {
//            Log.e(TAG, jsonElement.toString() + " - " + type.toString());
            Log.e(TAG, type.toString());
//            Class<? extends Type> classType = type.getClass();
            ArrayList<User> userList = new ArrayList<User>();
            JsonArray jsonArray = jsonElement.getAsJsonObject().get("result").getAsJsonArray();
            for(int i = 0; i < jsonArray.size(); i++) {
                userList.add(jsonDeserializationContext.<User>deserialize(jsonArray.get(i), User.class));
            }
            return userList;

        }
    }


}
