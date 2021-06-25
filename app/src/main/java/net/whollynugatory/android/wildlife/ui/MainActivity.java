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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import net.whollynugatory.android.wildlife.db.entity.UserEntity;
import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.ui.fragment.DataFragment;
import net.whollynugatory.android.wildlife.ui.fragment.EncounterDetailFragment;
import net.whollynugatory.android.wildlife.ui.fragment.EncounterFragment;
import net.whollynugatory.android.wildlife.ui.fragment.EncounterListFragment;
import net.whollynugatory.android.wildlife.ui.fragment.ListFragment;
import net.whollynugatory.android.wildlife.ui.fragment.SummaryFragment;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.ui.fragment.TryAgainLaterFragment;
import net.whollynugatory.android.wildlife.ui.fragment.UserSettingsFragment;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
  DataFragment.OnDataListener,
  EncounterFragment.OnEncounterListener,
  EncounterListFragment.OnEncounterListListener,
  ListFragment.OnSimpleListListener,
  SummaryFragment.OnSummaryListListener,
  TryAgainLaterFragment.OnTryAgainLaterListener {

  private static final String TAG = Utils.BASE_TAG + MainActivity.class.getSimpleName();

  private Snackbar mSnackbar;

  private UserEntity mUserEntity;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "++onCreate(Bundle)");
    setContentView(R.layout.activity_main);

    Toolbar mainToolbar = findViewById(R.id.main_toolbar);
    setSupportActionBar(mainToolbar);

    getSupportFragmentManager().addOnBackStackChangedListener(() -> {
      Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);
      if (fragment != null) {
        String fragmentClassName = fragment.getClass().getName();
        if (fragmentClassName.equals(SummaryFragment.class.getName())) {
          setTitle(getString(R.string.app_name));
        } else if (fragmentClassName.equals(UserSettingsFragment.class.getName())) {
          setTitle(getString(R.string.title_settings));
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
              replaceFragment(DataFragment.newInstance(Utils.TASK_ROOT));
            } else {
              replaceFragment(
                TryAgainLaterFragment.newInstance(
                  "UserData returned from server was unexpected. Try again later."));
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
  public boolean onOptionsItemSelected(MenuItem item) {

    Log.d(TAG, "++onOptionsItemSelected(MenuItem)");
    if (item.getItemId() == R.id.menu_home) {
      replaceFragment(SummaryFragment.newInstance());
    } else if (item.getItemId() == R.id.menu_settings) {
      replaceFragment(UserSettingsFragment.newInstance());
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

  /*
    Fragment Override(s)
   */
  @Override
  public void onDataEncountersPopulated() {

    Log.d(TAG, "++onDataEncountersPopulated(String)");
    replaceFragment(SummaryFragment.newInstance());
  }

  @Override
  public void onDataFailed(String message) {

    Log.d(TAG, "++onDataFailed(String)");
    Utils.setEncountersStamp(this, Utils.UNKNOWN_ID);
    showMessageInSnackBar(message);
  }

  @Override
  public void onDataMissing() {

    Log.d(TAG, "++onDataMissing()");
    Utils.setEncountersStamp(this, Utils.UNKNOWN_ID);
    showMessageInSnackBar(
      String.format(
        Locale.US,
        "No encounters found. %s",
        mUserEntity.IsContributor ? "Try adding some!" : "Please try again later."));
    replaceFragment(SummaryFragment.newInstance());
  }

  @Override
  public void onDataTasksPopulated() {

    Log.d(TAG, "++onDataTasksPopulated(String)");
    replaceFragment(DataFragment.newInstance(Utils.WILDLIFE_ROOT));
  }

  @Override
  public void onDataWildlifePopulated() {

    Log.d(TAG, "++onDataWildlifePopulated(String)");
    replaceFragment(DataFragment.newInstance(Utils.ENCOUNTER_ROOT));
  }

  @Override
  public void onEncounterAdded() {

    Log.d(TAG, "++onEncounterAdded()");
    showMessageInSnackBar("Encounter Added!");
  }

  @Override
  public void onEncounterDeleted() {

    Log.d(TAG, "++onEncounterDeleted()");
    replaceFragment(DataFragment.newInstance(Utils.ENCOUNTER_ROOT));
  }

  @Override
  public void onEncountersRecorded() {

    Log.d(TAG, "++onEncountersRecorded()");
    replaceFragment(DataFragment.newInstance(Utils.ENCOUNTER_ROOT));
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
  public void onSummaryAddEncounter() {

    Log.d(TAG, "++onSummaryAddEncounter()");
    replaceFragment(EncounterFragment.newInstance());
  }

  @Override
  public void onSummaryClicked(int summaryId) {

    Log.d(TAG, "++onSummaryClicked(int)");
    replaceFragment(ListFragment.newInstance(summaryId));
  }

  @Override
  public void onSummaryTotalEncounters() {

    Log.d(TAG, "++onSummaryTotalEncounters()");
    replaceFragment(EncounterListFragment.newInstance());
  }

  @Override
  public void onTaskListSet(String titleUpdate) {

    setTitle(titleUpdate);
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

  @Override
  public void onUnknownList() {

    Log.d(TAG, "onUnknownList()");
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
