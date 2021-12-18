package com.maliotis.speechrecognition

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import android.media.AudioManager





class MainActivity : AppCompatActivity() {

    val TAG = MainActivity::class.java.simpleName

    lateinit var speechButton: ImageView
    lateinit var textFromSpeech: EditText
    lateinit var speechRecognizer: SpeechRecognizer
    val speechIntent = getSpeechRecognitionIntent()
    var listening = false

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        speechButton = findViewById(R.id.button)
        textFromSpeech = findViewById(R.id.text)

        speechButton.setOnClickListener {
            listening = !listening
            if (listening) {
                speechRecognizer.startListening(speechIntent)
                speechButton.imageTintList = ColorStateList.valueOf(Color.RED)
                muteAudio()

            }
            else {
                shouldStopListening()
                speechButton.imageTintList = null
                unmuteAudio()
            }
        }

        checkPermission()
        setSpeechRecognizer()

    }

    private fun setSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object: RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) {
                Log.d(TAG, "onReadyForSpeech: ")
            }
            override fun onBeginningOfSpeech() {
                Log.d(TAG, "onBeginningOfSpeech: ")
                textFromSpeech.hint = "Listening..."
            }

            override fun onRmsChanged(p0: Float) {
                Log.d(TAG, "onRmsChanged: ")
            }

            override fun onBufferReceived(p0: ByteArray?) {
                Log.d(TAG, "onBufferReceived: ")
            }

            override fun onEndOfSpeech() {
                Log.d(TAG, "onEndOfSpeech: ")
                shouldStopListening()
            }

            override fun onError(p0: Int) {}

            override fun onResults(p0: Bundle?) {
                if (!listening) return
                val data: ArrayList<String> =
                    p0?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) as ArrayList<String>
                val currentText = textFromSpeech.text.toString()
                textFromSpeech.setText(currentText + " " + data[0])
            }

            override fun onPartialResults(p0: Bundle?) {
                Log.d(TAG, "onPartialResults: ")
            }

            override fun onEvent(p0: Int, p1: Bundle?) {
                Log.d(TAG, "onEvent: ")
            }

        })
    }

    fun shouldStopListening() {
        if (!listening) return speechRecognizer.cancel()

        speechRecognizer.stopListening()
        Handler().postDelayed({
            runOnUiThread {
                speechRecognizer.startListening(speechIntent)
            }
        }, 100)
    }


    private fun checkPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO),12)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 12 && grantResults.isNotEmpty()){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
        }
    }

    fun getSpeechRecognitionIntent(): Intent {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        return intent
    }

    fun muteAudio() {
        val amanager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        amanager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0)

    }

    fun unmuteAudio() {
        val amanager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        amanager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, 0)
    }
}