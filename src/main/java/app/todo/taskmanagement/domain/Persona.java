package app.todo.taskmanagement.domain;

import app.todo.base.domain.AbstractEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "persona")
public class Persona extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "persona_id")
    private Long id;

    @NotNull
    @Column(name = "dni", nullable = false, unique = true)
    private Integer dni;

    @NotNull
    @Size(max = 50)
    @Column(name = "apellido", nullable = false, length = 50)
    private String apellido;

    @NotNull
    @Size(max = 50)
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "edad")
    private Integer edad;

    // --- Métodos getter/setter ---

    @Override
    public @Nullable Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getDni() {
        return dni;
    }

    public void setDni(Integer dni) {
        this.dni = dni;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }
}
