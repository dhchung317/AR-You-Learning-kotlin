package com.hyunki.aryoulearning2.util.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.TextView

import java.util.Locale

import javax.inject.Inject

class PronunciationUtil @Inject
constructor(context: Context) : TextToSpeech.OnInitListener {
    val textToSpeech: TextToSpeech = TextToSpeech(context, this)

    fun textToSpeechAnnouncer(textView: TextView, textToSpeech: TextToSpeech) {
        val letter = textView.text.toString().lowercase()
        val speakText = textToSpeech.speak(pronounceSingleLetter(letter),
                TextToSpeech.QUEUE_ADD, null)
        if (speakText == TextToSpeech.ERROR) {
            Log.e("TTS", "Error in converting Text to Speech!")
        }
    }

    fun textToSpeechAnnouncer(message: String, textToSpeech: TextToSpeech) {
        val speakText = textToSpeech.speak(message,
                TextToSpeech.QUEUE_ADD, null)
        if (speakText == TextToSpeech.ERROR) {
            Log.e("TTS", "Error in converting Text to Speech!")
        }
    }

    private fun pronounceSingleLetter(letter: String): String {
        when (letter) {
            "a" -> return "ayee"
            "b" -> return "bee"
            "c" -> return "cee"
            "d" -> return "dee"
            "e" -> return "e"
            "f" -> return "ef"
            "g" -> return "gee"
            "h" -> return "aitch"
            "i" -> return "i"
            "j" -> return "jay"
            "k" -> return "kay"
            "l" -> return "el"
            "m" -> return "em"
            "n" -> return "en"
            "o" -> return "o"
            "p" -> return "pee"
            "q" -> return "cue"
            "r" -> return "ar"
            "s" -> return "ess"
            "t" -> return "tee"
            "u" -> return "u"
            "v" -> return "vee"
            "w" -> return "double-u"
            "x" -> return "ex"
            "y" -> return "wy"
            "z" -> return "zed"
        }
        return letter
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.language = Locale.US
        } else {
            Log.e("TextToSpeechManager", "Initilization Failed!")

        }
    }
}
