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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import net.whollynugatory.android.wildlife.InsertEncountersAsync;
import net.whollynugatory.android.wildlife.InsertTasksAsync;
import net.whollynugatory.android.wildlife.InsertWildlifeAsync;
import net.whollynugatory.android.wildlife.db.WildlifeDatabase;
import net.whollynugatory.android.wildlife.db.entity.EncounterDetails;
import net.whollynugatory.android.wildlife.db.entity.EncounterEntity;
import net.whollynugatory.android.wildlife.db.entity.TaskEntity;
import net.whollynugatory.android.wildlife.db.entity.UserEntity;
import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.db.entity.WildlifeEntity;
import net.whollynugatory.android.wildlife.ui.fragment.EncounterDataFragment;
import net.whollynugatory.android.wildlife.ui.fragment.EncounterDetailFragment;
import net.whollynugatory.android.wildlife.ui.fragment.EncounterFragment;
import net.whollynugatory.android.wildlife.ui.fragment.EncounterListFragment;
import net.whollynugatory.android.wildlife.ui.fragment.ListFragment;
import net.whollynugatory.android.wildlife.ui.fragment.SummaryFragment;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.ui.fragment.TaskDataFragment;
import net.whollynugatory.android.wildlife.ui.fragment.UserSettingsFragment;
import net.whollynugatory.android.wildlife.ui.fragment.WildlifeDataFragment;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
  EncounterDataFragment.OnEncounterDataListener,
  EncounterFragment.OnEncounterListener,
  EncounterListFragment.OnEncounterListListener,
  ListFragment.OnSimpleListListener,
  SummaryFragment.OnSummaryListListener,
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
    setSupportActionBar(mainToolbar);

    getSupportFragmentManager().addOnBackStackChangedListener(() -> {
      Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);
      if (fragment != null) {
        String fragmentClassName = fragment.getClass().getName();
        if (fragmentClassName.equals(SummaryFragment.class.getName())) {
          mAddEncounterButton.setVisibility(View.VISIBLE);
        } else if (fragmentClassName.equals(UserSettingsFragment.class.getName())) {
          setTitle(getString(R.string.title_settings));
          mAddEncounterButton.setVisibility(View.INVISIBLE);
        } else {
          setTitle(getString(R.string.app_name));
          mAddEncounterButton.setVisibility(View.INVISIBLE);
        }
      }
    });

    FirebaseMessaging.getInstance().subscribeToTopic("wildlifeNotification");

    String userId = getIntent().getStringExtra(Utils.ARG_FIREBASE_USER_ID);
    if (userId == null || userId.equals(Utils.UNKNOWN_USER_ID)) {
      userId = Utils.getUserId(this);
    }

    if (userId.isEmpty() || userId.equals(Utils.UNKNOWN_USER_ID)) {
      showMessageInSnackBar("Unable to determine user data. Please sign out of app and try again.");
    } else {
      String finalUserId = userId;
      FirebaseDatabase.getInstance().getReference().child(Utils.USERS_ROOT).child(userId).get()
        .addOnCompleteListener(task -> {

          mAddEncounterButton.setVisibility(View.INVISIBLE);
          if (!task.isSuccessful()) {
            Log.e(TAG, "Error getting data", task.getException());
            showMessageInSnackBar("There was a problem accessing data. Try again later.");
          } else {
            DataSnapshot result = task.getResult();
            if (result != null) {
              mUserEntity = result.getValue(UserEntity.class);
              Utils.setUserId(this, finalUserId);
              if (mUserEntity == null) {
                // TODO: default following user to ???
                mUserEntity = new UserEntity();
                mUserEntity.Id = finalUserId;
                String path = Utils.combine(Utils.USERS_ROOT, finalUserId);
                FirebaseDatabase.getInstance().getReference().child(path).setValue(mUserEntity)
                  .addOnFailureListener(e -> Log.e(TAG, "Could not create new user entry in firebase.", e));
              } else {
                mUserEntity.Id = finalUserId;
                Utils.setFollowingUserId(this, mUserEntity.FollowingId);
                if (mUserEntity.CanAdd) {
                  mAddEncounterButton.setVisibility(View.VISIBLE);
                  mAddEncounterButton.setOnClickListener(v ->
                    replaceFragment(EncounterFragment.newInstance()));
                }
              }

              // TODO: add animation/notification that work is happening
              replaceFragment(TaskDataFragment.newInstance());
            } else {
              showMessageInSnackBar("Data returned from cloud was unexpected. Try again later.");
            }
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
    if (item.getItemId() == R.id.menu_home) {
      replaceFragment(SummaryFragment.newInstance());
    } else if (item.getItemId() == R.id.menu_settings) {
      replaceFragment(UserSettingsFragment.newInstance());
    } else if (item.getItemId() == R.id.menu_logout) {
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
  public void onEncounterFailure(String message) {

    Log.d(TAG, "++onEncounterFailure(String)");
    showMessageInSnackBar(message);
  }

  @Override
  public void onEncounterDataFailure(String message) {

    Log.d(TAG, "++onEncounterDataFailure(String)");
    Utils.setEncountersStamp(this, Utils.UNKNOWN_ID);
    showMessageInSnackBar(message);
  }

  @Override
  public void onEncounterDataMissing() {

    Log.d(TAG, "++onEncounterDataMissing()");
    Utils.setEncountersStamp(this, Utils.UNKNOWN_ID);
    showMessageInSnackBar(
      String.format(
        Locale.US,
        "No encounters found. %s",
        mUserEntity.CanAdd ? "Try adding some!" : "Please try again later."));
  }

  @Override
  public void onEncounterDataPopulate(List<EncounterEntity> encounterEntityList) {

    Log.d(TAG, "++onEncounterDataPopulate(List<EncounterEntity>)");
    new InsertEncountersAsync(
      MainActivity.this,
      WildlifeDatabase.getInstance(this).encounterDao(),
      encounterEntityList).execute();
  }

  @Override
  public void onEncounterDataSynced() {

    Log.d(TAG, "++onEncounterDataSynced()");
    replaceFragment(SummaryFragment.newInstance());
  }

  @Override
  public void onEncounterDetailsClicked(EncounterDetails encounterDetails) {

    Log.d(TAG, "++onEncounterDetailsClicked(EncounterDetails)");
    replaceFragment(EncounterDetailFragment.newInstance(encounterDetails));
  }

  @Override
  public void onEncounterItemSelected(String encounterId) {

    Log.d(TAG, "++onEncounterItemSelected(String)");
  }

  @Override
  public void onEncounterListPopulated() {

    Log.d(TAG, "++onEncounterListPopulated()");
    // TODO: add notification about something/anything (or just delete this callback)
  }

  @Override
  public void onSummaryMostEncountered() {

    Log.d(TAG, "onSummaryMostEncountered()");
    replaceFragment(ListFragment.newInstance(Utils.ListTypes.MostEncountered));
  }

  @Override
  public void onSummaryTotalEncounters() {

    Log.d(TAG, "++onSummaryTotalEncounters()");
    replaceFragment(EncounterListFragment.newInstance());
  }

  @Override
  public void onSummaryUniqueEncounters() {

    Log.d(TAG, "++onSummaryUniqueEncounters()");
    replaceFragment(ListFragment.newInstance(Utils.ListTypes.UniqueEncountered));
  }

  @Override
  public void onTaskDataFailure(String message) {

    Log.d(TAG, "++onTaskDataFailure(String)");
    Utils.setTasksStamp(this, Utils.UNKNOWN_ID);
    showMessageInSnackBar(message);
  }

  @Override
  public void onTaskDataMissing() {

    Log.d(TAG, "++onTaskDataMissing()");
    Utils.setTasksStamp(this, Utils.UNKNOWN_ID);
    showMessageInSnackBar("Task data is missing from Firebase, cannot update local database.");
  }

  @Override
  public void onTaskDataPopulate(List<TaskEntity> taskEntityList) {

    Log.d(TAG, "++onTaskDataPopulate(List<TaskEntity>)");
    Utils.setTaskList(this, taskEntityList);
    new InsertTasksAsync(
      MainActivity.this,
      WildlifeDatabase.getInstance(this).taskDao(),
      taskEntityList).execute();
  }

  @Override
  public void onTaskDataSynced() {

    Log.d(TAG, "++onTaskDataSynced()");
    replaceFragment(WildlifeDataFragment.newInstance());
  }

  @Override
  public void onTaskItemSelected(String taskId) {

    Log.d(TAG, "++onTaskItemSelected(String)");
  }

  @Override
  public void onUnknownList() {

    Log.d(TAG, "onUnknownList()");
  }

  @Override
  public void onWildlifeDataFailure(String message) {

    Log.d(TAG, "++onWildlifeDataFailure(String)");
    Utils.setWildlifeStamp(this, Utils.UNKNOWN_ID);
    showMessageInSnackBar(message);
  }

  @Override
  public void onWildlifeDataMissing() {

    Log.d(TAG, "++onWildlifeDataMissing()");
    Utils.setWildlifeStamp(this, Utils.UNKNOWN_ID);
    showMessageInSnackBar("Task data is missing from Firebase, cannot update local database.");
  }

  @Override
  public void onWildlifeDataPopulate(List<WildlifeEntity> wildlifeEntityList) {

    Log.d(TAG, "++onWildlifeDataPopulate(List<WildlifeEntity>)");
    new InsertWildlifeAsync(
      MainActivity.this,
      WildlifeDatabase.getInstance(this).wildlifeDao(),
      wildlifeEntityList).execute();
  }

  @Override
  public void onWildlifeDataSynced() {

    Log.d(TAG, "++onWildlifeDataSynced()");
    replaceFragment(EncounterDataFragment.newInstance());
  }

  @Override
  public void onWildlifeItemSelected(String wildlifeId) {

    Log.d(TAG, "++onWildlifeItemSelected(String)");
  }

  /*
    Public Method(s)
   */
  public void encounterInsertionComplete() {

    Log.d(TAG, "++encounterInsertionComplete()");
    replaceFragment(SummaryFragment.newInstance());
  }

  public void taskInsertionComplete() {

    Log.d(TAG, "++taskInsertionComplete()");
    replaceFragment(WildlifeDataFragment.newInstance());
  }

  public void wildlifeInsertionComplete() {

    Log.d(TAG, "++wildlifeInsertionComplete()");
    replaceFragment(EncounterDataFragment.newInstance());
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
