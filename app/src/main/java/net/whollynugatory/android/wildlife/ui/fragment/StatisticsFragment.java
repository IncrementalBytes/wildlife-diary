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
import net.whollynugatory.android.wildlife.databinding.FragmentStatisticsBinding;
import net.whollynugatory.android.wildlife.db.viewmodel.WildlifeViewModel;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Locale;

public class StatisticsFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + StatisticsFragment.class.getSimpleName();

  public interface OnStatisticsListListener {

    void onStatisticsAddEncounter();

    void onStatisticsMostEncountered();

    void onStatisticsTotalEncounters();

    void onStatisticsUniqueEncounters();
  }

  private FragmentStatisticsBinding mBinding;

  private CardView mEuthanasiaCard;
  private ImageView mNewTotalEncountersImage;
  private ImageView mNewUniqueEncountersImage;

  private OnStatisticsListListener mCallback;

  private String mFollowingUserId;
  private WildlifeViewModel mWildlifeViewModel;

  public static StatisticsFragment newInstance() {

    Log.d(TAG, "++newInstance()");
    return new StatisticsFragment();
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    Log.d(TAG, "++onAttach(Context)");
    try {
      mCallback = (OnStatisticsListListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(
        String.format(Locale.US, "Missing interface implementations for %s", context.toString()));
    }
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_statistics, container, false);
    mBinding.setFragment(this);

    View view = mBinding.getRoot();
    mEuthanasiaCard = view.findViewById(R.id.statistics_card_handled_euthanasia);
    mNewTotalEncountersImage = view.findViewById(R.id.statistics_image_total_encounters_new);
    mNewUniqueEncountersImage = view.findViewById(R.id.statistics_image_unique_encounters_new);
    FloatingActionButton addEncounterButton = view.findViewById(R.id.statistics_fab_add);

    mNewTotalEncountersImage.setVisibility(View.GONE);
    mNewUniqueEncountersImage.setVisibility(View.GONE);
    addEncounterButton.setOnClickListener(v -> mCallback.onStatisticsAddEncounter());

    if (Utils.getIsContributor(getContext())) {
      addEncounterButton.setVisibility(View.VISIBLE);
    } else {
      addEncounterButton.setVisibility(View.GONE);
    }

    mWildlifeViewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);
    mFollowingUserId = Utils.getFollowingUserId(getActivity());
    mWildlifeViewModel.getStatistics(mFollowingUserId).observe(
      getViewLifecycleOwner(),
      statisticsDetails -> mBinding.setStatistics(statisticsDetails));
    Date in = new Date();
    LocalDateTime localDateTime = LocalDateTime.ofInstant(in.toInstant(), ZoneId.systemDefault()).minusDays(6);
    ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
    mWildlifeViewModel.getNewEncounters(mFollowingUserId, zonedDateTime.toInstant().toEpochMilli()).observe(
      getViewLifecycleOwner(),
      newEncounters -> {

        if (newEncounters != null && newEncounters.size() > 0) {
          mNewTotalEncountersImage.setVisibility(View.VISIBLE);
        } else {
          mNewTotalEncountersImage.setVisibility(View.GONE);
        }
      }
    );

    mWildlifeViewModel.getNewUnique(mFollowingUserId, zonedDateTime.toInstant().toEpochMilli()).observe(
      getViewLifecycleOwner(),
      newUnique -> {

        if (newUnique != null && newUnique.size() > 0) {
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

    if (view != null) {
      if (view.getId() == R.id.statistics_card_total_encounters) {
        mWildlifeViewModel.getAllEncounterDetails(mFollowingUserId).observe(
          getViewLifecycleOwner(),
          encounterDetailsList -> {

            Utils.setEncounterDetailsList(getContext(), encounterDetailsList);
            mCallback.onStatisticsTotalEncounters();
          });
      } else if (view.getId() == R.id.statistics_card_unique_encounters) {
        mWildlifeViewModel.getUniqueEncountered(mFollowingUserId).observe(
          getViewLifecycleOwner(),
          uniqueEncounters -> {

            Utils.setEncounterDetailsList(getContext(), uniqueEncounters);
            mCallback.onStatisticsUniqueEncounters();
          });
      } else if (view.getId() == R.id.statistics_card_most_encountered) {
        mWildlifeViewModel.getMostEncountered(mFollowingUserId).observe(
          getViewLifecycleOwner(),
          mostEncountered -> {

            Utils.setWildlifeSummaryList(getContext(), mostEncountered);
            mCallback.onStatisticsMostEncountered();
          });
      }
    }
  }

  private void updateUI() {

    mEuthanasiaCard.setVisibility(Utils.getShowSensitive(getActivity()) ? View.VISIBLE : View.GONE);
  }
}
