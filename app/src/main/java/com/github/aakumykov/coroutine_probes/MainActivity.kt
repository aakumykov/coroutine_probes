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

class MainActivity : AppCompatActivity() {

    private val name = "Корутина"
    private val ce = CancellationException("Самоотмена $name")
    private var externalJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rootScope = CoroutineScope(Dispatchers.IO)

        val list = buildList<String> { repeat(8) { i -> add("Файл-${i + 1}") } }

        rootScope.launch {
            log("-----> rootScope (начало)")

            val rootLocalScope = this

            // Перебор кусков (начало)
            var chunkNum = 1
            list.chunked(3).forEach { chunk ->

                launch { // Скачивание одного куска
                    val chunkLocalScope = this

                    log("-> Скачивание куска-$chunkNum")
                    chunk.map { fileName ->

                        // Скачивание одного файла (старт)
                        launch (SupervisorJob(rootLocalScope.coroutineContext.job)) {
                            log("Скачивание $fileName")
                            delay(1000)
                        } // Скачивание одного файла (финиш)

                    }.joinAll() // Ожидание скачивания файлов из куска

                }.join() // ожидание скачивания куска

                chunkNum++
            } // Перебор кусков (конец)

            log("-----> rootScope (конец)")
        } // rootJob
    }

    private fun log(text: String) = Log.d(TAG, text)

    companion object {
        val TAG: String = "KOTLIN"
    }
}