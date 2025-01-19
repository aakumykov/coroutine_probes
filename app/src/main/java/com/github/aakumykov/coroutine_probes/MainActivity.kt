package com.github.aakumykov.coroutine_probes

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val name = "Корутина"
        val scope = CoroutineScope(Dispatchers.IO)
        val ce = CancellationException("Самоотмена $name")

        this.job = scope.launch {
            try {
                Log.d(TAG,"$name (старт)")
                delay(100)

//                cancel(ce)
//                this.cancel(ce)
//                this.coroutineContext.job.cancel(ce)
//                scope.cancel(ce)

                this@MainActivity.job?.cancel(ce)
                    ?: run { Log.w(TAG, "this@MainActivity.job == null") }

                Log.d(TAG,"$name (финиш)")
            } catch (e: CancellationException) {
                Log.d(TAG,"${e.javaClass.simpleName}: ${e.message}")
            } finally {
                Log.d(TAG,"$name (финальный штрих)")
            }
        }
    }

    companion object {
        val TAG: String = "KOTLIN"
    }
}