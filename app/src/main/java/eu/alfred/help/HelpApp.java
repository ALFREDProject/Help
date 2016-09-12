package eu.alfred.help;

import android.app.Application;
import android.content.Intent;

import eu.alfred.help.ui.activity.FallDetectorActivity;
import eu.alfred.help.util.FallDetector;

public class HelpApp extends Application implements FallDetector.FallDetectorListener {

    private FallDetector fallDetector;

    @Override
    public void onCreate() {
        super.onCreate();
        fallDetector = new FallDetector(this);
        fallDetector.setFallDetectorListener(this);
        fallDetector.resume();
    }

    @Override
    public void onFallDetected() {
        startSignal5Seconds();
    }

    private void startSignal5Seconds() {
        Intent intent = new Intent(this, FallDetectorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
