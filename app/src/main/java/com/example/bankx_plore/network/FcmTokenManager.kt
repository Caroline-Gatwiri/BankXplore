package com.example.bankx_plore.network//package com.example.bankx_plore.network
//
//import com.google.firebase.messaging.FirebaseMessaging
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//object FcmTokenManager {
//
//    // Retrieve FCM Token and send it to the backend
//    fun getFcmTokenAndSendToBackend(userId: String, backendApi: BackendApi) {
//        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                val fcmToken = task.result
//                println("FCM Token: $fcmToken") // Debugging purpose
//
//                // Send the token to the backend
//                sendTokenToBackend(userId, fcmToken, backendApi)
//            } else {
//                println("Error retrieving FCM token: ${task.exception?.message}")
//            }
//        }
//    }
//
//    // Send the token to the backend
//    private fun sendTokenToBackend(userId: String, fcmToken: String?, backendApi: BackendApi) {
//        if (fcmToken.isNullOrEmpty()) {
//            println("FCM token is null or empty")
//            return
//        }
//
//        // Send the token to the backend using Retrofit
//        backendApi.registerFcmToken(userId, fcmToken).enqueue(object : Callback<Void> {
//            override fun onResponse(call: Call<Void>, response: Response<Void>) {
//                if (response.isSuccessful) {
//                    println("FCM token sent to backend successfully")
//                } else {
//                    println("Failed to send FCM token to backend: ${response.errorBody()}")
//                }
//            }
//
//            override fun onFailure(call: Call<Void>, t: Throwable) {
//                println("Error sending FCM token to backend: ${t.message}")
//            }
//        })
//    }
//}
