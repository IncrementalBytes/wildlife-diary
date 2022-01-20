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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;

public class MainActivity extends AppCompatActivity implements
  NavigationBarView.OnItemSelectedListener,
  NavigationView.OnNavigationItemSelectedListener {

  private static final String TAG = Utils.BASE_TAG + MainActivity.class.getSimpleName();

  private AppBarConfiguration mAppBarConfiguration;
  private NavController mNavController;
  private NavigationView mNavigationView;
  private DrawerLayout mDrawerLayout;

  /*
    Activity Override(s)
  */
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "++onCreate(Bundle)");
    String displayName = getIntent().getStringExtra(Utils.ARG_DISPLAY_NAME);
    String userId = getIntent().getStringExtra(Utils.ARG_USER_ID);

    setContentView(R.layout.activity_main);

    mDrawerLayout = findViewById(R.id.main_drawer_layout);
    BottomNavigationView bottomNavigationView = findViewById(R.id.main_bottom_navigation);
    mNavigationView = findViewById(R.id.main_navigation_view);
    Toolbar toolbar = findViewById(R.id.main_toolbar);

    setSupportActionBar(toolbar);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
      this,
      mDrawerLayout,
      toolbar,
      R.string.navigation_drawer_open,
      R.string.navigation_drawer_close);
    mDrawerLayout.addDrawerListener(toggle);
    toggle.syncState();
    mNavigationView.setNavigationItemSelectedListener(this);

    bottomNavigationView.setOnItemSelectedListener(this);
    View navigationHeaderView = mNavigationView.inflateHeaderView(R.layout.header_main);
    TextView navigationFullName = navigationHeaderView.findViewById(R.id.header_text_full_name);
    navigationFullName.setText(displayName);

    Bundle arguments = new Bundle();
    arguments.putString(Utils.ARG_USER_ID, userId);
    arguments.putString(Utils.ARG_DISPLAY_NAME, displayName);
    NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
      .findFragmentById(R.id.main_fragment_container);
    if (navHostFragment != null) {
      mNavController = navHostFragment.getNavController();
      mNavController.setGraph(R.navigation.nav_graph, arguments);
      mAppBarConfiguration = new AppBarConfiguration.Builder(mNavController.getGraph()).build();
    } else {
      Log.e(TAG, "NavController was not initialized properly.");
    }
  }

  @Override
  public void onBackPressed() {

    Log.d(TAG, "++onBackPressed()");
    if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
      mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    mNavController.popBackStack();
  }

  @Override
  public void onPause() {
    super.onPause();

    Log.d(TAG, "++onPause()");
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {

    Log.d(TAG, "++onPrepareOptionsMenu(Menu)");
    mNavigationView.getMenu().setGroupVisible(R.id.group_contributor, Utils.getIsContributor(this));

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

    Log.d(TAG, "++onSupportNavigateUp()");
    return NavigationUI.navigateUp(mNavController, mAppBarConfiguration) || super.onSupportNavigateUp();
  }

  /*
    Override(s)
   */
  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {

    if (item.getItemId() == R.id.menu_home) {
      mNavController.navigate(R.id.recentFragment);
    } else if (item.getItemId() == R.id.menu_settings) {
      mNavController.navigate(R.id.userSettingsFragment);
    } else if (item.getItemId() == R.id.menu_administrative) {
      mNavController.navigate(R.id.administrativeFragment);
    } else if (item.getItemId() == R.id.menu_cleanup) {
      mNavController.navigate(R.id.cleanUpListFragment);
    } else if (item.getItemId() == R.id.menu_crash) {
      throw new RuntimeException("Test Crash"); // Force a crash
    } else if (item.getItemId() == R.id.menu_sync) {
      Utils.setLocalTimeStamp(getApplicationContext(), R.string.pref_key_stamp_encounters, Utils.UNKNOWN_ID);
      Utils.setLocalTimeStamp(getApplicationContext(), R.string.pref_key_stamp_wildlife, Utils.UNKNOWN_ID);
      Utils.setLocalTimeStamp(getApplicationContext(), R.string.pref_key_stamp_tasks, Utils.UNKNOWN_ID);
      mNavController.navigate(R.id.dataFragment);
    } else if (item.getItemId() == R.id.navigation_statistics) {
      mNavController.navigate(R.id.statisticsFragment);
    } else if (item.getItemId() == R.id.navigation_date) {
      mNavController.navigate(R.id.dateListFragment);
    } else if (item.getItemId() == R.id.navigation_recent) {
      mNavController.navigate(R.id.recentFragment);
    } else if (item.getItemId() == R.id.menu_logout) {
      AlertDialog alertDialog = new AlertDialog.Builder(getApplicationContext())
        .setMessage(R.string.logout_message)
        .setPositiveButton(android.R.string.yes, (dialog, which) -> signOut())
        .setNegativeButton(android.R.string.no, null)
        .create();
      alertDialog.show();
    }

    if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
      mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    return true;
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
