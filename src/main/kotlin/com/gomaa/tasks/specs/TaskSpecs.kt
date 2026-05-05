package com.gomaa.tasks.specs

import com.gomaa.tasks.model.Task
import com.gomaa.tasks.model.User
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDate
import java.time.LocalDateTime

object TaskSpecs {

    fun statusEquals(status: String): Specification<Task> {
        return Specification { root, _, cb ->
            cb.equal(root.get<String>("status"), status.uppercase())
        }
    }

    fun dueDateAfter(date: LocalDate): Specification<Task> {
        return Specification { root, _, cb ->
            cb.greaterThan(root.get("dueDate"), date)
        }
    }

    fun dueDateBefore(date: LocalDate): Specification<Task> {
        return Specification { root, _, cb ->
            cb.lessThan(root.get("dueDate"), date)
        }
    }

    fun assigneeEquals(assignee: String): Specification<Task> {
        return Specification { root, _, cb ->
            cb.equal(root.get<String>("assignee"), assignee)
        }
    }

    fun hasTagsContaining(tag: String): Specification<Task> {
        return Specification { root, _, cb ->
            cb.isTrue(root.join<Task, List<String>>("tags").`in`(listOf(tag)))
        }
    }

    fun withFilters(
        afterId: LocalDateTime?, assignee: String?, dueDateFrom: LocalDate?, dueDateTo: LocalDate?
    ): Specification<Task> {
        return Specification.where<Task> { root, _, cb ->
            val predicates = mutableListOf<Predicate>()

            afterId?.let {
                predicates.add(cb.greaterThan(root.get("createdAt"), it))
            }
            assignee?.let { username ->
                predicates.add(cb.equal(root.get<User>("user").get<String>("username"), username))
            }
            dueDateFrom?.let {
                predicates.add(cb.greaterThan(root.get("dueDate"), it))
            }
            dueDateTo?.let {
                predicates.add(cb.lessThan(root.get("dueDate"), it))
            }

            cb.and(*predicates.toTypedArray())
        }
    }
}