import android.app.Application
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner

object ThreadMonitor {

    private const val TAG = "ThreadMonitor"

    // 常见系统线程前缀（会被过滤）
    private val systemThreadPrefixes = listOf(
        "main", "Finalizer", "ReferenceQueueDaemon", "HeapTaskDaemon",
        "Binder:", "RenderThread", "JDWP", "Signal Catcher", "AsyncTask",
        "arch_disk_io_", "Okio Watchdog"
    )

    fun init(application: Application) {
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_STOP -> { // App 进入后台
                        Log.d(TAG, "App 进入后台，开始检测线程")
                        checkRunningThreads()
                    }
                    else -> {}
                }
            }
        )
    }

    fun checkRunningThreads() {
        val allThreads = Thread.getAllStackTraces().keys
        val runningThreads = allThreads.filter { it.isAlive && !it.isDaemon }
            .filter { thread -> systemThreadPrefixes.none { thread.name.startsWith(it) } }

        Log.d(TAG, "后台运行的业务线程数: ${runningThreads.size}")
        runningThreads.forEach { thread ->
            Log.d(TAG, "Thread: ${thread.name}, State: ${thread.state}")
        }
    }
}
