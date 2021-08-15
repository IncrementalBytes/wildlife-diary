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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.db.entity.EncounterDetails;
import net.whollynugatory.android.wildlife.db.viewmodel.WildlifeViewModel;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecentFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + RecentFragment.class.getSimpleName();

  public interface OnRecentListener {

    void onRecentAddEncounter();

    void onRecentItemClicked(String encounterId);
  }

  private OnRecentListener mCallback;

  public static RecentFragment newInstance() {

    Log.d(TAG, "++newInstance()");
    RecentFragment fragment = new RecentFragment();
    Bundle arguments = new Bundle();
    fragment.setArguments(arguments);
    return fragment;
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    Log.d(TAG, "++onAttach(Context)");
    try {
      mCallback = (OnRecentListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(
        String.format(Locale.US, "Missing interface implementations for %s", context.toString()));
    }
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    final View view = inflater.inflate(R.layout.fragment_recent, container, false);
    FloatingActionButton addEncounterButton = view.findViewById(R.id.recent_fab_add);
    addEncounterButton.setOnClickListener(v -> mCallback.onRecentAddEncounter());
    RecyclerView recyclerView = view.findViewById(R.id.recent_recycler_view);
    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    EncounterAdapter encounterAdapter = new EncounterAdapter(getContext());
    recyclerView.setAdapter(encounterAdapter);

    if (Utils.getIsContributor(getContext())) {
      addEncounterButton.setVisibility(View.VISIBLE);
    } else {
      addEncounterButton.setVisibility(View.GONE);
    }

    WildlifeViewModel wildlifeViewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);
    String followingUserId = Utils.getFollowingUserId(getActivity());
    Date in = new Date();
    LocalDateTime localDateTime = LocalDateTime.ofInstant(in.toInstant(), ZoneId.systemDefault()).minusDays(6);
    ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
    Log.d(TAG, "Local dateTime (epoch): " + zonedDateTime.toInstant().toEpochMilli());
    wildlifeViewModel.getAllEncounterDetails(followingUserId).observe(
      getViewLifecycleOwner(),
      encounterDetailsList -> {

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
      });

    return view;
  }

  private class EncounterAdapter extends RecyclerView.Adapter<EncounterAdapter.EncounterHolder> {

    private final String TAG = Utils.BASE_TAG + EncounterAdapter.class.getSimpleName();

    private final LayoutInflater mInflater;
    private List<EncounterDetails> mEncounterDetails;

    public EncounterAdapter(Context context) {

      mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public EncounterAdapter.EncounterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

      View itemView = mInflater.inflate(R.layout.encounter_item, parent, false);
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
      mEncounterDetails = new ArrayList<>(encounterDetailsCollection);
      mEncounterDetails.sort((a, b) -> Long.compare(b.Date, a.Date));
      notifyDataSetChanged();
    }

    class EncounterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

      private final String TAG = Utils.BASE_TAG + EncounterHolder.class.getSimpleName();

      private final TextView mAbbreviationTextView;
      private final TextView mEncounterDateTextView;
      private final TextView mWildlifeTextView;
      private final ImageView mNewEncounterImageView;

      private EncounterDetails mEncounterDetails;

      EncounterHolder(View itemView) {
        super(itemView);

        mAbbreviationTextView = itemView.findViewById(R.id.encounter_item_abbreviation);
        mEncounterDateTextView = itemView.findViewById(R.id.encounter_item_date);
        mWildlifeTextView = itemView.findViewById(R.id.encounter_item_wildlife);
        mNewEncounterImageView = itemView.findViewById(R.id.encounter_item_image_new);
        itemView.setOnClickListener(this);
      }

      void bind(EncounterDetails encounterDetails) {

        mEncounterDetails = encounterDetails;

        mAbbreviationTextView.setText(mEncounterDetails.WildlifeAbbreviation);
        mEncounterDateTextView.setText(Utils.fromTimestamp(mEncounterDetails.Date));
        mWildlifeTextView.setText(mEncounterDetails.WildlifeSpecies);
        mNewEncounterImageView.setVisibility(mEncounterDetails.IsNew ? View.VISIBLE : View.GONE);
      }

      @Override
      public void onClick(View view) {

        Log.d(TAG, "++EncounterHolder::onClick(View)");
        mCallback.onRecentItemClicked(mEncounterDetails.EncounterId);
      }
    }
  }
}
