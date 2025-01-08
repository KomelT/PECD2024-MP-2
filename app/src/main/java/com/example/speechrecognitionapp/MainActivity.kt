package com.example.speechrecognitionapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.speechrecognitionapp.databinding.ActivityMainBinding
import android.util.Log


class MainActivity : AppCompatActivity()/*, RecordingCallback*/ {
    private lateinit var binding: ActivityMainBinding

    private var mSpeechRecognizer: SpeechRecognizer? = null
    private var mIsListening = false // this will be needed later
    private var mUserInfoText: TextView? = null
    private var mUserUtteranceOutput: TextView? = null

    private var mCommandsList: MutableList<String>? = null

    companion object {
        // This constant is needed to verify the audio permission result
        private const val ASR_PERMISSION_REQUEST_CODE = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        verifyAudioPermissions()

        mUserUtteranceOutput = findViewById(R.id.user_utterance_output)
        mUserInfoText = findViewById(R.id.user_info_text)

        createSpeechRecognizer()

        initCommands()

        val trigger = findViewById<Button>(R.id.btnRecord)

        trigger.setOnClickListener {
            // Handle audio sessions here
            if (mIsListening) {
                handleSpeechEnd()
            } else {
                handleSpeechBegin()
            }
        }
    }

    private fun initCommands() {
        mCommandsList = ArrayList()
        mCommandsList!!.add("Cart")
        mCommandsList!!.add("Cancel")
        mCommandsList!!.add("Home")
        mCommandsList!!.add("Search")
    }

    private fun handleSpeechBegin() {
        // start audio session
        mUserInfoText!!.setText(R.string.listening)
        mIsListening = true
        mSpeechRecognizer!!.startListening(createIntent())
    }

    private fun handleSpeechEnd() {
        // end audio session
        mUserInfoText!!.setText(R.string.detected_speech)
        mIsListening = false
        mSpeechRecognizer!!.cancel()
    }

    private fun createIntent(): Intent {
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")

        return i
    }

    private fun createSpeechRecognizer() {
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        mSpeechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle) {}
            override fun onBeginningOfSpeech() {
                Log.d("IDK", "Talking started")
            }
            override fun onRmsChanged(rmsdB: Float) {
                //Log.d("IDK", "${rmsdB}")
            }
            override fun onBufferReceived(buffer: ByteArray) {}

            override fun onEndOfSpeech() {
                //handleSpeechEnd()
            }

            override fun onError(error: Int) {
                handleSpeechEnd()
            }

            override fun onResults(results: Bundle) {
                // Called when recognition results are ready. This callback will be called when the
                // audio session has been completed and user utterance has been parsed.

                // This ArrayList contains the recognition results, if the list is non-empty,
                // handle the user utterance
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.size > 0) {
                    // The results are added in decreasing order of confidence to the list
                    val command = matches[0]
                    mUserUtteranceOutput!!.text = command
                    handleCommand(command)
                }

                handleSpeechBegin()
            }

            override fun onPartialResults(partialResults: Bundle) {
                // Called when partial recognition results are available, this callback will be
                // called each time a partial text result is ready while the user is speaking.
                val matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.size > 0) {
                    // handle partial speech results
                    val partialText = matches[0]
                    mUserUtteranceOutput!!.text = partialText
                }
            }

            override fun onEvent(eventType: Int, params: Bundle) {}
        })
    }

    private fun handleCommand(command: String) {
        if (mCommandsList!!.contains(command)) {
            // Successful utterance, notify user
            Toast.makeText(this, "Executing: $command", Toast.LENGTH_LONG).show()
        } else {
            // Unsucessful utterance, show failure message on screen
            Toast.makeText(this, "Could not recognize command", Toast.LENGTH_LONG).show()
        }
    }

    private fun verifyAudioPermissions() {
        if (checkCallingOrSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                ASR_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == ASR_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // audio permission granted
                Toast.makeText(this, "You can now use voice commands!", Toast.LENGTH_LONG).show()
            } else {
                // audio permission denied
                Toast.makeText(this, "Please provide microphone permission to use voice.", Toast.LENGTH_LONG).show()
            }
        }
    }
}