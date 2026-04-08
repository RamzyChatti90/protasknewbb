package com.carnival.protasknewbb.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.carnival.protasknewbb.IntegrationTest;
import com.carnival.protasknewbb.domain.Task;
import com.carnival.protasknewbb.domain.User;
import com.carnival.protasknewbb.domain.enumeration.TaskStatus;
import com.carnival.protasknewbb.repository.TaskRepository;
import com.carnival.protasknewbb.repository.UserRepository;
import com.carnival.protasknewbb.security.AuthoritiesConstants;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link DashboardResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(username = "user", authorities = { AuthoritiesConstants.USER })
class DashboardResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final TaskStatus DEFAULT_STATUS = TaskStatus.TODO;
    private static final TaskStatus UPDATED_STATUS = TaskStatus.IN_PROGRESS;

    private static final Instant DEFAULT_DUE_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DUE_DATE = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS);

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDashboardMockMvc;

    private Task task;
    private User currentUser;

    /**
     * Create an entity for this test.
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Task createEntity(EntityManager em, User user) {
        Task task = new Task()
            .title(DEFAULT_TITLE)
            .description(DEFAULT_DESCRIPTION)
            .status(DEFAULT_STATUS)
            .dueDate(DEFAULT_DUE_DATE)
            .assignedTo(user);
        return task;
    }

    public static Task createUpdatedEntity(EntityManager em, User user) {
        Task task = new Task()
            .title(UPDATED_TITLE)
            .description(UPDATED_DESCRIPTION)
            .status(UPDATED_STATUS)
            .dueDate(UPDATED_DUE_DATE)
            .assignedTo(user);
        return task;
    }

    @BeforeEach
    public void initTest() {
        // Ensure a user exists for the @WithMockUser annotation
        currentUser = userRepository.findOneByLogin("user").orElseGet(() -> {
            User user = new User();
            user.setLogin("user");
            user.setPassword("$2a$10$M9m3eYdFfX1/Z5/Z5/Z5/Z5/Z5/Z5/Z5/Z5/Z5/Z5/Z5/Z5"); // Dummy password
            user.setActivated(true);
            user.setEmail("user@localhost");
            user.setFirstName("test");
            user.setLastName("user");
            user.setLangKey("en");
            user.setAuthorities(Collections.singleton(em.find(com.carnival.protasknewbb.domain.Authority.class, AuthoritiesConstants.USER)));
            em.persist(user);
            em.flush();
            return user;
        });
        task = createEntity(em, currentUser);
    }

    @Test
    @Transactional
    void getTasksForCurrentUser() throws Exception {
        // Initialize the database
        taskRepository.saveAndFlush(task);

        // Get all the tasks for the current user
        restDashboardMockMvc
            .perform(get("/api/dashboard/tasks").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(task.getId().intValue()))
            .andExpect(jsonPath("$.[*].title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.[*].status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.[*].assignedToLogin").value(currentUser.getLogin()));

        // Create another task for a different user, it should not be returned
        User otherUser = new User();
        otherUser.setLogin("otheruser");
        otherUser.setPassword("$2a$10$M9m3eYdFfX1/Z5/Z5/Z5/Z5/Z5/Z5/Z5/Z5/Z5/Z5/Z5/Z5"); // Dummy password
        otherUser.setActivated(true);
        otherUser.setEmail("otheruser@localhost");
        otherUser.setFirstName("other");
        otherUser.setLastName("user");
        otherUser.setLangKey("en");
        otherUser.setAuthorities(Collections.singleton(em.find(com.carnival.protasknewbb.domain.Authority.class, AuthoritiesConstants.USER)));
        em.persist(otherUser);
        em.flush();

        Task otherUserTask = createEntity(em, otherUser);
        otherUserTask.setTitle("Other User Task");
        taskRepository.saveAndFlush(otherUserTask);

        restDashboardMockMvc
            .perform(get("/api/dashboard/tasks").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.length()").value(1)); // Should still be 1 task for 'user'
    }

    @Test
    @Transactional
    void getTaskStatusDistributionForCurrentUser() throws Exception {
        // Initialize the database with multiple tasks for the current user
        Task task1 = createEntity(em, currentUser).status(TaskStatus.TODO).title("Task 1");
        Task task2 = createEntity(em, currentUser).status(TaskStatus.TODO).title("Task 2");
        Task task3 = createEntity(em, currentUser).status(TaskStatus.IN_PROGRESS).title("Task 3");
        Task task4 = createEntity(em, currentUser).status(TaskStatus.DONE).title("Task 4");
        taskRepository.saveAllAndFlush(List.of(task1, task2, task3, task4));

        // Get the task status distribution for the current user
        restDashboardMockMvc
            .perform(get("/api/dashboard/task-status-distribution").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.TODO").value(2))
            .andExpect(jsonPath("$.IN_PROGRESS").value(1))
            .andExpect(jsonPath("$.DONE").value(1));

        // Add a task for another user, it should not affect the current user's distribution
        User otherUser = new User();
        otherUser.setLogin("otheruser2");
        otherUser.setPassword("$2a$10$M9m3eYdFfX1/Z5/Z5/Z5/Z5/Z5/Z5/Z5/Z5/Z5/Z5/Z5/Z5"); // Dummy password
        otherUser.setActivated(true);
        otherUser.setEmail("otheruser2@localhost");
        otherUser.setFirstName("other");
        otherUser.setLastName("user2");
        otherUser.setLangKey("en");
        otherUser.setAuthorities(Collections.singleton(em.find(com.carnival.protasknewbb.domain.Authority.class, AuthoritiesConstants.USER)));
        em.persist(otherUser);
        em.flush();

        Task otherUserTask = createEntity(em, otherUser).status(TaskStatus.CANCELED).title("Other User Canceled Task");
        taskRepository.saveAndFlush(otherUserTask);

        restDashboardMockMvc
            .perform(get("/api/dashboard/task-status-distribution").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.TODO").value(2))
            .andExpect(jsonPath("$.IN_PROGRESS").value(1))
            .andExpect(jsonPath("$.DONE").value(1))
            .andExpect(jsonPath("$.CANCELED").doesNotExist()); // CANCELED task should not be counted for current user
    }
}
