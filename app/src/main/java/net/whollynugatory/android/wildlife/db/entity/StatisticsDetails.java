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

/**
 * Class containing total tasks and other overall details for all encounters.
 */
public class StatisticsDetails {

  public String MostEncountered;

  public int TotalBanded;

  public int TotalEncounters;

  public int TotalForceFed;

  public int TotalGavage;

  public int TotalHandledEuthanasia;

  public int TotalHandledExam;

  public int TotalHandledForceFed;

  public int TotalHandledGavage;

  public int TotalHandledMedication;

  public int TotalHandledSubcutaneous;

  public int TotalOcularMedicated;

  public int TotalOralMedicated;

  public int TotalSpeciesEncountered;

  public int TotalSubcutaneous;

  public int TotalSyringeFed;

  public StatisticsDetails() {

    MostEncountered = Utils.UNKNOWN_ID;
    TotalBanded = 0;
    TotalEncounters = 0;
    TotalForceFed = 0;
    TotalGavage = 0;
    TotalHandledEuthanasia = 0;
    TotalHandledExam = 0;
    TotalHandledForceFed = 0;
    TotalHandledGavage = 0;
    TotalHandledMedication = 0;
    TotalHandledSubcutaneous = 0;
    TotalOcularMedicated = 0;
    TotalOralMedicated = 0;
    TotalSpeciesEncountered = 0;
    TotalSubcutaneous = 0;
    TotalSyringeFed = 0;
  }
}
