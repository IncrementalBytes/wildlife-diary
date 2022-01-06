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
import androidx.navigation.fragment.NavHostFragment;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.databinding.FragmentStatisticsBinding;
import net.whollynugatory.android.wildlife.db.viewmodel.WildlifeViewModel;
import net.whollynugatory.android.wildlife.ui.viewmodel.FragmentDataViewModel;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class StatisticsFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + StatisticsFragment.class.getSimpleName();

  private FragmentStatisticsBinding mBinding;

  private CardView mEuthanasiaCard;
  private ImageView mNewTotalEncountersImage;
  private ImageView mNewFirstEncounteredImage;

  private String mFollowingUserId;
  private boolean mShowSensitive;
  private WildlifeViewModel mWildlifeViewModel;

  /*
    Fragment Override(s)
   */
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_statistics, container, false);
    mBinding.setFragment(this);

    View view = mBinding.getRoot();
    mEuthanasiaCard = view.findViewById(R.id.statistics_card_handled_euthanasia);
    mNewTotalEncountersImage = view.findViewById(R.id.statistics_image_total_encounters_new);
    mNewFirstEncounteredImage = view.findViewById(R.id.statistics_image_first_encountered_new);

    mNewTotalEncountersImage.setVisibility(View.GONE);
    mNewFirstEncounteredImage.setVisibility(View.GONE);

    mWildlifeViewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);
    mFollowingUserId = Utils.getFollowingUserId(getContext());
    mShowSensitive = Utils.getShowSensitive(getContext());
    boolean showSensitive = Utils.getShowSensitive(getContext());
    mWildlifeViewModel.getStatistics(mFollowingUserId, showSensitive).observe(
      getViewLifecycleOwner(),
      statisticsDetails -> mBinding.setStatistics(statisticsDetails));
    Date in = new Date();
    LocalDateTime localDateTime = LocalDateTime.ofInstant(in.toInstant(), ZoneId.systemDefault()).minusDays(6);
    ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
    mWildlifeViewModel.getNewEncounters(mFollowingUserId, zonedDateTime.toInstant().toEpochMilli(), mShowSensitive).observe(
      getViewLifecycleOwner(),
      newEncounters -> {

        if (newEncounters != null && newEncounters.size() > 0) {
          mNewTotalEncountersImage.setVisibility(View.VISIBLE);
        } else {
          mNewTotalEncountersImage.setVisibility(View.GONE);
        }
      }
    );

    mWildlifeViewModel.getNewUnique(mFollowingUserId, zonedDateTime.toInstant().toEpochMilli(), mShowSensitive).observe(
      getViewLifecycleOwner(),
      newUnique -> {

        if (newUnique != null && newUnique.size() > 0) {
          mNewFirstEncounteredImage.setVisibility(View.VISIBLE);
        } else {
          mNewFirstEncounteredImage.setVisibility(View.GONE);
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
        mWildlifeViewModel.getAllEncounterDetails(mFollowingUserId, mShowSensitive).observe(
          getViewLifecycleOwner(),
          encounterDetailsList -> {

            FragmentDataViewModel viewModel = new ViewModelProvider(requireActivity())
              .get(FragmentDataViewModel.class);
            viewModel.setEncounterDetailsList(encounterDetailsList);
            NavHostFragment.findNavController(requireParentFragment())
              .navigate(R.id.action_statisticsFragment_to_encounterDetailsListFragment);
          });
      } else if (view.getId() == R.id.statistics_card_unique_encounters) {
        mWildlifeViewModel.getFirstEncountered(mFollowingUserId, mShowSensitive).observe(
          getViewLifecycleOwner(),
          firstEncounteredList -> {

            FragmentDataViewModel viewModel = new ViewModelProvider(requireActivity())
              .get(FragmentDataViewModel.class);
            viewModel.setEncounterDetailsList(firstEncounteredList);
            NavHostFragment.findNavController(requireParentFragment())
              .navigate(R.id.action_statisticsFragment_to_firstEncounteredListFragment);
          });
      } else if (view.getId() == R.id.statistics_card_most_encountered) {
        mWildlifeViewModel.getMostEncountered(mFollowingUserId, mShowSensitive).observe(
          getViewLifecycleOwner(),
          mostEncountered -> {

            FragmentDataViewModel viewModel = new ViewModelProvider(requireActivity())
              .get(FragmentDataViewModel.class);
            viewModel.setWildlifeSummaryList(mostEncountered);
            NavHostFragment.findNavController(requireParentFragment())
              .navigate(R.id.action_statisticsFragment_to_mostEncounteredListFragment);
          });
      }
    }
  }

  private void updateUI() {

    mEuthanasiaCard.setVisibility(Utils.getShowSensitive(getContext()) ? View.VISIBLE : View.GONE);
  }
}
