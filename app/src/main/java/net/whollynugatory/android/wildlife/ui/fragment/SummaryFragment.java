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

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.databinding.FragmentSummaryBinding;
import net.whollynugatory.android.wildlife.db.viewmodel.WildlifeViewModel;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Locale;

public class SummaryFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + SummaryFragment.class.getSimpleName();

  public interface OnSummaryListListener {

    void onSummaryAddEncounter();
    void onSummaryClicked(int summaryId);
    void onSummaryTotalEncounters();
  }

  private FragmentSummaryBinding mBinding;

  private CardView mEuthanasiaCard;
  private ImageView mNewTotalEncountersImage;
  private ImageView mNewUniqueEncountersImage;

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
    mNewTotalEncountersImage = view.findViewById(R.id.summary_image_total_encounters_new);
    mNewUniqueEncountersImage = view.findViewById(R.id.summary_image_unique_encounters_new);
    FloatingActionButton addEncounterButton = view.findViewById(R.id.summary_fab_add);

    mNewTotalEncountersImage.setVisibility(View.GONE);
    mNewUniqueEncountersImage.setVisibility(View.GONE);
    addEncounterButton.setOnClickListener(v -> mCallback.onSummaryAddEncounter());

    if (Utils.getIsContributor(getContext())) {
      addEncounterButton.setVisibility(View.VISIBLE);
    } else {
      addEncounterButton.setVisibility(View.GONE);
    }

    WildlifeViewModel wildlifeViewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);
    String followingUserId = Utils.getFollowingUserId(getActivity());
    wildlifeViewModel.getSummary(followingUserId).observe(
      getViewLifecycleOwner(),
      summaryDetails -> mBinding.setSummary(summaryDetails));
    Date in = new Date();
    LocalDateTime localDateTime = LocalDateTime.ofInstant(in.toInstant(), ZoneId.systemDefault()).minusDays(6);
    ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
    wildlifeViewModel.getNewEncountersCount(followingUserId, zonedDateTime.toInstant().toEpochMilli()).observe(
      getViewLifecycleOwner(),
      newEncountersCount -> {

        if (newEncountersCount != null && newEncountersCount > 0) {
          mNewTotalEncountersImage.setVisibility(View.VISIBLE);
        } else {
          mNewTotalEncountersImage.setVisibility(View.GONE);
        }
      }
    );

    wildlifeViewModel.getNewUniqueCount(followingUserId, zonedDateTime.toInstant().toEpochMilli()).observe(
      getViewLifecycleOwner(),
      newUniqueCount -> {

        if (newUniqueCount != null && newUniqueCount > 0) {
          mNewUniqueEncountersImage.setVisibility(View.VISIBLE);
        } else {
          mNewUniqueEncountersImage.setVisibility(View.GONE);
        }
      }
    );

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
