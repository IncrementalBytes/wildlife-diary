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

    void onSummaryMostEncountered();
    void onSummaryTotalEncounters();
    void onSummaryUniqueEncounters();
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
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    Log.d(TAG, "++onActivityCreated(Bundle)");
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
    wildlifeViewModel.getSummary(followingUserId).observe(getViewLifecycleOwner(), summaryDetails ->
      mBinding.setSummary(summaryDetails));
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

    TextView targetDescription = null;
    if (view.getId() == R.id.summary_card_total_encounters) {
      mCallback.onSummaryTotalEncounters();
    } else if (view.getId() == R.id.summary_card_unique_encounters) {
      mCallback.onSummaryUniqueEncounters();
    } else if (view.getId() == R.id.summary_card_most_encountered) {
      mCallback.onSummaryMostEncountered();
    } else if (view.getId() == R.id.summary_card_banded) {
      // TODO: list of wildlife banded
    } else if (view.getId() == R.id.summary_card_handled_euthanasia) {
      // TODO: list of wildlife handled for euthanasia
    } else if (view.getId() == R.id.summary_card_handled_exam) {
      // TODO: list of wildlife handled for exam
    } else if (view.getId() == R.id.summary_card_handled_force_fed) {
      // TODO: list of wildlife handled for force fed
    } else if (view.getId() == R.id.summary_card_handled_gavage) {
      // TODO: list of wildlife handled for gavage
    } else if (view.getId() == R.id.summary_card_handled_medication) {
      // TODO: list of wildlife handled for medication
    } else if (view.getId() == R.id.summary_card_handled_subcutaneous) {
      // TODO: list of wildlife handled for subcutaneous
    } else if (view.getId() == R.id.summary_card_force_fed) {
      // TODO: list of wildlife force fed
    } else if (view.getId() == R.id.summary_card_gavage) {
      // TODO: list of wildlife handled for gavage
    } else if (view.getId() == R.id.summary_card_medication_ocular) {
      // TODO: list of wildlife medicated ocular
    } else if (view.getId() == R.id.summary_card_medication_oral) {
      // TODO: list of wildlife medicated oral
    } else if (view.getId() == R.id.summary_card_subcutaneous) {
      // TODO: list of wildlife given subcutaneous fluids
    }

    if (targetDescription != null) {
      targetDescription.setVisibility(targetDescription.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }
  }

  private void updateUI() {

    mEuthanasiaCard.setVisibility(Utils.getShowSensitive(getActivity()) ? View.VISIBLE : View.GONE);
  }
}
