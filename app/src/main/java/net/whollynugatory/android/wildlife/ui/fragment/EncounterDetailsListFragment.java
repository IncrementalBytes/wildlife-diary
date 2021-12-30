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

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.db.entity.EncounterDetails;
import net.whollynugatory.android.wildlife.db.viewmodel.WildlifeViewModel;
import net.whollynugatory.android.wildlife.ui.viewmodel.FragmentDataViewModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EncounterDetailsListFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + EncounterDetailsListFragment.class.getSimpleName();

  private EncounterAdapter mEncounterAdapter;

  /*
    Fragment Override(s)
  */
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    final View view = inflater.inflate(R.layout.fragment_list_only, container, false);
    RecyclerView recyclerView = view.findViewById(R.id.content_list);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    mEncounterAdapter = new EncounterAdapter(getContext());
    recyclerView.setAdapter(mEncounterAdapter);

    FragmentDataViewModel viewModel = new ViewModelProvider(requireActivity()).get(FragmentDataViewModel.class);
    List<EncounterDetails> encounterDetailsList = viewModel.getEncounterDetailsList().getValue();
    if (encounterDetailsList == null || encounterDetailsList.size() == 0) {
      WildlifeViewModel wildlifeViewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);
      String followingUserId = Utils.getFollowingUserId(getContext());
      boolean showSensitive = Utils.getShowSensitive(getContext());
      wildlifeViewModel.getAllEncounterDetails(followingUserId, showSensitive).observe(
        getViewLifecycleOwner(),
        totalEncounters -> mEncounterAdapter.setEncounterDetailsList(totalEncounters));
    } else {
      mEncounterAdapter.setEncounterDetailsList(encounterDetailsList);
    }

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
    public EncounterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

      View itemView = mInflater.inflate(R.layout.item_encounter, parent, false);
      return new EncounterHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EncounterHolder holder, int position) {

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
      Log.d(TAG, "Encounter list is " + encounterDetailsCollection.size());
      List<String> encounterIds = new ArrayList<>();
      List<EncounterDetails> condensedEncounters = new ArrayList<>();
      for (EncounterDetails encounterDetails : encounterDetailsCollection) {
        if (!encounterIds.contains(encounterDetails.EncounterId)) {
          condensedEncounters.add(encounterDetails);
          encounterIds.add(encounterDetails.EncounterId);
        }
      }

      int currentSize = mEncounterDetails.size();
      mEncounterDetails.clear();
      mEncounterDetails.addAll(condensedEncounters);
      mEncounterDetails.sort((a, b) -> Long.compare(b.Date, a.Date));
      notifyItemRangeRemoved(0, currentSize);
      notifyItemRangeInserted(0, condensedEncounters.size());
    }

    class EncounterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

      private final String TAG = Utils.BASE_TAG + EncounterHolder.class.getSimpleName();

      private final TextView mAbbreviationTextView;
      private final TextView mEncounterDateTextView;
      private final ImageView mNewEncounterImageView;
      private final TextView mWildlifeTextView;

      private EncounterDetails mEncounterDetails;

      EncounterHolder(View itemView) {
        super(itemView);

        mAbbreviationTextView = itemView.findViewById(R.id.encounter_item_abbreviation);
        mEncounterDateTextView = itemView.findViewById(R.id.encounter_item_date);
        mNewEncounterImageView = itemView.findViewById(R.id.encounter_item_image_new);
        mWildlifeTextView = itemView.findViewById(R.id.encounter_item_wildlife);
        itemView.setOnClickListener(this);
      }

      void bind(EncounterDetails encounterDetails) {

        mEncounterDetails = encounterDetails;

        mAbbreviationTextView.setText(mEncounterDetails.WildlifeAbbreviation);
        mEncounterDateTextView.setText(Utils.fromTimestamp(mEncounterDetails.Date));
        mNewEncounterImageView.setVisibility(mEncounterDetails.IsNew ? View.VISIBLE : View.GONE);
        mWildlifeTextView.setText(mEncounterDetails.WildlifeSpecies);
      }

      @Override
      public void onClick(View view) {

        Log.d(TAG, "++EncounterHolder::onClick(View)");
        FragmentDataViewModel viewModel = new ViewModelProvider(requireActivity())
          .get(FragmentDataViewModel.class);
        viewModel.setEncounterId(mEncounterDetails.EncounterId);
        if (Utils.getIsContributor(getContext())) {
          Navigation.findNavController(view).navigate(R.id.action_EncounterDetailsList_to_Encounter);
        } else {
          Navigation.findNavController(view).navigate(R.id.action_EncounterDetailsList_to_EncounterDetails);
        }
      }
    }
  }
}
