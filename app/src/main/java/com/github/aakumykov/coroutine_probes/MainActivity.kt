package com.github.aakumykov.coroutine_probes

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var externalJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val name = "Корутина"
        val job = Job()
        val scope = CoroutineScope(job)
        val ce = CancellationException("Самоотмена $name")

        externalJob = scope.launch {
            try {
                Log.d(TAG,"$name (старт)")
                delay(100)

                cancel(ce)
                this.cancel(ce)
                this.coroutineContext.job.cancel(ce)
                job.cancel(ce)
                scope.cancel(ce)

                externalJob?.cancel(ce)
                    ?: run { Log.w(TAG, "externalJob == null") }

                Log.d(TAG,"$name (финиш)")
            } catch (e: CancellationException) {
                Log.w(TAG,"${e.javaClass.simpleName}: ${e.message}")
            } finally {
                Log.d(TAG,"$name (финальный штрих)")
            }
        }
    }

    companion object {
        val TAG: String = "KOTLIN"
    }
}