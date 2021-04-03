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
package net.whollynugatory.android.wildlife;

public class SpinnerItemState {

  private String mId;
  private boolean mSelected;
  private String mTitle;

  public SpinnerItemState() {

    mId = Utils.UNKNOWN_ID;
    mSelected = false;
    mTitle = Utils.UNKNOWN_STRING;
  }

  public SpinnerItemState(String id, String title) {

    mId = id;
    mSelected = false;
    mTitle = title;
  }

  public String getId() {

    return mId;
  }

  public String getTitle() {

    return mTitle;
  }

  public boolean isSelected() {

    return !mId.equals(Utils.UNKNOWN_ID) && mSelected;
  }

  public void setId(String id) {

    mId = id;
  }

  public void setSelected(boolean selected) {

    mSelected = !mId.equals(Utils.UNKNOWN_ID) && selected;
  }

  public void setTitle(String title) {

    mTitle = title;
  }
}
