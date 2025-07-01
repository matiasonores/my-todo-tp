package app.todo.taskmanagement.service;

import app.todo.taskmanagement.domain.Persona;
import app.todo.taskmanagement.domain.PersonaRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class PersonaService {

    private final PersonaRepository personaRepository;

    PersonaService(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    public void createPersona(Integer dni, String apellido, String nombre, @Nullable Integer edad) {
        var persona = new Persona();
        persona.setDni(dni);
        persona.setApellido(apellido);
        persona.setNombre(nombre);
        persona.setEdad(edad);
        personaRepository.saveAndFlush(persona);
    }

    public void updatePersona(Persona persona) {
        personaRepository.saveAndFlush(persona);
    }

    public List<Persona> list(Pageable pageable) {
        return personaRepository.findAllBy(pageable).toList();
    }
    public List<Persona> listAll() {
        return personaRepository.findAll();
    }
    public void deletePersona(Long id) {
        personaRepository.deleteById(id);
    }
}
