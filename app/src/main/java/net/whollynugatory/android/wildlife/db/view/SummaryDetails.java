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
package net.whollynugatory.android.wildlife.db.view;

public class SummaryDetails {

  public int Banded;

  public int ForceFed;

  public int Gavaged;

  public int HandledEuthanasia;

  public int HandledExam;

  public int HandledMedication;

  public int HandledSubcutaneous;

  public int MedicationOral;

  public int MedicationOcular;

  public String MostEncountered;

  public int SpeciesEncountered;

  public int Subcutaneous;

  public int TotalEncounters;

  public SummaryDetails() {

    Banded = 0;
    ForceFed = 0;
    HandledEuthanasia = 0;
    HandledExam = 0;
    HandledMedication = 0;
    HandledSubcutaneous = 0;
    Gavaged = 0;
    MedicationOral = 0;
    MedicationOcular = 0;
    MostEncountered = "";
    SpeciesEncountered = 0;
    Subcutaneous = 0;
    TotalEncounters = 0;
  }
}
