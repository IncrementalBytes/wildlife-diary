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

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.databinding.FragmentTasksBinding;
import net.whollynugatory.android.wildlife.db.viewmodel.WildlifeViewModel;

public class TasksFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + TasksFragment.class.getSimpleName();

  private FragmentTasksBinding mBinding;

  private CardView mEuthanasiaCard;

  /*
    Fragment Override(s)
   */
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_tasks, container, false);
    mBinding.setFragment(this);

    View view = mBinding.getRoot();
    mEuthanasiaCard = view.findViewById(R.id.tasks_card_handled_euthanasia);

    WildlifeViewModel wildlifeViewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);
    String followingUserId = Utils.getFollowingUserId(getContext());
    boolean showSensitive = Utils.getShowSensitive(getContext());
    wildlifeViewModel.getStatistics(followingUserId, showSensitive).observe(
      getViewLifecycleOwner(),
      statisticsDetails -> mBinding.setStatistics(statisticsDetails));

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

  /*
    Private Method(s)
   */
  private void updateUI() {

    mEuthanasiaCard.setVisibility(Utils.getShowSensitive(getContext()) ? View.VISIBLE : View.GONE);
  }
}
