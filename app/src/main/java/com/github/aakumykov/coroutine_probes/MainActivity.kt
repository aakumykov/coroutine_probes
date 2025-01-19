package com.github.aakumykov.coroutine_probes

import android.os.Bundle
import android.util.Log
import android.widget.Button
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

    private val name = "Корутина"
    private val ce = CancellationException("Самоотмена $name")
    private var externalJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.startButton).setOnClickListener {
            runCoroutine()
        }

        findViewById<Button>(R.id.stopButton).setOnClickListener {
            externalJob?.cancel(ce)
                ?: run { Log.w(TAG, "externalJob == null") }
        }
    }

    private fun runCoroutine() {

        val job = Job()
        val scope = CoroutineScope(job)

        externalJob = scope.launch {
            try {
                Log.d(TAG,"$name (старт)")
                delay(3000)

                cancel(ce)
                this.cancel(ce)
                this.coroutineContext.job.cancel(ce)
                job.cancel(ce)
                scope.cancel(ce)

                if (externalJob != null) {
                    Log.d(TAG, "externalJob != null, пробую отменить")
                    externalJob!!.cancel(ce)
                } else {
                    Log.w(TAG, "externalJob == null")
                }

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