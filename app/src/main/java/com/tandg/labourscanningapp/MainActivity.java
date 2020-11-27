package com.tandg.labourscanningapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Parcelable;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tandg.labourscanningapp.webservice.SpreadsheetWebService;

import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    private long timeCountInMilliSeconds = 59 * 60000;

    int endTime = 250;
    int progress;

    private enum TimerStatus {
        STARTED,
        STOPPED
    }

    String tagID, tagIDMain;

    TextView txtTagName, txtTimer;
    private TimerStatus timerStatus = TimerStatus.STOPPED;
    private CountDownTimer countDownTimer;

    private ProgressBar progressBarCircle;
    private TextView textViewTime,txtTimerNew;

    Handler handler;
    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L ;
    int Seconds, Minutes, MilliSeconds, Hours ;

    String house , row;
    char number;
    NfcAdapter mAdapter;
    PendingIntent mPendingIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mAdapter == null) {
            //nfc not support your device.
            return;
        }
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        initResources();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        GetDataFromTag(tag, intent);

    }

    private void GetDataFromTag(Tag tag, Intent intent) {
        Ndef ndef = Ndef.get(tag);
        try {
            ndef.connect();
//            txtType.setText(ndef.getType().toString());
//            txtSize.setText(String.valueOf(ndef.getMaxSize()));
//            txtWrite.setText(ndef.isWritable() ? "True" : "False");
            Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if (messages != null) {
                NdefMessage[] ndefMessages = new NdefMessage[messages.length];
                for (int i = 0; i < messages.length; i++) {
                    ndefMessages[i] = (NdefMessage) messages[i];
                }
                NdefRecord record = ndefMessages[0].getRecords()[0];

                byte[] payload = record.getPayload();
                String text = new String(payload);
                //String text2 = text.substring(3);
                tagIDMain = text;
                sendData();
                ndef.close();



            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Cannot Read From Tag.", Toast.LENGTH_LONG).show();
        }
    }

    private void sendData() {

        Log.e(TAG, "Comparision:  "+tagIDMain+ " AND "+tagID );

        boolean check = tagIDMain.equals(tagID);

        Log.e(TAG, "Is it equal : "+check );

        //if(tagIDMain.equals(tagID)) {

            countDownTimer.cancel();

            TimeBuff += MillisecondTime;

            handler.removeCallbacks(runnable);

            new SendSpreadsheetClass().execute();

            Intent intent = new Intent(this, ReadNfc.class);
            startActivity(intent);
            finish();

       // }else {

           // Toast.makeText(this,"Invalid Tag scanned",Toast.LENGTH_SHORT).show();
       // }
    }

    private void initResources() {

        tagID = getIntent().getStringExtra("tagName");

        house = tagID.substring(0,3);
        number = tagID.charAt(3);
        row = tagID.substring(tagID.length() - 4);
        Log.e(TAG, "initResources House: " + house + "  Number : "+number +"   Row : "+row);
        handler = new Handler() ;

        txtTagName = findViewById(R.id.txt_tag_text);
        progressBarCircle = (ProgressBar) findViewById(R.id.progressBarCircle);
        textViewTime = (TextView) findViewById(R.id.textViewTime);
        txtTimerNew = findViewById(R.id.textViewTimeNew);


        txtTagName.setText(house+" "+number+ " Row "+row);

        /*Animation*/
        RotateAnimation makeVertical = new RotateAnimation(0, -90, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        makeVertical.setFillAfter(true);
        progressBarCircle.startAnimation(makeVertical);
        progressBarCircle.setSecondaryProgress(endTime);
        progressBarCircle.setProgress(0);
        
        startTimer();

        fn_countdown();

    }

    private void fn_countdown() {

            String timeInterval = "3800";
            progress = 1;
            endTime = Integer.parseInt(timeInterval); // up to finish time

            countDownTimer = new CountDownTimer(endTime * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    setProgress(progress, endTime);
                    progress = progress + 1;
                    int seconds = (int) (millisUntilFinished / 1000) % 60;
                    int minutes = (int) ((millisUntilFinished / (1000 * 60)) % 60);
                    int hours = (int) ((millisUntilFinished / (1000 * 60 * 60)) % 24);
                    String newtime = hours + ":" + minutes + ":" + seconds;

                    if (newtime.equals("0:0:0")) {
                        txtTimerNew.setText("00:00:00");
                    } else if ((String.valueOf(hours).length() == 1) && (String.valueOf(minutes).length() == 1) && (String.valueOf(seconds).length() == 1)) {
                        txtTimerNew.setText("0" + hours + ":0" + minutes + ":0" + seconds);
                    } else if ((String.valueOf(hours).length() == 1) && (String.valueOf(minutes).length() == 1)) {
                        txtTimerNew.setText("0" + hours + ":0" + minutes + ":" + seconds);
                    } else if ((String.valueOf(hours).length() == 1) && (String.valueOf(seconds).length() == 1)) {
                        txtTimerNew.setText("0" + hours + ":" + minutes + ":0" + seconds);
                    } else if ((String.valueOf(minutes).length() == 1) && (String.valueOf(seconds).length() == 1)) {
                        txtTimerNew.setText(hours + ":0" + minutes + ":0" + seconds);
                    } else if (String.valueOf(hours).length() == 1) {
                        txtTimerNew.setText("0" + hours + ":" + minutes + ":" + seconds);
                    } else if (String.valueOf(minutes).length() == 1) {
                        txtTimerNew.setText(hours + ":0" + minutes + ":" + seconds);
                    } else if (String.valueOf(seconds).length() == 1) {
                        txtTimerNew.setText(hours + ":" + minutes + ":0" + seconds);
                    } else {
                        txtTimerNew.setText(hours + ":" + minutes + ":" + seconds);
                    }

                }

                @Override
                public void onFinish() {
                    setProgress(progress, endTime);


                }
            };
            countDownTimer.start();
    }

    public void setProgress(int startTime, int endTime) {
        progressBarCircle.setMax(endTime);
        progressBarCircle.setSecondaryProgress(endTime);
        progressBarCircle.setProgress(startTime);

    }

    private void startTimer() {

        StartTime = SystemClock.uptimeMillis();
        handler.postDelayed(runnable, 0);


    }

    @Override
    public void onBackPressed() {

    }


    private void setProgressBarValues() {

        progressBarCircle.setMax((int) timeCountInMilliSeconds / 1000);
        progressBarCircle.setProgress((int) timeCountInMilliSeconds / 1000);
    }

    public Runnable runnable = new Runnable() {

        public void run() {

            MillisecondTime = SystemClock.uptimeMillis() - StartTime;

            UpdateTime = TimeBuff + MillisecondTime;

            Seconds = (int) (UpdateTime / 1000);

            Hours = Seconds / 3600;

            Minutes = Seconds / 60;

            Seconds = Seconds % 60;

            MilliSeconds = (int) (UpdateTime % 1000);

            String newtime = Minutes + ":" + Seconds;

            if (newtime.equals("0:0")) {
                textViewTime.setText("00:00:00");

                } else if ((String.valueOf(Hours).length() == 1) && (String.valueOf(Minutes).length() == 1) && (String.valueOf(Seconds).length() == 1)) {
                textViewTime.setText("0" + Hours + ":0" + Minutes + ":0" + Seconds);
                } else if ((String.valueOf(Hours).length() == 1) && (String.valueOf(Minutes).length() == 1)) {
                textViewTime.setText("0" + Hours + ":0" + Minutes + ":" + Seconds);
                } else if ((String.valueOf(Hours).length() == 1) && (String.valueOf(Seconds).length() == 1)) {
                textViewTime.setText("0" + Hours + ":" + Minutes + ":0" + Seconds);
                } else if ((String.valueOf(Minutes).length() == 1) && (String.valueOf(Seconds).length() == 1)) {
                textViewTime.setText(Hours + ":0" + Minutes + ":0" + Seconds);
                } else if (String.valueOf(Hours).length() == 1) {
                textViewTime.setText("0" + Hours + ":" + Minutes + ":" + Seconds);
                } else if (String.valueOf(Minutes).length() == 1) {
                textViewTime.setText(Hours + ":0" + Seconds + ":" + Seconds);
                } else if (String.valueOf(Seconds).length() == 1) {
                textViewTime.setText(Hours + ":" + Minutes + ":0" + Seconds);
                } else {
                textViewTime.setText(Hours + ":" + Minutes + ":" + Seconds);
                }


            handler.postDelayed(this, 0);
        }

    };

    private class SendSpreadsheetClass extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            try {

                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.
                        Builder().permitAll().build();

                StrictMode.setThreadPolicy(policy);


                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl("https://docs.google.com/forms/u/0/d/e/")
                                .build();
                        final SpreadsheetWebService spreadsheetWebService = retrofit.create(SpreadsheetWebService.class);

                        String time = Hours + ":"+ Minutes + ":" + Seconds;

                        Call<Void> completeQuestionnaireCall = spreadsheetWebService.submitTime(tagID, "00:00:00", time);

                        Log.e(TAG, "Data : "+tagID+ " "+ time );
                        completeQuestionnaireCall.enqueue(callCallback);




            } catch (Exception ex) {

                Log.i("Mail", "Failed" + ex);
            }

            return null;
        }
    }


    private final Callback<Void> callCallback = new Callback<Void>() {
        @Override
        public void onResponse(Call<Void> call, Response<Void> response) {

            Log.d(TAG, "Submitted. " + response);
        }

        @Override
        public void onFailure(Call<Void> call, Throwable t) {
            Log.e(TAG, "Failed", t);
            call.cancel();
        }
    };

    @Override
    public void onClick(View v) {

        int id = v.getId();

        switch (id) {



        }
    }
}
