/*
 * Copyright 2022 Ryan Ward
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
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.databinding.FragmentStatisticsBinding;
import net.whollynugatory.android.wildlife.db.viewmodel.WildlifeViewModel;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class StatisticsFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + StatisticsFragment.class.getSimpleName();

  private FragmentStatisticsBinding mBinding;

  private ImageView mNewTotalEncountersImage;
  private ImageView mNewFirstEncounteredImage;

  /*
    Fragment Override(s)
   */
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_statistics, container, false);
    mBinding.setFragment(this);

    View view = mBinding.getRoot();
    mNewTotalEncountersImage = view.findViewById(R.id.statistics_image_total_encounters_new);
    mNewFirstEncounteredImage = view.findViewById(R.id.statistics_image_first_encountered_new);

    mNewTotalEncountersImage.setVisibility(View.GONE);
    mNewFirstEncounteredImage.setVisibility(View.GONE);

    WildlifeViewModel wildlifeViewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);
    String followingUserId = Utils.getFollowingUserId(getContext());
    boolean showSensitive = Utils.getShowSensitive(getContext());
    wildlifeViewModel.getStatistics(followingUserId, showSensitive).observe(
      getViewLifecycleOwner(),
      statisticsDetails -> mBinding.setStatistics(statisticsDetails));

    // determine if any entries are within a week
    Date in = new Date();
    LocalDateTime localDateTime = LocalDateTime.ofInstant(in.toInstant(), ZoneId.systemDefault()).minusDays(6);
    long epochMilli = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    wildlifeViewModel.getNewEncounters(followingUserId, epochMilli, showSensitive).observe(
      getViewLifecycleOwner(),
      newEncounters -> {

        if (newEncounters != null && newEncounters.size() > 0) {
          mNewTotalEncountersImage.setVisibility(View.VISIBLE);
        } else {
          mNewTotalEncountersImage.setVisibility(View.GONE);
        }
      }
    );

    wildlifeViewModel.getNewUnique(followingUserId, epochMilli, showSensitive).observe(
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

  public void onCardClick(View view) {

    if (view != null) {
      if (view.getId() == R.id.statistics_card_total_encounters) {
        NavHostFragment.findNavController(requireParentFragment())
          .navigate(R.id.action_statisticsFragment_to_encounterDetailsListFragment);
      } else if (view.getId() == R.id.statistics_card_unique_encounters) {
        NavHostFragment.findNavController(requireParentFragment())
          .navigate(R.id.action_statisticsFragment_to_firstEncounteredListFragment);
      } else if (view.getId() == R.id.statistics_card_most_encountered) {
        NavHostFragment.findNavController(requireParentFragment())
          .navigate(R.id.action_statisticsFragment_to_mostEncounteredListFragment);
      } else if (view.getId() == R.id.statistics_card_total_tasks) {
        NavHostFragment.findNavController(requireParentFragment())
          .navigate(R.id.action_statisticsFragment_to_tasksFragment);
      }
    }
  }
}
