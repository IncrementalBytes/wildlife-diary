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

import net.whollynugatory.android.wildlife.db.entity.UserEntity;
import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.ui.fragment.EncounterDataFragment;
import net.whollynugatory.android.wildlife.ui.fragment.EncounterDetailFragment;
import net.whollynugatory.android.wildlife.ui.fragment.EncounterFragment;
import net.whollynugatory.android.wildlife.ui.fragment.EncounterListFragment;
import net.whollynugatory.android.wildlife.ui.fragment.SettingsFragment;
import net.whollynugatory.android.wildlife.ui.fragment.SummaryFragment;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.ui.fragment.TaskDataFragment;
import net.whollynugatory.android.wildlife.ui.fragment.WildlifeDataFragment;

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

    mAddEncounterButton = findViewById(R.id.main_fab_add);
    mAddEncounterButton.setVisibility(View.INVISIBLE);
    Toolbar mainToolbar = findViewById(R.id.main_toolbar);
    BottomNavigationView navigationView = findViewById(R.id.main_nav_bottom);
    setSupportActionBar(mainToolbar);

    getSupportFragmentManager().addOnBackStackChangedListener(() -> {
      Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);
      if (fragment != null) {
        String fragmentClassName = fragment.getClass().getName();
        if (fragmentClassName.equals(SettingsFragment.class.getName())) {
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
          replaceFragment(SettingsFragment.newInstance());
          return true;
      }

      return false;
    });

    FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    String userId = getIntent().getStringExtra(Utils.ARG_FIREBASE_USER_ID);
    Log.d(TAG, "Firebase UID: " + userId);
    FirebaseDatabase.getInstance().getReference().child(Utils.USERS_ROOT).child(userId).get()
      .addOnCompleteListener(task -> {

        mAddEncounterButton.setVisibility(View.INVISIBLE);
        if (!task.isSuccessful()) {
          Log.e(TAG, "Error getting data", task.getException());
          showMessageInSnackBar("There was a problem accessing data. Try again later.");
        } else {
          mUserEntity = task.getResult().getValue(UserEntity.class);
          if (mUserEntity == null) {
            mUserEntity = new UserEntity();
            mUserEntity.Id = userId;
            FirebaseDatabase.getInstance().getReference().child(Utils.USERS_ROOT).child(userId).setValue(mUserEntity)
              .addOnFailureListener(e -> Log.e(TAG, "Could not create new user entry in firebase.", e));
          } else {
            mUserEntity.Id = userId;
            if (mUserEntity.CanAdd) {
              mAddEncounterButton.setVisibility(View.VISIBLE);
              mAddEncounterButton.setOnClickListener(v -> replaceFragment(EncounterFragment.newInstance(mUserEntity.Id)));
            }
          }

          replaceFragment(TaskDataFragment.newInstance());
        }
      });

    // TODO: add animation/notification that work is happening
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
    if (item.getItemId() == R.id.menu_settings) {
      // TODO: disable home menu
      // TODO: show (if hidden) bottom navigation
      replaceFragment(SettingsFragment.newInstance());
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

  /*
    Fragment Override(s)
   */
  @Override
  public void onEncounterAdded() {

    Log.d(TAG, "++onEncounterAdded()");
    replaceFragment(EncounterDataFragment.newInstance());
  }

  @Override
  public void onEncounterDataMissing() {

    Log.d(TAG, "++onEncounterDataMissing()");
    String message = "No encounters found.";
    if (mUserEntity.CanAdd) {
      message += " Try adding some!";
    } else {
      message += " Please try again later.";
    }

    showMessageInSnackBar(message);
  }

  @Override
  public void onEncounterDataPopulated() {

    Log.d(TAG, "++onEncounterDataPopulated()");
    replaceFragment(EncounterListFragment.newInstance(mUserEntity.Id));
  }

  @Override
  public void onEncounterDetailsClicked(String encounterId) {

    Log.d(TAG, "onEncounterDetailsClicked(String)");
    replaceFragment(EncounterDetailFragment.newInstance(encounterId));
  }

  @Override
  public void onEncounterListPopulated(int size) {

    Log.d(TAG, "++onEncounterListPopulated(int)");
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
}
