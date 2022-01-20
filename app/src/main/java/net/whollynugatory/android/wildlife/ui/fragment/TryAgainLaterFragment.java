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
package net.whollynugatory.android.wildlife.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;

public class TryAgainLaterFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + TryAgainLaterFragment.class.getSimpleName();

  /*
    Fragment Override(s)
   */
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    View view = inflater.inflate(R.layout.fragment_try_again_later, container, false);
    TextView message = view.findViewById(R.id.try_again_text_message);

    Bundle arguments = getArguments();
    if (arguments != null) {
      message.setText(arguments.getString(Utils.ARG_MESSAGE));
    } // TODO: if message is not set, then what?

    return view;
  }
}
