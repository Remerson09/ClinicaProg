
package aula2603.repository;

import aula2603.model.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MedicoRepository extends JpaRepository<Medico, Long> {

    // Método padrão do Spring Data JPA para busca por nome ignorando case
    List<Medico> findByNomeContainingIgnoreCase(String nome);

    // Método para ordenar por nome (já existente)
    List<Medico> findAllByOrderByNomeAsc();

    // Método com query customizada (mantido para referência, mas o controller usa o padrão acima)
    @Query("SELECT m FROM Medico m WHERE LOWER(m.nome) LIKE LOWER(CONCAT(\'%\', :nome, \'%\'))")
    List<Medico> buscarPorNome(@Param("nome") String nome);
}

