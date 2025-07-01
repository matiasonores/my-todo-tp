package app.todo.taskmanagement.ui.view;

import app.todo.base.ui.component.ViewToolbar;
import app.todo.taskmanagement.domain.Persona;
import app.todo.taskmanagement.service.PersonaService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.Optional;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("persona-list")
@PageTitle("Personas")
@Menu(order = 1, icon = "vaadin:user", title = "Personas")
@PermitAll
public class PersonaListView extends Main {

    private final PersonaService personaService;

    private final IntegerField dniField;
    private final TextField apellidoField;
    private final TextField nombreField;
    private final IntegerField edadField;
    private final Button createBtn;
    private final Grid<Persona> personaGrid;

    public PersonaListView(PersonaService personaService) {
        this.personaService = personaService;

        dniField = new IntegerField("DNI");
        dniField.setPlaceholder("12345678");
        dniField.setMin(1000000);
        dniField.setMax(99999999);
        dniField.setStepButtonsVisible(true);

        apellidoField = new TextField("Apellido");
        apellidoField.setMaxLength(50);
        apellidoField.setPlaceholder("Ej: Pérez");

        nombreField = new TextField("Nombre");
        nombreField.setMaxLength(50);
        nombreField.setPlaceholder("Ej: Juan");

        edadField = new IntegerField("Edad");
        edadField.setMin(0);
        edadField.setMax(130);
        edadField.setStepButtonsVisible(true);

        createBtn = new Button("Agregar Persona", event -> createPersona());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        personaGrid = new Grid<>(Persona.class, false);
        personaGrid.setItems(query -> personaService.list(toSpringPageRequest(query)).stream());

        personaGrid.addColumn(Persona::getDni).setHeader("DNI");
        personaGrid.addColumn(Persona::getApellido).setHeader("Apellido");
        personaGrid.addColumn(Persona::getNombre).setHeader("Nombre");
        personaGrid.addColumn(p -> Optional.ofNullable(p.getEdad()).map(Object::toString).orElse("N/A"))
                .setHeader("Edad");

        // Columna con botón Eliminar
        personaGrid.addComponentColumn(persona -> {
            Button editButton = new Button("Editar", e -> openEditPersonaDialog(persona));
            editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

            Button deleteButton = new Button("Eliminar", click -> {
                personaService.deletePersona(persona.getId());
                personaGrid.getDataProvider().refreshAll();
                Notification.show("Persona eliminada", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
            });
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

            HorizontalLayout actionsLayout = new HorizontalLayout(editButton, deleteButton);
            actionsLayout.setSpacing(true);
            return actionsLayout;
        }).setHeader("Acciones");
        
        personaGrid.setSizeFull();

        setSizeFull();
        addClassNames(
                LumoUtility.BoxSizing.BORDER,
                LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM,
                LumoUtility.Gap.SMALL
        );

        add(new ViewToolbar("Personas", ViewToolbar.group(dniField, apellidoField, nombreField, edadField, createBtn)));
        add(personaGrid);
    }

    private void createPersona() {
        try {
            if (dniField.getValue() == null || nombreField.isEmpty() || apellidoField.isEmpty()) {
                Notification.show("DNI, Nombre y Apellido son obligatorios", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            personaService.createPersona(
                    dniField.getValue(),
                    apellidoField.getValue(),
                    nombreField.getValue(),
                    edadField.getValue()
            );

            personaGrid.getDataProvider().refreshAll();
            clearForm();

            Notification.show("Persona agregada", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception ex) {
            Notification.show("Error al guardar: " + ex.getMessage(), 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void clearForm() {
        dniField.clear();
        apellidoField.clear();
        nombreField.clear();
        edadField.clear();
    }
    
    private void openEditPersonaDialog(Persona persona) {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        IntegerField dniField = new IntegerField("DNI");
        dniField.setValue(persona.getDni());
        dniField.setMin(1000000);
        dniField.setMax(99999999);

        TextField apellidoField = new TextField("Apellido");
        apellidoField.setValue(persona.getApellido());

        TextField nombreField = new TextField("Nombre");
        nombreField.setValue(persona.getNombre());

        IntegerField edadField = new IntegerField("Edad");
        edadField.setValue(Optional.ofNullable(persona.getEdad()).orElse(0));

        Button saveButton = new Button("Guardar", event -> {
            // Validaciones básicas
            if (dniField.isEmpty() || apellidoField.isEmpty() || nombreField.isEmpty()) {
                Notification.show("DNI, Apellido y Nombre son obligatorios", 3000, Notification.Position.BOTTOM_END);
                return;
            }

            // Actualizar persona
            persona.setDni(dniField.getValue());
            persona.setApellido(apellidoField.getValue());
            persona.setNombre(nombreField.getValue());
            persona.setEdad(edadField.getValue());

            personaService.updatePersona(persona);
            personaGrid.getDataProvider().refreshAll();
            Notification.show("Persona actualizada", 3000, Notification.Position.BOTTOM_END);
            dialog.close();
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancelar", e -> dialog.close());

        VerticalLayout layout = new VerticalLayout(dniField, apellidoField, nombreField, edadField, saveButton, cancelButton);
        dialog.add(layout);
        dialog.open();
    }
}
