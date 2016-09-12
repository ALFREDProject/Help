package eu.alfred.help.ui.activity;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import eu.alfred.help.R;
import eu.alfred.help.util.PhoneCommunicationUtils;

public class FallDetectorActivity extends AppCompatActivity {

    private static final String TAG = "FallDetectorActivity";
    private Timer timer;

    @InjectView(R.id.textViewTitle)
    TextView textViewTitle;
    @InjectView(R.id.buttonNeedHelp)
    Button buttonNeedHelp;
    @InjectView(R.id.textViewTimeLeft)
    TextView textViewTimeLeft;
    @InjectView(R.id.progressBarTimeLeft)
    ProgressBar progressBarTimeLeft;

    private static final int DEFAULT_TIMER_SECONDS = 30;
    private int secondsLeft;

    private TextToSpeech textToSpeech;
    private boolean textToSpeechInit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fall_detector);
        ButterKnife.inject(this);

        init();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        releaseTextToSpeech();
        cancelTimer();
        init();
    }

    private void init() {
        initViews();
        setListeners();

        secondsLeft = DEFAULT_TIMER_SECONDS;
        progressBarTimeLeft.setMax(DEFAULT_TIMER_SECONDS);
        textViewTimeLeft.setText("" + secondsLeft);

        textToSpeechInit = false;
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                textToSpeechInit = status == TextToSpeech.SUCCESS;
                speak(getString(R.string.contacting_emergency)+" " + secondsLeft + " "+getString(R.string.seconds)+". "+getString(R.string.to_cancel_touch_button)+".", new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {

                    }

                    @Override
                    public void onDone(String utteranceId) {
                        initTimer();
                    }

                    @Override
                    public void onError(String utteranceId) {
                        initTimer();
                    }
                });
            }
        });

        hideSystemUI();
    }

    private void initTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUI();
                    }
                });
            }
        }, 1000, 1000);
    }

    private void updateUI() {
        secondsLeft -= 1;
        if (secondsLeft > 0) {
            textViewTimeLeft.setText("" + secondsLeft);
            progressBarTimeLeft.setProgress(secondsLeft);
            if (secondsLeft <= 5) {
                speak("" + secondsLeft);
                vibrate(300);
            } else if (secondsLeft % 10 == 0) {
                speak(secondsLeft + " "+getString(R.string.seconds_remaining_contact));
                vibrate(750);
            } else if (secondsLeft % 5 == 0) {
                speak(secondsLeft + " "+getString(R.string.seconds_remaining));
                vibrate(500);
            }else{
                vibrate(300);
            }
        } else if (secondsLeft == 0) {
            textViewTimeLeft.setText("" + secondsLeft);
            progressBarTimeLeft.setProgress(secondsLeft);
            cancelTimer();
            speak(getString(R.string.contacting_emergency), new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {

                }

                @Override
                public void onDone(String utteranceId) {
                    contactEmergency();
                }

                @Override
                public void onError(String utteranceId) {
                    contactEmergency();
                }
            });
        }
    }

    private void vibrate(long duration) {
        if (duration > 0) {
            Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(duration);
        }
    }

    public void contactEmergency() {
        PhoneCommunicationUtils.call(FallDetectorActivity.this, getHelpNumber());
    }

    private String getHelpNumber() {
        return "+34954685987";
    }

    private void speak(@NonNull String textToRead) {
        speak(textToRead, null);
    }

    private void speak(@NonNull String textToRead, UtteranceProgressListener utteranceProgressListener) {
        Log.d(TAG, "textToRead " + textToRead);
        try {
            if (textToSpeech != null && !TextUtils.isEmpty(textToRead) && textToSpeechInit) {
                textToSpeech.setLanguage(Locale.getDefault());
                if (textToRead.length() > TextToSpeech.getMaxSpeechInputLength()) {
                    textToRead = textToRead.substring(0, TextToSpeech.getMaxSpeechInputLength() - 1);
                }
                textToSpeech.setSpeechRate(1.0f);
                textToSpeech.setOnUtteranceProgressListener(utteranceProgressListener);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    textToSpeech.speak(textToRead, TextToSpeech.QUEUE_ADD, null, "" + System.currentTimeMillis());
                } else {
                    textToSpeech.speak(textToRead, TextToSpeech.QUEUE_ADD, null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initViews() {
        initActionBar();
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    private void setListeners() {
        buttonNeedHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelTimer();
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            textToSpeech.stop();
        } catch (Exception ignored) {
        }
        cancelTimer();
        finish();
    }

    public void cancelTimer() {
        try {
            if (timer != null) {
                timer.cancel();
                timer.purge();
                timer = null;
            }
        } catch (Exception ignored) {
        }
    }

    private void releaseTextToSpeech() {
        try {
            textToSpeechInit = false;
            if (textToSpeech.isSpeaking()) {
                textToSpeech.stop();
            }
            textToSpeech.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseTextToSpeech();
    }

    @Override
    public void onBackPressed() {
    }
    private void hideSystemUI() {
        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
