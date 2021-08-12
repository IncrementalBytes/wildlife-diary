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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.db.entity.CleanUpDetails;
import net.whollynugatory.android.wildlife.db.viewmodel.WildlifeViewModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CleanUpListFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + CleanUpListFragment.class.getSimpleName();

  public interface OnCleanUpListListener {

    void onCleanUpListSet(String titleUpdate);
  }

  private OnCleanUpListListener mCallback;

  public static CleanUpListFragment newInstance() {

    Log.d(TAG, "++newInstance()");
    return new CleanUpListFragment();
  }

  /*
  Fragment Override(s)
*/
  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    Log.d(TAG, "++onAttach(Context)");
    try {
      mCallback = (OnCleanUpListListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(
        String.format(Locale.US, "Missing interface implementations for %s", context.toString()));
    }
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    final View view = inflater.inflate(R.layout.fragment_cleanup_list, container, false);
    RecyclerView recyclerView = view.findViewById(R.id.cleanup_recycler_view);
    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

    WildlifeViewModel wildlifeViewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);

      CleanUpAdapter cleanUpAdapter = new CleanUpAdapter(getContext());
      recyclerView.setAdapter(cleanUpAdapter);
      wildlifeViewModel.getCleanUpItems().observe(getViewLifecycleOwner(), cleanUpDetailsList -> {

        Log.d(TAG, "CleanUp list is " + cleanUpDetailsList.size());
        cleanUpAdapter.setCleanUpList(cleanUpDetailsList);
      });

    return view;
  }

  static class CleanUpAdapter extends RecyclerView.Adapter<CleanUpAdapter.CleanUpHolder> {

    private final String TAG = Utils.BASE_TAG + CleanUpAdapter.class.getSimpleName();

    private final LayoutInflater mInflater;

    private List<CleanUpDetails> mCleanUpDetailsList;

    public CleanUpAdapter(Context context) {

      mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public CleanUpAdapter.CleanUpHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

      View itemView = mInflater.inflate(R.layout.clean_up_item, parent, false);
      return new CleanUpHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CleanUpAdapter.CleanUpHolder holder, int position) {

      if (mCleanUpDetailsList != null) {
        CleanUpDetails cleanUpDetails = mCleanUpDetailsList.get(position);
        holder.bind(cleanUpDetails);
      } else {
        Log.w(TAG, "CleanUpDetails is empty at this time.");
      }
    }

    @Override
    public int getItemCount() {

      return mCleanUpDetailsList != null ? mCleanUpDetailsList.size() : 0;
    }

    public void setCleanUpList(Collection<CleanUpDetails> cleanUpDetailsCollection) {

      Log.d(TAG, "++setCleanUpList(Collection<CleanUpDetails>)");
      mCleanUpDetailsList = new ArrayList<>(cleanUpDetailsCollection);
      notifyDataSetChanged();
    }

    static class CleanUpHolder extends RecyclerView.ViewHolder {

      private final String TAG = Utils.BASE_TAG + CleanUpAdapter.CleanUpHolder.class.getSimpleName();

      private final TextView mNameText;
      private final TextView mTypeText;

      private CleanUpDetails mCleanUpDetails;

      CleanUpHolder(View itemView) {
        super(itemView);

        mNameText = itemView.findViewById(R.id.clean_up_item_name);
        mTypeText = itemView.findViewById(R.id.clean_up_item_type);
        ImageView deleteImage = itemView.findViewById(R.id.clean_up_item_image);
        deleteImage.setOnClickListener(view -> {

          HashMap<String, Object> cleanUpEntities = new HashMap<>();
          Log.d(TAG, "Deleting " + mCleanUpDetails.Name + " from " + mCleanUpDetails.Type);
          cleanUpEntities.put(mCleanUpDetails.Id, null);
          FirebaseDatabase.getInstance().getReference(mCleanUpDetails.Type).updateChildren(cleanUpEntities)
            .addOnCompleteListener(task -> {

              if (!task.isSuccessful()) {
                Log.e(TAG, "Unable to delete encounter(s).", task.getException());
              } else {
                Utils.updateRemoteDataStamp(mCleanUpDetails.Type);
              }
            });
        });
      }

      void bind(CleanUpDetails cleanUpDetails) {

        mCleanUpDetails = cleanUpDetails;
        mNameText.setText(mCleanUpDetails.Name);
        mTypeText.setText(mCleanUpDetails.Type);
      }
    }
  }
}
