package com.gomaa.tasks.exceptions

class TaskNotFoundException : RuntimeException() {
    override val message: String get() = "Task not found"
}