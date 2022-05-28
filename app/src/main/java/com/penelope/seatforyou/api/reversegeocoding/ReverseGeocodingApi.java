package com.penelope.seatforyou.api.reversegeocoding;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import javax.inject.Inject;

public class ReverseGeocodingApi {

    private static final String URL_FORMAT = "https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc?X-NCP-APIGW-API-KEY-ID={API_KEY_ID}&X-NCP-APIGW-API-KEY={API_KEY}&output=json&coords={LONGITUDE},{LATITUDE}";
    private static final String ARG_API_KEY_ID = "{API_KEY_ID}";
    private static final String ARG_API_KEY = "{API_KEY}";
    private static final String ARG_LONGITUDE = "{LONGITUDE}";
    private static final String ARG_LATITUDE = "{LATITUDE}";
    private static final String API_KEY_ID = "zswi02dkah";
    private static final String API_KEY = "fPUGgAzkLygXDfwxZKKkNaFz2umWxwM9xvnC4IDR";


    private final RequestQueue requestQueue;

    @Inject
    public ReverseGeocodingApi(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    public void get(double latitude, double longitude, OnSuccessListener<String> onSuccessListener, OnFailureListener onFailureListener) {

        // API 호출을 위한 URL 을 구성한다
        String url = URL_FORMAT
                .replace(ARG_API_KEY_ID, API_KEY_ID)
                .replace(ARG_API_KEY, API_KEY)
                .replace(ARG_LONGITUDE, String.valueOf(longitude))
                .replace(ARG_LATITUDE, String.valueOf(latitude));

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url,
                null,
                response -> {
                    try {
                        // result 배열
                        JSONArray results = response.getJSONArray("results");
                        if (results.length() == 0) {
                            onFailureListener.onFailure(new Exception("No reverse geocoding results"));
                            return;
                        }

                        // 첫 번째 result 를 취한다
                        JSONObject result = results.getJSONObject(0);
                        // result 로부터 주소명을 추출한다
                        JSONObject region = result.getJSONObject("region");
                        JSONObject area1 = region.getJSONObject("area1");
                        JSONObject area2 = region.getJSONObject("area2");
                        JSONObject area3 = region.getJSONObject("area3");
                        JSONObject area4 = region.has("area4") ? region.getJSONObject("area4") : null;
                        String name1 = area1.getString("name");
                        String name2 = area2.getString("name");
                        String name3 = area3.getString("name");
                        String name4 = area4 != null ? area4.getString("name") : null;

                        String address = String.format(Locale.getDefault(),
                                "%s %s %s", name1, name2, name3
                        );
                        if (name4 != null && !name4.isEmpty()) {
                            address = address + " " + name4;
                        }
                        onSuccessListener.onSuccess(address);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        onFailureListener.onFailure(e);
                    }
                },
                onFailureListener::onFailure);

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(jsonObjectRequest);
    }
}
