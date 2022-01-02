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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.db.entity.EncounterDetails;
import net.whollynugatory.android.wildlife.db.viewmodel.WildlifeViewModel;
import net.whollynugatory.android.wildlife.ui.viewmodel.FragmentDataViewModel;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class RecentFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + RecentFragment.class.getSimpleName();

  /*
    Fragment Override(s)
   */
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    final View view = inflater.inflate(R.layout.fragment_recent, container, false);
    FloatingActionButton addEncounterButton = view.findViewById(R.id.recent_fab_add);
    if (Utils.getIsContributor(getContext())) {
      addEncounterButton.setVisibility(View.VISIBLE);
      addEncounterButton.setOnClickListener(addEncounterButtonView -> {
        FragmentDataViewModel viewModel = new ViewModelProvider(this).get(FragmentDataViewModel.class);
        viewModel.setEncounterId("");
        Navigation.findNavController(addEncounterButtonView).navigate(R.id.encounterFragment);
      });
    } else {
      addEncounterButton.setVisibility(View.INVISIBLE);
    }

    RecyclerView recyclerView = view.findViewById(R.id.content_list);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    EncounterAdapter encounterAdapter = new EncounterAdapter(getContext());
    recyclerView.setAdapter(encounterAdapter);

    WildlifeViewModel wildlifeViewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);
    String followingUserId = Utils.getFollowingUserId(getContext());
    Date in = new Date();
    LocalDateTime localDateTime = LocalDateTime.ofInstant(in.toInstant(), ZoneId.systemDefault()).minusDays(6);
    ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
    Log.d(TAG, "Local dateTime (epoch): " + zonedDateTime.toInstant().toEpochMilli());
    boolean showSensitive = Utils.getShowSensitive(getContext());
    wildlifeViewModel.getAllEncounterDetails(followingUserId, showSensitive).observe(
      getViewLifecycleOwner(),
      encounterDetailsList -> {

        if (encounterDetailsList.isEmpty()) {
          encounterAdapter.setEncounterDetailsList(encounterDetailsList);
        } else {
          String targetDate = Utils.fromTimestamp(encounterDetailsList.get(0).Date);
          List<String> encounterIds = new ArrayList<>();
          List<EncounterDetails> recentEncounterDetailsList = new ArrayList<>();
          for (EncounterDetails encounterDetails : encounterDetailsList) {
            String currentDate = Utils.fromTimestamp(encounterDetails.Date);
            if (currentDate.equals(targetDate)) {
              if (!encounterIds.contains(encounterDetails.EncounterId)) {
                recentEncounterDetailsList.add(encounterDetails);
                encounterIds.add(encounterDetails.EncounterId);
              }
            } else {
              break;
            }
          }

          wildlifeViewModel.getNewUnique(followingUserId, zonedDateTime.toInstant().toEpochMilli()).observe(
            getViewLifecycleOwner(),
            uniqueList -> {

              for (String uniqueId : uniqueList) {
                for (EncounterDetails encounterDetails : recentEncounterDetailsList) {
                  if (encounterDetails.WildlifeId.equals(uniqueId)) {
                    encounterDetails.IsNew = true;
                  }
                }
              }

              encounterAdapter.setEncounterDetailsList(recentEncounterDetailsList);
            });
        }
      });

    return view;
  }

  private class EncounterAdapter extends RecyclerView.Adapter<EncounterAdapter.EncounterHolder> {

    private final String TAG = Utils.BASE_TAG + EncounterAdapter.class.getSimpleName();

    private final LayoutInflater mInflater;
    private final List<EncounterDetails> mEncounterDetails;

    public EncounterAdapter(Context context) {

      mInflater = LayoutInflater.from(context);
      mEncounterDetails = new ArrayList<>();
    }

    @NonNull
    @Override
    public EncounterAdapter.EncounterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

      View itemView = mInflater.inflate(R.layout.item_encounter, parent, false);
      return new EncounterAdapter.EncounterHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EncounterAdapter.EncounterHolder holder, int position) {

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

    public void setEncounterDetailsList(Collection<EncounterDetails> encounterDetailsCollection) {

      Log.d(TAG, "++setEncounterSummaryList(Collection<EncounterDetails>)");
      int currentSize = mEncounterDetails.size();
      mEncounterDetails.clear();
      mEncounterDetails.addAll(encounterDetailsCollection);
      notifyItemRangeRemoved(0, currentSize);
      notifyItemRangeInserted(0, encounterDetailsCollection.size());
    }

    class EncounterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

      private final String TAG = Utils.BASE_TAG + EncounterHolder.class.getSimpleName();

      private final TextView mAbbreviationTextView;
      private final TextView mEncounterDateTextView;
      private final ImageView mNewEncounterImageView;
      private final ImageView mWildlifeImageView;
      private final TextView mWildlifeTextView;

      private EncounterDetails mEncounterDetails;

      EncounterHolder(View itemView) {
        super(itemView);

        mAbbreviationTextView = itemView.findViewById(R.id.encounter_item_abbreviation);
        mEncounterDateTextView = itemView.findViewById(R.id.encounter_item_date);
        mNewEncounterImageView = itemView.findViewById(R.id.encounter_item_image_new);
        mWildlifeImageView = itemView.findViewById(R.id.encounter_item_image);
        mWildlifeTextView = itemView.findViewById(R.id.encounter_item_wildlife);
        itemView.setOnClickListener(this);
      }

      void bind(EncounterDetails encounterDetails) {

        mEncounterDetails = encounterDetails;

        mAbbreviationTextView.setText(mEncounterDetails.WildlifeAbbreviation);
        mEncounterDateTextView.setText(Utils.fromTimestamp(mEncounterDetails.Date));
        mNewEncounterImageView.setVisibility(mEncounterDetails.IsNew ? View.VISIBLE : View.GONE);
        Glide.with(getContext())
          .load(encounterDetails.ImageUrl)
          .placeholder(R.drawable.ic_placeholder_dark)
          .error(R.drawable.ic_error_dark)
          .into(mWildlifeImageView);
        mWildlifeTextView.setText(mEncounterDetails.WildlifeSpecies);
      }

      @Override
      public void onClick(View view) {

        Log.d(TAG, "++EncounterHolder::onClick(View)");
        FragmentDataViewModel viewModel = new ViewModelProvider(requireActivity())
          .get(FragmentDataViewModel.class);
        if (Utils.getIsContributor(getContext())) {
          viewModel.setEncounterId(mEncounterDetails.EncounterId);
          Navigation.findNavController(view).navigate(R.id.action_recentFragment_to_encounterFragment);
        } else {
          viewModel.setEncounterId(mEncounterDetails.EncounterId);
          Navigation.findNavController(view).navigate(R.id.action_recentFragment_to_encounterDetailFragment);
        }
      }
    }
  }
}
