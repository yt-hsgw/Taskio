package com.yt_hsgw.taskio.model

data class TaskItem(
    val id: String,
    val title: String,
    val description: String?,
    val createdAt: String,
    val isStarted: Boolean = false,
    val isFinished: Boolean = false,
    val dueDate: String? = null,
    val scheduledDate: String? = null,
    val repeatDays: List<Int>? = null,
    val isRecurring: Boolean = false
)

fun TaskResponse.toTaskItem() = TaskItem(
    id = this.id,
    title = this.title,
    description = this.description,
    createdAt = this.created_at,
    dueDate = this.due_date,
    scheduledDate = this.scheduled_date,
    repeatDays = this.repeat_days,
    isRecurring = this.is_recurring
)