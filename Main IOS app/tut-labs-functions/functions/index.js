const functions = require("firebase-functions");
const {admin} = require("firebase-admin");

admin.initializeApp();

exports.createTutor = functions.https.onCall(async (data, context) => {
  const {name, email} = data;

  try {
    // 1. Create the Firebase Auth user
    const userRecord = await admin.auth().createUser({
      email,
      emailVerified: false,
      password: Math.random().toString(36).slice(-8), // temp random password
      displayName: name,
    });

    // 2. Set custom role in Firestore
    await admin.firestore().collection("users").doc(userRecord.uid).set({
      name,
      email,
      role: "tutor",
    });

    // 3. Send password reset email
    await admin.auth().generatePasswordResetLink(email);

    return {success: true, message: "Tutor created and email sent."};
  } catch (error) {
    console.error("Error creating tutor:", error);
    throw new functions.https.HttpsError("internal", error.message);
  }
});
