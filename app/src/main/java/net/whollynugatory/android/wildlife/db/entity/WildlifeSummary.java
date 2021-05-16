package net.whollynugatory.android.wildlife.db.entity;

import net.whollynugatory.android.wildlife.Utils;

public class WildlifeSummary {

  public String WildlifeId;
  public String FriendlyName;
  public int EncounterCount;

  public WildlifeSummary() {

    WildlifeId = Utils.UNKNOWN_ID;
    FriendlyName = Utils.UNKNOWN_STRING;
    EncounterCount = 0;
  }
}
