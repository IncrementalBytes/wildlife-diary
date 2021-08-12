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

import static java.util.Map.Entry.comparingByKey;
import static java.util.stream.Collectors.toMap;

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
import net.whollynugatory.android.wildlife.db.entity.EncounterDetails;
import net.whollynugatory.android.wildlife.db.viewmodel.WildlifeViewModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class DateListFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + DateListFragment.class.getSimpleName();

  public interface OnDateListListener {

    void onDateListItemClicked();
  }

  private OnDateListListener mCallback;

  public static DateListFragment newInstance() {

    Log.d(TAG, "++newInstance()");
    DateListFragment fragment = new DateListFragment();
    Bundle arguments = new Bundle();
    fragment.setArguments(arguments);
    return fragment;
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    Log.d(TAG, "++onAttach(Context)");
    try {
      mCallback = (OnDateListListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(
        String.format(Locale.US, "Missing interface implementations for %s", context.toString()));
    }
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    final View view = inflater.inflate(R.layout.fragment_date_list, container, false);
    RecyclerView recyclerView = view.findViewById(R.id.date_list_recycler_view);
    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    DateAdapter dateAdapter = new DateAdapter(getContext());
    recyclerView.setAdapter(dateAdapter);

    WildlifeViewModel wildlifeViewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);
    String followingUserId = Utils.getFollowingUserId(getActivity());
    wildlifeViewModel.getAllEncounterDetails(followingUserId).observe(
      getViewLifecycleOwner(),
      totalEncounterList -> {

      HashMap<String, DateItem> dateMap = new HashMap<>();
      for (EncounterDetails encounterDetails : totalEncounterList) {
        String simplifiedDateString = Utils.fromTimestamp(encounterDetails.Date);
        if (dateMap.containsKey(simplifiedDateString)) {
          DateItem dateItem = dateMap.get(simplifiedDateString);
          if (dateItem != null) {
            dateItem.Value++;
          } else {
            Log.w(TAG, "Could not get date instance from map: " + simplifiedDateString);
          }
        } else {
          dateMap.put(
            simplifiedDateString,
            new DateItem(String.valueOf(encounterDetails.Date), simplifiedDateString, 1));
        }
      }

      List<DateItem> sortedDateCollection = new ArrayList<>(dateMap.values());
      sortedDateCollection.sort((s1, s2) -> s2.TimeStamp.compareTo(s1.TimeStamp));
      dateAdapter.setDateSummaryList(sortedDateCollection);
    });

    return view;
  }

  private class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateHolder> {

    private final String TAG = Utils.BASE_TAG + DateAdapter.class.getSimpleName();

    private final LayoutInflater mInflater;
    private List<DateItem> mDateItems;

    public DateAdapter(Context context) {

      mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public DateAdapter.DateHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

      View itemView = mInflater.inflate(R.layout.date_list_item, parent, false);
      return new DateAdapter.DateHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DateAdapter.DateHolder holder, int position) {

      if (mDateItems != null) {
        DateItem dateItem = mDateItems.get(position);
        holder.bind(dateItem);
      } else {
        Log.w(TAG, "DateSummary is empty at this time.");
      }
    }

    @Override
    public int getItemCount() {

      return mDateItems != null ? mDateItems.size() :0;
    }

    public void setDateSummaryList(Collection<DateItem> dateItemCollection) {

      Log.d(TAG, "++setDateSummaryList(Collection<DateItem>)");
      mDateItems = new ArrayList<>(dateItemCollection);
      notifyDataSetChanged();
    }

    class DateHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

      private final TextView mDateTextView;
      private final TextView mCountTextView;

      private DateItem mDateItem;

      DateHolder(View itemView) {
        super(itemView);

        mDateTextView = itemView.findViewById(R.id.date_list_item_text_name);
        mCountTextView = itemView.findViewById(R.id.date_list_item_text_desc);
        mCountTextView.setTextSize(18f);
      }

      void bind(DateItem dateItem) {

        mDateItem = dateItem;

        mDateTextView.setText(mDateItem.Identifier);
        mCountTextView.setText(String.valueOf(mDateItem.Value));
      }

      @Override
      public void onClick(View view) {

//        Log.d(TAG, "++EncounterHolder::onClick(View)");
//        Utils.setEncounterDetailsList(getContext(), mDateItem.Identifier);
//        mCallback.onDateListItemClicked();
      }
    }
  }

  static class DateItem implements Comparable<String> {

    public String Identifier;
    public String TimeStamp;
    public int Value;

    public DateItem(String timeStamp, String identifier, int value) {

      TimeStamp = timeStamp;
      Identifier = identifier;
      Value = value;
    }

    @Override
    public int compareTo(String s) {

      return Identifier.compareTo(s);
    }
  }
}
