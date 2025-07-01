package app.todo.taskmanagement.ui.view;

import app.todo.base.ui.component.ViewToolbar;
import app.todo.taskmanagement.domain.Task;
import app.todo.taskmanagement.domain.Persona;
import app.todo.taskmanagement.service.PersonaService;
import app.todo.taskmanagement.service.TaskService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Optional;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;
import org.springframework.data.domain.Sort;

@Route("task-list")
@PageTitle("Task List")
@Menu(order = 0, icon = "vaadin:clipboard-check", title = "Task List")
@PermitAll // When security is enabled, allow all authenticated users
public class TaskListView extends Main {

    private final TaskService taskService;
    private final PersonaService personaService;
    
    final TextField description;
    final DatePicker dueDate;
    final Button createBtn;
    final Grid<Task> taskGrid;
    final ComboBox<Persona> personaComboBox;
    
    public TaskListView(TaskService taskService, PersonaService personaService, Clock clock) {
        this.taskService = taskService;
        this.personaService = personaService;
        
        description = new TextField();
        description.setPlaceholder("What do you want to do?");
        description.setAriaLabel("Task description");
        description.setMaxLength(Task.DESCRIPTION_MAX_LENGTH);
        description.setMinWidth("20em");

        dueDate = new DatePicker();
        dueDate.setPlaceholder("Due date");
        dueDate.setAriaLabel("Due date");
        dueDate.setValue(LocalDate.now());

        createBtn = new Button("Create", event -> createTask());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        //personaComboBox = new ComboBox<>("Asignar a");
        personaComboBox = new ComboBox<>();
        personaComboBox.setItemLabelGenerator(p -> p.getApellido() + ", " + p.getNombre());
        personaComboBox.setItems(personaService.listAll());
        personaComboBox.setPlaceholder("Seleccionar persona");
        personaComboBox.setMinWidth("15em");
        
        var dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withZone(clock.getZone())
                .withLocale(getLocale());
        var dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(getLocale());

        taskGrid = new Grid<>();
        //taskGrid.setItems(query -> taskService.list(toSpringPageRequest(query)).stream());
        taskGrid.setItems(query -> {
            var pageable = toSpringPageRequest(query).withSort(Sort.by(Sort.Direction.DESC, "creationDate"));
            return taskService.list(pageable).stream();
        });
        taskGrid.addComponentColumn(task -> {
                        Checkbox checkbox = new Checkbox(task.isDone());
                        checkbox.addValueChangeListener(event -> {
                                task.setDone(event.getValue());
                                taskService.updateTask(task); // Make sure you have this method to persist
                                // changes
                        });
                        return checkbox;
                }).setHeader("Done");

        taskGrid.addColumn(Task::getDescription).setHeader("Description");
        taskGrid.addColumn(task -> Optional.ofNullable(task.getDueDate()).map(dateFormatter::format).orElse("Never"))
                .setHeader("Due Date");
        taskGrid.addColumn(task -> {
            Persona p = task.getPersona();
            return p != null ? p.getApellido() + ", " + p.getNombre() : "-";
        }).setHeader("Asignado a");
        taskGrid.addColumn(task -> dateTimeFormatter.format(task.getCreationDate())).setHeader("Creation Date");
        
        taskGrid.addComponentColumn(task -> {
            Button editButton = new Button("Editar", e -> openEditTaskDialog(task));
            editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

            Button deleteButton = new Button("Eliminar", click -> {
                taskService.deleteTask(task.getId());
                taskGrid.getDataProvider().refreshAll();
                Notification.show("Tarea eliminada", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
            });
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

            HorizontalLayout actionsLayout = new HorizontalLayout(editButton, deleteButton);
            actionsLayout.setSpacing(true);
            return actionsLayout;
        }).setHeader("Acciones");
        
        taskGrid.setSizeFull();

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

        add(new ViewToolbar("Task List", ViewToolbar.group(description, dueDate, personaComboBox, createBtn)));
        add(taskGrid);
    }

    private void createTask() {
        if (description.isEmpty() || personaComboBox.isEmpty()) {
            Notification.show("Descripción y Persona son obligatorios", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        taskService.createTask(description.getValue(), dueDate.getValue(), personaComboBox.getValue());
        taskGrid.getDataProvider().refreshAll();
        description.clear();
        dueDate.clear();
        personaComboBox.clear();

        Notification.show("Tarea agregada", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
    
    private void openEditTaskDialog(Task task) {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        TextField descriptionField = new TextField("Descripción");
        descriptionField.setValue(task.getDescription());
        descriptionField.setMaxLength(Task.DESCRIPTION_MAX_LENGTH);

        DatePicker dueDatePicker = new DatePicker("Fecha de Vencimiento");
        dueDatePicker.setValue(Optional.ofNullable(task.getDueDate()).orElse(null));
        
        ComboBox<Persona> personaComboBox = new ComboBox<>("Asignar a");
        personaComboBox.setItemLabelGenerator(p -> p.getApellido() + ", " + p.getNombre());
        personaComboBox.setItems(personaService.listAll());
        personaComboBox.setValue(task.getPersona());
        
        Button saveButton = new Button("Guardar", event -> {
        	if (descriptionField.isEmpty() || personaComboBox.isEmpty()) {
                Notification.show("Descripción y Persona son obligatorios", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            task.setDescription(descriptionField.getValue());
            task.setDueDate(dueDatePicker.getValue());
            task.setPersona(personaComboBox.getValue());
            
            taskService.updateTask(task);
            taskGrid.getDataProvider().refreshAll();

            Notification.show("Tarea actualizada", 3000, Notification.Position.BOTTOM_END);
            dialog.close();
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancelar", e -> dialog.close());

        VerticalLayout layout = new VerticalLayout(descriptionField, dueDatePicker, personaComboBox, saveButton, cancelButton);
        dialog.add(layout);
        dialog.open();
    }
}
