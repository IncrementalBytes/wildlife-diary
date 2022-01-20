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

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.db.entity.UserEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdministrativeFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + AdministrativeFragment.class.getSimpleName();

  private AdministrativeAdapter mAdministrativeAdapter;

  /*
  Fragment Override(s)
*/
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    final View view = inflater.inflate(R.layout.fragment_list_only, container, false);
    RecyclerView recyclerView = view.findViewById(R.id.content_list);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    mAdministrativeAdapter = new AdministrativeAdapter(getContext());
    recyclerView.setAdapter(mAdministrativeAdapter);

    FirebaseDatabase.getInstance().getReference(Utils.USERS_ROOT).get().addOnCompleteListener(task -> {

      List<UserEntity> userList = new ArrayList<>();
      for (DataSnapshot snapshot : task.getResult().getChildren()) {
        UserEntity userEntity = snapshot.getValue(UserEntity.class);
        if (userEntity != null) {
          userEntity.Id = snapshot.getKey();
          userList.add(userEntity);
        }
      }

      mAdministrativeAdapter.setContributorList(userList);
    });

    return view;
  }

  private class AdministrativeAdapter extends RecyclerView.Adapter<AdministrativeAdapter.AdministrativeHolder> {

    private final String TAG = Utils.BASE_TAG + AdministrativeAdapter.class.getSimpleName();

    private final LayoutInflater mInflater;

    private final HashMap<String, UserEntity> mContributorList;
    private final List<UserEntity> mUserEntityList;

    public AdministrativeAdapter(Context context) {

      mInflater = LayoutInflater.from(context);
      mContributorList = new HashMap<>();
      mUserEntityList = new ArrayList<>();
    }

    @NonNull
    @Override
    public AdministrativeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

      View itemView = mInflater.inflate(R.layout.item_administrative, parent, false);
      return new AdministrativeHolder(itemView);
  }
    @Override
    public void onBindViewHolder(@NonNull AdministrativeHolder holder, int position) {

      if (mUserEntityList != null) {
        UserEntity userEntity = mUserEntityList.get(position);
        holder.bind(userEntity);
      } else {
        Log.w(TAG, "User map is empty.");
      }
    }

    @Override
    public int getItemCount() {

      return mUserEntityList != null ? mUserEntityList.size() : 0;
    }

    public void setContributorList(List<UserEntity> userEntityList) {

      Log.d(TAG, "++setContributorList(List<UserEntity>)");
      for (UserEntity userEntity : userEntityList) {
        if (userEntity.IsContributor) {
          mContributorList.putIfAbsent(userEntity.Id, userEntity);
        }
      }

      int currentSize = mUserEntityList.size();
      mUserEntityList.clear();
      mUserEntityList.addAll(userEntityList);
      notifyItemRangeRemoved(0, currentSize);
      notifyItemRangeInserted(0, userEntityList.size());
    }

    class AdministrativeHolder extends RecyclerView.ViewHolder implements AdapterView.OnItemSelectedListener {

      private final String TAG = Utils.BASE_TAG + AdministrativeHolder.class.getSimpleName();

      private final Spinner mFollowSpinner;
      private final TextView mNameTextView;

      private final ArrayAdapter<UserEntity> mContributorsAdapter;
      private UserEntity mUserEntity;

      AdministrativeHolder(View itemView) {
        super(itemView);

        mFollowSpinner = itemView.findViewById(R.id.administrative_item_spinner_following);
        mNameTextView = itemView.findViewById(R.id.administrative_item_text_name);

        mContributorsAdapter = new ArrayAdapter<>(
          getContext(),
          android.R.layout.simple_spinner_dropdown_item,
          mContributorList.values().toArray(new UserEntity[0]));
        mFollowSpinner.setAdapter(mContributorsAdapter);
      }

      void bind(UserEntity userEntity) {

        mUserEntity = userEntity;

        mNameTextView.setText(mUserEntity.DisplayName.isEmpty() ? mUserEntity.Id : mUserEntity.DisplayName);
        UserEntity contributor = mContributorList.get(mUserEntity.FollowingId);
        mFollowSpinner.setSelection(mContributorsAdapter.getPosition(contributor));

        mFollowSpinner.setOnItemSelectedListener(this);
      }

      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        UserEntity newFollowingUserEntity = (UserEntity) adapterView.getItemAtPosition(i);
        if (!mUserEntity.FollowingId.equals(newFollowingUserEntity.Id)) {
          Utils.setFollowingUserId(getContext(), newFollowingUserEntity.Id);
          Map<String, Object> updates = new HashMap<>();
          mUserEntity.FollowingId = newFollowingUserEntity.Id;
          updates.put(Utils.combine(Utils.USERS_ROOT, mUserEntity.Id), mUserEntity);
          FirebaseDatabase.getInstance().getReference().updateChildren(updates)
            .addOnSuccessListener(unused ->
              Log.d(
                TAG,
                String.format(
                  Locale.US,
                  "Updating %s from %s to %s",
                  mUserEntity.Id,
                  mUserEntity.FollowingId,
                  newFollowingUserEntity.Id)))
            .addOnFailureListener(e -> Log.e(TAG, "Could not create new user entry in firebase.", e));
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {

        // do nothing
      }
    }
  }
}
