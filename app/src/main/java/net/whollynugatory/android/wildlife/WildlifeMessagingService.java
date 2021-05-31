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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import net.whollynugatory.android.wildlife.ui.MainActivity;

public class WildlifeMessagingService extends FirebaseMessagingService {

  private static final String TAG = Utils.BASE_TAG + WildlifeMessagingService.class.getSimpleName();

  @Override
  public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

    Log.d(TAG, "++onMessageReceived(RemoteMessage)");
    String notificationTitle = null, notificationBody = null;
    Log.d(TAG, "From: " + remoteMessage.getFrom());
    if (remoteMessage.getNotification() != null) {
      Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
      notificationTitle = remoteMessage.getNotification().getTitle();
      notificationBody = remoteMessage.getNotification().getBody();
    }

    sendLocalNotification(notificationTitle, notificationBody);
  }

  @Override
  public void onNewToken(@NonNull String token) {
    super.onNewToken(token);

    sendRegistrationToServer(token);
  }

  private void sendLocalNotification(String notificationTitle, String notificationBody) {

    Log.d(TAG, "++sendLocalNotification(String, String)");
    Intent intent = new Intent(this, MainActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

    Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    String notificationId = getString(R.string.default_notification_channel_id);
    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, notificationId)
      .setAutoCancel(true)
      .setSmallIcon(R.drawable.ic_notification_dark)
      .setContentIntent(pendingIntent)
      .setContentTitle(notificationTitle)
      .setContentText(notificationBody)
      .setSound(defaultSoundUri);

    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.notify(1234, notificationBuilder.build());
  }

  private void sendRegistrationToServer(String token) {

    Log.d(TAG, "++sendRegistrationToServer(String)");
    // TODO: update firebase
  }
}
