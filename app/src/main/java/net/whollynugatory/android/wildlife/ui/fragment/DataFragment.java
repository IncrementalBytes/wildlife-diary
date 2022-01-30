/*
 * Copyright 2022 Ryan Ward
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
package net.whollynugatory.android.wildlife.ui.fragment;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.db.WildlifeDatabase;
import net.whollynugatory.android.wildlife.db.entity.EncounterEntity;
import net.whollynugatory.android.wildlife.db.entity.TaskEntity;
import net.whollynugatory.android.wildlife.db.entity.UserEntity;
import net.whollynugatory.android.wildlife.db.entity.WildlifeEntity;

import java.util.Objects;

public class DataFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + DataFragment.class.getSimpleName();

  private TextView mStatusText;

  /*
    Fragment Override(s)
  */
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    View view = inflater.inflate(R.layout.fragment_data, container, false);
    mStatusText = view.findViewById(R.id.data_text_status);
    ImageView pawImage = view.findViewById(R.id.data_image_paw);
    pawImage.setBackgroundResource(R.drawable.anim_paw_dark);
    AnimationDrawable pawAnimation = (AnimationDrawable) pawImage.getBackground();

    pawAnimation.start();
    getUserInfo();
    return view;
  }

  private void executeDataProcessing(String dataToSync) {

    Log.d(TAG, "executeDataProcessing()");
    FirebaseDatabase.getInstance().getReference().child(Utils.DATA_STAMPS_ROOT).get()
      .addOnSuccessListener(result -> {

        updateUI("Grabbing remote data stamps...");
        String remoteDataStamp = Utils.UNKNOWN_ID;
        if (result != null) {
          for (DataSnapshot dataSnapshot : result.getChildren()) {
            String id = dataSnapshot.getKey();
            if (id != null && id.equals(dataToSync)) {
              Object valueObject = dataSnapshot.getValue();
              if (valueObject != null) {
                remoteDataStamp = dataSnapshot.getValue().toString();
                break;
              }
            }
          }

          updateUI("Comparing local data with " + dataToSync);
          String localDataStamp = Utils.UNKNOWN_ID;
          switch (dataToSync) {
            case Utils.ENCOUNTER_ROOT:
              localDataStamp = Utils.getLocalTimeStamp(getContext(), R.string.pref_key_stamp_encounters);
              break;
            case Utils.TASK_ROOT:
              localDataStamp = Utils.getLocalTimeStamp(getContext(), R.string.pref_key_stamp_tasks);
              break;
            case Utils.WILDLIFE_ROOT:
              localDataStamp = Utils.getLocalTimeStamp(getContext(), R.string.pref_key_stamp_wildlife);
              break;
          }

          if (remoteDataStamp.equals(Utils.UNKNOWN_ID)) {
            Log.w(TAG, "Remote dataStamp was unexpected: " + remoteDataStamp);
            onDataMissing();
          } else if (localDataStamp.equals(Utils.UNKNOWN_ID) || !localDataStamp.equalsIgnoreCase(remoteDataStamp)) {
            switch (dataToSync) {
              case Utils.ENCOUNTER_ROOT:
                WildlifeDatabase.databaseWriteExecutor.execute(() ->
                  WildlifeDatabase.getInstance(getContext()).encounterDao().deleteAll());
                break;
              case Utils.TASK_ROOT:
                WildlifeDatabase.databaseWriteExecutor.execute(() ->
                  WildlifeDatabase.getInstance(getContext()).encounterDao().deleteAll());
                WildlifeDatabase.databaseWriteExecutor.execute(() ->
                  WildlifeDatabase.getInstance(getContext()).taskDao().deleteAll());
                break;
              case Utils.WILDLIFE_ROOT:
                WildlifeDatabase.databaseWriteExecutor.execute(() ->
                  WildlifeDatabase.getInstance(getContext()).encounterDao().deleteAll());
                WildlifeDatabase.databaseWriteExecutor.execute(() ->
                  WildlifeDatabase.getInstance(getContext()).wildlifeDao().deleteAll());
                break;
            }

            populateTable(dataToSync, remoteDataStamp);
          } else if (localDataStamp.equals(remoteDataStamp)) {
            Log.d(TAG, "Local data in-sync with " + dataToSync);
            updateUI("Local data matches " + dataToSync);
            switch (dataToSync) {
              case Utils.ENCOUNTER_ROOT:
                setHasOptionsMenu(true);
                NavHostFragment.findNavController(this).navigate(R.id.recentFragment);
                break;
              case Utils.TASK_ROOT:
                executeDataProcessing(Utils.WILDLIFE_ROOT);
                break;
              case Utils.WILDLIFE_ROOT:
                executeDataProcessing(Utils.ENCOUNTER_ROOT);
                break;
            }
          } else {
            Log.w(TAG, "Unexpected results between local and remote data stamps.");
            tryAgain("Timestamp check failed.");
          }
        }
      })
      .addOnFailureListener(e -> {
        Log.d(TAG, "Retrieving data stamps was unsuccessful.", e);
        Toast.makeText(getContext(), "", Toast.LENGTH_SHORT).show();
      });
  }

  private void getUserInfo() {

    Log.d(TAG, "getUserInfo()");
    String userId = Utils.getUserId(getContext());
    String userName = Utils.getUserName(getContext());
    if (userId == null || userId.isEmpty() || Objects.equals(userId, Utils.UNKNOWN_USER_ID)) {
      tryAgain("Unable to determine user data. Please sign out of app and try again.");
    } else {
      Log.d(TAG, "UserId: " + userId);
      final String finalUserId = userId;
      final String finalUserName = userName;
      FirebaseDatabase.getInstance().getReference().child(Utils.USERS_ROOT).child(userId).get()
        .addOnSuccessListener(result -> {

          if (result != null) {
            UserEntity userEntity = result.getValue(UserEntity.class);
            String path = Utils.combine(Utils.USERS_ROOT, finalUserId);
            if (userEntity == null) {
              userEntity = new UserEntity(); // sets following user id
              userEntity.Id = finalUserId; // assign firebase user id
              userEntity.DisplayName = finalUserName;
              FirebaseDatabase.getInstance().getReference().child(path).setValue(userEntity)
                .addOnFailureListener(e -> Log.e(TAG, "Could not create new user entry in firebase.", e));
              Utils.setFollowingUserId(getContext(), Utils.DEFAULT_FOLLOWING_USER_ID);
            } else if (userEntity.DisplayName.isEmpty()) {
              userEntity.Id = finalUserId;
              userEntity.DisplayName = finalUserName;
              Utils.setFollowingUserId(getContext(), userEntity.FollowingId);
              FirebaseDatabase.getInstance().getReference().child(path).setValue(userEntity)
                .addOnFailureListener(e -> Log.w(TAG, "Could not update user entry in firebase.", e));
            } else {
              Utils.setFollowingUserId(getContext(), userEntity.FollowingId);
            }

            Utils.setIsContributor(getContext(), userEntity.IsContributor);
            executeDataProcessing(Utils.TASK_ROOT);
          } else {
            tryAgain("UserData returned from server was unexpected. Try again later.");
          }

          requireActivity().invalidateOptionsMenu();
        })
        .addOnFailureListener(e -> {
          Log.e(TAG, "Error getting data", e);
          tryAgain("There was a problem accessing data. Try again later.");
        });
    }
  }

  private void onDataMissing() {

    Log.d(TAG, "++onDataMissing()");
    Toast.makeText(getContext(), "No data found.", Toast.LENGTH_SHORT).show();
    NavHostFragment.findNavController(this).navigate(R.id.recentFragment);
  }

  private void populateTable(String dataRoot, String remoteDataStamp) {

    Log.d(TAG, "++populateTable(String, String)");
    switch (dataRoot) {
      case Utils.ENCOUNTER_ROOT:
        String followingUserId = Utils.getFollowingUserId(getContext());
        FirebaseDatabase.getInstance().getReference().child(dataRoot).orderByChild("UserId").equalTo(followingUserId).get()
          .addOnSuccessListener(task -> {

            Log.d(TAG, "Attempting inserts: " + task.getChildrenCount());
            if (getActivity() != null) {
              for (DataSnapshot dataSnapshot : task.getChildren()) {
                String id = dataSnapshot.getKey();
                EncounterEntity encounterEntity = dataSnapshot.getValue(EncounterEntity.class);
                if (encounterEntity != null && id != null) {
                  encounterEntity.Id = id;
                  if (encounterEntity.isValid()) {
                    WildlifeDatabase.databaseWriteExecutor.execute(() ->
                      WildlifeDatabase.getInstance(getContext()).encounterDao().insert(encounterEntity));
                  } else {
                    Log.w(TAG, "Encounter entity was invalid, not adding: " + encounterEntity);
                  }
                }
              }

              Utils.setLocalTimeStamp(getContext(), R.string.pref_key_stamp_encounters, remoteDataStamp);
              NavHostFragment.findNavController(this).navigate(R.id.recentFragment);
            }
          })
          .addOnFailureListener(e -> {
            Log.e(TAG, "Error getting data", e);
            tryAgain("Getting remote data failed.");
          });
        break;
      case Utils.TASK_ROOT:
      case Utils.WILDLIFE_ROOT:
        FirebaseDatabase.getInstance().getReference().child(dataRoot).get()
          .addOnSuccessListener(task -> {

            Log.d(TAG, "Attempting inserts: " + task.getChildrenCount());
            if (getActivity() != null) {
              for (DataSnapshot dataSnapshot : task.getChildren()) {
                String id = dataSnapshot.getKey();
                switch (dataRoot) {
                  case Utils.TASK_ROOT:
                    TaskEntity taskEntity = dataSnapshot.getValue(TaskEntity.class);
                    if (taskEntity != null && id != null) {
                      taskEntity.Id = id;
                      if (taskEntity.isValid()) {
                        WildlifeDatabase.databaseWriteExecutor.execute(() ->
                          WildlifeDatabase.getInstance(getContext()).taskDao().insert(taskEntity));
                      } else {
                        Log.w(TAG, "Task entity was invalid, not adding: " + taskEntity);
                      }
                    }

                    break;
                  case Utils.WILDLIFE_ROOT:
                    WildlifeEntity wildlifeEntity = dataSnapshot.getValue(WildlifeEntity.class);
                    if (wildlifeEntity != null && id != null) {
                      wildlifeEntity.Id = id;
                      if (wildlifeEntity.isValid()) {
                        WildlifeDatabase.databaseWriteExecutor.execute(() ->
                          WildlifeDatabase.getInstance(getContext()).wildlifeDao().insert(wildlifeEntity));
                      } else {
                        Log.w(TAG, "Wildlife entity was invalid, not adding: " + wildlifeEntity);
                      }
                    }

                    break;
                }
              }

              switch (dataRoot) {
                case Utils.TASK_ROOT:
                  Utils.setLocalTimeStamp(getContext(), R.string.pref_key_stamp_tasks, remoteDataStamp);
                  executeDataProcessing(Utils.WILDLIFE_ROOT);
                  break;
                case Utils.WILDLIFE_ROOT:
                  Utils.setLocalTimeStamp(getContext(), R.string.pref_key_stamp_wildlife, remoteDataStamp);
                  executeDataProcessing(Utils.ENCOUNTER_ROOT);
                  break;
              }
            } else {
              Toast.makeText(getContext(), "Activity is invalid.", Toast.LENGTH_SHORT).show();
            }
          })
          .addOnFailureListener(e -> {
            Log.e(TAG, "Error getting data", e);
            tryAgain("Getting remote data failed.");
          });
        break;
    }
  }

  private void tryAgain(String message) {

    Log.d(TAG, "++tryAgain(String)");
    Bundle arguments = new Bundle();
    arguments.putString(Utils.ARG_MESSAGE, message);
    NavHostFragment.findNavController(this).navigate(R.id.tryAgainLaterFragment, arguments);
  }

  private void updateUI(String message) {

    if (mStatusText != null) {
      mStatusText.setText(message);
    }
  }
}
