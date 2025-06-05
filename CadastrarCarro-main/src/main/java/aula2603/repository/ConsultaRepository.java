
package aula2603.repository;

import aula2603.model.entity.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    /**
     * Busca todas as consultas de um paciente com os dados do médico e paciente carregados (evita LazyInitializationException).
     */
    @Query("SELECT c FROM Consulta c JOIN FETCH c.paciente JOIN FETCH c.medico WHERE c.paciente.id = :pacienteId")
    List<Consulta> findByPacienteIdWithMedico(@Param("pacienteId") Long pacienteId);

    /**
     * Busca todas as consultas de um médico com os dados do paciente carregados.
     */
    @Query("SELECT c FROM Consulta c JOIN FETCH c.paciente JOIN FETCH c.medico WHERE c.medico.id = :medicoId") // Query adicionada
    List<Consulta> findByMedicoIdWithPaciente(@Param("medicoId") Long medicoId);

    /**
     * Busca todas as consultas com os dados de paciente e médico já carregados.
     */
    @Query("SELECT DISTINCT c FROM Consulta c LEFT JOIN FETCH c.paciente LEFT JOIN FETCH c.medico")
    List<Consulta> findAllWithPacienteAndMedico();

    /**
     * Busca uma consulta por ID com os dados de paciente e médico já carregados.
     */
    @Query("SELECT c FROM Consulta c LEFT JOIN FETCH c.paciente LEFT JOIN FETCH c.medico WHERE c.id = :id")
    Optional<Consulta> findByIdWithPacienteAndMedico(@Param("id") Long id);

    /**
     * Busca todas as consultas de um determinado médico (sem fetch explícito).
     */
    List<Consulta> findByMedicoId(Long medicoId);

    /**
     * Busca todas as consultas de um determinado paciente (sem fetch explícito).
     */
    List<Consulta> findByPacienteId(Long pacienteId);

    /**
     * Busca consultas por data (LocalDate), convertendo para LocalDateTime.
     */
    default List<Consulta> findByData(LocalDate data) {
        LocalDateTime inicio = data.atStartOfDay();
        LocalDateTime fim = data.plusDays(1).atStartOfDay();
        return findByDataBetween(inicio, fim);
    }

    /**
     * Busca consultas entre um intervalo de datas (LocalDateTime) com fetch.
     */
    @Query("SELECT c FROM Consulta c LEFT JOIN FETCH c.paciente LEFT JOIN FETCH c.medico " +
            "WHERE c.data >= :inicio AND c.data < :fim")
    List<Consulta> findByDataBetween(@Param("inicio") LocalDateTime inicio,
                                     @Param("fim") LocalDateTime fim);
}

