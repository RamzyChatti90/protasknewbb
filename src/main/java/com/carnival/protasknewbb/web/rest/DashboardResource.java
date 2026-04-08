package com.carnival.protasknewbb.web.rest;

import com.carnival.protasknewbb.domain.enumeration.TaskStatus;
import com.carnival.protasknewbb.service.TaskService;
import com.carnival.protasknewbb.service.dto.TaskDashboardDTO;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing the dashboard data.
 */
@RestController
@RequestMapping("/api")
public class DashboardResource {

    private final Logger log = LoggerFactory.getLogger(DashboardResource.class);

    private final TaskService taskService;

    public DashboardResource(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * {@code GET /dashboard/tasks} : get all tasks for the current user.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of tasks in body.
     */
    @GetMapping("/dashboard/tasks")
    public ResponseEntity<List<TaskDashboardDTO>> getTasksForCurrentUser() {
        log.debug("REST request to get tasks for current user for dashboard");
        List<TaskDashboardDTO> tasks = taskService.getTasksForCurrentUser();
        return ResponseEntity.ok().body(tasks);
    }

    /**
     * {@code GET /dashboard/task-status-distribution} : get the distribution of tasks by status for the current user.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the map of status distribution in body.
     */
    @GetMapping("/dashboard/task-status-distribution")
    public ResponseEntity<Map<TaskStatus, Long>> getTaskStatusDistributionForCurrentUser() {
        log.debug("REST request to get task status distribution for current user for dashboard");
        Map<TaskStatus, Long> distribution = taskService.getTaskStatusDistributionForCurrentUser();
        return ResponseEntity.ok().body(distribution);
    }
}
