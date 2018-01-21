package com.movesense.mds.sampleapp.example_app_using_mds_api;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import com.movesense.mds.sampleapp.BuildConfig;
import com.movesense.mds.sampleapp.R;
import com.movesense.mds.sampleapp.example_app_using_mds_api.logs.LogsListAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class SendLogsToGoogleDriveActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        EasyPermissions.PermissionCallbacks {

    @BindView(R.id.resultTextView) TextView resultTextView;
    @BindView(R.id.logsFileListView) ListView logsFileListView;

    private GoogleAccountCredential mCredential;
    private static final String[] SCOPES = {DriveScopes.DRIVE};
    private GoogleApiClient mGoogleApiClient;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final int RESOLVE_CONNECTION_REQUEST_CODE = 1005;

    private final String MOVESENSE_DIRECTORY_GOOGLEDRIVE_NAME = "MovesenseLog";

    private final String LOG_TAG = SendLogsToGoogleDriveActivity.class.getSimpleName();
    private ProgressDialog mProgress;

    private final List<File> logsFileList = new ArrayList<>();
    private File fileToSend;
    private LogsListAdapter logsFileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_logs_to_google_drive);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Saved Data");
        }

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Drive API ...");

        // Create Adapter for listView
        logsFileAdapter = new LogsListAdapter(logsFileList);
        logsFileListView.setAdapter(logsFileAdapter);


        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        // Query logs from Movesense folder
        queryLogsFile();

    }

    private void queryLogsFile() {
        // Query logs from Movesense folder
        logsFileList.clear();

        File externalDirectory = Environment.getExternalStorageDirectory();
        File dirFile = new File(externalDirectory + "/Movesense");
        if (dirFile.exists()) {
            File[] logs = dirFile.listFiles();

            // Check if any file exists
            if (logs != null) {
                for (File file : logs) {
                    logsFileList.add(file);
                    Log.e(LOG_TAG, "Query File: " + file.getName());
                }

                logsFileAdapter.notifyDataSetChanged();
            } else {
                Log.e(LOG_TAG, "Query file failed. File[] = null");
                resultTextView.setText("Logs directory is empty or not loaded. Please subscribe sensor and back.");
            }
        } else {
            Log.e(LOG_TAG, "Movesense logs dir not exists.");
            resultTextView.setText("Logs directory not exists");
        }
    }

    @OnItemClick(R.id.logsFileListView)
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Show dialog for open / send file
        final File clickedFile = logsFileList.get(position);

        new AlertDialog.Builder(this)
                .setTitle("Choose a file action")
                .setItems(new CharSequence[]{"Open file", "Send file to Google Drive"},
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        // Open File
                                        Intent intent = new Intent();
                                        intent.setAction(android.content.Intent.ACTION_VIEW);
                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        Uri uri = FileProvider.getUriForFile(SendLogsToGoogleDriveActivity.this,
                                                BuildConfig.APPLICATION_ID, clickedFile);
                                        intent.setDataAndType(uri, getMimeType(clickedFile.getName()));
                                        startActivity(intent);
                                        break;

                                    case 1:
                                        // Send file to Google Drive
                                        fileToSend = clickedFile;
                                        getResultsFromApi();
                                        break;
                                }
                            }
                        })
                .show();

    }

    private String getMimeType(String url) {
        String parts[] = url.split("\\.");
        String extension = parts[parts.length - 1];
        String type = null;
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // connectGoogleApiClient();
    }

    private void connectGoogleApiClient() {
        if (mGoogleApiClient == null) {
            // Create the API client and bind it to an instance variable.
            // We use this instance as the callback for connection and connection
            // failures.
            // Since no account name is passed, the user is prompted to choose.
            Log.d(LOG_TAG, "mGoogleApiClient = new GoogleApiClient.Builder(this)");

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        Log.d(LOG_TAG, "mGoogleApiClient.connect();");
        // Connect the client. Once connected, the camera is launched.
        mGoogleApiClient.connect();
    }


    @Override
    protected void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.send_logs_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            menu.findItem(R.id.googleDriveItem).setTitle(R.string.change_account);
        } else {
            menu.findItem(R.id.googleDriveItem).setTitle(R.string.log_in);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.googleDriveItem:
                // Log Out
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    // Disconnect & Logout

                    mGoogleApiClient.clearDefaultAccountAndReconnect().setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            Log.d(LOG_TAG, "LOG OUT onResult() status: " + status);
                            invalidateOptionsMenu();
                            Toast.makeText(SendLogsToGoogleDriveActivity.this, "You are log out", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                // Log in
                if (mGoogleApiClient == null) {
                    //mGoogleApiClient.connect();
                    connectGoogleApiClient();
                    invalidateOptionsMenu();
                }

                break;

            case R.id.refreshFiles:
                queryLogsFile();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getResultsFromApi() {
        Log.d(LOG_TAG, "getResultsFromApi()");
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            Toast.makeText(this, R.string.no_network_connection_available, Toast.LENGTH_SHORT).show();
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        Log.d(LOG_TAG, "chooseAccount()");
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "onActivityResult() requestCode: " + requestCode + " resultCode: " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(this, "This app requires Google Play Services. Please install " +
                            "Google Play Services on your device and relaunch this app.", Toast.LENGTH_SHORT).show();
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
            case RESOLVE_CONNECTION_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                } else if (resultCode == RESULT_CANCELED) {
                    mGoogleApiClient = null;
                    Toast.makeText(this, "Connection failed. Please check correct Api setup in README", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(LOG_TAG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
        Log.d(LOG_TAG, "onPermissionsGranted()");
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
        Log.d(LOG_TAG, "onPermissionsDenied()");
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        Log.d(LOG_TAG, "isDeviceOnline()");
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        Log.d(LOG_TAG, "isGooglePlayServicesAvailable()");
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        Log.d(LOG_TAG, "acquireGooglePlayServices()");
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        Log.d(LOG_TAG, "showGooglePlayServicesAvailabilityErrorDialog()");
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                SendLogsToGoogleDriveActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(LOG_TAG, "onConnected()");
        Toast.makeText(this, "You are logged in!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOG_TAG, "onConnectionSuspended()");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(LOG_TAG, "onConnectionFailed() ConnectionResult: " + connectionResult);
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
        }
    }

    /**
     * An asynchronous task that handles the Drive API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, Void> {
        private com.google.api.services.drive.Drive mService = null;
        private Exception mLastError = null;
        private com.google.api.services.drive.model.File directoryFile;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.drive.Drive.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Movesense logs")
                    .build();
        }

        /**
         * Background task to call Drive API.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected Void doInBackground(Void... params) {
            for (Void param : params) {
                Log.e(LOG_TAG, "PARAMS: " + param);
            }
            try {
                Log.e(LOG_TAG, "=====");
                sendFileToGoogleDrive();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
            }
            return null;
        }

        private void sendFileToGoogleDrive() throws IOException {
            Log.d(LOG_TAG, "sendFileToGoogleDrive()");

            ///////////////  Send File to Drive

            // Query for folder from Google Drive
            com.google.api.services.drive.Drive.Files.List filesonDrive = mService.files().
                    list().setQ(
                    "mimeType='application/vnd.google-apps.folder' and trashed=false");
            // Execute query
            FileList fileList = filesonDrive.execute();

            for (com.google.api.services.drive.model.File file : fileList.getFiles()) {
                Log.d(LOG_TAG, "Folder: " + file.getName());
                if (file.getName().equals(MOVESENSE_DIRECTORY_GOOGLEDRIVE_NAME)) {
                    // We found a parent folder
                    directoryFile = file;
                    break;
                }
            }

            // Check if parent folder exists
            if (directoryFile != null) {
                Log.d(LOG_TAG, "directoryFile != null");
                // Insert File to parent folder

                String folderId = directoryFile.getId();

                com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
                fileMetadata.setName(fileToSend.getName());
                fileMetadata.setMimeType(getMimeType(fileToSend.getName()));
                fileMetadata.setParents(Collections.singletonList(folderId));
                FileContent mediaContent = new FileContent("application/vnd.google-apps.document", fileToSend);
                mService.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();
            } else {
                Log.d(LOG_TAG, "directoryFile == null");
                // Create parent dir and then insert file to folder
                com.google.api.services.drive.model.File directoryFile = new com.google.api.services.drive.model.File();
                directoryFile.setName(MOVESENSE_DIRECTORY_GOOGLEDRIVE_NAME);
                directoryFile.setMimeType("application/vnd.google-apps.folder");

                com.google.api.services.drive.model.File file = mService.files().create(directoryFile)
                        .setFields("id")
                        .execute();

                String folderId = file.getId();
                Log.d(LOG_TAG, "folderID: " + folderId);

                // Insert file to directory
                com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
                fileMetadata.setName(fileToSend.getName());
                fileMetadata.setMimeType(getMimeType(fileToSend.getName()));
                fileMetadata.setParents(Collections.singletonList(folderId));
                FileContent mediaContent = new FileContent("application/vnd.google-apps.document", fileToSend);
                mService.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();
            }
        }

        @Override
        protected void onPreExecute() {
            mProgress.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgress.hide();
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            SendLogsToGoogleDriveActivity.REQUEST_AUTHORIZATION);
                } else {
                    Toast.makeText(SendLogsToGoogleDriveActivity.this, "The following error occurred:\n"
                            + mLastError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SendLogsToGoogleDriveActivity.this, "Request cancelled.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
