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

        runCoroutine()
    }

    private fun runCoroutine() {
        Log.d(TAG, "------------- runCoroutine() -------------")

        val job = Job()
        val scope = CoroutineScope(job)

        externalJob = scope.launch {
            try {
                Log.d(TAG,"$name (старт)")
                delay(1000)

                cancel(ce)
                this.cancel(ce)
                this.coroutineContext.job.cancel(ce)
                job.cancel(ce)
                scope.cancel(ce)

                if (externalJob != null) {
                    Log.i(TAG, "externalJob: ${externalJob}")
                    Log.i(TAG, "       this: ${this}")
                    Log.d(TAG, "externalJob != null, пробую отменить")
                    externalJob!!.cancel(ce)
                } else {
                    Log.w(TAG, "externalJob == null")
                }

                throw ce

                Log.d(TAG,"$name (финиш)")

            } catch (e: CancellationException) {
                Log.w(TAG,"${e.javaClass.simpleName}: ${e.message}")
                // [Документация](https://kotlinlang.org/docs/cancellation-and-timeouts.html#cancellation-is-cooperative)
                //  говорит, что для нормального фнкционирования иерархии корутин пойманное CancellationException
                //  должно быть проброшено дальше.
                //  Но и это не помогает отменить корутину изнутри.
                throw e

            } finally {
                Log.d(TAG,"$name (финальный штрих)")
            }
        }
    }

    companion object {
        val TAG: String = "KOTLIN"
    }
}