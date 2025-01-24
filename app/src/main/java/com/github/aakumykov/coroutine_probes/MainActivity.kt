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
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

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
        log("========= work() clicked ========")
        log("")

        val rootScope = CoroutineScope(Dispatchers.IO)

        val rootEH = CoroutineExceptionHandler { coroutineContext, throwable ->
            logE("ERROR in rootScope: ${throwable.message}")
        }

        val list: List<Int> = listOf(1,2,3,4,5)

        rootJob = rootScope.launch (rootEH) {
            try {

                val rootLocalScope = this
                log("-----> rootScope (start)")

                launch {
                    try {

                        val childLocalScope = this

//                val childJob = Job(childLocalScope.coroutineContext.job)
                        val childJob = SupervisorJob(childLocalScope.coroutineContext.job)

                        list.map {  i ->
                            launch (childJob) {
                                val name = "processing file-$i"
                                try {
                                    if (random.nextBoolean()) {
                                        log("  $name")
                                        delay(1000)
                                    } else {
                                        throw Exception("Error in $name")
                                    }
                                } catch (e: CancellationException) {
                                    logW("$name cancelled: ${e.message}")
                                    throw e
                                }
                            }
                        }.joinAll()

                        debugJobStates(childJob, "before complete()")
                        childJob.complete()
                        debugJobStates(childJob, "after complete()")

                    } catch (e: CancellationException) {
                        logW("Middle Job cancelled: ${e.message}")
                        throw e
                    }

                }.join()

                log("-----> rootScope (finish)")

            } catch (e: CancellationException) {
                logW("rootJob cancelled: ${e.message}")
            }
        }
    }


    private fun cancelWork() {
        rootJob?.cancel(CancellationException("Отменено пользователем"))
    }


    private fun debugJobStates(job: Job, comment: String) {
        logI("Job ($comment):")
        logI("    * isActive: ${job.isActive}")
        logI("    * isCompleted: ${job.isCompleted}")
        logI("    * isCancelled: ${job.isCancelled}")
    }

    private fun log(text: String) = Log.d(TAG, text)
    private fun logI(text: String) = Log.i(TAG, text)
    private fun logW(text: String) = Log.w(TAG, text)
    private fun logE(text: String) = Log.e(TAG, text)

    companion object {
        val TAG: String = "KOTLIN"
    }

    private val random: Random = Random
}
