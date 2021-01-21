/* MainActivity.java -- 
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

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends TabActivity {

    private static final String TAG = "MainPage";

    private static final String KEY_CASEID = "caseId";
    private static final String KEY_PHOTOSHA512 = "photoSHA512";
    private static final String KEY_W3W = "w3w";

    private EditText editTextCaseId;
    private EditText editTextPhotoSHA512;
    private EditText editTextW3w;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost(); // The activity TabHost
        TabHost.TabSpec spec; // Reusable TabSpec for each tab
        Intent intent; // Reusable Intent for each tab

        /*
        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, UploadActivity.class);
        String sUploadTabTitle = getString(R.string.upload_text);
        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost
                .newTabSpec("upload")
                .setIndicator(sUploadTabTitle,
                        res.getDrawable(R.drawable.ic_tab_upload))
                .setContent(intent);
        tabHost.addTab(spec);
         */

        // Do the same for the other tabs
        String sFileTabTitle = getString(R.string.tab_file);
        intent = new Intent().setClass(this, FileActivity.class);
        spec = tabHost
                .newTabSpec("file")
                .setIndicator(sFileTabTitle,
                        res.getDrawable(R.drawable.ic_tab_file))
                .setContent(intent);
        tabHost.addTab(spec);

        /*
        // Do the same for the other tabs
        String sTextTabTitle = getString(R.string.tab_text);
        intent = new Intent().setClass(this, TextActivity.class);
        spec = tabHost
                .newTabSpec("text")
                .setIndicator(sTextTabTitle,
                        res.getDrawable(R.drawable.ic_tab_text))
                .setContent(intent);
        tabHost.addTab(spec);
         */

        /*

        // Do the same for the other tabs
        String sPostingTabTitle = getString(R.string.tab_posting);
        intent = new Intent().setClass(this, PostingActivity.class);
        spec = tabHost
                .newTabSpec("post")
                .setIndicator(sPostingTabTitle,
                        res.getDrawable(R.drawable.ic_tab_text))
                .setContent(intent);
        tabHost.addTab(spec);
         */

        tabHost.setCurrentTab(0);

        // methods called to get a smoother gradient background on all devices
        //getWindow().setFormat(PixelFormat.RGBA_8888);
        // especially for Donut 1.6
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout_menu, menu);
        return true;
    }

    public void saveNote(View v) {
        String caseId = editTextCaseId.getText().toString();
        String photoSHA512 = editTextPhotoSHA512.getText().toString();
        String w3w = editTextW3w.getText().toString();

        Map<String, Object> note = new HashMap<>();
        note.put(KEY_CASEID, caseId);
        note.put(KEY_PHOTOSHA512, photoSHA512);
        note.put(KEY_W3W, w3w);


        db.collection("evidenceMetadata").document().set(note)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Evidence Securely Uploaded", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error, Could Not Upload", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, e.toString());
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_help:
                LayoutInflater help_inflater = getLayoutInflater();
                View HelpView = help_inflater.inflate(R.layout.help,
                        (ViewGroup) findViewById(R.id.help_layout_root));

                new AlertDialog.Builder(this)
                        .setIcon(0)
                        .setTitle(getString(R.string.label_menu_help))
                        .setView(HelpView)
                        .setPositiveButton(getString(R.string.Close_but),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        // TODO Auto-generated method stub
                                    }
                                }).show();
                break;
            case R.id.menu_rateit:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri
                        .parse("market://details?id=" + getPackageName()));
                startActivity(intent);
                break;
            case R.id.menu_about:
                LayoutInflater about_inflater = getLayoutInflater();
                View AboutView = about_inflater.inflate(R.layout.about,
                        (ViewGroup) findViewById(R.id.about_layout_root));

                TextView vVersion = (TextView) AboutView
                        .findViewById(R.id.about_version);
                String sVersion = vVersion.getText().toString();
                vVersion.setText(sVersion + " " + getSoftwareVersion());

                new AlertDialog.Builder(this)
                        .setIcon(0)
                        .setTitle(getString(R.string.label_menu_about))
                        .setView(AboutView)
                        .setPositiveButton(getString(R.string.Close_but),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        // TODO Auto-generated method stub
                                    }
                                }).show();
                break;
            default:
                break;
        }
        return true;
    }

    private String getSoftwareVersion() {
        String sRetString = "";
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(
                    getPackageName(), 0);
            sRetString = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("AboutActivity", "Package name not found", e);
        }
        ;
        return sRetString;
    }
}