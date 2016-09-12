package eu.alfred.help.ui.activity;

import android.app.ActionBar;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import eu.alfred.help.R;
import eu.alfred.help.util.Constants;
import eu.alfred.help.util.PhoneCommunicationUtils;
import eu.alfred.help.util.Prefs;
import eu.alfred.help.util.StringUtils;
import eu.alfred.ui.BackToPAButton;
import eu.alfred.ui.CircleButton;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    @InjectView(R.id.imageButtonLocalSettings)
    ImageButton imageButtonLocalSettings;
    @InjectView(R.id.editTextSetAddress)
    EditText editTextSetAddress;
    @InjectView(R.id.buttonSetAddress)
    ImageButton buttonSetAddress;

    @InjectView(R.id.layoutCaregiver)
    View layoutCaregiver;
    @InjectView(R.id.imageViewCaregiver)
    View imageViewCaregiver;
    @InjectView(R.id.buttonContactCaregiver)
    View buttonContactCaregiver;
    @InjectView(R.id.layoutHelp)
    View layoutHelp;
    @InjectView(R.id.imageViewHelp)
    View imageViewHelp;
    @InjectView(R.id.buttonNeedHelp)
    View buttonNeedHelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        init();
    }

    private void init() {
        initViews();
        setListeners();
    }

    private void initViews() {
        initActionBar();
        circleButton = (CircleButton) findViewById(R.id.voiceControlBtn);
        backToPAButton = (BackToPAButton) findViewById(R.id.backControlBtn);
        editTextSetAddress.setText(Prefs.getString(Constants.KEY_CADE_URL, Constants.LOCAL_CADE_URL));
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    private void setListeners() {
        circleButton.setOnTouchListener(new MicrophoneTouchListener());
        backToPAButton.setOnTouchListener(new BackTouchListener());
        imageButtonLocalSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextSetAddress.getVisibility() == View.GONE) {
                    editTextSetAddress.setVisibility(View.VISIBLE);
                    buttonSetAddress.setVisibility(View.VISIBLE);
                } else {
                    editTextSetAddress.setVisibility(View.GONE);
                    buttonSetAddress.setVisibility(View.GONE);
                }
            }
        });
        editTextSetAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Prefs.setString(Constants.KEY_CADE_URL, editTextSetAddress.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        buttonSetAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cade.SetCadeBackendUrl(editTextSetAddress.getText().toString());
            }
        });

        View.OnClickListener helpListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                needHelp();
            }
        };
        View.OnClickListener caregiverListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactCaregiver();
            }
        };
        layoutCaregiver.setOnClickListener(helpListener);
        imageViewCaregiver.setOnClickListener(helpListener);
        buttonContactCaregiver.setOnClickListener(helpListener);
        layoutHelp.setOnClickListener(caregiverListener);
        imageViewHelp.setOnClickListener(caregiverListener);
        buttonNeedHelp.setOnClickListener(caregiverListener);
    }

    @Override
    public void performAction(String command, Map<String, String> map) {
        Log.d(TAG, "performAction command: #" + command + "#, map: #" + StringUtils.getReadableString(map) + "#");
        if (TextUtils.equals(command, Constants.CADE_ACTION_NEED_HELP)) {
            needHelp();
        } else if (TextUtils.equals(command, Constants.CADE_ACTION_CONTACT_CAREGIVER)) {
            contactCaregiver();
        }
        cade.sendActionResult(true);
    }

    @Override
    public void performWhQuery(String command, Map<String, String> map) {
    }

    @Override
    public void performValidity(String command, Map<String, String> map) {
    }

    @Override
    public void performEntityRecognizer(String command, Map<String, String> map) {
    }

    private void needHelp() {
        PhoneCommunicationUtils.call(this, getNeedHelpPhoneNumber());
    }

    private void contactCaregiver() {
        PhoneCommunicationUtils.call(this, getContactCaregiverPhoneNumber());
    }

    private void fallDetected() {
        PhoneCommunicationUtils.call(this, getFallDetectedPhoneNumber());
    }

    private String getNeedHelpPhoneNumber() {
        return "+34123456781";  // TODO
    }

    private String getContactCaregiverPhoneNumber() {
        return "+34123456782";  // TODO
    }

    private String getFallDetectedPhoneNumber() {
        return "+34123456783";  // TODO
    }

    @Override
    protected void onResume() {
        super.onResume();
        editTextSetAddress.setText(Prefs.getString(Constants.KEY_CADE_URL, Constants.LOCAL_CADE_URL));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
