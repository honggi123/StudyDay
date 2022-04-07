package com.coworkerteam.coworker.ui.main

import android.media.AudioFormat
import android.media.AudioFormat.CHANNEL_IN_MONO
import android.media.AudioFormat.CHANNEL_IN_STEREO
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Process
import android.util.Log
import com.konovalov.vad.Vad
import com.konovalov.vad.VadConfig
import com.konovalov.vad.VadListener



    class VoiceRecorder(private val callback: Listener, config: VadConfig?) {
        private val vad: Vad?
        private var audioRecord: AudioRecord? = null
        private var thread: Thread? = null
        private var isListening = false
        fun updateConfig(config: VadConfig?) {
            vad!!.config = config
        }

        fun start() {
            stop()
            audioRecord = createAudioRecord()
            if (audioRecord != null) {
                isListening = true
                audioRecord!!.startRecording()
                thread = Thread(ProcessVoice())
                thread!!.start()
                vad!!.start()
            } else {
                Log.w(TAG, "Failed start Voice Recorder!")
            }
        }

        fun stop() {
            isListening = false
            if (thread != null) {
                thread!!.interrupt()
                thread = null
            }
            if (audioRecord != null) {
                try {
                    audioRecord!!.release()
                } catch (e: Exception) {
                    Log.e(TAG, "Error stop AudioRecord ", e)
                }
                audioRecord = null
            }
            vad?.stop()
        }

        private fun createAudioRecord(): AudioRecord? {
            try {
                val minBufSize = AudioRecord.getMinBufferSize(
                    vad!!.config.sampleRate.value,
                    PCM_CHANNEL,
                    PCM_ENCODING_BIT
                )
                if (minBufSize == AudioRecord.ERROR_BAD_VALUE) {
                    return null
                }
                val audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    vad.config.sampleRate.value,
                    PCM_CHANNEL,
                    PCM_ENCODING_BIT,
                    minBufSize
                )
                if (audioRecord.state == AudioRecord.STATE_INITIALIZED) {
                    return audioRecord
                } else {
                    audioRecord.release()
                }
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Error can't create AudioRecord ", e)
            }
            return null
        }

        private val numberOfChannels: Int
            private get() {
                when (PCM_CHANNEL) {
                    CHANNEL_IN_MONO -> return 1
                    CHANNEL_IN_STEREO -> return 2
                }
                return 1
            }

        private inner class ProcessVoice : Runnable {
            override fun run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
                while (!Thread.interrupted() && isListening && audioRecord != null) {
                    val buffer = ShortArray(vad!!.config.frameSize.value * numberOfChannels * 2)
                    audioRecord!!.read(buffer, 0, buffer.size)
                    detectSpeech(buffer)
                }
            }

            private fun detectSpeech(buffer: ShortArray) {
                vad!!.addContinuousSpeechListener(buffer, object : VadListener {
                    override fun onSpeechDetected() {
                        callback.onSpeechDetected()
                    }

                    override fun onNoiseDetected() {
                        callback.onNoiseDetected()
                    }
                })
            }
        }

        interface Listener {
            fun onSpeechDetected()
            fun onNoiseDetected()
        }

        companion object {
            private const val PCM_CHANNEL = CHANNEL_IN_MONO
            private val PCM_ENCODING_BIT: Int = AudioFormat.ENCODING_PCM_16BIT
            private val TAG = VoiceRecorder::class.java.simpleName
        }

        init {
            vad = Vad(config)
        }
    }
