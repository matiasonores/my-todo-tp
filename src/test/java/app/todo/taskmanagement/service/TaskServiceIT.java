package app.todo.taskmanagement.service;

import app.todo.TestcontainersConfiguration;
import app.todo.taskmanagement.domain.Task;
import app.todo.taskmanagement.domain.Persona;
import app.todo.taskmanagement.domain.TaskRepository;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class TaskServiceIT {

    @Autowired
    TaskService taskService;

    @Autowired
    TaskRepository taskRepository;
    
    @Autowired
    PersonaService personaService;
    
    @Autowired
    Clock clock;
    
    private Persona persona;
    
    @AfterEach
    void cleanUp() {
        taskRepository.deleteAll();
    }
    // Inicializamos una persona para usar en los tests
    @BeforeEach
    void setup() {
        personaService.createPersona(12345678, "Perez", "Juan", 30);
        persona = personaService.list(PageRequest.ofSize(1)).get(0);
    }
    @Test
    public void tasks_are_stored_in_the_database_with_the_current_timestamp() {
        var now = clock.instant();
        var due = LocalDate.of(2025, 2, 7);
        taskService.createTask("Do this", due, persona);
        assertThat(taskService.list(PageRequest.ofSize(1))).singleElement()
                .matches(task -> task.getDescription().equals("Do this") && due.equals(task.getDueDate())
                        && task.getCreationDate().isAfter(now) && task.getPersona().equals(persona));
    }

    @Test
    public void tasks_are_validated_before_they_are_stored() {
        assertThatThrownBy(() -> taskService.createTask("X".repeat(Task.DESCRIPTION_MAX_LENGTH + 1), null, persona))
                .isInstanceOf(ValidationException.class);
        assertThat(taskRepository.count()).isEqualTo(0);
    }
}
