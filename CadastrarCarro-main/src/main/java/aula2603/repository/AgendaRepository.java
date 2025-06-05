/*package aula2603.repository;

import aula2603.model.entity.Agenda;
import aula2603.model.entity.AgendaStatus;
import aula2603.model.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AgendaRepository extends JpaRepository<Agenda, Long> {

    // Encontra agendas disponíveis para um médico específico após uma data/hora
    List<Agenda> findByMedicoAndStatusAndDataHoraInicioAfter(Medico medico, AgendaStatus status, LocalDateTime dataHora);

    // Encontra todas as agendas disponíveis após uma data/hora
    List<Agenda> findByStatusAndDataHoraInicioAfter(AgendaStatus status, LocalDateTime dataHora);

    // Encontra todas as agendas disponíveis (geral)
    List<Agenda> findByStatus(AgendaStatus status);

    // Encontra agendas por médico e intervalo de tempo (útil para evitar conflitos)
    List<Agenda> findByMedicoAndDataHoraInicioBetween(Medico medico, LocalDateTime inicio, LocalDateTime fim);

    // Query para buscar agendas disponíveis com médico carregado (evita N+1)
    @Query("SELECT a FROM Agenda a JOIN FETCH a.medico WHERE a.status = :status AND a.dataHoraInicio > :dataHora ORDER BY a.dataHoraInicio ASC")
    List<Agenda> findDisponiveisComMedico(@Param("status") AgendaStatus status, @Param("dataHora") LocalDateTime dataHora);

    // Query para buscar todas as agendas com médico carregado
    @Query("SELECT a FROM Agenda a JOIN FETCH a.medico ORDER BY a.dataHoraInicio ASC")
    List<Agenda> findAllWithMedico();

}*/
