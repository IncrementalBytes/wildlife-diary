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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

  private static final String TAG = Utils.BASE_TAG + SignInActivity.class.getSimpleName();

  private ProgressBar mProgressBar;
  private Snackbar mSnackbar;

  private ActivityResultLauncher<Intent> mActivityResultLauncher;
  private FirebaseAuth mAuth;
  private GoogleSignInClient mGoogleSignInClient;

  /*
    Activity Override(s)
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "++onCreate(Bundle)");
    setContentView(R.layout.activity_sign_in);

    SignInButton signInWithGoogleButton = findViewById(R.id.sign_in_button_google);
    signInWithGoogleButton.setSize(SignInButton.SIZE_STANDARD);
    signInWithGoogleButton.setOnClickListener(this);

    mProgressBar = findViewById(R.id.sign_in_progress);
    mProgressBar.setVisibility(View.INVISIBLE);

    mAuth = FirebaseAuth.getInstance();

    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestIdToken(getString(R.string.default_web_client_id))
      .requestEmail()
      .build();

    mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    mActivityResultLauncher = registerForActivityResult(
      new ActivityResultContracts.StartActivityForResult(),
      result -> {

        if (result.getResultCode() == Activity.RESULT_OK) {
          Intent data = result.getData();
          Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
          try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null) {
              Log.d(TAG, "FirebaseAuthWithGoogle:" + account.getId());
              firebaseAuthenticateWithGoogle(account.getIdToken());
            } else {
              showErrorInSnackBar("Account returned is invalid.");
            }
          } catch (ApiException e) {
            showErrorInSnackBar("Google sign in failed", e);
          }
        }
      });
  }

  @Override
  public void onStart() {
    super.onStart();

    Log.d(TAG, "++onStart()");
    if (mAuth.getCurrentUser() != null) {
      authenticateSuccess();
    }
  }

  /*
      View Override(s)
   */
  @Override
  public void onClick(View view) {

    Log.d(TAG, "++onClick()");
    if (view.getId() == R.id.sign_in_button_google) {
      Intent signInIntent = mGoogleSignInClient.getSignInIntent();
      mActivityResultLauncher.launch(signInIntent);
    }
  }

  /*
    Private Method(s)
   */
  private void authenticateSuccess() {

    Log.d(TAG, "++authenticateSuccess()");
    if (mAuth.getCurrentUser() != null) {
      Intent intent = new Intent(SignInActivity.this, MainActivity.class);
      intent.putExtra(Utils.ARG_FIREBASE_USER_ID, mAuth.getCurrentUser().getUid());
      startActivity(intent);
      finish();
    } else {
      String message = "Authentication did not return expected account information; please try again.";
      showErrorInSnackBar(message);
    }
  }

  private void firebaseAuthenticateWithGoogle(String tokenId) {

    Log.d(TAG, "++firebaseAuthWithGoogle(String)");
    mProgressBar.setVisibility(View.VISIBLE);
    mProgressBar.setIndeterminate(true);
    AuthCredential credential = GoogleAuthProvider.getCredential(tokenId, null);
    mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {

        if (task.isSuccessful()) {
          Log.d(TAG, "signInWithCredential:success");
          authenticateSuccess();
        } else {
          Log.w(TAG, "signInWithCredential:failure", task.getException());
          showErrorInSnackBar("Authentication Failed.");
        }

        mProgressBar.setIndeterminate(false);
      });
  }

  private void showErrorInSnackBar(String message) {
    showErrorInSnackBar(message, null);
  }

  private void showErrorInSnackBar(String message, Exception ex) {

    if (ex == null) {
      Log.e(TAG, message);
    } else {
      Log.e(TAG, message, ex);
    }

    mSnackbar = Snackbar.make(
      findViewById(R.id.activity_sign_in),
      message,
      Snackbar.LENGTH_INDEFINITE);
    mSnackbar.setAction(R.string.dismiss, v -> mSnackbar.dismiss());
    mSnackbar.show();
  }
}
