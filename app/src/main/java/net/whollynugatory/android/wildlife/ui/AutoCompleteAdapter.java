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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

public class AutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

  private ArrayList<String> mFullList;
  private ArrayList<String> mOriginalValues;
  private ArrayFilter mFilter;

  public AutoCompleteAdapter(Context context, int resource, int textViewResourceId, List<String> objects) {
    super(context, resource, textViewResourceId, objects);

    mFullList = (ArrayList<String>) objects;
    mOriginalValues = new ArrayList<>(mFullList);
  }

  @Override
  public int getCount() {
    return mFullList.size();
  }

  @Override
  public String getItem(int position) {
    return mFullList.get(position);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    return super.getView(position, convertView, parent);
  }

  @Override
  public Filter getFilter() {

    if (mFilter == null) {
      mFilter = new ArrayFilter();
    }

    return mFilter;
  }

  private class ArrayFilter extends Filter {

    private final Object mLock = new Object();

    @Override
    protected FilterResults performFiltering(CharSequence prefix) {

      FilterResults results = new FilterResults();
      if (mOriginalValues == null) {
        synchronized (mLock) {
          mOriginalValues = new ArrayList<>(mFullList);
        }
      }

      if (prefix == null || prefix.length() == 0) {
        synchronized (mLock) {
          ArrayList<String> list = new ArrayList<>(mOriginalValues);
          results.values = list;
          results.count = list.size();
        }
      } else {
        final String prefixString = prefix.toString().toLowerCase();
        ArrayList<String> values = mOriginalValues;
        int count = values.size();
        ArrayList<String> newValues = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
          String item = values.get(i);
          if (item.toLowerCase().contains(prefixString)) {
            newValues.add(item);
          }

        }

        results.values = newValues;
        results.count = newValues.size();
      }

      return results;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {

      if (results.values != null) {
        mFullList = (ArrayList<String>) results.values;
      } else {
        mFullList = new ArrayList<>();
      }

      if (results.count > 0) {
        notifyDataSetChanged();
      } else {
        notifyDataSetInvalidated();
      }
    }
  }
}
