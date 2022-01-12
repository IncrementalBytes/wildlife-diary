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
package net.whollynugatory.android.wildlife.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import net.whollynugatory.android.wildlife.db.entity.EncounterDetails;
import net.whollynugatory.android.wildlife.db.entity.StatisticsDetails;

import java.util.List;

public class FragmentDataViewModel extends ViewModel {

  private final MutableLiveData<List<EncounterDetails>> mEncounterDetailsList = new MutableLiveData<>();
  private final MutableLiveData<String> mEncounterId = new MutableLiveData<>();
  private final MutableLiveData<Boolean> mIsContributor = new MutableLiveData<>();
  private final MutableLiveData<String> mMessage = new MutableLiveData<>();
  private final MutableLiveData<StatisticsDetails> mStatisticsDetails = new MutableLiveData<>();
  private final MutableLiveData<String> mUserId = new MutableLiveData<>();
  private final MutableLiveData<String> mUserName = new MutableLiveData<>();

  public LiveData<List<EncounterDetails>> getEncounterDetailsList() {

    return mEncounterDetailsList;
  }

  public LiveData<String> getEncounterId() {

    return mEncounterId;
  }

  public LiveData<Boolean> getIsContributor() {

    return mIsContributor;
  }

  public LiveData<String> getMessage() {

    return mMessage;
  }

  public LiveData<StatisticsDetails> getStatisticsDetails() {

    return mStatisticsDetails;
  }

  public LiveData<String> getUserId() {

    return mUserId;
  }

  public LiveData<String> getUserName() {

    return mUserName;
  }

  public void setEncounterDetailsList(List<EncounterDetails> encounterDetailsList) {

    mEncounterDetailsList.setValue(encounterDetailsList);
  }

  public void setEncounterId(String encounterId) {

    mEncounterId.setValue(encounterId);
  }

  public void setIsContributor(boolean isContributor) {

    mIsContributor.setValue(isContributor);
  }

  public void setMessage(String message) {

    mMessage.setValue(message);
  }

  public void setStatisticsDetails(StatisticsDetails statisticsDetails) {

    mStatisticsDetails.setValue(statisticsDetails);
  }

  public void setUserId(String userId) {

    mUserId.setValue(userId);
  }

  public void setUserName(String fullName) {

    mUserName.setValue(fullName);
  }
}
