package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.model.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

data class TtsPlaybackState(
    val isSpeaking: Boolean = false,
    val isInitialized: Boolean = false,
    val isUnavailable: Boolean = false
)

interface DiagnosisSpeaker {
    val state: StateFlow<TtsPlaybackState>
    fun speak(chunks: List<String>, language: AppLanguage)
    fun stop()
    fun shutdown()
}

class AndroidDiagnosisSpeaker(context: Context) : DiagnosisSpeaker {

    private val _state = MutableStateFlow(TtsPlaybackState())
    override val state: StateFlow<TtsPlaybackState> = _state.asStateFlow()

    private var tts: TextToSpeech? = null
    private var finalUtteranceId: String? = null
    private var pendingChunks: List<String>? = null
    private var pendingLanguage: AppLanguage? = null

    init {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.setSpeechRate(0.92f)
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _state.update { it.copy(isSpeaking = true) }
                        }

                        override fun onDone(utteranceId: String?) {
                            if (utteranceId == finalUtteranceId) {
                                _state.update { it.copy(isSpeaking = false) }
                            }
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            _state.update { it.copy(isSpeaking = false) }
                        }

                        override fun onError(utteranceId: String?, errorCode: Int) {
                            _state.update { it.copy(isSpeaking = false) }
                        }
                    })
                    _state.update { it.copy(isInitialized = true, isUnavailable = false) }
                    val queued = pendingChunks
                    val queuedLang = pendingLanguage
                    if (queued != null && queuedLang != null) {
                        pendingChunks = null
                        pendingLanguage = null
                        speak(queued, queuedLang)
                    }
                } else {
                    _state.update { it.copy(isInitialized = false, isUnavailable = true, isSpeaking = false) }
                }
            }
        } catch (_: Exception) {
            _state.update { it.copy(isInitialized = false, isUnavailable = true, isSpeaking = false) }
        }
    }

    override fun speak(chunks: List<String>, language: AppLanguage) {
        if (chunks.isEmpty()) return
        val engine = tts
        if (engine == null || _state.value.isUnavailable) {
            _state.update { it.copy(isUnavailable = true, isSpeaking = false) }
            return
        }
        if (!_state.value.isInitialized) {
            pendingChunks = chunks
            pendingLanguage = language
            _state.update { it.copy(isSpeaking = true) }
            return
        }

        val targetLocale = when (language) {
            AppLanguage.HINDI -> Locale("hi", "IN")
            AppLanguage.ENGLISH -> Locale("en", "IN")
        }
        val langSupport = engine.setLanguage(targetLocale)
        if (langSupport == TextToSpeech.LANG_MISSING_DATA || langSupport == TextToSpeech.LANG_NOT_SUPPORTED) {
            val fallbackLocale = if (language == AppLanguage.ENGLISH) Locale.US else Locale.getDefault()
            val fallbackSupport = engine.setLanguage(fallbackLocale)
            if (fallbackSupport == TextToSpeech.LANG_MISSING_DATA || fallbackSupport == TextToSpeech.LANG_NOT_SUPPORTED) {
                _state.update { it.copy(isUnavailable = true, isSpeaking = false) }
                return
            }
        }

        engine.stop()
        val sessionPrefix = "krishi_tts_${System.currentTimeMillis()}"
        finalUtteranceId = "${sessionPrefix}_${chunks.lastIndex}"
        _state.update { it.copy(isSpeaking = true, isUnavailable = false) }

        chunks.forEachIndexed { index, text ->
            val mode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            engine.speak(text, mode, null, "${sessionPrefix}_$index")
        }
    }

    override fun stop() {
        pendingChunks = null
        pendingLanguage = null
        runCatching { tts?.stop() }
        _state.update { it.copy(isSpeaking = false) }
    }

    override fun shutdown() {
        stop()
        runCatching { tts?.shutdown() }
        tts = null
    }
}
