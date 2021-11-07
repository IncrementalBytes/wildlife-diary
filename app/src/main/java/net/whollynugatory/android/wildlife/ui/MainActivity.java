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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import net.whollynugatory.android.wildlife.db.entity.UserEntity;
import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.ui.fragment.DateListFragment;
import net.whollynugatory.android.wildlife.ui.fragment.EncounterDetailFragment;
import net.whollynugatory.android.wildlife.ui.fragment.StatisticsFragment;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.ui.fragment.UniqueEncounterListFragment;
import net.whollynugatory.android.wildlife.ui.fragment.UserSettingsFragment;
import net.whollynugatory.android.wildlife.ui.viewmodel.FragmentDataViewModel;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = Utils.BASE_TAG + MainActivity.class.getSimpleName();

  private AppBarConfiguration mAppBarConfiguration;
  private NavController mNavController;

  private UserEntity mUserEntity;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "++onCreate(Bundle)");
    setContentView(R.layout.activity_main);

    BottomNavigationView bottomNavigationView = findViewById(R.id.main_bottom_navigation);
    Toolbar mainToolbar = findViewById(R.id.main_toolbar);
    setSupportActionBar(mainToolbar);

    NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
      .findFragmentById(R.id.main_fragment_container);
    if (navHostFragment != null) {
      mNavController = navHostFragment.getNavController();
      mAppBarConfiguration = new AppBarConfiguration.Builder(mNavController.getGraph()).build();
    }

    bottomNavigationView.setOnItemSelectedListener(item -> {

      if (item.getItemId() == R.id.navigation_statistics) {
        mNavController.navigate(R.id.action_Menu_to_StatisticsFragment);
      } else if (item.getItemId() == R.id.navigation_date) {
        mNavController.navigate(R.id.action_Menu_to_ByDateFragment);
      } else if (item.getItemId() == R.id.navigation_recent) {
        mNavController.navigate(R.id.action_Menu_to_RecentFragment);
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

    FragmentDataViewModel viewModel = new ViewModelProvider(this).get(FragmentDataViewModel.class);
    if (userId.isEmpty() || userId.equals(Utils.UNKNOWN_USER_ID)) {
      // TODO: disable all other controls
      viewModel.setMessage("Unable to determine user data. Please sign out of app and try again.");
      mNavController.navigate(R.id.action_TryAgainFragment);
    } else {
      String finalUserId = userId;
      FirebaseDatabase.getInstance().getReference().child(Utils.USERS_ROOT).child(userId).get()
        .addOnCompleteListener(task -> {

          if (!task.isSuccessful()) {
            Log.e(TAG, "Error getting data", task.getException());
            // TODO: disable all other controls
            viewModel.setMessage("There was a problem accessing data. Try again later.");
            mNavController.navigate(R.id.action_TryAgainFragment);
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
              mNavController.navigate(R.id.action_DataFragment);
            } else {
              // TODO: disable all other controls
              viewModel.setMessage("UserData returned from server was unexpected. Try again later.");
              mNavController.navigate(R.id.action_TryAgainFragment);
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
      mNavController.navigate(R.id.action_Menu_to_RecentFragment);
    } else if (item.getItemId() == R.id.menu_settings) {
      mNavController.navigate(R.id.action_Menu_to_SettingsFragment);
    } else if (item.getItemId() == R.id.menu_cleanup) {
      mNavController.navigate(R.id.action_Menu_to_CleanupFragment);
    } else if (item.getItemId() == R.id.menu_crash) {
      throw new RuntimeException("Test Crash"); // Force a crash
    } else if (item.getItemId() == R.id.menu_sync) {
      Utils.setLocalTimeStamp(this, R.string.pref_key_stamp_encounters, Utils.UNKNOWN_ID);
      Utils.setLocalTimeStamp(this, R.string.pref_key_stamp_wildlife, Utils.UNKNOWN_ID);
      Utils.setLocalTimeStamp(this, R.string.pref_key_stamp_tasks, Utils.UNKNOWN_ID);
      mNavController.navigate(R.id.action_DataFragment);
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
  public void onPause() {
    super.onPause();

    Log.d(TAG, "++onPause()");
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

  @Override
  public void onResume() {
    super.onResume();

    Log.d(TAG, "++onResume()");
  }

  @Override
  protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);

    Log.d(TAG, "++onRestoreInstanceState(Bundle)");
  }

  @Override
  protected void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);

    Log.d(TAG, "++onSaveInstanceState(Bundle)");
  }

  @Override
  public boolean onSupportNavigateUp() {

    return NavigationUI.navigateUp(mNavController, mAppBarConfiguration) || super.onSupportNavigateUp();
  }

  /*
    Private Method(s)
   */
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
