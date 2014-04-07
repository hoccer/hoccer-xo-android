package com.hoccer.xo.android.activity;

import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.XoConfiguration;
import com.hoccer.xo.android.view.AttachmentTransferControlView;
import com.hoccer.xo.release.R;

import net.hockeyapp.android.CrashManager;

import org.apache.log4j.Logger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;

public class XoPreferenceActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final Logger LOG = Logger.getLogger(XoPreferenceActivity.class);

    private AttachmentTransferControlView mSpinner;

    private Handler mDialogDismisser;

    private Dialog mWaitingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
        LOG.debug("onCreate()");
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        if (XoConfiguration.DEVELOPMENT_MODE_ENABLED) {
            addPreferencesFromResource(R.xml.development_preferences);
        } else {
            addPreferencesFromResource(R.xml.preferences);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForCrashesIfEnabled();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LOG.debug("onOptionsItemSelected(" + item.toString() + ")");
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void checkForCrashesIfEnabled() {
        if (XoConfiguration.reportingEnable()) {
            CrashManager.register(this, XoConfiguration.HOCKEYAPP_ID);
        }
    }

    public void createDialog() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.waiting_dialog, null);
        mSpinner = (AttachmentTransferControlView) view.findViewById(R.id.content_progress);

        mWaitingDialog = new Dialog(this);
        mWaitingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mWaitingDialog.setContentView(view);
        mWaitingDialog.getWindow()
                .setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mWaitingDialog.setCanceledOnTouchOutside(false);
        if (!isFinishing()) {
            mWaitingDialog.show();
        }
        mWaitingDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return true;
            }
        });

        Handler spinnerStarter = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                mSpinner.prepareToUpload();
                mSpinner.spin();
            }
        };
        mDialogDismisser = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                mWaitingDialog.dismiss();
                mSpinner.completeAndGone();
            }
        };
        spinnerStarter.sendEmptyMessageDelayed(0, 500);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("preference_keysize")) {
            createDialog();
            regenerateKeys();
        }
    }

    private void regenerateKeys() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    XoApplication.getXoClient().regenerateKeyPair();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    mDialogDismisser.sendEmptyMessage(0);
                }
            }
        });
        t.start();
    }

    @Override
    protected void onDestroy() {
        if (mSpinner != null) {
            mSpinner.completeAndGone();
        }
        super.onDestroy();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals("preference_export")) {
            doExport();
            return true;
        } else if (preference.getKey().equals("preference_import")) {
            doImport();
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void doImport() {
        final File credentialsFile = new File(
                XoApplication.getExternalStorage() + File.separator + "credentials.json");
        if (credentialsFile == null || !credentialsFile.exists()) {
            Toast.makeText(this, getString(R.string.cant_find_credentials), Toast.LENGTH_LONG)
                    .show();
            return;
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final EditText passwordInput = new EditText(this);
        passwordInput.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        dialogBuilder.setTitle(R.string.export_credentials_dialog_title);
        dialogBuilder
                .setPositiveButton(R.string.common_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String password = passwordInput.getText().toString();
                        if (password != null && password.length() > 0) {
                            importCredentials(credentialsFile, password);
                            dialog.dismiss();
                        } else {
                            Toast.makeText(XoPreferenceActivity.this, R.string.no_password,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
        dialogBuilder
                .setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        passwordInput.setText("");
                        dialog.dismiss();
                    }
                });
        dialogBuilder.setView(passwordInput);
        dialogBuilder.show();
    }

    private void importCredentials(File credentialsFile, String password) {
        byte[] credentials = new byte[(int) credentialsFile.length()];



        XoApplication.getXoClient().setCryptedCredentialsFromContainer(credentials, password);
        Toast.makeText(this, "Successfully imported credentials.", Toast.LENGTH_LONG).show();
    }

    private void doExport() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final EditText passwordInput = new EditText(this);
        passwordInput.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        dialogBuilder.setTitle(R.string.export_credentials_dialog_title);
        dialogBuilder
                .setPositiveButton(R.string.common_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String password = passwordInput.getText().toString();
                        if (password != null && password.length() > 0) {
                            exportCredentials(password);
                            dialog.dismiss();
                        } else {
                            Toast.makeText(XoPreferenceActivity.this, R.string.no_password,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
        dialogBuilder
                .setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        passwordInput.setText("");
                        dialog.dismiss();
                    }
                });
        dialogBuilder.setView(passwordInput);
        dialogBuilder.show();
    }

    private void exportCredentials(String password) {
        try {
            byte[] credentialsContainer = XoApplication.getXoClient()
                    .makeCryptedCredentialsContainer(password);

            FileOutputStream fos = new FileOutputStream(
                    XoApplication.getExternalStorage() + File.separator + "credentials.json");
            fos.write(credentialsContainer);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            LOG.error("error while writing credentials container to filesyystem.", e);
            Toast.makeText(this, R.string.export_credentials_failure, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            LOG.error("error while generating credentials container", e);
            Toast.makeText(this, R.string.export_credentials_failure, Toast.LENGTH_LONG).show();
        }
        Toast.makeText(this, R.string.export_credentials_success, Toast.LENGTH_LONG).show();
    }

}
