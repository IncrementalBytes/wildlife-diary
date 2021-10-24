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
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import net.whollynugatory.android.wildlife.db.entity.UserEntity;
import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.ui.fragment.CleanUpListFragment;
import net.whollynugatory.android.wildlife.ui.fragment.DataFragment;
import net.whollynugatory.android.wildlife.ui.fragment.DateListFragment;
import net.whollynugatory.android.wildlife.ui.fragment.EncounterDetailFragment;
import net.whollynugatory.android.wildlife.ui.fragment.EncounterDetailsListFragment;
import net.whollynugatory.android.wildlife.ui.fragment.EncounterFragment;
import net.whollynugatory.android.wildlife.ui.fragment.MostEncounteredFragment;
import net.whollynugatory.android.wildlife.ui.fragment.RecentFragment;
import net.whollynugatory.android.wildlife.ui.fragment.StatisticsFragment;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.ui.fragment.TryAgainLaterFragment;
import net.whollynugatory.android.wildlife.ui.fragment.UniqueEncounterListFragment;
import net.whollynugatory.android.wildlife.ui.fragment.UserSettingsFragment;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
  DataFragment.OnDataListener,
  DateListFragment.OnDateListListener,
  EncounterDetailsListFragment.OnEncounterListListener,
  EncounterFragment.OnEncounterListener,
  RecentFragment.OnRecentListener,
  StatisticsFragment.OnStatisticsListListener,
  TryAgainLaterFragment.OnTryAgainLaterListener {

  private static final String TAG = Utils.BASE_TAG + MainActivity.class.getSimpleName();

  private Snackbar mSnackbar;

  private UserEntity mUserEntity;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "++onCreate(Bundle)");
    setContentView(R.layout.activity_main);

    BottomNavigationView bottomNavigationView = findViewById(R.id.main_bottom_navigation);
    Toolbar mainToolbar = findViewById(R.id.main_toolbar);

    setSupportActionBar(mainToolbar);
    bottomNavigationView.setOnItemSelectedListener(item -> {

      if (item.getItemId() == R.id.navigation_statistics) {
        replaceFragment(StatisticsFragment.newInstance());
      } else if (item.getItemId() == R.id.navigation_date) {
        replaceFragment(DateListFragment.newInstance());
      } else if (item.getItemId() == R.id.navigation_recent) {
        replaceFragment(RecentFragment.newInstance());
      }

      return true;
    });

    getSupportFragmentManager().addOnBackStackChangedListener(() -> {
      Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);
      if (fragment != null) {
        String fragmentClassName = fragment.getClass().getName();
        if (fragmentClassName.equals(UserSettingsFragment.class.getName())) {
          setTitle(getString(R.string.title_settings));
        } else if (fragmentClassName.equals(StatisticsFragment.class.getName())) {
          setTitle(getString(R.string.title_statistics));
        } else if (fragmentClassName.equals(DateListFragment.class.getName())) {
          setTitle(getString(R.string.title_encounters_by_date));
        } else if (fragmentClassName.equals(EncounterDetailFragment.class.getName())) {
          setTitle(getString(R.string.title_encounter_details));
        } else if (fragmentClassName.equals(UniqueEncounterListFragment.class.getName())) {
          setTitle(getString(R.string.title_unique_encounters));
        } else {
          setTitle(getString(R.string.app_name));
        }
      }
    });

    String userId = getIntent().getStringExtra(Utils.ARG_FIREBASE_USER_ID);
    if (userId == null || userId.equals(Utils.UNKNOWN_USER_ID)) {
      userId = Utils.getUserId(this);
    }

    if (userId.isEmpty() || userId.equals(Utils.UNKNOWN_USER_ID)) {
      replaceFragment(
        TryAgainLaterFragment.newInstance(
          "Unable to determine user data. Please sign out of app and try again."));
    } else {
      String finalUserId = userId;
      FirebaseDatabase.getInstance().getReference().child(Utils.USERS_ROOT).child(userId).get()
        .addOnCompleteListener(task -> {

          if (!task.isSuccessful()) {
            Log.e(TAG, "Error getting data", task.getException());
            replaceFragment(
              TryAgainLaterFragment.newInstance(
                "There was a problem accessing data. Try again later."));
          } else {
            DataSnapshot result = task.getResult();
            if (result != null) {
              mUserEntity = result.getValue(UserEntity.class);
              if (mUserEntity == null) {
                mUserEntity = new UserEntity(); // sets following user id
                mUserEntity.Id = finalUserId; // assign firebase user id
                String path = Utils.combine(Utils.USERS_ROOT, finalUserId);
                FirebaseDatabase.getInstance().getReference().child(path).setValue(mUserEntity)
                  .addOnFailureListener(e -> Log.e(TAG, "Could not create new user entry in firebase.", e));
              } else {
                mUserEntity.Id = finalUserId;
              }

              Utils.setFollowingUserId(this, mUserEntity.FollowingId);
              Utils.setUserId(this, finalUserId);
              Utils.setIsContributor(this, mUserEntity.IsContributor);
              replaceFragment(DataFragment.newInstance());
            } else {
              replaceFragment(
                TryAgainLaterFragment.newInstance(
                  "UserData returned from server was unexpected. Try again later."));
            }

            invalidateOptionsMenu();
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
  public boolean onOptionsItemSelected(MenuItem item) {

    Log.d(TAG, "++onOptionsItemSelected(MenuItem)");
    if (item.getItemId() == R.id.menu_home) {
      replaceFragment(RecentFragment.newInstance());
    } else if (item.getItemId() == R.id.menu_settings) {
      replaceFragment(UserSettingsFragment.newInstance());
    } else if (item.getItemId() == R.id.menu_cleanup) {
      replaceFragment(CleanUpListFragment.newInstance());
    } else if (item.getItemId() == R.id.menu_crash) {
      throw new RuntimeException("Test Crash"); // Force a crash
    } else if (item.getItemId() == R.id.menu_sync) {
      Utils.setLocalTimeStamp(this, R.string.pref_key_stamp_encounters, Utils.UNKNOWN_ID);
      Utils.setLocalTimeStamp(this, R.string.pref_key_stamp_wildlife, Utils.UNKNOWN_ID);
      Utils.setLocalTimeStamp(this, R.string.pref_key_stamp_tasks, Utils.UNKNOWN_ID);
      replaceFragment(DataFragment.newInstance());
    } else if (item.getItemId() == R.id.menu_logout) {
      AlertDialog alertDialog = new AlertDialog.Builder(this)
        .setMessage(R.string.logout_message)
        .setPositiveButton(android.R.string.yes, (dialog, which) -> signOut())
        .setNegativeButton(android.R.string.no, null)
        .create();
      alertDialog.show();
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {

    Log.d(TAG, "++onPrepareOptionsMenu(Menu)");
    if (mUserEntity != null) {
      MenuItem menuItem = menu.findItem(R.id.menu_cleanup);
      menuItem.setVisible(mUserEntity.IsContributor);
      menuItem = menu.findItem(R.id.menu_crash);
      menuItem.setVisible(mUserEntity.IsContributor);
    }

    return super.onPrepareOptionsMenu(menu);
  }

  /*
    Fragment Override(s)
   */
  @Override
  public void onDataEncountersPopulated() {

    Log.d(TAG, "++onDataEncountersPopulated(String)");
    replaceFragment(RecentFragment.newInstance());
  }

  @Override
  public void onDataFailed(String message) {

    Log.d(TAG, "++onDataFailed(String)");
    Utils.setLocalTimeStamp(this, R.string.pref_key_stamp_encounters, Utils.UNKNOWN_ID);
    showMessageInSnackBar(message);
  }

  @Override
  public void onDataMissing() {

    Log.d(TAG, "++onDataMissing()");
    Utils.setLocalTimeStamp(this, R.string.pref_key_stamp_encounters, Utils.UNKNOWN_ID);
    showMessageInSnackBar(
      String.format(
        Locale.US,
        "No encounters found. %s",
        mUserEntity.IsContributor ? "Try adding some!" : "Please try again later."));
    replaceFragment(StatisticsFragment.newInstance());
  }

  @Override
  public void onDateListItemClicked() {

    Log.d(TAG, "++onDateListItemClicked()");
    replaceFragment(EncounterDetailsListFragment.newInstance());
  }

  @Override
  public void onEncounterAdded() {

    Log.d(TAG, "++onEncounterAdded()");
    showMessageInSnackBar("Encounter Added!");
  }

  @Override
  public void onEncounterDeleted() {

    Log.d(TAG, "++onEncounterDeleted()");
    replaceFragment(DataFragment.newInstance());
  }

  @Override
  public void onEncountersRecorded() {

    Log.d(TAG, "++onEncountersRecorded()");
    replaceFragment(DataFragment.newInstance());
  }

  @Override
  public void onEncounterFailed(String message) {

    Log.d(TAG, "++onEncounterFailed(String)");
    showMessageInSnackBar(message);
  }

  @Override
  public void onEncounterDetailsClicked(String encounterId) {

    Log.d(TAG, "++onEncounterDetailsClicked(String)");
    if (mUserEntity.IsContributor) {
      replaceFragment(EncounterFragment.newInstance(encounterId));
    } else {
      replaceFragment(EncounterDetailFragment.newInstance(encounterId));
    }
  }

  @Override
  public void onRecentAddEncounter() {

    Log.d(TAG, "++onRecentAddEncounter()");
    replaceFragment(EncounterFragment.newInstance());
  }

  @Override
  public void onRecentItemClicked(String encounterId) {

    Log.d(TAG, "++onRecentItemClicked(String)");
    if (mUserEntity.IsContributor) {
      replaceFragment(EncounterFragment.newInstance(encounterId));
    } else {
      replaceFragment(EncounterDetailFragment.newInstance(encounterId));
    }
  }

  @Override
  public void onStatisticsMostEncountered() {

    Log.d(TAG, "++onStatisticsMostEncountered()");
    replaceFragment(MostEncounteredFragment.newInstance());
  }

  @Override
  public void onStatisticsTotalEncounters() {

    Log.d(TAG, "++onStatisticsTotalEncounters()");
    Utils.setEncounterDetailsList(this, new ArrayList<>());
    replaceFragment(EncounterDetailsListFragment.newInstance());
  }

  @Override
  public void onStatisticsUniqueEncounters() {

    Log.d(TAG, "++onStatisticsUniqueEncounters()");
    replaceFragment(UniqueEncounterListFragment.newInstance());
  }

  @Override
  public void onTryAgainLaterSignOut() {

    Log.d(TAG, "++onTryAgainLaterSignOut()");
    signOut();
  }

  @Override
  public void onTryAgainLaterTryAgain() {

    Log.d(TAG, "++onTryAgainLaterTryAgain()");
    finish();
    startActivity(getIntent());
  }

  /*
    Private Method(s)
   */
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

  private void signOut() {

    Log.d(TAG, "++signOut()");
    FirebaseAuth.getInstance().signOut();
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestIdToken(getString(R.string.default_web_client_id))
      .requestEmail()
      .build();
    GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
    googleSignInClient.signOut().addOnCompleteListener(this, task -> {

      startActivity(new Intent(getApplicationContext(), SignInActivity.class));
      finish();
    });
  }
}
