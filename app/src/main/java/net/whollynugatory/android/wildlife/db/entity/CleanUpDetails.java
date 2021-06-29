package net.whollynugatory.android.wildlife.db.entity;

import net.whollynugatory.android.wildlife.Utils;

public class CleanUpDetails {

  public String Id;

  public String Name;

  public String Type;

  public CleanUpDetails() {
    Id = Utils.UNKNOWN_ID;
    Name = Utils.UNKNOWN_STRING;
    Type = Utils.UNKNOWN_STRING;
  }
}
