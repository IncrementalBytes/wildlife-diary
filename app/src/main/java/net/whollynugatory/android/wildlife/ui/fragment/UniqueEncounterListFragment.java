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

import com.bumptech.glide.Glide;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.db.entity.EncounterDetails;
import net.whollynugatory.android.wildlife.db.viewmodel.WildlifeViewModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class UniqueEncounterListFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + UniqueEncounterListFragment.class.getSimpleName();

  private UniqueEncounterAdapter mUniqueEncounterAdapter;

  public static UniqueEncounterListFragment newInstance() {

    Log.d(TAG, "++newInstance()");
    return new UniqueEncounterListFragment();
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    final View view = inflater.inflate(R.layout.fragment_unique_list, container, false);
    RecyclerView recyclerView = view.findViewById(R.id.unique_encounter_list_view);
    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    mUniqueEncounterAdapter = new UniqueEncounterAdapter(getContext());
    recyclerView.setAdapter(mUniqueEncounterAdapter);

    List<EncounterDetails> uniqueEncountersList = Utils.getEncounterDetailsList(getContext());
    if (uniqueEncountersList.size() == 0) {
      WildlifeViewModel wildlifeViewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);
      String followingUserId = Utils.getFollowingUserId(getContext());
      wildlifeViewModel.getUniqueEncountered(followingUserId).observe(
        getViewLifecycleOwner(),
        uniqueEncounters -> {

          Log.d(TAG, "Unique encounter list is " + uniqueEncounters.size());
          mUniqueEncounterAdapter.setUniqueEncountersList(uniqueEncounters);
        });
    } else {
      Log.d(TAG, "Unique encounter list is " + uniqueEncountersList.size());
      mUniqueEncounterAdapter.setUniqueEncountersList(uniqueEncountersList);
    }

    return view;
  }

  private class UniqueEncounterAdapter extends RecyclerView.Adapter<UniqueEncounterAdapter.EncounterHolder> {

    private final String TAG = Utils.BASE_TAG + UniqueEncounterAdapter.class.getSimpleName();

    private final LayoutInflater mInflater;
    private List<EncounterDetails> mEncounterDetails;

    public UniqueEncounterAdapter(Context context) {

      mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public UniqueEncounterAdapter.EncounterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

      View itemView = mInflater.inflate(R.layout.unique_encounter_item, parent, false);
      return new UniqueEncounterAdapter.EncounterHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UniqueEncounterAdapter.EncounterHolder holder, int position) {

      if (mEncounterDetails != null) {
        EncounterDetails encounterDetails = mEncounterDetails.get(position);
        holder.bind(encounterDetails);
      } else {
        Log.w(TAG, "EncounterDetails is empty at this time.");
      }
    }

    @Override
    public int getItemCount() {

      return mEncounterDetails != null ? mEncounterDetails.size() : 0;
    }

    public void setUniqueEncountersList(Collection<EncounterDetails> encounterDetailsCollection) {

      Log.d(TAG, "++setUniqueEncountersList(Collection<EncounterDetails>)");
      mEncounterDetails = new ArrayList<>(encounterDetailsCollection);
      mEncounterDetails.sort((a, b) -> Long.compare(b.Date, a.Date));
      notifyDataSetChanged();
    }

    class EncounterHolder extends RecyclerView.ViewHolder {

      private final TextView mEncounterDateTextView;
      private final ImageView mEncounterImageView;
      private final TextView mWildlifeTextView;

      EncounterHolder(View itemView) {
        super(itemView);

        mEncounterDateTextView = itemView.findViewById(R.id.unique_encounter_item_text_date);
        mEncounterImageView = itemView.findViewById(R.id.unique_encounter_item_image_wildlife);
        mWildlifeTextView = itemView.findViewById(R.id.unique_encounter_item_text_wildlife);
      }

      void bind(EncounterDetails encounterDetails) {

        if (!encounterDetails.ImageUrl.equals(Utils.UNKNOWN_STRING)) {
          Glide.with(getContext())
            .load(encounterDetails.ImageUrl)
            .placeholder(R.drawable.ic_placeholder_dark)
            .error(R.drawable.ic_error_dark)
            .into(mEncounterImageView);
        } else {
          mEncounterImageView.setVisibility(View.GONE);
        }

        mWildlifeTextView.setText(encounterDetails.WildlifeSpecies);
        mEncounterDateTextView.setText(
          String.format(
            Locale.US,
            getString(R.string.format_first_encountered),
            Utils.fromTimestamp(encounterDetails.Date)));
      }
    }
  }
}
