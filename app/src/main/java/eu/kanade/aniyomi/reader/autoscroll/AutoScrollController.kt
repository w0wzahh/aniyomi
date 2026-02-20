package eu.kanade.aniyomi.reader.autoscroll

import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Lightweight auto-scroll controller for a RecyclerView reader.
 * Usage:
 *  val controller = AutoScrollController(recyclerView)
 *  controller.setSpeedPxPerFrame(4) // adjust
 *  controller.start()
 *  controller.stop()
 */
class AutoScrollController(
    private val recycler: RecyclerView,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private var job: Job? = null
    private var speedPxPerFrame = 4
    private val frameDelayMs = 16L

    fun setSpeedPxPerFrame(px: Int) {
        speedPxPerFrame = px
    }

    fun start() {
        if (job?.isActive == true) return
        job = scope.launch {
            val uiHandler = Handler(Looper.getMainLooper())
            while (isActive) {
                try {
                    // Use smoothScrollBy for smoother behaviour if available
                    uiHandler.post {
                        recycler.scrollBy(0, speedPxPerFrame)
                    }
                } catch (_: Exception) {
                }
                delay(frameDelayMs)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    fun isRunning(): Boolean = job?.isActive == true
}
