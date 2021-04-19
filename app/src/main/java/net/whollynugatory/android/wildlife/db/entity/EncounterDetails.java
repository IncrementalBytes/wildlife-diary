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
 * Class defining what tasks were performed during an encounter.
 */
public class EncounterDetails {

  public long Date;

  public String DescriptionBanded;

  public String DescriptionForceFed;

  public String DescriptionGavage;

  public String DescriptionHandledEuthanasia;

  public String DescriptionHandledExam;

  public String DescriptionHandledForceFed;

  public String DescriptionHandledGavage;

  public String DescriptionHandledMedication;

  public String DescriptionHandledSubcutaneous;

  public String DescriptionOcularMedication;

  public String DescriptionOralMedication;

  public String DescriptionSubcutaneous;

  public String EncounterId;

  public boolean IsBanded;

  public boolean IsForceFed;

  public boolean IsGavage;

  public boolean IsHandledEuthanasia;

  public boolean IsHandledExam;

  public boolean IsHandledForceFed;

  public boolean IsHandledGavage;

  public boolean IsHandledMedication;

  public boolean IsHandledSubcutaneous;

  public boolean IsOcularMedication;

  public boolean IsOralMedication;

  public boolean IsSubcutaneous;

  public String UserId;

  public String WildlifeAbbreviation;

  public String WildlifeId;

  public String WildlifeSpecies;


  public EncounterDetails() {

    Date = 0;
    DescriptionBanded = "";
    DescriptionForceFed = "";
    DescriptionGavage = "";
    DescriptionHandledEuthanasia = "";
    DescriptionHandledExam = "";
    DescriptionHandledForceFed = "";
    DescriptionHandledGavage = "";
    DescriptionHandledMedication = "";
    DescriptionHandledSubcutaneous = "";
    DescriptionOcularMedication = "";
    DescriptionOralMedication = "";
    DescriptionSubcutaneous = "";
    EncounterId = Utils.UNKNOWN_ID;
    IsBanded = false;
    IsForceFed = false;
    IsGavage = false;
    IsHandledEuthanasia = false;
    IsHandledExam = false;
    IsHandledForceFed = false;
    IsHandledGavage = false;
    IsHandledMedication = false;
    IsHandledSubcutaneous = false;
    IsOcularMedication = false;
    IsOralMedication = false;
    IsSubcutaneous = false;
    UserId = Utils.UNKNOWN_USER_ID;
    WildlifeAbbreviation = Utils.UNKNOWN_STRING;
    WildlifeId = Utils.UNKNOWN_ID;
    WildlifeSpecies = Utils.UNKNOWN_STRING;
  }
}
