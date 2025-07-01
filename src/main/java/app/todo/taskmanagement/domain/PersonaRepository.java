package app.todo.taskmanagement.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PersonaRepository extends JpaRepository<Persona, Long>, JpaSpecificationExecutor<Persona> {

    // Devuelve un "slice" paginado de todas las personas (sin contar el total de filas)
    Slice<Persona> findAllBy(Pageable pageable);
}
