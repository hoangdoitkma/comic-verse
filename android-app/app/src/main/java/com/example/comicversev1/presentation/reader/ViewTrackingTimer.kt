package com.example.comicversev1.presentation.reader

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Kotlin-based Timer using Coroutines to track view duration (anti-spam)
 */
class ViewTrackingTimer {
    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    /**
     * Listener interface to be implemented by Java code
     */
    interface OnTimerCompletedListener {
        fun onTimerCompleted(comicId: Int, chapterId: Int)
    }

    /**
     * Start the 5-second timer for a specific chapter.
     * If a timer is already running, it will be cancelled.
     */
    fun startTimer(comicId: Int, chapterId: Int, listener: OnTimerCompletedListener?) {
        // Cancel any existing timer to avoid multiple concurrent timers
        cancelTimer()

        Log.d("ViewTrackingTimer", ">>> Starting 5s timer for chapter: $chapterId")
        timerJob = scope.launch {
            delay(5000L) // Wait for 5 seconds
            
            // If the coroutine is not cancelled after 5s, it means the user stayed on the chapter
            Log.d("ViewTrackingTimer", ">>> Timer completed for chapter: $chapterId")
            listener?.onTimerCompleted(comicId, chapterId)
            
            // Reset job
            timerJob = null
        }
    }

    /**
     * Cancel the current timer. Call this when scrolling to another chapter or exiting the reader.
     */
    fun cancelTimer() {
        if (timerJob?.isActive == true) {
            Log.d("ViewTrackingTimer", ">>> Timer cancelled")
            timerJob?.cancel()
        }
        timerJob = null
    }
}
