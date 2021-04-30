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
import android.widget.TextView;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.TaskItem;

import java.util.ArrayList;
import java.util.List;

public class TaskListItemAdapter extends ArrayAdapter<TaskItem> {

  private final Context mContext;
  private final ArrayList<TaskItem> mList;

  public TaskListItemAdapter(Context context, int resource, List<TaskItem> objects) {
    super(context, resource, objects);

    mContext = context;
    mList = (ArrayList<TaskItem>) objects;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    return getCustomView(position, convertView);
  }

  public View getCustomView(final int position, View convertView) {

    final ViewHolder holder;
    if (convertView == null) {
      LayoutInflater layoutInflater = LayoutInflater.from(mContext);
      convertView = layoutInflater.inflate(R.layout.task_list_item, null);
      holder = new ViewHolder();
      holder.NameText = convertView.findViewById(R.id.task_list_item_text_name);
      holder.DescriptionText = convertView.findViewById(R.id.task_list_item_text_desc);
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    holder.NameText.setText(mList.get(position).getName());
    holder.DescriptionText.setText(mList.get(position).getDescription());
    return convertView;
  }

  private static class ViewHolder {

    private TextView NameText;
    private TextView DescriptionText;
  }
}
