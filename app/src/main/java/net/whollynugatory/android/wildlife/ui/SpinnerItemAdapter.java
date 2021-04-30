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
package net.whollynugatory.android.wildlife.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.SpinnerItemState;

import java.util.ArrayList;
import java.util.List;

public class SpinnerItemAdapter extends ArrayAdapter<SpinnerItemState> {

  private final Context mContext;
  private final ArrayList<SpinnerItemState> mListState;
  private boolean mIsFromView = false;

  public SpinnerItemAdapter(Context context, int resource, List<SpinnerItemState> objects) {
    super(context, resource, objects);

    mContext = context;
    mListState = (ArrayList<SpinnerItemState>) objects;
  }

  @Override
  public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {

    return getCustomView(position, convertView);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    return getCustomView(position, convertView);
  }

  public View getCustomView(final int position, View convertView) {

    final ViewHolder holder;
    if (convertView == null) {
      LayoutInflater layoutInflater = LayoutInflater.from(mContext);
      convertView = layoutInflater.inflate(R.layout.spinner_item, null);
      holder = new ViewHolder();
      holder.NameText = convertView.findViewById(R.id.spinner_text_name);
      holder.ItemCheckBox = convertView.findViewById(R.id.spinner_check);
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    holder.NameText.setText(mListState.get(position).getTitle());

    mIsFromView = true;
    holder.ItemCheckBox.setChecked(mListState.get(position).isSelected());
    mIsFromView = false;

    if ((position == 0)) {
      holder.ItemCheckBox.setVisibility(View.INVISIBLE);
    } else {
      holder.ItemCheckBox.setVisibility(View.VISIBLE);
    }

    holder.ItemCheckBox.setTag(position);
    holder.ItemCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

      int getPosition = (Integer) buttonView.getTag();
      if (!mIsFromView) {
        mListState.get(position).setSelected(isChecked);
      }
    });

    return convertView;
  }

  private static class ViewHolder {

    private TextView NameText;
    private CheckBox ItemCheckBox;
  }
}
