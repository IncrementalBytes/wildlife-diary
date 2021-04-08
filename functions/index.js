const functions = require("firebase-functions");

const admin = require("firebase-admin");
admin.initializeApp(functions.config().firebase);

exports.pushNotification = functions.database.ref("/Encounters/{pushId}")
    .onWrite((change, context) => {
      console.log(
          "Push notification event triggered for ",
          context.params.pushId);

      const payload = {
        notification: {
          title: "Wildlife Diary",
          body: "New Encounter Added!",
          sound: "default",
        },
      };

      const options = {
        priority: "high",
        timeToLive: 60 * 60 * 24,
      };

      return admin.messaging().sendToTopic(
          "wildlifeNotification",
          payload,
          options);
    });
