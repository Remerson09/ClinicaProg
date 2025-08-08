package aula2603.repository;

import aula2603.model.entity.Medico;
import aula2603.model.entity.StatusMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {

    // Buscar médico por CRM (exato)
    Medico findByCrm(String crm);

    // Buscar médicos por nome (contém, ignorando case)
    List<Medico> findByNomeContainingIgnoreCase(String nome);

    // Buscar médicos por CRM (contém, ignorando case) - para busca parcial
    List<Medico> findByCrmContainingIgnoreCase(String crm);

    // Buscar médicos por nome ou CRM
    @Query("SELECT m FROM Medico m WHERE LOWER(m.nome) LIKE LOWER(CONCAT(\'%\', :termo, '\\%')) OR LOWER(m.crm) LIKE LOWER(CONCAT(\'%\', :termo, '\\%'))")
    List<Medico> findByNomeOrCrmContaining(@Param("termo") String termo);

    // Verificar se CRM existe
    boolean existsByCrm(String crm);

    // Contar médicos por CRM (para validação)
    long countByCrm(String crm);

    // Buscar médicos com consultas (JOIN FETCH)
    @Query("SELECT DISTINCT m FROM Medico m LEFT JOIN FETCH m.consultaList")
    List<Medico> findAllWithConsultas();

    // Buscar médico com consultas por ID
    @Query("SELECT m FROM Medico m LEFT JOIN FETCH m.consultaList WHERE m.id = :id")
    Optional<Medico> findByIdWithConsultas(@Param("id") Long id);

    // Buscar médicos com agendas
    @Query("SELECT DISTINCT m FROM Medico m LEFT JOIN FETCH m.agendaList")
    List<Medico> findAllWithAgendas();

    // Buscar médicos ordenados por nome
    List<Medico> findAllByOrderByNomeAsc();

    // Buscar médicos por parte do CRM (apenas números)
    @Query("SELECT m FROM Medico m WHERE m.crm LIKE CONCAT(\'CRM-%\', :numero, '\\%')")
    List<Medico> findByCrmNumero(@Param("numero") String numero);

    List<Medico> findByStatus(StatusMedico statusMedico);
}


