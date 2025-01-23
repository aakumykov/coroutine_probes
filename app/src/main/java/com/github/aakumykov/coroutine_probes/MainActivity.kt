package com.github.aakumykov.coroutine_probes

import android.app.TaskStackBuilder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import android.util.Log
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private val name = "Корутина"
    private val ce = CancellationException("Самоотмена $name")
    private var rootJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.startButton).setOnClickListener { startWork() }
        findViewById<Button>(R.id.cancelButton).setOnClickListener { cancelWork() }
        startWork()
    }

    private fun startWork() {
        log("")
        log("")
        log("========= work() ========")
        val rootScope = CoroutineScope(Dispatchers.IO)

        val list: List<Int> = listOf(1,2,3,4,5)

        rootJob = Job()
        val mediumJob = Job(rootJob)
        val lowestJob = SupervisorJob(mediumJob)

        rootScope.launch (rootJob!!) {
            try {

                log("-----> rootScope (начало)")

                launch (mediumJob) {
                    val childLocalScope = this

                    log("-> Перед обработкой списка")
                    delay(1000)

                    list.map {  i ->
                        launch (lowestJob) {
                            try {
                                log("Скачивание файла-$i")
                                delay(1000)
                            } catch (e: CancellationException) {
                                logW("Скачивание файла-$i отменено: ${e.message}")
                            }
                        }
                    }.joinAll()

                    log("-> После обработки списка")

                }.join()

                log("-----> rootScope (конец)")

            } catch (e: CancellationException) {
                logW("rootScope отменён: ${e.message}")
                throw e
            }
        }
    }

    private fun cancelWork() {
        rootJob?.cancel(CancellationException("Отменено пользователем"))
            ?: run { visibleError("нет externalJob") }
    }

    private fun visibleError(errorMsg: String) {
        logE(errorMsg)
        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
    }

    private fun log(text: String) = Log.d(TAG, text)
    private fun logW(text: String) = Log.w(TAG, text)
    private fun logE(text: String) = Log.e(TAG, text)

    companion object {
        val TAG: String = "KOTLIN"
    }
}
