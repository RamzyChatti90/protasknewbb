package com.carnival.protasknewbb.repository;

import com.carnival.protasknewbb.domain.Task;
import com.carnival.protasknewbb.domain.enumeration.TaskStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Task entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByAssignedTo_Login(String login);

    // Interface projection for task status distribution
    interface TaskStatusCount {
        TaskStatus getStatus();
        Long getCount();
    }

    @Query("SELECT t.status as status, COUNT(t) as count FROM Task t WHERE t.assignedTo.id = :userId GROUP BY t.status")
    List<TaskStatusCount> countTasksByStatusForUser(@Param("userId") Long userId);
}
