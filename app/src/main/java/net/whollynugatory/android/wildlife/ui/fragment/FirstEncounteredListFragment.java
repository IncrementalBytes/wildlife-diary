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
import net.whollynugatory.android.wildlife.ui.viewmodel.FragmentDataViewModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class FirstEncounteredListFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + FirstEncounteredListFragment.class.getSimpleName();

  private FirstEncounteredAdapter mFirstEncounteredAdapter;

  /*
    Fragment Override(s)
   */
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    final View view = inflater.inflate(R.layout.fragment_list_only, container, false);
    RecyclerView recyclerView = view.findViewById(R.id.content_list);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    mFirstEncounteredAdapter = new FirstEncounteredAdapter(getContext());
    recyclerView.setAdapter(mFirstEncounteredAdapter);

    FragmentDataViewModel viewModel = new ViewModelProvider(requireActivity()).get(FragmentDataViewModel.class);
    List<EncounterDetails> firstEncounteredList = viewModel.getEncounterDetailsList().getValue();
    if (firstEncounteredList == null || firstEncounteredList.size() == 0) {
      WildlifeViewModel wildlifeViewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);
      String followingUserId = Utils.getFollowingUserId(getContext());
      boolean showSensitive = Utils.getShowSensitive(getContext());
      wildlifeViewModel.getFirstEncountered(followingUserId, showSensitive).observe(
        getViewLifecycleOwner(),
        encounteredList -> {

          Log.d(TAG, "First encountered list is " + encounteredList.size());
          mFirstEncounteredAdapter.setFirstEncounteredList(encounteredList);
        });
    } else {
      Log.d(TAG, "First encountered list is " + firstEncounteredList.size());
      mFirstEncounteredAdapter.setFirstEncounteredList(firstEncounteredList);
    }

    return view;
  }

  private class FirstEncounteredAdapter extends RecyclerView.Adapter<FirstEncounteredAdapter.EncounterHolder> {

    private final String TAG = Utils.BASE_TAG + FirstEncounteredAdapter.class.getSimpleName();

    private final LayoutInflater mInflater;
    private final List<EncounterDetails> mEncounterDetails;

    public FirstEncounteredAdapter(Context context) {

      mInflater = LayoutInflater.from(context);
      mEncounterDetails = new ArrayList<>();
    }

    @NonNull
    @Override
    public FirstEncounteredAdapter.EncounterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

      View itemView = mInflater.inflate(R.layout.item_first_encountered, parent, false);
      return new FirstEncounteredAdapter.EncounterHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FirstEncounteredAdapter.EncounterHolder holder, int position) {

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

    public void setFirstEncounteredList(Collection<EncounterDetails> encounterDetailsCollection) {

      Log.d(TAG, "++setFirstEncounteredList(Collection<EncounterDetails>)");
      int currentSize = mEncounterDetails.size();
      mEncounterDetails.clear();
      mEncounterDetails.addAll(encounterDetailsCollection);
      notifyItemRangeRemoved(0, currentSize);
      notifyItemRangeInserted(0, encounterDetailsCollection.size());
    }

    class EncounterHolder extends RecyclerView.ViewHolder {

      private final TextView mEncounterDateTextView;
      private final ImageView mEncounterImageView;
      private final TextView mWildlifeTextView;

      EncounterHolder(View itemView) {
        super(itemView);

        mEncounterDateTextView = itemView.findViewById(R.id.first_encountered_item_text_date);
        mEncounterImageView = itemView.findViewById(R.id.first_encountered_item_image_wildlife);
        mWildlifeTextView = itemView.findViewById(R.id.first_encountered_item_text_wildlife);
      }

      void bind(EncounterDetails encounterDetails) {

        if (!encounterDetails.ImageUrl.equals(Utils.UNKNOWN_STRING) && getContext() != null) {
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
