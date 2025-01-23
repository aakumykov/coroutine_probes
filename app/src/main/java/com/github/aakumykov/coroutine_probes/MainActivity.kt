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
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val name = "Корутина"
    private val ce = CancellationException("Самоотмена $name")
    private var rootJob: Job? = null
    private val random: Random get() = Random

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

        val rootEH = CoroutineExceptionHandler { coroutineContext, throwable ->
            logE("Ошибка в корневой корутине: ${throwable.message}")
        }

        val mediumEH = CoroutineExceptionHandler { coroutineContext, throwable ->
            logE("Ошибка в серединной корутине: ${throwable.message}")
        }

        val lowEH = CoroutineExceptionHandler { coroutineContext, throwable ->
            logE("Ошибка в низкоуровневой корутине: ${throwable.message}")
        }

        val list: List<Int> = buildList { repeat(8) { i-> add(i) } }

        rootJob = Job()
        val mediumJob = Job(rootJob)
        val lowestJob = SupervisorJob(mediumJob)

        rootScope.launch (rootJob!! + rootEH) {
            try {
                log("-----> rootScope (начало)")

                var chunkNum = 1
                var chunkSize = -1

                list.chunked(3).forEach { chunk ->
                    chunkSize = chunk.size
                    log("-> Обработка куска-$chunkNum ($chunkSize)")
//                    delay(500)

                    launch (mediumJob + mediumEH) {
                        try {
//                            if (random.nextInt(1,101) > 90) throw Exception("Ошибка куска $chunkNum")

                            chunk.map {  i ->
                                launch (lowestJob + lowEH) {
                                    try {
//                                        if (random.nextBoolean()) {
                                            log("Скачивание файла-$i")
//                                            delay(1000)
//                                        } else {
//                                            throw Exception("Ошибка скачивания файла-$i")
//                                        }
                                    } catch (e: CancellationException) {
                                        logW("Скачивание файла-$i отменено: ${e.message}")
                                        throw e
                                    }
                                }
                            }.joinAll()

                        } catch (e: CancellationException) {
                            logW("Обработка куска-$chunkNum ($chunkSize) отменена: ${e.message}")
                            throw e
                        }
                    }.join()

                    chunkNum++
                }

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
