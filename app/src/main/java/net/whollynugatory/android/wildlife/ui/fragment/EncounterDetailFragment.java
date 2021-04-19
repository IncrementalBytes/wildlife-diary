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
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.databinding.FragmentEncounterDetailsBinding;
import net.whollynugatory.android.wildlife.db.viewmodel.WildlifeViewModel;

public class EncounterDetailFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + EncounterDetailFragment.class.getSimpleName();

  private FragmentEncounterDetailsBinding mBinding;

  private String mEncounterId;

  public static EncounterDetailFragment newInstance(String encounterId) {

    Log.d(TAG, "++newInstance(String)");
    EncounterDetailFragment fragment = new EncounterDetailFragment();
    Bundle arguments = new Bundle();
    arguments.putString(Utils.ARG_ENCOUNTER_ID, encounterId);
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
    Bundle arguments = getArguments();
    if (arguments != null) {
      if (arguments.containsKey(Utils.ARG_ENCOUNTER_ID)) {
        mEncounterId = arguments.getString(Utils.ARG_ENCOUNTER_ID);
      } else {
        mEncounterId = Utils.UNKNOWN_ID;
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
    WildlifeViewModel wildlifeViewModel = new ViewModelProvider(this).get(WildlifeViewModel.class);
    mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_encounter_details, container, false);
    mBinding.setFragment(this);
    wildlifeViewModel.getEncounterDetails(mEncounterId).observe(
      getViewLifecycleOwner(),
      encounterDetails -> mBinding.setEncounterDetails(encounterDetails));

    return mBinding.getRoot();
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
    mBinding = null;
  }

  @Override
  public void onResume() {
    super.onResume();

    Log.d(TAG, "++onResume()");
  }

  /*
    Public Method(s)
   */
  public void onCardClick(View view) {

    TextView targetDescription = null;
    if (view.getId() == R.id.encounter_details_card_banded) {
      targetDescription = view.findViewById(R.id.encounter_details_text_banded_desc);
    } else if (view.getId() == R.id.encounter_details_card_force_fed) {
      targetDescription = view.findViewById(R.id.encounter_details_text_force_fed_desc);
    } else if (view.getId() == R.id.encounter_details_card_gavage) {
      targetDescription = view.findViewById(R.id.encounter_details_text_gavage_desc);
    } else if (view.getId() == R.id.encounter_details_card_handled_euthanasia) {
      targetDescription = view.findViewById(R.id.encounter_details_text_handled_euthanasia_desc);
    } else if (view.getId() == R.id.encounter_details_card_handled_exam) {
      targetDescription = view.findViewById(R.id.encounter_details_text_handled_exam_desc);
    } else if (view.getId() == R.id.encounter_details_card_handled_force_fed) {
      targetDescription = view.findViewById(R.id.encounter_details_text_handled_force_fed_desc);
    } else if (view.getId() == R.id.encounter_details_card_handled_gavage) {
      targetDescription = view.findViewById(R.id.encounter_details_text_handled_gavage_desc);
    } else if (view.getId() == R.id.encounter_details_card_handled_medication) {
      targetDescription = view.findViewById(R.id.encounter_details_text_handled_medication_desc);
    } else if (view.getId() == R.id.encounter_details_card_handled_subcutaneous) {
      targetDescription = view.findViewById(R.id.encounter_details_text_handled_subcutaneous_desc);
    } else if (view.getId() == R.id.encounter_details_card_ocular_medication) {
      targetDescription = view.findViewById(R.id.encounter_details_text_ocular_medication_desc);
    } else if (view.getId() == R.id.encounter_details_card_oral_medication) {
      targetDescription = view.findViewById(R.id.encounter_details_text_oral_medication_desc);
    } else if (view.getId() == R.id.encounter_details_card_subcutaneous) {
      targetDescription = view.findViewById(R.id.encounter_details_text_subcutaneous_desc);
    }

    if (targetDescription != null) {
      targetDescription.setVisibility(targetDescription.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }
  }
}
