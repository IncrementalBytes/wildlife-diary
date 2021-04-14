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
import android.widget.TableRow;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.databinding.FragmentSummaryBinding;
import net.whollynugatory.android.wildlife.db.viewmodel.WildlifeViewModel;

public class SummaryFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + SummaryFragment.class.getSimpleName();

  public static SummaryFragment newInstance() {

    Log.d(TAG, "++newInstance()");
    return new SummaryFragment();
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
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    WildlifeViewModel wildlifeViewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);
    FragmentSummaryBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_summary, container, false);
    View view = binding.getRoot();
    wildlifeViewModel.getSummary(Utils.getShowSensitive(getActivity())).observe(getViewLifecycleOwner(), binding::setSummary);
    TableRow euthanasiaRow = view.findViewById(R.id.summary_row_handled_euthanasia);
    euthanasiaRow.setVisibility(Utils.getShowSensitive(getActivity()) ? View.VISIBLE : View.GONE);
    return view;
  }
}
