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

import net.whollynugatory.android.wildlife.Utils;

public class WildlifeSummary {

  public int EncounterCount;
  public String ImageAttribution;
  public String ImageUrl;
  public String WildlifeId;
  public String WildlifeSpecies;

  public WildlifeSummary() {

    EncounterCount = 0;
    ImageAttribution = Utils.UNKNOWN_STRING;
    ImageUrl = Utils.UNKNOWN_STRING;
    WildlifeId = Utils.UNKNOWN_ID;
    WildlifeSpecies = Utils.UNKNOWN_STRING;
  }
}
