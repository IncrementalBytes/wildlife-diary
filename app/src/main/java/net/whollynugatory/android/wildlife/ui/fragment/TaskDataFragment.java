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
package net.whollynugatory.android.wildlife.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.db.entity.TaskEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TaskDataFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + TaskDataFragment.class.getSimpleName();

  public interface OnTaskDataListener {

    void onTaskDataFailure(String message);

    void onTaskDataMissing();

    /*
      Instructs caller to update local db with passed data.
     */
    void onTaskDataPopulate(List<TaskEntity> taskEntityList);

    void onTaskDataSynced();
  }

  private OnTaskDataListener mCallback;

  public static TaskDataFragment newInstance() {

    Log.d(TAG, "++newInstance()");
    return new TaskDataFragment();
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    Log.d(TAG, "++onAttach(Context)");
    try {
      mCallback = (OnTaskDataListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(String.format(Locale.US, "Missing interface implementations for %s", context.toString()));
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "++onCreate(Bundle)");
    FirebaseDatabase.getInstance().getReference().child(Utils.DATA_STAMPS_ROOT).get().addOnCompleteListener(
      task -> {

        if (!task.isSuccessful()) {
          Log.d(TAG, "Checking data stamp for Tasks was unsuccessful.", task.getException());
          mCallback.onTaskDataFailure("Unable to retrieve Task data stamp.");
        } else {
          String remoteStamp = Utils.UNKNOWN_ID;
          DataSnapshot resultSnapshot = task.getResult();
          if (resultSnapshot != null) {
            for (DataSnapshot dataSnapshot : resultSnapshot.getChildren()) {
              String id = dataSnapshot.getKey();
              if (id != null && id.equals(Utils.TASK_ROOT)) {
                Object valueObject = dataSnapshot.getValue();
                if (valueObject != null) {
                  remoteStamp = valueObject.toString();
                  break;
                }
              }
            }

            String taskStamp = Utils.getTasksStamp(getActivity());
            if (taskStamp.equals(Utils.UNKNOWN_ID) || remoteStamp.equals(Utils.UNKNOWN_ID) || !taskStamp.equalsIgnoreCase(remoteStamp)) {
              Utils.setTasksStamp(getActivity(), remoteStamp);
              populateTaskTable();
            } else {
              Log.d(TAG, "Task data in-sync.");
              mCallback.onTaskDataSynced();
            }
          } else {
            mCallback.onTaskDataFailure("Task data stamp not found.");
          }
        }
      });
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    return inflater.inflate(R.layout.fragment_task_data, container, false);
  }

  private void populateTaskTable() {

    Log.d(TAG, "++populateTaskTable()");
    FirebaseDatabase.getInstance().getReference().child(Utils.TASK_ROOT).get()
      .addOnCompleteListener(task -> {

        if (!task.isSuccessful()) {
          Log.e(TAG, "Error getting data.", task.getException());
          mCallback.onTaskDataFailure("Could not retrieve Task data.");
        } else {
          DataSnapshot resultSnapshot = task.getResult();
          if (resultSnapshot != null) {
            if (resultSnapshot.getChildrenCount() > 0) {
              Log.d(TAG, "Attempting Task inserts: " + resultSnapshot.getChildrenCount());
              if (getActivity() != null) {
                List<TaskEntity> taskEntityList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : resultSnapshot.getChildren()) {
                  TaskEntity taskEntity = dataSnapshot.getValue(TaskEntity.class);
                  String id = dataSnapshot.getKey();
                  if (taskEntity != null && id != null) {
                    taskEntity.Id = id;
                    if (taskEntity.isValid()) {
                      taskEntityList.add(taskEntity);
                    } else {
                      Log.w(TAG, "Task entity was invalid, not adding: " + taskEntity.Id);
                    }
                  }
                }

                mCallback.onTaskDataPopulate(taskEntityList);
              } else {
                mCallback.onTaskDataFailure("App was not ready for operation at this time.");
              }
            } else {
              mCallback.onTaskDataMissing();
            }
          } else {
            mCallback.onTaskDataFailure("Task results not found.");
          }
        }
      });
  }
}
