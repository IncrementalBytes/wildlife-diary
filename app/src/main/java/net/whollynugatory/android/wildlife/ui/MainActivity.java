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
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.ui.viewmodel.FragmentDataViewModel;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = Utils.BASE_TAG + MainActivity.class.getSimpleName();

  private AppBarConfiguration mAppBarConfiguration;
  private BottomNavigationView mBottomNavigationView;
  private NavController mNavController;

  private boolean mDisableGroupMenu;
  private UserEntity mUserEntity;

  /*
    Activity Override(s)
  */
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "++onCreate(Bundle)");
    setContentView(R.layout.activity_main);

    mDisableGroupMenu = false;
    mBottomNavigationView = findViewById(R.id.main_bottom_navigation);
    Toolbar mainToolbar = findViewById(R.id.main_toolbar);
    setSupportActionBar(mainToolbar);

    NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
      .findFragmentById(R.id.main_fragment_container);
    if (navHostFragment != null) {
      mNavController = navHostFragment.getNavController();
      mAppBarConfiguration = new AppBarConfiguration.Builder(mNavController.getGraph()).build();
    } else {
      Log.e(TAG, "NavController was not initialized properly.");
      mDisableGroupMenu = true;
    }

    mBottomNavigationView.setOnItemSelectedListener(item -> {

      if (item.getItemId() == R.id.navigation_statistics) {
        mNavController.navigate(R.id.action_Menu_to_StatisticsFragment);
      } else if (item.getItemId() == R.id.navigation_date) {
        mNavController.navigate(R.id.action_Menu_to_ByDateFragment);
      } else if (item.getItemId() == R.id.navigation_recent) {
        mNavController.navigate(R.id.action_Menu_to_RecentFragment);
      }

      return true;
    });

    getUserInfo();
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
      mNavController.navigate(R.id.action_Menu_to_DataFragment);
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
      if (menuItem != null) {
        menuItem.setVisible(mUserEntity.IsContributor);
      }

      menuItem = menu.findItem(R.id.menu_crash);
      if (menuItem != null) {
        menuItem.setVisible(mUserEntity.IsContributor);
      }
    }

    menu.setGroupEnabled(R.id.menu_grouping, !mDisableGroupMenu);

    // disable bottom navigation
    mBottomNavigationView.setEnabled(false);
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
  private void getUserInfo() {

    Log.d(TAG, "getUserInfo()");
    String userId = Utils.getUserId(this);
    if (userId.isEmpty() || userId.equals(Utils.UNKNOWN_USER_ID)) {
      tryAgain("Unable to determine user data. Please sign out of app and try again.");
    } else {
      Log.d(TAG, "UserId: " + userId);
      FirebaseDatabase.getInstance().getReference().child(Utils.USERS_ROOT).child(userId).get()
        .addOnCompleteListener(task -> {

          if (!task.isSuccessful()) {
            Log.e(TAG, "Error getting data", task.getException());
            tryAgain("There was a problem accessing data. Try again later.");
          } else {
            DataSnapshot result = task.getResult();
            if (result != null) {
              mUserEntity = result.getValue(UserEntity.class);
              if (mUserEntity == null) {
                mUserEntity = new UserEntity(); // sets following user id
                mUserEntity.Id = userId; // assign firebase user id
                String path = Utils.combine(Utils.USERS_ROOT, userId);
                FirebaseDatabase.getInstance().getReference().child(path).setValue(mUserEntity)
                  .addOnFailureListener(e -> Log.e(TAG, "Could not create new user entry in firebase.", e));
              } else {
                mUserEntity.Id = userId;
              }

              Utils.setFollowingUserId(this, mUserEntity.FollowingId);
              Utils.setUserId(this, userId);
              Utils.setIsContributor(this, mUserEntity.IsContributor);
              invalidateOptionsMenu();
            } else {
              tryAgain("UserData returned from server was unexpected. Try again later.");
            }
          }
        });
    }
  }

  private void tryAgain(String message) {

    Log.d(TAG, "++tryAgain(String)");
    FragmentDataViewModel viewModel = new ViewModelProvider(this).get(FragmentDataViewModel.class);
    viewModel.setMessage(message);

    // disable action bar
    mDisableGroupMenu = true;
    invalidateOptionsMenu();

    mNavController.navigate(R.id.action_to_TryAgainLaterFragment);
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
