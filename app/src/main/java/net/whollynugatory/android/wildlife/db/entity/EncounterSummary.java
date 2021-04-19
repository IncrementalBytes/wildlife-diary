package net.whollynugatory.android.wildlife.db.entity;

import net.whollynugatory.android.wildlife.Utils;

public class EncounterSummary {

  public long Date;

  public String EncounterId;

  public String UserId;

  public String WildlifeAbbreviation;

  public String WildlifeId;

  public String WildlifeSpecies;


  public EncounterSummary() {

    Date = 0;
    EncounterId = Utils.UNKNOWN_ID;
    UserId = Utils.UNKNOWN_USER_ID;
    WildlifeAbbreviation = Utils.UNKNOWN_STRING;
    WildlifeId = Utils.UNKNOWN_ID;
    WildlifeSpecies = Utils.UNKNOWN_STRING;
  }
}
