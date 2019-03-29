package com.bitcoin.slpwallet.presentation

enum class TaskStatus {
    IDLE,
    UNDERWAY,
    SUCCESS,
    ERROR
}

data class ProgressTask<ResultType>(var status: TaskStatus, var result: ResultType? = null, var message: String? = null) {

    companion object {
        fun <ResultType> idle(): ProgressTask<ResultType> {
            return ProgressTask(TaskStatus.IDLE)
        }

        fun <ResultType> underway(result: ResultType): ProgressTask<ResultType> {
            return ProgressTask(TaskStatus.UNDERWAY, result)
        }

        fun <ResultType> success(result: ResultType): ProgressTask<ResultType> {
            return ProgressTask(TaskStatus.SUCCESS, result)
        }

        fun <ResultType> error(message: String): ProgressTask<ResultType> {
            return ProgressTask(TaskStatus.ERROR, message = message)
        }
    }

}