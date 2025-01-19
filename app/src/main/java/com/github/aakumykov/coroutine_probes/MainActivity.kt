package com.github.aakumykov.coroutine_probes

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
        val ee = CancellationException("Внешняя отмена $name")

        externalJob = scope.launch {
            try {
                Log.d(TAG,"$name (старт)")
                delay(1000)

                cancel(ce)
                this.cancel(ce)
                this.coroutineContext.job.cancel(ce)
                job.cancel(ce)
                scope.cancel(ce)

                externalJob?.cancel(ce)
                    ?: run { Log.w(TAG, "externalJob is null") }

                Log.d(TAG,"$name (финиш)")
            } catch (e: CancellationException) {
                Log.w(TAG,"${e.javaClass.simpleName}: ${e.message}")
            } finally {
                Log.d(TAG,"$name (финальный штрих)")
            }
        }

        lifecycleScope.launch (Dispatchers.IO) {
            delay(100)
            externalJob!!.cancel(ee)
        }
    }

    companion object {
        val TAG: String = "KOTLIN"
    }
}