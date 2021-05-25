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

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;

import java.util.Locale;

public class TryAgainLaterFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + TryAgainLaterFragment.class.getSimpleName();

  public interface OnTryAgainLaterListener {

    void onTryAgainLaterSignOut();
    void onTryAgainLaterTryAgain();
  }

  private OnTryAgainLaterListener mCallback;

  private String mMessage;

  public static TryAgainLaterFragment newInstance(String message) {

    Log.d(TAG, "++newInstance(String)");
    TryAgainLaterFragment fragment = new TryAgainLaterFragment();
    Bundle arguments = new Bundle();
    arguments.putString(Utils.ARG_MESSAGE, message);
    fragment.setArguments(arguments);
    return fragment;
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    Log.d(TAG, "++onAttach(Context)");
    Bundle arguments = getArguments();
    if (arguments != null && arguments.containsKey(Utils.ARG_MESSAGE)) {
      mMessage = arguments.getString(Utils.ARG_MESSAGE);
    } else {
      mMessage = "An unexpected error occurred. Try again later or sign out/sign into app.";
    }

    try {
      mCallback = (OnTryAgainLaterListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(
        String.format(
          Locale.US,
          "Missing interface implementations for %s",
          context.toString()));
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "++onCreate(Bundle)");
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    View view = inflater.inflate(R.layout.fragment_try_again_later, container, false);
    TextView message = view.findViewById(R.id.try_again_text_message);
    message.setText(mMessage);
    Button tryAgainButton = view.findViewById(R.id.try_again_button_try_again);
    tryAgainButton.setOnClickListener(view1 -> mCallback.onTryAgainLaterTryAgain());
    Button signOutButton = view.findViewById(R.id.try_again_button_sign_out);
    signOutButton.setOnClickListener(view12 -> mCallback.onTryAgainLaterSignOut());
    return view;
  }
}
