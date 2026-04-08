package com.carnival.protasknewbb.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.carnival.protasknewbb.domain.Task;
import com.carnival.protasknewbb.domain.User;
import com.carnival.protasknewbb.domain.enumeration.TaskStatus;
import com.carnival.protasknewbb.repository.TaskRepository;
import com.carnival.protasknewbb.repository.UserRepository;
import com.carnival.protasknewbb.security.AuthoritiesConstants;
import com.carnival.protasknewbb.security.SecurityUtils;
import com.carnival.protasknewbb.service.dto.TaskDashboardDTO;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setLogin("user");
        currentUser.setAuthorities(Collections.singleton(new SimpleGrantedAuthority(AuthoritiesConstants.USER)));

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(
            new UsernamePasswordAuthenticationToken(currentUser.getLogin(), "password", currentUser.getAuthorities())
        );
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getTasksForCurrentUserShouldReturnTasks() {
        // Given
        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Task 1");
        task1.setStatus(TaskStatus.TODO);
        task1.setDueDate(Instant.now().plusSeconds(3600));
        task1.setAssignedTo(currentUser);

        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");
        task2.setStatus(TaskStatus.IN_PROGRESS);
        task2.setDueDate(Instant.now().plusSeconds(7200));
        task2.setAssignedTo(currentUser);

        when(taskRepository.findByAssignedToIsCurrentUser()).thenReturn(Arrays.asList(task1, task2));

        // When
        List<TaskDashboardDTO> result = taskService.getTasksForCurrentUser();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(task1.getId());
        assertThat(result.get(0).getTitle()).isEqualTo(task1.getTitle());
        assertThat(result.get(0).getStatus()).isEqualTo(task1.getStatus());
        assertThat(result.get(0).getAssignedToLogin()).isEqualTo(currentUser.getLogin());
        assertThat(result.get(1).getId()).isEqualTo(task2.getId());
        verify(taskRepository, times(1)).findByAssignedToIsCurrentUser();
    }

    @Test
    void getTaskStatusDistributionForCurrentUserShouldReturnDistribution() {
        // Given
        List<Map<TaskStatus, Long>> mockDistribution = new ArrayList<>();
        mockDistribution.add(Map.of(TaskStatus.TODO, 5L));
        mockDistribution.add(Map.of(TaskStatus.IN_PROGRESS, 3L));
        mockDistribution.add(Map.of(TaskStatus.DONE, 2L));

        when(taskRepository.countTasksByStatusForCurrentUser()).thenReturn(mockDistribution);

        // When
        Map<TaskStatus, Long> result = taskService.getTaskStatusDistributionForCurrentUser();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsEntry(TaskStatus.TODO, 5L);
        assertThat(result).containsEntry(TaskStatus.IN_PROGRESS, 3L);
        assertThat(result).containsEntry(TaskStatus.DONE, 2L);
        verify(taskRepository, times(1)).countTasksByStatusForCurrentUser();
    }

    @Test
    void saveShouldAssignTaskToCurrentUserIfAssignedToIsNull() {
        // Given
        Task newTask = new Task();
        newTask.setTitle("New Task");
        newTask.setStatus(TaskStatus.TODO);
        newTask.setDueDate(Instant.now().plusDays(1));

        when(userRepository.findOneByLogin(currentUser.getLogin())).thenReturn(Optional.of(currentUser));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Task savedTask = taskService.save(newTask);

        // Then
        assertThat(savedTask.getAssignedTo()).isEqualTo(currentUser);
        verify(userRepository, times(1)).findOneByLogin(currentUser.getLogin());
        verify(taskRepository, times(1)).save(newTask);
    }

    @Test
    void saveShouldNotChangeAssignedToIfAlreadySet() {
        // Given
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setLogin("anotheruser");

        Task existingTask = new Task();
        existingTask.setTitle("Existing Task");
        existingTask.setStatus(TaskStatus.DONE);
        existingTask.setAssignedTo(anotherUser);

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Task savedTask = taskService.save(existingTask);

        // Then
        assertThat(savedTask.getAssignedTo()).isEqualTo(anotherUser);
        verify(userRepository, never()).findOneByLogin(anyString());
        verify(taskRepository, times(1)).save(existingTask);
    }
}
