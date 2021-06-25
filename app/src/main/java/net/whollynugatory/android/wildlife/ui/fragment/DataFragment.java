package net.whollynugatory.android.wildlife.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import net.whollynugatory.android.wildlife.R;
import net.whollynugatory.android.wildlife.Utils;
import net.whollynugatory.android.wildlife.db.WildlifeDatabase;
import net.whollynugatory.android.wildlife.db.entity.EncounterEntity;
import net.whollynugatory.android.wildlife.db.entity.TaskEntity;
import net.whollynugatory.android.wildlife.db.entity.WildlifeEntity;

import java.util.Locale;

public class DataFragment extends Fragment {

  private static final String TAG = Utils.BASE_TAG + DataFragment.class.getSimpleName();

  public interface OnDataListener {

    void onDataEncountersPopulated();

    void onDataFailed(String message);

    void onDataMissing();

    void onDataTasksPopulated();

    void onDataWildlifePopulated();
  }

  private OnDataListener mCallback;

  private TextView mStatusText;

  private String mDataToSync;

  public static DataFragment newInstance(String specificData) {

    Log.d(TAG, "++newInstance(String)");
    DataFragment fragment = new DataFragment();
    Bundle arguments = new Bundle();
    arguments.putString(Utils.ARG_DATA_TO_SYNC, specificData);
    fragment.setArguments(arguments);
    return fragment;
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    Log.d(TAG, "++onAttach(Context)");
    Bundle arguments = getArguments();
    if (arguments != null && arguments.containsKey(Utils.ARG_DATA_TO_SYNC)) {
      mDataToSync = arguments.getString(Utils.ARG_DATA_TO_SYNC);
      Log.d(TAG, "DataToSync: " + mDataToSync);
    } else {
      mDataToSync = Utils.UNKNOWN_STRING;
      Log.e(TAG, "Arguments were null.");
    }

    try {
      mCallback = (OnDataListener) context;
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
    FirebaseDatabase.getInstance().getReference().child(Utils.DATA_STAMPS_ROOT).get().addOnCompleteListener(
      task -> {

        if (!task.isSuccessful()) {
          Log.d(TAG, "Retrieving data stamps was unsuccessful.", task.getException());
          mCallback.onDataFailed("Unable to retrieve data stamps.");
        } else {
          updateUI("Grabbing remote data stamps...");
          String remoteDataStamp = Utils.UNKNOWN_ID;
          DataSnapshot resultSnapshot = task.getResult();
          if (resultSnapshot != null) {
            for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
              String id = dataSnapshot.getKey();
              if (id != null && id.equals(mDataToSync)) {
                Object valueObject = dataSnapshot.getValue();
                if (valueObject != null) {
                  remoteDataStamp = dataSnapshot.getValue().toString();
                  break;
                }
              }
            }

            updateUI("Comparing local data with " + mDataToSync);
            String localDataStamp = Utils.UNKNOWN_ID;
            switch (mDataToSync) {
              case Utils.ENCOUNTER_ROOT:
                localDataStamp = Utils.getEncountersStamp(getActivity());
                break;
              case Utils.TASK_ROOT:
                localDataStamp = Utils.getTasksStamp(getActivity());
                break;
              case Utils.WILDLIFE_ROOT:
                localDataStamp = Utils.getWildlifeStamp(getActivity());
                break;
            }

            if (remoteDataStamp.equals(Utils.UNKNOWN_ID)) {
              Log.w(TAG, "Remote dataStamp was unexpected: " + remoteDataStamp);
              mCallback.onDataMissing();
            } else if (localDataStamp.equals(Utils.UNKNOWN_ID) || !localDataStamp.equalsIgnoreCase(remoteDataStamp)) {
              populateTable(mDataToSync, remoteDataStamp);
            } else if (localDataStamp.equals(remoteDataStamp)){
              Log.d(TAG, "Local data in-sync with " + mDataToSync);
              updateUI("Local data matches " + mDataToSync);
              switch (mDataToSync) {
                case Utils.ENCOUNTER_ROOT:
                  mCallback.onDataEncountersPopulated();
                  break;
                case Utils.TASK_ROOT:
                  mCallback.onDataTasksPopulated();
                  break;
                case Utils.WILDLIFE_ROOT:
                  mCallback.onDataWildlifePopulated();
                  break;
              }
            } else {
              Log.w(TAG, "Unexpected results between local and remote data stamps.");
              mCallback.onDataFailed("Unable to sync data at this time.");
            }
          }
        }
      });
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Log.d(TAG, "++onCreateView(LayoutInflater, ViewGroup, Bundle)");
    View view = inflater.inflate(R.layout.fragment_data, container, false);
    mStatusText = view.findViewById(R.id.data_text_status);
    return view;
  }

  private void populateTable(String dataRoot, String updatedDataStamp) {

    Log.d(TAG, "++populateTable(String, String)");
    FirebaseDatabase.getInstance().getReference().child(dataRoot).get()
      .addOnCompleteListener(task -> {

        if (!task.isSuccessful()) {
          Log.e(TAG, "Error getting data", task.getException());
          mCallback.onDataFailed("Could not retrieve data from " + dataRoot);
        } else {
          DataSnapshot resultSnapshot = task.getResult();
          if (resultSnapshot != null) {
            if (resultSnapshot.getChildrenCount() > 0) {
              Log.d(TAG, "Attempting inserts: " + resultSnapshot.getChildrenCount());
              if (getActivity() != null) {
                for (DataSnapshot dataSnapshot : resultSnapshot.getChildren()) {
                  String id = dataSnapshot.getKey();
                  switch (dataRoot) {
                    case Utils.ENCOUNTER_ROOT:
                      EncounterEntity encounterEntity = dataSnapshot.getValue(EncounterEntity.class);
                      if (encounterEntity != null && id != null) {
                        encounterEntity.Id = id;
                        if (encounterEntity.isValid()) {
                          WildlifeDatabase.databaseWriteExecutor.execute(() ->
                            WildlifeDatabase.getInstance(getContext()).encounterDao().insert(encounterEntity));
                        } else {
                          Log.w(TAG, "Encounter entity was invalid, not adding: " + encounterEntity.toString());
                        }
                      }

                      break;
                    case Utils.TASK_ROOT:
                      TaskEntity taskEntity = dataSnapshot.getValue(TaskEntity.class);
                      if (taskEntity != null && id != null) {
                        taskEntity.Id = id;
                        if (taskEntity.isValid()) {
                          WildlifeDatabase.databaseWriteExecutor.execute(() ->
                            WildlifeDatabase.getInstance(getContext()).taskDao().insert(taskEntity));
                        } else {
                          Log.w(TAG, "Task entity was invalid, not adding: " + taskEntity.toString());
                        }
                      }

                      break;
                    case Utils.WILDLIFE_ROOT:
                      WildlifeEntity wildlifeEntity = dataSnapshot.getValue(WildlifeEntity.class);
                      if (wildlifeEntity != null && id != null) {
                        wildlifeEntity.Id = id;
                        if (wildlifeEntity.isValid()) {
                          WildlifeDatabase.databaseWriteExecutor.execute(() ->
                            WildlifeDatabase.getInstance(getContext()).wildlifeDao().insert(wildlifeEntity));
                        } else {
                          Log.w(TAG, "Wildlife entity was invalid, not adding: " + wildlifeEntity.toString());
                        }
                      }

                      break;
                  }
                }

                switch (dataRoot) {
                  case Utils.ENCOUNTER_ROOT:
                    Utils.setEncountersStamp(getActivity(), updatedDataStamp);
                    mCallback.onDataEncountersPopulated();
                    break;
                  case Utils.TASK_ROOT:
                    Utils.setTasksStamp(getActivity(), updatedDataStamp);
                    mCallback.onDataTasksPopulated();
                    break;
                  case Utils.WILDLIFE_ROOT:
                    Utils.setWildlifeStamp(getActivity(), updatedDataStamp);
                    mCallback.onDataWildlifePopulated();
                    break;
                }
              } else {
                mCallback.onDataFailed("App was not ready for operation at this time.");
              }
            } else {
              mCallback.onDataMissing();
            }
          } else {
            mCallback.onDataFailed("Results not found for " + dataRoot);
          }
        }
      });
  }

  private void updateUI(String message) {

    if (mStatusText != null) {
      mStatusText.setText(message);
    }
  }
}
