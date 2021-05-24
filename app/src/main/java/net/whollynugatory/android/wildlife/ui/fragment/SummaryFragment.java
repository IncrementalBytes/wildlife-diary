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

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.databinding.FragmentSummaryBinding;
import net.whollynugatory.android.wildlife.db.viewmodel.WildlifeViewModel;

import java.util.Locale;

public class SummaryFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + SummaryFragment.class.getSimpleName();

  public interface OnSummaryListListener {

    void onSummaryClicked(int summaryId);
    void onSummarySet();
    void onSummaryTotalEncounters();
  }

  private FragmentSummaryBinding mBinding;

  private CardView mEuthanasiaCard;

  private OnSummaryListListener mCallback;

  public static SummaryFragment newInstance() {

    Log.d(TAG, "++newInstance()");
    SummaryFragment fragment = new SummaryFragment();
    Bundle arguments = new Bundle();
    fragment.setArguments(arguments);
    return fragment;
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    Log.d(TAG, "++onAttach(Context)");
    try {
      mCallback = (OnSummaryListListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(
        String.format(Locale.US, "Missing interface implementations for %s", context.toString()));
    }
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_summary, container, false);
    mBinding.setFragment(this);

    View view = mBinding.getRoot();
    mEuthanasiaCard = view.findViewById(R.id.summary_card_handled_euthanasia);

    WildlifeViewModel wildlifeViewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);
    String followingUserId = Utils.getFollowingUserId(getActivity());
    wildlifeViewModel.getSummary(followingUserId).observe(getViewLifecycleOwner(), summaryDetails -> {
      mBinding.setSummary(summaryDetails);
      mCallback.onSummarySet();
    });
    return view;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    Log.d(TAG, "++onDestroy()");
    mBinding = null;
  }

  @Override
  public void onResume() {
    super.onResume();

    updateUI();
  }

  public void onCardClick(View view) {

    if (view == null) {
      Log.d(TAG, "No-op");
    } else if (view.getId() == R.id.summary_card_total_encounters) {
      mCallback.onSummaryTotalEncounters();
    } else {
      mCallback.onSummaryClicked(view.getId());
    }
  }

  private void updateUI() {

    mEuthanasiaCard.setVisibility(Utils.getShowSensitive(getActivity()) ? View.VISIBLE : View.GONE);
  }
}
