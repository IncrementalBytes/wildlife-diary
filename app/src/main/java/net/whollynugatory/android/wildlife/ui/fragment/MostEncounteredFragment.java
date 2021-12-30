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
  import net.whollynugatory.android.wildlife.db.entity.WildlifeSummary;
  import net.whollynugatory.android.wildlife.db.viewmodel.WildlifeViewModel;
  import net.whollynugatory.android.wildlife.ui.viewmodel.FragmentDataViewModel;

  import java.util.ArrayList;
  import java.util.Collection;
  import java.util.List;
  import java.util.Locale;

public class MostEncounteredFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + MostEncounteredFragment.class.getSimpleName();

  private MostEncounteredAdapter mMostEncounteredAdapter;

  /*
    Fragment Override(s)
   */
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    final View view = inflater.inflate(R.layout.fragment_list_only, container, false);
    RecyclerView recyclerView = view.findViewById(R.id.content_list);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    mMostEncounteredAdapter = new MostEncounteredAdapter(getContext());
    recyclerView.setAdapter(mMostEncounteredAdapter);

    FragmentDataViewModel viewModel = new ViewModelProvider(requireActivity()).get(FragmentDataViewModel.class);
    List<WildlifeSummary> mostEncounteredList = viewModel.getWildlifeSummaryList().getValue();
    if (mostEncounteredList == null || mostEncounteredList.size() == 0) {
      WildlifeViewModel wildlifeViewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);
      String followingUserId = Utils.getFollowingUserId(getContext());
      wildlifeViewModel.getMostEncountered(followingUserId).observe(
        getViewLifecycleOwner(),
        mostEncountered -> {

          Log.d(TAG, "Most encountered list is " + mostEncountered.size());
          mMostEncounteredAdapter.setMostEncounteredList(mostEncountered);
        });
    } else {
      Log.d(TAG, "Most encountered list is " + mostEncounteredList.size());
      mMostEncounteredAdapter.setMostEncounteredList(mostEncounteredList);
    }

    return view;
  }

  private class MostEncounteredAdapter extends RecyclerView.Adapter<MostEncounteredAdapter.EncounterHolder> {

    private final String TAG = Utils.BASE_TAG + MostEncounteredAdapter.class.getSimpleName();

    private final LayoutInflater mInflater;
    private final List<WildlifeSummary> mWildlifeSummaries;

    public MostEncounteredAdapter(Context context) {

      mInflater = LayoutInflater.from(context);
      mWildlifeSummaries = new ArrayList<>();
    }

    @NonNull
    @Override
    public MostEncounteredAdapter.EncounterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

      View itemView = mInflater.inflate(R.layout.item_most_encountered, parent, false);
      return new MostEncounteredAdapter.EncounterHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MostEncounteredAdapter.EncounterHolder holder, int position) {

      if (mWildlifeSummaries != null) {
        WildlifeSummary wildlifeSummary = mWildlifeSummaries.get(position);
        holder.bind(wildlifeSummary);
      } else {
        Log.w(TAG, "EncounterDetails is empty at this time.");
      }
    }

    @Override
    public int getItemCount() {

      return mWildlifeSummaries != null ? mWildlifeSummaries.size() : 0;
    }

    public void setMostEncounteredList(Collection<WildlifeSummary> wildlifeSummaryCollection) {

      Log.d(TAG, "++setMostEncounteredList(Collection<WildlifeSummary>)");
      int currentSize = mWildlifeSummaries.size();
      mWildlifeSummaries.clear();
      mWildlifeSummaries.addAll(wildlifeSummaryCollection);
      mWildlifeSummaries.sort((a, b) -> Long.compare(b.EncounterCount, a.EncounterCount));
      notifyItemRangeRemoved(0, currentSize);
      notifyItemRangeInserted(0, wildlifeSummaryCollection.size());
    }

    class EncounterHolder extends RecyclerView.ViewHolder {

      private final TextView mEncounterCountTextView;
      private final ImageView mEncounterImageView;
      private final TextView mWildlifeTextView;

      EncounterHolder(View itemView) {
        super(itemView);

        mEncounterCountTextView = itemView.findViewById(R.id.most_encountered_item_text_count);
        mEncounterImageView = itemView.findViewById(R.id.most_encountered_item_image_wildlife);
        mWildlifeTextView = itemView.findViewById(R.id.most_encountered_item_text_wildlife);
      }

      void bind(WildlifeSummary wildlifeSummary) {

        if (!wildlifeSummary.ImageUrl.equals(Utils.UNKNOWN_STRING) && getContext() != null) {
          Glide.with(getContext())
            .load(wildlifeSummary.ImageUrl)
            .placeholder(R.drawable.ic_placeholder_dark)
            .error(R.drawable.ic_error_dark)
            .into(mEncounterImageView);
        } else {
          mEncounterImageView.setVisibility(View.GONE);
        }

        mWildlifeTextView.setText(wildlifeSummary.WildlifeSpecies);
        mEncounterCountTextView.setText(
          String.format(
            Locale.US,
            getString(R.string.format_number_of_encounters),
            wildlifeSummary.EncounterCount));
      }
    }
  }
}
