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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.db.view.EncounterDetails;
import net.whollynugatory.android.wildlife.db.viewmodel.WildlifeViewModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class EncounterListFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + EncounterListFragment.class.getSimpleName();

  public interface OnEncounterListListener {

    void onEncounterListPopulated(int size);
    void onEncounterDetailsClicked(String encounterId);
  }

  private OnEncounterListListener mCallback;

  private EncounterAdapter mEncounterAdapter;
  private RecyclerView mRecyclerView;

  private String mUserId;

  public static EncounterListFragment newInstance(String userId) {

    Log.d(TAG, "++newInstance(String)");
    EncounterListFragment fragment = new EncounterListFragment();
    Bundle arguments = new Bundle();
    arguments.putSerializable(Utils.ARG_FIREBASE_USER_ID, userId);
    fragment.setArguments(arguments);
    return fragment;
  }

  /*
    Fragment Override(s)
  */
  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    Log.d(TAG, "++onActivityCreated(Bundle)");
    mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    mEncounterAdapter = new EncounterAdapter(getContext());
    mRecyclerView.setAdapter(mEncounterAdapter);
    WildlifeViewModel wildlifeViewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);

    wildlifeViewModel.getRecentEncounterDetails().observe(getViewLifecycleOwner(), encounterDetailsList -> {

      boolean showSensitive = Utils.getShowSensitive(getContext());
      HashMap<String, EncounterDetails> encounterDetailsHashMap = new HashMap<>();
      for (EncounterDetails encounterDetails : encounterDetailsList) {
        if (encounterDetails.IsSensitive) {
          if (showSensitive && !encounterDetailsHashMap.containsKey(encounterDetails.EncounterId)) {
            encounterDetailsHashMap.put(encounterDetails.EncounterId, encounterDetails);
          }
        } else if(!encounterDetailsHashMap.containsKey(encounterDetails.EncounterId)) {
          encounterDetailsHashMap.put(encounterDetails.EncounterId, encounterDetails);
        }
      }

      mEncounterAdapter.setEncounterList(encounterDetailsHashMap.values());
      mCallback.onEncounterListPopulated(encounterDetailsHashMap.size());
    });
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    Log.d(TAG, "++onAttach(Context)");
    try {
      mCallback = (OnEncounterListListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(String.format(Locale.US, "Missing interface implementations for %s", context.toString()));
    }

    Bundle arguments = getArguments();
    if (arguments != null) {
      if (arguments.containsKey(Utils.ARG_FIREBASE_USER_ID)) {
        mUserId = (String) arguments.getSerializable(Utils.ARG_FIREBASE_USER_ID);
      } else {
        mUserId = "";
      }
    } else {
      Log.e(TAG, "Arguments were null.");
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "++onCreate(Bundle)");
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    final View view = inflater.inflate(R.layout.fragment_encounter_list, container, false);
    mRecyclerView = view.findViewById(R.id.encounter_list_view);
    return view;
  }

  @Override
  public void onDetach() {
    super.onDetach();

    Log.d(TAG, "++onDetach()");
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    Log.d(TAG, "++onDestroy()");
  }

  @Override
  public void onResume() {
    super.onResume();

    Log.d(TAG, "++onResume()");
  }

  public class EncounterAdapter extends RecyclerView.Adapter<EncounterAdapter.EncounterHolder> {

    private final String TAG = Utils.BASE_TAG + EncounterAdapter.class.getSimpleName();

    private final LayoutInflater mInflater;
    private List<EncounterDetails> mEncounterDetailsList;

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

      if (mEncounterDetailsList != null) {
        EncounterDetails encounterDetails = mEncounterDetailsList.get(position);
        holder.bind(encounterDetails);
      } else {
        // No books!
      }
    }

    @Override
    public int getItemCount() {

      if (mEncounterDetailsList != null) {
        return mEncounterDetailsList.size();
      } else {
        return 0;
      }
    }

    public void setEncounterList(Collection<EncounterDetails> encounterDetailsCollection) {

      Log.d(TAG, "++setEncounterList(Collection<EncounterDetails>)");
      mEncounterDetailsList = new ArrayList<>(encounterDetailsCollection);
      mEncounterDetailsList.sort((a, b) -> Long.compare(b.Date, a.Date));
      notifyDataSetChanged();
    }

    class EncounterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

      private final String TAG = Utils.BASE_TAG + EncounterAdapter.EncounterHolder.class.getSimpleName();

      private final TextView mAbbreviationTextView;
      private final TextView mEncounterDateTextView;
      private final TextView mWildlifeTextView;

      private EncounterDetails mEncounterDetails;

      EncounterHolder(View itemView) {
        super(itemView);

        mAbbreviationTextView = itemView.findViewById(R.id.encounter_item_abbreviation);
        mEncounterDateTextView = itemView.findViewById(R.id.encounter_item_date);
        mWildlifeTextView = itemView.findViewById(R.id.encounter_item_wildlife);

        itemView.setOnClickListener(this);
      }

      void bind(EncounterDetails encounterDetails) {

        mEncounterDetails = encounterDetails;

        mAbbreviationTextView.setText(mEncounterDetails.WildlifeAbbreviation);
        mEncounterDateTextView.setText(Utils.displayDate(mEncounterDetails.Date));
        mWildlifeTextView.setText(mEncounterDetails.WildlifeSpecies);
      }

      @Override
      public void onClick(View view) {

        Log.d(TAG, "++EncounterHolder::onClick(View)");
        mCallback.onEncounterDetailsClicked(mEncounterDetails.EncounterId);
      }
    }
  }
}
