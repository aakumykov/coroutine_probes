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

        val list: List<Int> = listOf(1,2,3,4,5)

        rootJob = rootScope.launch {
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
                                    log("  $name")
                                    delay(1000)
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

    companion object {
        val TAG: String = "KOTLIN"
    }
}
