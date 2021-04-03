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
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.db.entity.TaskEntity;
import net.whollynugatory.android.wildlife.db.viewmodel.WildlifeViewModel;

import java.util.Locale;

public class TaskDataFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + TaskDataFragment.class.getSimpleName();

  public interface OnTaskDataListener {

    void onTaskDataMissing();

    void onTaskDataPopulated();
  }

  private OnTaskDataListener mCallback;

  public static TaskDataFragment newInstance() {

    Log.d(TAG, "++newInstance()");
    return new TaskDataFragment();
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    Log.d(TAG, "++onActivityCreated(Bundle)");
    WildlifeViewModel wildlifeViewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);
    wildlifeViewModel.taskCount().observe(getViewLifecycleOwner(), taskCount -> populateTaskTable());
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
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    return inflater.inflate(R.layout.fragment_task_data, container, false);
  }

  private void populateTaskTable() {

    Log.d(TAG, "++populateTaskTable()");
    FirebaseDatabase.getInstance().getReference().child(Utils.TASK_ROOT).get()
      .addOnCompleteListener(task -> {

        if (!task.isSuccessful()) {
          Log.e(TAG, "Error getting data", task.getException());
        } else {
          if (task.getResult().getChildrenCount() > 0) {
            WildlifeViewModel wildlifeViewModel = new ViewModelProvider(getActivity()).get(WildlifeViewModel.class);
            for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
              TaskEntity taskEntity = dataSnapshot.getValue(TaskEntity.class);
              taskEntity.Id = dataSnapshot.getKey();
              wildlifeViewModel.insertTask(taskEntity);
            }

            mCallback.onTaskDataPopulated();
          } else {
            mCallback.onTaskDataMissing();
          }
        }
      });
  }
}
