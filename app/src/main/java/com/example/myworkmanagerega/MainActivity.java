package com.example.myworkmanagerega;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnOneTimeTask, btnPeriodicTask, btnCancelTask;
    EditText editCity;
    TextView tvStatus;

    private PeriodicWorkRequest periodicWorkRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnOneTimeTask = findViewById(R.id.btn_one_task_time);
        btnPeriodicTask = findViewById(R.id.btn_run_periodic_task_time);
        btnCancelTask = findViewById(R.id.btn_cancel_periodic_task);
        editCity = findViewById(R.id.edt_city_name);
        tvStatus = findViewById(R.id.tv_status);

        btnOneTimeTask.setOnClickListener(this);
        btnPeriodicTask.setOnClickListener(this);
        btnCancelTask.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_one_task_time:
                startOneTaskTime();
                break;
            case R.id.btn_run_periodic_task_time:
                startPeridicTask();
                break;
            case R.id.btn_cancel_periodic_task:
                cancelPeridicTask();
                break;
        }
    }

    private void startOneTaskTime(){
        tvStatus.setText(getString(R.string.status));

        Data data = new Data.Builder()
                .putString(MyWorker.EXTRA_CITY, editCity.getText().toString())
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(MyWorker.class)
                .setInputData(data)
                .setInitialDelay(5, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance()
//                .beginWith( //OneTimeWorkRequest)     //Metode eksekusi Chaining (berantai)
//                .then(work b)
//                .then(work c)
                .enqueue(oneTimeWorkRequest);

        WorkManager.getInstance().getWorkInfoByIdLiveData(oneTimeWorkRequest.getId())
                .observe(MainActivity.this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                String status = workInfo.getState().name();
                tvStatus.append("\n"+status);

                if (workInfo.getState()== WorkInfo.State.SUCCEEDED){
                    startPeridicTask();
                    Toast.makeText(MainActivity.this, "periodic mulai", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void startPeridicTask(){
        tvStatus.setText(getString(R.string.status));

        Data data = new Data.Builder()
                .putString(MyWorker.EXTRA_CITY, editCity.getText().toString())
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        periodicWorkRequest = new PeriodicWorkRequest.Builder(MyWorker.class, 15, TimeUnit.MINUTES)
                .setInputData(data)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance().enqueue(periodicWorkRequest);
        WorkManager.getInstance().getWorkInfoByIdLiveData(periodicWorkRequest.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        String status = workInfo.getState().name();
                        tvStatus.append("\n" +status);
                        btnCancelTask.setEnabled(false);
                        if (workInfo.getState()== WorkInfo.State.ENQUEUED){
                            btnCancelTask.setEnabled(true);
                        }
                    }
                });
    }

    private void cancelPeridicTask(){
        WorkManager.getInstance().cancelWorkById(periodicWorkRequest.getId());

    }
}
