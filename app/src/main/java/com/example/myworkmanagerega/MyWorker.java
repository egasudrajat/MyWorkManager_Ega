package com.example.myworkmanagerega;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONObject;

import java.text.DecimalFormat;

import cz.msebera.android.httpclient.Header;

public class MyWorker extends Worker {
    final String APP_ID = "ace702e642a032fc75f35553f8108e9d";
    public static final String EXTRA_CITY = "Majalengka";
    private static final String TAG = MyWorker.class.getSimpleName();
    private Result resultStatus;


    public MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Result status = null;
        String dataCity = getInputData().getString(EXTRA_CITY);
        if (!TextUtils.isEmpty(dataCity)){
             status = getCurrenWeather(dataCity);
        }
        return status;
    }

    private Result getCurrenWeather(final String city) {
        String url = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + APP_ID;
        Log.d(TAG, "getCurrenWeather: Mulai");

        SyncHttpClient httpClient = new SyncHttpClient();
        httpClient.post(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                Log.d(TAG, "onSuccess: " + result);

                try {
                    JSONObject responseObject = new JSONObject(result);
                    String currentWeather = responseObject.getJSONArray("weather").getJSONObject(0).getString("main");
                    String description = responseObject.getJSONArray("weather").getJSONObject(0).getString("description");
                    Double tempInKelvin = responseObject.getJSONObject("main").getDouble("temp");

                    Double tempInCelcius = tempInKelvin - 273;
                    String temperature = new DecimalFormat("##.##").format(tempInCelcius);

                    String title = "Current Weather "+city;
                    String message = currentWeather + ", " + description + " with" + temperature + " Celcius";

                    showNotification(title, message);
                    Log.d(TAG, "onSuccess: Selesai");

                    resultStatus = Result.success();


                } catch (Exception e) {
                    showNotification("get current weather gagal",""+e);
                    Log.d(TAG, "onSuccess: Gagal");
                    resultStatus = Result.failure();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                showNotification("get current weather gagal", error.getMessage());
                Log.d(TAG, "onSuccess: Gagal");
                resultStatus = Result.failure();
            }
        });

        return resultStatus;
    }

    private static final int NOTIFICATION_ID = 1;
    private static final String  CHANNEL_ID = "channel_01";
    private static final String CHANNEL_NAME = "mychannel";

    private void showNotification(String title, String description){
        NotificationManager notificationManager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_android_black_24dp)
                .setContentTitle(title)
                .setContentText(description)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent))
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            notification.setChannelId(CHANNEL_ID);
            if (notificationManager != null){
            notificationManager.createNotificationChannel(channel);
            }
        }

        if (notificationManager != null){
            notificationManager.notify(NOTIFICATION_ID, notification.build());
        }
    }
}
