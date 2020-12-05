package edu.uw.group1app.ui.weather;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;


import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.function.IntFunction;

import edu.uw.group1app.R;

public class CurrentWeatherViewModel extends AndroidViewModel {

    private MutableLiveData<JSONObject> mCity;
    private MutableLiveData<JSONObject> mTemperature;
    private MutableLiveData<JSONObject> mStatus;

    public CurrentWeatherViewModel(@NonNull Application application) {
        super(application);
        mCity = new MutableLiveData<>();
        mCity.setValue(new JSONObject());
        mTemperature = new MutableLiveData<>();
        mTemperature.setValue(new JSONObject());
        mStatus = new MutableLiveData<JSONObject>();
        mStatus.setValue(new JSONObject());
    }

    public void addResponseObserver(@NonNull LifecycleOwner owner,
                                    @NonNull Observer<? super JSONObject> observer) {
        //mCity.observe(owner, observer);
        //mTemperature.observe(owner, observer);
        mStatus.observe(owner, observer);
    }

    private void handleError(final VolleyError error) {
        //you should add much better error handling in a production release.
        //i.e. YOUR PTOJECT
        Log.e("CONNECTION ERROR", error.getLocalizedMessage());

        throw new IllegalStateException(error.getMessage());
    }


    private void handleResult(final JSONArray result) {

        IntFunction<String> getString =
                getApplication().getResources()::getString;

        try {

            JSONObject data = result.getJSONObject(0);

            Log.d("WEATHER" , "" + data);

            mStatus.setValue(data);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
        }


    }

    public void connect(){
        String url = "https://mobileapp-group-backend.herokuapp.com/weather";

        Request request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                this::handleResult,
                this::handleError
        );

        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        Volley.newRequestQueue(getApplication().getApplicationContext())
                .add(request);
    }
}

