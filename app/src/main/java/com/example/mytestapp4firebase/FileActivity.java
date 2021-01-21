/* FileActivity.java -- 
   Copyright (C) 2010 Christophe Bouyer (Hobby One)

This file is part of Hash Droid.

Hash Droid is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Hash Droid is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Hash Droid. If not, see <http://www.gnu.org/licenses/>.
 */

package com.example.mytestapp4firebase;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.ClipboardManager;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.android.volley.AuthFailureError;
import com.android.volley.BuildConfig;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.what3words.androidwrapper.What3WordsV3;
import com.what3words.androidwrapper.voice.VoiceBuilder;
import com.what3words.javawrapper.request.Coordinates;
import com.what3words.javawrapper.response.Suggestion;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FileActivity extends Activity implements Runnable {

    private Button mSelectFileButton = null;
    private CheckBox mCheckBox = null;
    private Button mGenerateButton = null;
    private Button mCopyButton = null;
    private Spinner mSpinner = null;
    private TextView mResultTV = null;
    private String msFileSize = "";
    private String msHash = "";
    private String w3w = "";
    private String SLat = "";
    private String SLng = "";
    private String[] mFunctions;
    private ClipboardManager mClipboard = null;
    private final int SELECT_FILE_REQUEST = 0;
    private HashFunctionOperator mHashOpe = null;
    private ProgressDialog mProgressDialog = null;
    private int miItePos = -1;
    private Uri mSelectedFileUri = null;

    private EditText mEditText = null;
    private String msToHash = "";


    /****************
     Code for camera portion of the app BELOW
     ****************/

    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int GALLERY_REQUEST_CODE = 105;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    StorageReference storageReference;
    Button cameraBtn, galleryBtn, buttonConvertTo3wa;
    String userID, currentPhotoPath;
    ImageView selectedImage;
    FirebaseUser firebaseUser;

    /****************
     Code for Posting portion of the app BELOW
     ****************/
    EditText e2, e3, textInputConvertTo3wa;
    TextView t1;
    Button b1;
    String s1, s2, s3;

    /****************
    Code for location portion of the app BELOW
     ****************/
    private static final String TAG = FileActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";
    private final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private Button mStartUpdatesButton;
    private Button mStopUpdatesButton;
    private TextView mLastUpdateTimeTextView;
    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;
    private String mLatitudeLabel;
    private String mLongitudeLabel;
    private String mLastUpdateTimeLabel;
    private Boolean mRequestingLocationUpdates;
    private String mLastUpdateTime;

    private What3WordsV3 wrapper;
    private TextView resultConvertTo3wa;

    private EditText caseID;

    /**
     * Called when the activity is first created.
     */
    @SuppressLint("WrongConstant")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file);

        mSelectFileButton = (Button) findViewById(R.id.SelectFileButton);
        mGenerateButton = (Button) findViewById(R.id.GenerateButton);
        mSpinner = (Spinner) findViewById(R.id.spinner);
        mResultTV = (TextView) findViewById(R.id.label_result);
        mCopyButton = (Button) findViewById(R.id.CopyButton);
        mClipboard = (ClipboardManager) getSystemService("clipboard");
        mFunctions = getResources().getStringArray(R.array.Algo_Array);
        mCheckBox = (CheckBox) findViewById(R.id.UpperCaseCB);
        mEditText = (EditText) findViewById(R.id.textToHash);

        /****************
         Code for Posting portion of the app BELOW
         ****************/
        t1 = (TextView)findViewById(R.id.textViewPhotoSHA512);
        caseID = (EditText)findViewById(R.id.editTextCaseID);
        //e2 = (EditText)findViewById(R.id.textViewPhotoSHA512);
        e3 = (EditText)findViewById(R.id.editTextW3w);
        b1 = (Button)findViewById(R.id.save);

        /****************
         Code for Location portion of the app BELOW
         ****************/
        mStartUpdatesButton = (Button) findViewById(R.id.start_updates_button);
        mStopUpdatesButton = (Button) findViewById(R.id.stop_updates_button);
        mLatitudeTextView = (TextView) findViewById(R.id.latitude_text);
        mLongitudeTextView = (TextView) findViewById(R.id.longitude_text);
        mLastUpdateTimeTextView = (TextView) findViewById(R.id.last_update_time_text);

        // Set labels.
        mLatitudeLabel = getResources().getString(R.string.latitude_label);
        mLongitudeLabel = getResources().getString(R.string.longitude_label);
        mLastUpdateTimeLabel = getResources().getString(R.string.last_update_time_label);

        mRequestingLocationUpdates = true;
        mLastUpdateTime = "";

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
        startLocationUpdates();

        /****************
         Code for What3Words portion of the app BELOW
         ****************/

        wrapper = new What3WordsV3("0K0S5IWQ", this);

        buttonConvertTo3wa = findViewById(R.id.buttonConvertTo3wa);
        textInputConvertTo3wa = findViewById(R.id.textInputConvertTo3wa);
        resultConvertTo3wa = findViewById(R.id.resultConvertTo3wa);

        //convert-to-3wa sample
        buttonConvertTo3wa.setOnClickListener(view -> {
            try {
                //String[] latLong = textInputConvertTo3wa.getText().toString().replaceAll("\\s", "").split(",");
                String Lat = SLat;
                String Lng = SLng;
                Double lat = Double.parseDouble(Lat);
                Double lng = Double.parseDouble(Lng);
                if (lat != null && lng != null) {
                    Observable.fromCallable(() -> wrapper.convertTo3wa(new Coordinates(lat, lng)).execute())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(result -> {
                                if (result.isSuccessful()) {
                                    resultConvertTo3wa.setText(String.format("3 word address: %s", result.getWords()));

                                    String w3wString = result.getWords();

                                    w3w = w3wString;

                                } else {
                                    resultConvertTo3wa.setText(result.getError().getMessage());
                                }
                            });
                } else {
                    resultConvertTo3wa.setText("invalid lat,long");
                }
            } catch (Exception e) {
                resultConvertTo3wa.setText("invalid lat,long");
            }
        });

        /****************
         Code for camera portion of the app BELOW
         ****************/

        selectedImage = findViewById(R.id.displayImage);
        cameraBtn = findViewById(R.id.cameraBtn);
        galleryBtn = findViewById(R.id.galleryBtn);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askCameraPermission();

            }
        });
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(gallery, GALLERY_REQUEST_CODE);
            }
        });

        /****************
         Code for camera portion of the app ABOVE
         ****************/

        userID = firebaseAuth.getCurrentUser().getUid();
        firebaseUser = firebaseAuth.getCurrentUser();

        /*
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    RequestQueue requestQueue = Volley.newRequestQueue(FileActivity.this);
                    String URL = "https://us-central1-boecloudtest.cloudfunctions.net/addEvidenceMetadata";
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("CaseID", e1.getText().toString());
                    jsonBody.put("PhotoSHA512", t1.getText().toString()); //e2.getText().toString());
                    jsonBody.put("w3w", e3.getText().toString());
                    final String requestBody = jsonBody.toString();

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i("VOLLEY", response);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("VOLLEY", error.toString());
                        }
                    }) {
                        @Override
                        public String getBodyContentType() {
                            return "application/json; charset=utf-8";
                        }

                        @Override
                        public byte[] getBody() throws AuthFailureError {
                            try {
                                return requestBody == null ? null : requestBody.getBytes("utf-8");
                            } catch (UnsupportedEncodingException uee) {
                                VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                                return null;
                            }
                        }

                        @Override
                        protected Response<String> parseNetworkResponse(NetworkResponse response) {
                            String responseString = "";
                            if (response != null) {
                                responseString = String.valueOf(response.statusCode);
                                // can get more details such as response.headers
                            }
                            return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                        }
                    };

                    requestQueue.add(stringRequest);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
         */

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.Algo_Array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setSelection(11); // SHA-512 by default

        mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView,
                                       View selectedItemView, int position, long id) {
                // your code here
                // Hide the copy button
                if (!msHash.equals(""))
                    mCopyButton.setVisibility(View.INVISIBLE);
                // Clean the result text view
                if (mResultTV != null)
                    mResultTV.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

        mSelectFileButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent openExplorerIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    if (null != openExplorerIntent) {
                        openExplorerIntent.addCategory(Intent.CATEGORY_OPENABLE);
                        openExplorerIntent.setType("*/*");
                        startActivityForResult(Intent.createChooser(openExplorerIntent, "Select a file"), SELECT_FILE_REQUEST);
                    }
                } catch (ActivityNotFoundException e) {
                }
            }
        });

        mGenerateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditText.getText().toString().length() < 1) {
                    // Perform action on clicks
                    if (null != mSelectedFileUri) {
                        miItePos = mSpinner.getSelectedItemPosition();
                        File fileToHash = new File(mSelectedFileUri.getPath());
                        if (fileToHash != null)
                            ComputeAndDisplayHash();
                        else {
                            String sWrongFile = getString(R.string.wrong_file);
                            if (mResultTV != null)
                                mResultTV.setText(sWrongFile);
                            if (mCopyButton != null)
                                mCopyButton.setVisibility(View.INVISIBLE);
                        }
                    }
                } else {
                    // Perform action on clicks
                    miItePos = mSpinner.getSelectedItemPosition();
                    Editable InputEdit = mEditText.getText();
                    msToHash = InputEdit.toString();
                    ComputeAndDisplayHash();
                }
            }
        });

        mCopyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform action on clicks
                if (mClipboard != null) {
                    mClipboard.setText(msHash);
                    String sCopied = getString(R.string.copied);
                    Toast.makeText(FileActivity.this, sCopied,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        mCheckBox.setChecked(false); // lower case by default
        mCheckBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform action on clicks
                if (!msHash.equals("")) {
                    // A hash value has already been calculated,
                    // just convert it to lower or upper case
                    String OldHash = msHash;
                    if (mCheckBox.isChecked()) {
                        msHash = OldHash.toUpperCase();
                    } else {
                        msHash = OldHash.toLowerCase();
                    }
                    if (mResultTV != null) {
                        String sResult = mResultTV.getText().toString();
                        sResult = sResult.replaceAll(OldHash, msHash);
                        mResultTV.setText(sResult);
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check for the integer request code originally supplied to startResolutionForResult().
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Log.i(TAG, "User agreed to make required location settings changes.");
                    // Nothing to do. startLocationupdates() gets called in onResume again.
                    break;
                case Activity.RESULT_CANCELED:
                    Log.i(TAG, "User chose not to make required location settings changes.");
                    mRequestingLocationUpdates = false;
                    updateUI();
                    break;
            }
        }
        if (requestCode == SELECT_FILE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                mSelectedFileUri = data.getData(); //The uri with the location of the file
                if (null != mSelectedFileUri) {
                    Cursor cursor = getContentResolver().query(mSelectedFileUri, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        String ret = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                        //mSelectFileButton.setText(ret);
                        mGenerateButton.setText("Hash " + ret);
                    }
                }
            }
        }
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                File f = new File(currentPhotoPath);
                selectedImage.setImageURI(Uri.fromFile(f));
                Log.d("tag", "Absolute url of image is " + Uri.fromFile(f));

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);

                mSelectedFileUri = Uri.fromFile(f);
                //mSelectFileButton.setText(f.getName());
                mGenerateButton.setText("Hash " + f.getName());


                uploadImageToFirebase(f.getName(), contentUri);
            }
        }

        if (requestCode == GALLERY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri contentUri = data.getData();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HH:mm:ss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "_" + getFileExt(contentUri);
                Log.d("tag", "onActivityResult: Gallery Image Uri: " + imageFileName);
                selectedImage.setImageURI(contentUri);

                //uploadImageToFirebase(imageFileName, contentUri);

                if (data != null) {
                    mSelectedFileUri = data.getData(); //The uri with the location of the file
                    if (null != mSelectedFileUri) {
                        Cursor cursor = getContentResolver().query(mSelectedFileUri, null, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            String ret = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                            //mSelectFileButton.setText(ret);
                            mGenerateButton.setText("Hash " + ret);
                            uploadImageToFirebase(ret, contentUri);

                        }
                    }
                }
            }
        }
    }

    private void ComputeAndDisplayHash() {
        if (mHashOpe == null)
            mHashOpe = new HashFunctionOperator();
        String sAlgo = "";
        if (miItePos == 0)
            sAlgo = "Adler-32";
        else if (miItePos == 1)
            sAlgo = "CRC-32";
        else if (miItePos == 2)
            sAlgo = "Haval";
        else if (miItePos == 3)
            sAlgo = "md2";
        else if (miItePos == 4)
            sAlgo = "md4";
        else if (miItePos == 5)
            sAlgo = "md5";
        else if (miItePos == 6)
            sAlgo = "ripemd-128";
        else if (miItePos == 7)
            sAlgo = "ripemd-160";
        else if (miItePos == 8)
            sAlgo = "sha-1";
        else if (miItePos == 9)
            sAlgo = "sha-256";
        else if (miItePos == 10)
            sAlgo = "sha-384";
        else if (miItePos == 11)
            sAlgo = "sha-512";
        else if (miItePos == 12)
            sAlgo = "tiger";
        else if (miItePos == 13)
            sAlgo = "whirlpool";
        mHashOpe.SetAlgorithm(sAlgo);

        String sCalculating = getString(R.string.Calculating);
        mProgressDialog = ProgressDialog.show(FileActivity.this, "",
                sCalculating, true);

        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    // Call when the thread is started
    public void run() {
        msHash = "";
        msFileSize = "";

        if (mEditText.getText().toString().length() < 1) {
            if (null != mSelectedFileUri) {
                if (mHashOpe != null) {
                    InputStream inputStream = null;
                    try {
                        inputStream = getContentResolver().openInputStream(mSelectedFileUri);

                    } catch (FileNotFoundException e1) {
                    }
                    if (null != inputStream) {
                        msHash = mHashOpe.FileToHash(inputStream);
                    }
                }

                Cursor cursor = getContentResolver().query(mSelectedFileUri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    msFileSize = FileSizeDisplay(cursor.getLong(sizeIndex), false);
                }
                handler.sendEmptyMessage(0);
            }
        } else {
            if (mHashOpe != null)
                msHash = mHashOpe.StringToHash(msToHash);
            handler1.sendEmptyMessage(0);
        }
    }

    private String FileSizeDisplay(long lbytes, boolean bSI) {
        int unit = bSI ? 1000 : 1024;
        if (lbytes < unit)
            return lbytes + " B";
        int exp = (int) (Math.log(lbytes) / Math.log(unit));
        String pre = (bSI ? "kMGTPE" : "KMGTPE").charAt(exp - 1)
                + (bSI ? "" : "i");
        return String.format("%.2f %sB", lbytes / Math.pow(unit, exp), pre);
    }

    // This method is called when the computation is over
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // Hide the progress dialog
            if (mProgressDialog != null)
                mProgressDialog.dismiss();
            if (null != mSelectedFileUri) {
                File fileToHash = new File(mSelectedFileUri.getPath());
                if (fileToHash != null) {
                    Resources res = getResources();
                    String fileName = "";
                    Cursor cursor = getContentResolver().query(mSelectedFileUri, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                    String sFileNameTitle = String
                            .format(res.getString(R.string.FileName),
                                    fileName);
                    String sFileSizeTitle = String.format(
                            res.getString(R.string.FileSize), msFileSize);
                    String sFileHashTitle = "";
                    if (!msHash.equals("")) {
                        if (mCheckBox != null) {
                            if (mCheckBox.isChecked()) {
                                msHash = msHash.toUpperCase();
                            } else {
                                msHash = msHash.toLowerCase();
                            }
                        }
                        String Function = "";
                        if (miItePos >= 0)
                            Function = mFunctions[miItePos];
                        sFileHashTitle = String.format(
                                res.getString(R.string.Hash), Function, msHash);
                        // Show the copy button
                        if (mCopyButton != null)
                            mCopyButton.setVisibility(View.GONE);
                    } else {
                        sFileHashTitle = String.format(
                                res.getString(R.string.unable_to_calculate),
                                fileToHash.getName());
                        // Hide the copy button
                        if (mCopyButton != null)
                            mCopyButton.setVisibility(View.GONE);
                    }

                    if (mResultTV != null) {
                        mResultTV.setText(sFileNameTitle + sFileSizeTitle + sFileHashTitle);
                        postMethod(caseID.getText().toString(), msHash, w3w);
                    }

                    //t1.setText(sFileHashTitle);
                }
            }
        }
    };

    private void postMethod(String name, String hash, String w3w) {
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(FileActivity.this);
            //String URL = "https://u0dy7sdnw1-u0nl8tix72-connect.us0-aws.kaleido.io/";
            String URL = "https://us-central1-boecloudtest.cloudfunctions.net/addEvidenceMetadata";
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("CaseID", name);
            jsonBody.put("PhotoSHA512", hash);
            jsonBody.put("w3w", w3w);
            final String requestBody = jsonBody.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("VOLLEY", response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("VOLLEY", error.toString());
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                        // can get more details such as response.headers
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    // This method is called when the computation is over
    private Handler handler1 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // Hide the progress dialog
            if (mProgressDialog != null)
                mProgressDialog.dismiss();

            Resources res = getResources();
            String sTextTitle = String.format(res.getString(R.string.Text),
                    msToHash);
            String sTextHashTitle = "";
            if (!msHash.equals("")) {
                if (mCheckBox != null) {
                    if (mCheckBox.isChecked()) {
                        msHash = msHash.toUpperCase();
                    } else {
                        msHash = msHash.toLowerCase();
                    }
                }
                String Function = "";
                if (miItePos >= 0)
                    Function = mFunctions[miItePos];
                sTextHashTitle = String.format(res.getString(R.string.Hash),
                        Function, msHash);
                // Show the copy button
                if (mCopyButton != null)
                    mCopyButton.setVisibility(View.GONE);
            } else {
                sTextHashTitle = String.format(
                        res.getString(R.string.unable_to_calculate), msToHash);
                // Hide the copy button
                if (mCopyButton != null)
                    mCopyButton.setVisibility(View.GONE);
            }

            if (mResultTV != null) {
                mResultTV.setText(sTextTitle + sTextHashTitle);
                postMethod(caseID.getText().toString(), msHash, w3w);
            }
        }
    };

    private String getFileExt(Uri contentUri) {
        ContentResolver c = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(contentUri));
    }

    private void uploadImageToFirebase(String name, Uri contentUri) {
        final StorageReference image = storageReference.child("pictures/" + name);
        image.putFile(contentUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d("tag", "onSuccess: Uploaded Image URl is " + uri.toString());
                    }
                });

                Toast.makeText(FileActivity.this, "Image is uploaded.", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(FileActivity.this, "Upload Failed.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void askCameraPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        }

        else {
            dispatchTakePictureIntent();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.mytestapp4firebase.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /****************
     Code for Location portion of the app BELOW
     ****************/
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        KEY_REQUESTING_LOCATION_UPDATES);
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TIME_STRING)) {
                mLastUpdateTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME_STRING);
            }
            updateUI();
        }
    }
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = new SimpleDateFormat("yyyyMMdd_HH:mm:ss").format(new Date());
                updateLocationUI();
            }
        };
    }
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }
    public void startUpdatesButtonHandler(View view) {
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            setButtonsEnabledState();
            startLocationUpdates();
        }
    }
    public void stopUpdatesButtonHandler(View view) {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        stopLocationUpdates();
    }
    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        updateUI();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(FileActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(FileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                mRequestingLocationUpdates = false;
                        }

                        updateUI();
                    }
                });

    }
    private void updateUI() {
        setButtonsEnabledState();
        updateLocationUI();
    }
    private void setButtonsEnabledState() {
        if (mRequestingLocationUpdates) {
            mStartUpdatesButton.setEnabled(false);
            mStopUpdatesButton.setEnabled(true);
        } else {
            mStartUpdatesButton.setEnabled(true);
            mStopUpdatesButton.setEnabled(false);
        }
    }
    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            mLatitudeTextView.setText(String.format(Locale.ENGLISH, "%f",
                    mCurrentLocation.getLatitude()));
            mLongitudeTextView.setText(String.format(Locale.ENGLISH, "%f",
                    mCurrentLocation.getLongitude()));
            mLastUpdateTimeTextView.setText(String.format(Locale.ENGLISH, "%s: %s",
                    mLastUpdateTimeLabel, mLastUpdateTime));

            SLat = mLatitudeTextView.getText().toString().replaceAll("\\s", "");
            SLng = mLongitudeTextView.getText().toString().replaceAll("\\s", "");
            what3words();
        }
    }

    private void what3words() {
        String Lat = SLat;
        String Lng = SLng;
        Double lat = Double.parseDouble(Lat);
        Double lng = Double.parseDouble(Lng);
        if (lat != null && lng != null) {
            Observable.fromCallable(() -> wrapper.convertTo3wa(new Coordinates(lat, lng)).execute())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                        if (result.isSuccessful()) {
                            resultConvertTo3wa.setText(String.format("3 word address: %s", result.getWords()));

                            String w3wString = result.getWords();

                            w3w = w3wString;

                        } else {
                            resultConvertTo3wa.setText(result.getError().getMessage());
                        }
                    });
        }
    }

    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.");
            return;
        }

        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mRequestingLocationUpdates = false;
                        setButtonsEnabledState();
                    }
                });
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        // Within {@code onPause()}, we remove location updates. Here, we resume receiving
//        // location updates if the user has requested them.
//        if (mRequestingLocationUpdates && checkPermissions()) {
//            startLocationUpdates();
//        } else if (!checkPermissions()) {
//            requestPermissions();
//        }
//
//        updateUI();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        // Remove location updates to save battery.
//        startLocationUpdates();
//    }
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        savedInstanceState.putString(KEY_LAST_UPDATED_TIME_STRING, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }
    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(FileActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(FileActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mRequestingLocationUpdates) {
                    Log.i(TAG, "Permission granted, updates requested, starting location updates");
                    startLocationUpdates();
                }
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }

}