package eu.kanade.aniyomi.reader.autoscroll

import eu.kanade.tachiyomi.ui.reader.viewer.pager.PagerViewer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Simple pager auto-scroll controller that advances pages on a ViewPager-based viewer.
 * Speed is an integer 1..50 (higher == faster). Mapping to delay is internal.
 */
class PagerAutoScrollController(
    private val viewer: PagerViewer,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main),
) {
    private var job: Job? = null
    private var speed: Int = 4

    private fun delayForSpeed(): Long {
        val s = speed.coerceIn(1, 50)
        // map speed to delay ms: faster speed -> smaller delay
        return (3000L * (51 - s) / 50L)
    }

    fun setSpeed(s: Int) {
        speed = s.coerceIn(1, 50)
    }

    fun start() {
        if (job?.isActive == true) return
        job = scope.launch {
            while (isActive) {
                val ms = delayForSpeed()
                delay(ms)
                try {
                    // move to next page on UI thread
                    val adapterCount = viewer.pager.adapter?.count ?: 0
                    val cur = viewer.pager.currentItem
                    if (cur < adapterCount - 1) {
                        viewer.pager.post { viewer.pager.setCurrentItem(cur + 1, true) }
                    } else {
                        // reached end, stop
                        stop()
                        viewer.activity.showMenu()
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    fun isRunning(): Boolean = job?.isActive == true
}
