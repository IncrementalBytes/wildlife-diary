/*
 * Copyright 2021 Ryan Ward
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package net.whollynugatory.android.wildlife.ui;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import net.whollynugatory.android.wildlife.db.entity.UserEntity;
import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.ui.fragment.EncounterDataFragment;
import net.whollynugatory.android.wildlife.ui.fragment.EncounterDetailFragment;
import net.whollynugatory.android.wildlife.ui.fragment.EncounterFragment;
import net.whollynugatory.android.wildlife.ui.fragment.EncounterListFragment;
import net.whollynugatory.android.wildlife.ui.fragment.SummaryFragment;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.ui.fragment.TaskDataFragment;
import net.whollynugatory.android.wildlife.ui.fragment.UserSettingsFragment;
import net.whollynugatory.android.wildlife.ui.fragment.WildlifeDataFragment;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
  EncounterDataFragment.OnEncounterDataListener,
  EncounterFragment.OnEncounterListener,
  EncounterListFragment.OnEncounterListListener,
  TaskDataFragment.OnTaskDataListener,
  WildlifeDataFragment.OnWildlifeDataListener {

  private static final String TAG = Utils.BASE_TAG + MainActivity.class.getSimpleName();

  private FloatingActionButton mAddEncounterButton;
  private Snackbar mSnackbar;

  private UserEntity mUserEntity;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "++onCreate(Bundle)");
    setContentView(R.layout.activity_main);

    manageNotificationChannel();

    mAddEncounterButton = findViewById(R.id.main_fab_add);
    mAddEncounterButton.setVisibility(View.INVISIBLE);
    Toolbar mainToolbar = findViewById(R.id.main_toolbar);
    BottomNavigationView navigationView = findViewById(R.id.main_nav_bottom);
    setSupportActionBar(mainToolbar);

    getSupportFragmentManager().addOnBackStackChangedListener(() -> {
      Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);
      if (fragment != null) {
        String fragmentClassName = fragment.getClass().getName();
        if (fragmentClassName.equals(UserSettingsFragment.class.getName())) {
          setTitle(getString(R.string.title_settings));
        } else {
          setTitle(getString(R.string.app_name));
        }
      }
    });

    navigationView.setOnNavigationItemSelectedListener(menuItem -> {

      Log.d(TAG, "++onNavigationItemSelectedListener(MenuItem)");
      switch (menuItem.getItemId()) {
        case R.id.navigation_encounters:
          replaceFragment(EncounterListFragment.newInstance(mUserEntity.Id));
          return true;
        case R.id.navigation_summary:
          replaceFragment(SummaryFragment.newInstance());
          return true;
        case R.id.navigation_settings:
          replaceFragment(UserSettingsFragment.newInstance());
          return true;
      }

      return false;
    });

    FirebaseMessaging.getInstance().subscribeToTopic("wildlifeNotification");
    String userId = getIntent().getStringExtra(Utils.ARG_FIREBASE_USER_ID);
    if (userId == null || userId.length() < 0 || userId.equals(Utils.UNKNOWN_USER_ID)) {
      userId = Utils.getUserId(this);
    }

    if (userId.length() < 0 || userId.equals(Utils.UNKNOWN_USER_ID)) {
      showMessageInSnackBar("Unable to determine user data. Please sign out of app and try again.");
    } else {
      Log.d(TAG, "Firebase UID: " + userId);
      String finalUserId = userId;
      FirebaseDatabase.getInstance().getReference().child(Utils.USERS_ROOT).child(userId).get()
        .addOnCompleteListener(task -> {

          mAddEncounterButton.setVisibility(View.INVISIBLE);
          if (!task.isSuccessful()) {
            Log.e(TAG, "Error getting data", task.getException());
            showMessageInSnackBar("There was a problem accessing data. Try again later.");
          } else {
            mUserEntity = task.getResult().getValue(UserEntity.class);
            Utils.setUserId(this, finalUserId);
            if (mUserEntity == null) {
              mUserEntity = new UserEntity();
              mUserEntity.Id = finalUserId;
              FirebaseDatabase.getInstance().getReference().child(Utils.USERS_ROOT).child(finalUserId).setValue(mUserEntity)
                .addOnFailureListener(e -> Log.e(TAG, "Could not create new user entry in firebase.", e));
            } else {
              mUserEntity.Id = finalUserId;
              if (mUserEntity.CanAdd) {
                mAddEncounterButton.setVisibility(View.VISIBLE);
                mAddEncounterButton.setOnClickListener(v -> replaceFragment(EncounterFragment.newInstance(mUserEntity.Id)));
              }
            }

            // TODO: add animation/notification that work is happening

            replaceFragment(TaskDataFragment.newInstance());
          }
        });
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    Log.d(TAG, "++onCreateOptionsMenu(Menu)");
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    Log.d(TAG, "++onDestroy()");
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    Log.d(TAG, "++onOptionsItemSelected(MenuItem)");
    if (item.getItemId() == R.id.menu_settings) {
      // TODO: disable home menu
      // TODO: show (if hidden) bottom navigation
      replaceFragment(UserSettingsFragment.newInstance());
    } else if (item.getItemId() == R.id.menu_logout) {
      // TODO: hide bottom navigation
      // TODO: disable settings and sync menu
      AlertDialog dialog = new AlertDialog.Builder(this)
        .setMessage(R.string.logout_message)
        .setPositiveButton(android.R.string.yes, (dialog1, which) -> {

          // sign out of firebase
          FirebaseAuth.getInstance().signOut();

          // sign out of google, if necessary
          GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build();
          GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
          googleSignInClient.signOut().addOnCompleteListener(this, task -> {

            // return to sign-in activity
            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            finish();
          });
        })
        .setNegativeButton(android.R.string.no, null)
        .create();
      dialog.show();
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onPause() {
    super.onPause();

    Log.d(TAG, "++onPause()");
  }

  @Override
  protected void onResume() {
    super.onResume();

    Log.d(TAG, "++onResume()");
  }

  /*
    Fragment Override(s)
   */
  @Override
  public void onEncounterAdded() {

    Log.d(TAG, "++onEncounterAdded()");
    showMessageInSnackBar("Encounter Added!");
  }

  @Override
  public void onEncounterClosed() {

    Log.d(TAG, "++onEncounterClosed()");
    replaceFragment(EncounterDataFragment.newInstance());
  }

  @Override
  public void onEncounterDataMissing() {

    Log.d(TAG, "++onEncounterDataMissing()");
//    onEncounterListPopulated(0);
    showMessageInSnackBar(
      String.format(
        Locale.US,
        "No encounters found. %s",
        mUserEntity.CanAdd ? "Try adding some!" : "Please try again later."));
  }

  @Override
  public void onEncounterDataPopulated() {

    Log.d(TAG, "++onEncounterDataPopulated()");
    replaceFragment(EncounterListFragment.newInstance(mUserEntity.Id));
  }

  @Override
  public void onEncounterDetailsClicked(String encounterId) {

    Log.d(TAG, "++onEncounterDetailsClicked(String)");
    replaceFragment(EncounterDetailFragment.newInstance(encounterId));
  }

  @Override
  public void onEncounterListPopulated(int size) {

    Log.d(TAG, "++onEncounterListPopulated(int)");
    if (size < 1) {
//      showMessageInSnackBar(
//        String.format(
//          Locale.US,
//          "No encounters found. %s",
//          mUserEntity.CanAdd ? "Try adding some!" : "Please try again later."));
    }
  }

  @Override
  public void onTaskDataMissing() {

    Log.d(TAG, "++onTaskDataMissing()");
    showMessageInSnackBar("Task data is missing from Firebase, cannot update local database.");
  }

  @Override
  public void onTaskDataPopulated() {

    Log.d(TAG, "++onTaskDataPopulated()");
    replaceFragment(WildlifeDataFragment.newInstance());
  }

  @Override
  public void onWildlifeDataMissing() {

    Log.d(TAG, "++onWildlifeDataMissing()");
    showMessageInSnackBar("Task data is missing from Firebase, cannot update local database.");
  }

  @Override
  public void onWildlifeDataPopulated() {

    Log.d(TAG, "++onWildlifeDataPopulated()");
    replaceFragment(EncounterDataFragment.newInstance());
  }

  /*
    Public Method(s)
   */
  public void summaryTableSynced() {

    Log.d(TAG, "++summaryTableSynced()");
  }

  /*
    Private Method(s)
   */
  private void manageNotificationChannel() {

    Log.d(TAG, "++manageNotificationChannel()");
    NotificationManager notificationManager = getSystemService(NotificationManager.class);
    NotificationChannel notificationChannel = notificationManager.getNotificationChannel(getString(R.string.default_notification_channel_id));
    if (notificationChannel == null) {
      Log.d(TAG, "Creating notification channel.");
      notificationChannel = new NotificationChannel(
        getString(R.string.default_notification_channel_id),
        getString(R.string.channel_name),
        NotificationManager.IMPORTANCE_DEFAULT);
      notificationChannel.setDescription(getString(R.string.channel_description));
      notificationManager.createNotificationChannel(notificationChannel);
    } else {
      Log.d(TAG, "Notification channel already exists.");
    }
  }

  private void replaceFragment(Fragment fragment) {

    Log.d(TAG, "++replaceFragment()");
    getSupportFragmentManager()
      .beginTransaction()
      .replace(R.id.main_fragment_container, fragment)
      .addToBackStack(null)
      .commit();
  }

  private void showMessageInSnackBar(String message) {

    Log.w(TAG, message);
    mSnackbar = Snackbar.make(
      findViewById(R.id.activity_main),
      message,
      Snackbar.LENGTH_LONG);
    mSnackbar.setAction(R.string.dismiss, v -> mSnackbar.dismiss());
    mSnackbar.show();
  }
}
