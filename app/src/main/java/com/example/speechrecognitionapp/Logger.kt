package com.example.speechrecognitionapp

import org.json.JSONException
import org.json.JSONObject
import android.content.Context
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import java.io.*

class Logger(private val context: Context) {

    data class PredictionSession(
        val predictionsCount: Int,
        val keywords: List<String>,
        val time: Long
    )

    private val database = FirebaseDatabase.getInstance("https://pecd2024-mp-2-default-rtdb.europe-west1.firebasedatabase.app/").reference
    private val logFile = File(context.filesDir, "predictions_log.txt")


    // Variable for the periodic job
    private var uploadJob: Job? = null

    // Function to log predictions to the file
    fun logToFile(cont: Int, keywords: ArrayList<String>) {
        // BufferedWriter to write data to the log file
        val predictionSession = PredictionSession(cont, keywords, System.currentTimeMillis())
        val logData = JSONObject().apply {
            put("predictionsCount", predictionSession.predictionsCount)
            put("keywords", predictionSession.keywords)
            put("time", predictionSession.time)
        }.toString() + "\n"

        try {
            val bufferedWriter: BufferedWriter = BufferedWriter(OutputStreamWriter(FileOutputStream(logFile, true)))
            bufferedWriter.write(logData)
            bufferedWriter.flush()
            bufferedWriter.close()
            Log.d("Logger", "Prediction logged to file.")
        } catch (e: Exception) {
            Log.e("Logger", "Error writing to file: ${e.message}")
        }
    }

    // Function to upload the log file content to Firebase Realtime Database
    private fun uploadLogToFirebase() {
        if (logFile.exists()) {
            try {
                val reader = BufferedReader(FileReader(logFile))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (!line.isNullOrEmpty()) {
                        try {
                            val logEntry = JSONObject(line)
                            database.child("prediction_logs").push().setValue(logEntry.toString())
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.d("Logger", "Log entry uploaded to Firebase.")
                                    } else {
                                        Log.e("Logger", "Failed to upload log entry to Firebase: ${task.exception?.message}")
                                    }
                                }
                        } catch (e: JSONException) {
                            Log.e("Logger", "Error parsing JSON: ${e.message}")
                        }
                    }
                }
                reader.close()
                clearLogFile()
            } catch (e: Exception) {
                Log.e("Logger", "Error reading log file: ${e.message}")
            }
        }
    }

    // Function to clear the log file after successful upload
    private fun clearLogFile() {
        try {
            if (logFile.exists()) {
                FileOutputStream(logFile).use {
                    it.write("".toByteArray())
                }
                Log.d("Logger", "Log file cleared.")
            }
        } catch (e: Exception) {
            Log.e("Logger", "Error clearing log file: ${e.message}")
        }
    }

    // Function to start the periodic upload of the logs to Firebase
    fun startPeriodicUpload(intervalMillis: Long = 60000L) {  // Default interval: 1 minute
        uploadJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {  // Ensures coroutine stops when canceled
                delay(intervalMillis)
                uploadLogToFirebase()
            }
        }
    }

    // Function to stop the periodic upload
    fun stopPeriodicUpload() {
        uploadJob?.cancel()
        Log.d("Logger", "Periodic upload stopped.")
    }

}
