package com.carnival.protasknewbb.service.dto;

import com.carnival.protasknewbb.domain.enumeration.TaskStatus;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.carnival.protasknewbb.domain.Task} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TaskDashboardDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(min = 1, max = 255)
    private String title;

    @NotNull
    private TaskStatus status;

    private Instant dueDate;

    private String assignedToLogin;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Instant getDueDate() {
        return dueDate;
    }

    public void setDueDate(Instant dueDate) {
        this.dueDate = dueDate;
    }

    public String getAssignedToLogin() {
        return assignedToLogin;
    }

    public void setAssignedToLogin(String assignedToLogin) {
        this.assignedToLogin = assignedToLogin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TaskDashboardDTO)) {
            return false;
        }

        TaskDashboardDTO taskDashboardDTO = (TaskDashboardDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, taskDashboardDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TaskDashboardDTO{" +
            "id=" + getId() +
            ", title='" + getTitle() + "'" +
            ", status='" + getStatus() + "'" +
            ", dueDate='" + getDueDate() + "'" +
            ", assignedToLogin='" + getAssignedToLogin() + "'" +
            "}";
    }
}
