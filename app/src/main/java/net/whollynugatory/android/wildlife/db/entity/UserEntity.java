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
package net.whollynugatory.android.wildlife.db.entity;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;

import net.whollynugatory.android.wildlife.Utils;

import java.io.Serializable;

public class UserEntity implements Serializable {

  @Exclude
  public String Id;

  public String DisplayName;

  public String FollowingId;

  public final boolean IsContributor;

  public UserEntity() {

    Id = Utils.UNKNOWN_USER_ID;
    DisplayName = "";
    FollowingId = Utils.DEFAULT_FOLLOWING_USER_ID;
    IsContributor = false;
  }

  @NonNull
  @Override
  public String toString() {

    return DisplayName;
  }
}
