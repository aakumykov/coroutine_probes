package com.github.aakumykov.coroutine_probes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import android.util.Log
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private val name = "Корутина"
    private val ce = CancellationException("Самоотмена $name")
    private var externalJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.startButton).setOnClickListener { work() }
        work()
    }

    private fun work() {
        log("")
        log("")
        log("========= work() ========")
        val rootScope = CoroutineScope(Dispatchers.IO)

        val list: List<Int> = listOf(1,2,3,4,5)

        rootScope.launch {
            val rootLocalScope = this
            log("-----> rootScope (начало)")

            launch {
                val childLocalScope = this

                list.map {  i ->
                    launch (SupervisorJob(childLocalScope.coroutineContext.job)) {
                        log("Скачивание файла-$i")
                        delay(1000)
                    }
                }.joinAll()
            }.join()

            log("-----> rootScope (конец)")
        }
    }

    private fun log(text: String) = Log.d(TAG, text)

    companion object {
        val TAG: String = "KOTLIN"
    }
}
