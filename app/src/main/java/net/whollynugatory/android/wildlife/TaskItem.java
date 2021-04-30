package net.whollynugatory.android.wildlife;

public class TaskItem {

  private String mName;
  private String mDescription;

  public TaskItem() {

    mName = Utils.UNKNOWN_STRING;
    mDescription = Utils.UNKNOWN_STRING;
  }

  public TaskItem(String name, String description) {

    mName = name;
    mDescription = description;
  }

  public String getName() {

    return mName;
  }

  public String getDescription() {

    return mDescription;
  }

  public void setName(String name) {

    mName = name;
  }

  public void setDescription(String description) {

    mDescription = description;
  }
}
