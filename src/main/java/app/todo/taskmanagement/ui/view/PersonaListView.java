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
}
