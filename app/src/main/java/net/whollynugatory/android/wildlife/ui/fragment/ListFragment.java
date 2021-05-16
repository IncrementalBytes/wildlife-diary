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
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.db.viewmodel.WildlifeViewModel;
import net.whollynugatory.android.wildlife.ui.SimpleListItemAdapter;

import java.util.Locale;

public class ListFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + ListFragment.class.getSimpleName();

  public interface OnSimpleListListener {

    void onWildlifeItemSelected(String wildlifeId);

    void onEncounterItemSelected(String encounterId);

    void onTaskItemSelected(String taskId);

    void onUnknownList();
  }

  private OnSimpleListListener mCallback;

  private ListView mListView;

  private Utils.ListTypes mListType;

  private String mFollowingUserId;
  private WildlifeViewModel mWildlifeViewModel;

  public static ListFragment newInstance(Utils.ListTypes listType) {

    Log.d(TAG, "++newInstance()");
    ListFragment fragment = new ListFragment();
    Bundle arguments = new Bundle();
    arguments.putSerializable(Utils.ARG_LIST_TYPE, listType);
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
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    Log.d(TAG, "++onAttach(Context)");
    try {
      mCallback = (OnSimpleListListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(
        String.format(Locale.US, "Missing interface implementations for %s", context.toString()));
    }

    Bundle arguments = getArguments();
    if (arguments != null) {
      if (arguments.containsKey(Utils.ARG_LIST_TYPE)) {
        mListType = (Utils.ListTypes) arguments.getSerializable(Utils.ARG_LIST_TYPE);
      } else {
        mListType = Utils.ListTypes.Unknown;
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
    final View view = inflater.inflate(R.layout.fragment_simple_list, container, false);
    mListView = view.findViewById(R.id.simple_list_data);

    mFollowingUserId = Utils.getFollowingUserId(getContext());
    mWildlifeViewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);

    if (mListType == Utils.ListTypes.UniqueEncountered) {
      mWildlifeViewModel.getUniqueEncountered(mFollowingUserId).observe(getViewLifecycleOwner(), uniqueEncounteredList -> {

        Log.d(TAG, "Unique Encounter list is " + uniqueEncounteredList.size());
        ListAdapter customAdapter = new SimpleListItemAdapter(getActivity(), 0, uniqueEncounteredList);
        mListView.setAdapter(customAdapter);
      });
    } else if (mListType == Utils.ListTypes.MostEncountered) {
      mWildlifeViewModel.getMostEncountered(mFollowingUserId).observe(getViewLifecycleOwner(), mostEncounteredList -> {

        Log.d(TAG, "Most Encounter list is " + mostEncounteredList.size());
        ListAdapter customAdapter = new SimpleListItemAdapter(getActivity(), 0, mostEncounteredList);
        mListView.setAdapter(customAdapter);
      });
    } else {
      mCallback.onUnknownList();
    }

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
}
