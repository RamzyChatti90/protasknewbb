package com.carnival.protasknewbb.service;

import com.carnival.protasknewbb.domain.Task;
import com.carnival.protasknewbb.domain.enumeration.TaskStatus;
import com.carnival.protasknewbb.repository.TaskRepository;
import com.carnival.protasknewbb.security.SecurityUtils;
import com.carnival.protasknewbb.service.dto.TaskDashboardDTO;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Task}.
 */
@Service
@Transactional
public class TaskService {

    private final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Interface for task status count projection.
     * This is defined here to resolve compilation errors if `TaskRepository.TaskStatusCount` is not found,
     * and only `TaskService.java` can be modified.
     * In a typical project, this would be an inner interface of `TaskRepository` or a separate DTO interface
     * in the `com.carnival.protasknewbb.repository` or `com.carnival.protasknewbb.service.dto` package.
     */
    public interface TaskStatusCount {
        TaskStatus getStatus();
        Long getCount();
    }

    /**
     * Save a task.
     *
     * @param task the entity to save.
     * @return the persisted entity.
     */
    public Task save(Task task) {
        log.debug("Request to save Task : {}", task);
        return taskRepository.save(task);
    }

    /**
     * Update a task.
     *
     * @param task the entity to save.
     * @return the persisted entity.
     */
    public Task update(Task task) {
        log.debug("Request to update Task : {}", task);
        return taskRepository.save(task);
    }

    /**
     * Partially update a task.
     *
     * @param task the entity to update partially.
     * @return the persisted entity.
     */
    public java.util.Optional<Task> partialUpdate(Task task) {
        log.debug("Request to partially update Task : {}", task);

        return taskRepository
            .findById(task.getId())
            .map(existingTask -> {
                if (task.getTitle() != null) {
                    existingTask.setTitle(task.getTitle());
                }
                if (task.getDescription() != null) {
                    existingTask.setDescription(task.getDescription());
                }
                if (task.getDueDate() != null) {
                    existingTask.setDueDate(task.getDueDate());
                }
                if (task.getStatus() != null) {
                    existingTask.setStatus(task.getStatus());
                }

                return existingTask;
            })
            .map(taskRepository::save);
    }

    /**
     * Get all the tasks.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<Task> findAll() {
        log.debug("Request to get all Tasks");
        return taskRepository.findAll();
    }

    /**
     * Get one task by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public java.util.Optional<Task> findOne(Long id) {
        log.debug("Request to get Task : {}", id);
        return taskRepository.findById(id);
    }

    /**
     * Delete the task by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Task : {}", id);
        taskRepository.deleteById(id);
    }

    /**
     * Get tasks for the current user for the dashboard.
     *
     * @return the list of TaskDashboardDTO.
     */
    @Transactional(readOnly = true)
    public List<TaskDashboardDTO> getTasksForCurrentUser() {
        log.debug("Request to get tasks for current user");
        String userLogin = SecurityUtils
            .getCurrentUserLogin()
            .orElseThrow(() -> new IllegalStateException("Current user login not found"));

        return taskRepository
            .findAllByAssignedTo_Login(userLogin)
            .stream()
            .map(task ->
                new TaskDashboardDTO(
                    task.getId(),
                    task.getTitle(),
                    task.getStatus(),
                    task.getDueDate(),
                    task.getAssignedTo() != null ? task.getAssignedTo().getLogin() : null
                )
            )
            .collect(Collectors.toList());
    }

    /**
     * Get task status distribution for the current user.
     *
     * @return a map of TaskStatus to count.
     */
    @Transactional(readOnly = true)
    public Map<TaskStatus, Long> getTaskStatusDistributionForCurrentUser() {
        log.debug("Request to get task status distribution for current user");
        Long userId = SecurityUtils
            .getCurrentUserId()
            .orElseThrow(() -> new IllegalStateException("Current user ID not found"));

        return taskRepository
            .countTasksByStatusForUser(userId)
            .stream()
            .collect(Collectors.toMap(TaskService.TaskStatusCount::getStatus, TaskService.TaskStatusCount::getCount));
    }
}