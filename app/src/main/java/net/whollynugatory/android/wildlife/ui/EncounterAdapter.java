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
package net.whollynugatory.android.wildlife.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.db.entity.EncounterDetails;
import net.whollynugatory.android.wildlife.db.entity.WildlifeSummary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EncounterAdapter extends RecyclerView.Adapter<EncounterAdapter.EncounterHolder> {

  private final String TAG = Utils.BASE_TAG + EncounterAdapter.class.getSimpleName();

  private final LayoutInflater mInflater;
  private List<EncounterDetails> mEncounterDetails;
  private List<WildlifeSummary> mWildlifeSummaries;

  public EncounterAdapter(Context context) {

    mInflater = LayoutInflater.from(context);
  }

  @NonNull
  @Override
  public EncounterAdapter.EncounterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

    View itemView = mInflater.inflate(R.layout.simple_list_item, parent, false);
    return new EncounterHolder(itemView);
  }

  @Override
  public void onBindViewHolder(@NonNull EncounterAdapter.EncounterHolder holder, int position) {

    if (mEncounterDetails != null) {
      EncounterDetails encounterDetails = mEncounterDetails.get(position);
      holder.bind(encounterDetails);
    } else if (mWildlifeSummaries != null) {
      WildlifeSummary wildlifeSummary = mWildlifeSummaries.get(position);
      holder.bind(wildlifeSummary);
    } else {
      Log.w(TAG, "EncounterDetails is empty at this time.");
    }
  }

  @Override
  public int getItemCount() {

    return mEncounterDetails != null ? mEncounterDetails.size() : mWildlifeSummaries != null ? mWildlifeSummaries.size() : 0;
  }

  public void setEncounterDetailsList(Collection<EncounterDetails> encounterDetailsCollection) {

    Log.d(TAG, "++setEncounterSummaryList(Collection<EncounterDetails>)");
    mEncounterDetails = new ArrayList<>(encounterDetailsCollection);
    mEncounterDetails.sort((a, b) -> Long.compare(b.Date, a.Date));
    notifyDataSetChanged();
  }

  public void setWildlifeSummaryList(Collection<WildlifeSummary> wildlifeSummaryCollection) {

    Log.d(TAG, "++setWildlifeSummaryList(Collection<WildlifeSummary>)");
    mWildlifeSummaries = new ArrayList<>(wildlifeSummaryCollection);
    notifyDataSetChanged();
  }

  static class EncounterHolder extends RecyclerView.ViewHolder {

    private final TextView mTitleTextView;
    private final TextView mDetailsTextView;

    EncounterHolder(View itemView) {
      super(itemView);

      mTitleTextView = itemView.findViewById(R.id.simple_list_item_text_name);
      mDetailsTextView = itemView.findViewById(R.id.simple_list_item_text_desc);
      mDetailsTextView.setTextSize(18f);
    }

    void bind(EncounterDetails encounterDetails) {

      mTitleTextView.setText(encounterDetails.WildlifeSpecies);
      mDetailsTextView.setText(Utils.fromTimestamp(encounterDetails.Date));
    }

    void bind(WildlifeSummary wildlifeSummary) {

      mTitleTextView.setText(wildlifeSummary.WildlifeSpecies);
      mDetailsTextView.setText(String.valueOf(wildlifeSummary.EncounterCount));
    }
  }
}
