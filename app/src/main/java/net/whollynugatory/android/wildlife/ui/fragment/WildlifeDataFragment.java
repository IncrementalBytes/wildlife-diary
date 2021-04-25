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
import net.whollynugatory.android.wildlife.db.entity.WildlifeEntity;
import net.whollynugatory.android.wildlife.db.viewmodel.WildlifeViewModel;

import java.util.Locale;

public class WildlifeDataFragment  extends Fragment {

  private static final String TAG = Utils.BASE_TAG + WildlifeDataFragment.class.getSimpleName();

  public interface OnWildlifeDataListener {

    void onWildlifeDataFailure(String message);

    void onWildlifeDataMissing();

    void onWildlifeDataPopulated();
  }

  private OnWildlifeDataListener mCallback;

  public static WildlifeDataFragment newInstance() {

    Log.d(TAG, "++newInstance()");
    return new WildlifeDataFragment();
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    Log.d(TAG, "++onActivityCreated(Bundle)");
    FirebaseDatabase.getInstance().getReference().child(Utils.DATA_STAMPS_ROOT).get().addOnCompleteListener(
      task -> {

        if (!task.isSuccessful()) {
          Log.d(TAG, "Checking data stamp for Wildlife was unsuccessful.", task.getException());
          mCallback.onWildlifeDataFailure("Unable to retrieve Wildlife data stamp.");
        } else {
          String remoteStamp = Utils.UNKNOWN_ID;
          DataSnapshot resultSnapshot = task.getResult();
          if (resultSnapshot != null) {
            for (DataSnapshot dataSnapshot : resultSnapshot.getChildren()) {
              String id = dataSnapshot.getKey();
              if (id != null && id.equals(Utils.WILDLIFE_ROOT)) {
                Object valueObject = dataSnapshot.getValue();
                if (valueObject != null) {
                  remoteStamp = valueObject.toString();
                  break;
                }
              }
            }

            String wildlifeStamp = Utils.getWildlifeStamp(getActivity());
            if (wildlifeStamp.equals(Utils.UNKNOWN_ID) || remoteStamp.equals(Utils.UNKNOWN_ID) || !wildlifeStamp.equalsIgnoreCase(remoteStamp)) {
              populateWildlifeTable(remoteStamp);
            } else {
              Log.d(TAG, "Wildlife data in-sync.");
              mCallback.onWildlifeDataPopulated();
            }
          } else {
            mCallback.onWildlifeDataFailure("Wildlife data stamp not found.");
          }
        }
      });
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    Log.d(TAG, "++onAttach(Context)");
    try {
      mCallback = (OnWildlifeDataListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(String.format(Locale.US, "Missing interface implementations for %s", context.toString()));
    }
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    return inflater.inflate(R.layout.fragment_wildlife_data, container, false);
  }

  private void populateWildlifeTable(String dataStamp) {

    Log.d(TAG, "++populateWildlifeTable(String)");
    FirebaseDatabase.getInstance().getReference().child(Utils.WILDLIFE_ROOT).get()
      .addOnCompleteListener(task -> {

        if (!task.isSuccessful()) {
          Log.e(TAG, "Error getting data", task.getException());
          mCallback.onWildlifeDataFailure("Could not retrieve Wildlife data.");
        } else {
          DataSnapshot resultSnapshot = task.getResult();
          if (resultSnapshot != null) {
            if (resultSnapshot.getChildrenCount() > 0) {
              Log.d(TAG, "Attempting Wildlife inserts: " + resultSnapshot.getChildrenCount());
              if (getActivity() != null) {
                WildlifeViewModel wildlifeViewModel = new ViewModelProvider(getActivity()).get(WildlifeViewModel.class);
                boolean hadErrors = false;
                for (DataSnapshot dataSnapshot : resultSnapshot.getChildren()) {
                  WildlifeEntity wildlifeEntity = dataSnapshot.getValue(WildlifeEntity.class);
                  String id = dataSnapshot.getKey();
                  if (wildlifeEntity != null && id != null) {
                    wildlifeEntity.Id = id;
                    if (wildlifeEntity.isValid()) {
                      wildlifeViewModel.insertWildlife(wildlifeEntity);
                    } else {
                      Log.w(TAG, "Wildlife entity was invalid, not adding: " + wildlifeEntity.Id);
                    }
                  } else {
                    hadErrors = true;
                  }
                }

                if (hadErrors) {
                  mCallback.onWildlifeDataFailure("Populating local Wildlife database failed.");
                } else {
                  Utils.setWildlifeStamp(getActivity(), dataStamp);
                  mCallback.onWildlifeDataPopulated();
                }
              } else {
                mCallback.onWildlifeDataFailure("App was not ready for operation at this time.");
              }
            } else {
              mCallback.onWildlifeDataMissing();
            }
          } else {
            mCallback.onWildlifeDataFailure("Wildlife results not found.");
          }
        }
      });
  }
}
