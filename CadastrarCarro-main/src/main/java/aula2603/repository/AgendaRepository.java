package aula2603.repository;

import aula2603.model.entity.Agenda;
import aula2603.model.entity.AgendaStatus;
import aula2603.model.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgendaRepository extends JpaRepository<Agenda, Long> {



    // Busca agendas disponíveis com JOIN FETCH para evitar N+1
    @Query("SELECT a FROM Agenda a JOIN FETCH a.medico m " +
            "WHERE a.status = :status AND " +
            "m.status = 'ATIVO' AND " +
            "a.dataHoraInicio >= :dataAtual " +
            "ORDER BY a.dataHoraInicio")
    List<Agenda> findDisponiveisComMedicoAtivo(
            @Param("status") AgendaStatus status,
            @Param("dataAtual") LocalDateTime dataAtual);

    // Consulta para agenda do médico com paginação
    @Query("SELECT a FROM Agenda a WHERE " +
            "a.medico = :medico AND " +
            "a.dataHoraInicio BETWEEN :inicio AND :fim " +
            "ORDER BY a.dataHoraInicio")
    List<Agenda> findByMedicoAndPeriodo(
            @Param("medico") Medico medico,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);

    // Consulta para horários disponíveis por especialidade
    @Query("SELECT a FROM Agenda a JOIN FETCH a.medico m " +
            "WHERE m.especialidade = :especialidade AND " +
            "a.status = 'DISPONIVEL' AND " +
            "a.dataHoraInicio >= :dataAtual " +
            "ORDER BY a.dataHoraInicio")
    List<Agenda> findByEspecialidadeAndDisponivel(
            @Param("especialidade") String especialidade,
            @Param("dataAtual") LocalDateTime dataAtual);

    // Consulta para agenda do dia
    @Query("SELECT a FROM Agenda a WHERE " +
            "a.medico = :medico AND " +
            "CAST(a.dataHoraInicio AS localdate) = CAST(:data AS localdate) " +
            "ORDER BY a.dataHoraInicio")
    List<Agenda> findByMedicoAndData(
            @Param("medico") Medico medico,
            @Param("data") LocalDate data);

    // Verifica se médico tem agenda em determinado horário
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
            "FROM Agenda a WHERE " +
            "a.medico = :medico AND " +
            "a.status <> 'CANCELADO' AND " +
            "a.dataHoraInicio <= :horario AND " +
            "a.dataHoraFim > :horario")
    boolean existsMedicoOcupadoNoHorario(
            @Param("medico") Medico medico,
            @Param("horario") LocalDateTime horario);

    // Métodos derivados mantidos para compatibilidade
    List<Agenda> findByMedicoAndStatus(Medico medico, AgendaStatus status);

    List<Agenda> findByStatus(AgendaStatus status);

    List<Agenda> findByMedico(Medico medico);

    // Novo método para buscar agendas futuras de um médico
    @Query("SELECT a FROM Agenda a WHERE " +
            "a.medico = :medico AND " +
            "a.dataHoraInicio >= :dataAtual AND " +
            "a.status <> 'CANCELADO' " +
            "ORDER BY a.dataHoraInicio")
    List<Agenda> findAgendasFuturasByMedico(
            @Param("medico") Medico medico,
            @Param("dataAtual") LocalDateTime dataAtual);

    // Método para verificar disponibilidade imediata
    @Query("SELECT a FROM Agenda a WHERE " +
            "a.medico = :medico AND " +
            "a.status = 'DISPONIVEL' AND " +
            "a.dataHoraInicio <= :horario AND " +
            "a.dataHoraFim > :horario")
    List<Agenda> findDisponibilidadeNoMomento(
            @Param("medico") Medico medico,
            @Param("horario") LocalDateTime horario);

    @Query("SELECT a FROM Agenda a WHERE " +
            "a.medico = :medico AND " +
            "a.status <> 'CANCELADO' AND " +
            "((:inicio BETWEEN a.dataHoraInicio AND a.dataHoraFim) OR " +
            "(:fim BETWEEN a.dataHoraInicio AND a.dataHoraFim) OR " +
            "(a.dataHoraInicio BETWEEN :inicio AND :fim) OR " +
            "(a.dataHoraFim BETWEEN :inicio AND :fim))")
    List<Agenda> findConflitosDeHorario(
            @Param("medico") Medico medico,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);

    // Adicionado: Método para buscar agendas disponíveis de um médico em um período específico
    @Query("SELECT a FROM Agenda a WHERE " +
            "a.medico = :medico AND " +
            "a.status = :status AND " +
            "a.dataHoraInicio BETWEEN :inicio AND :fim " +
            "ORDER BY a.dataHoraInicio")
    List<Agenda> findByMedicoAndStatusAndPeriodo(
            @Param("medico") Medico medico,
            @Param("status") AgendaStatus status,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);

    List<Agenda> findByMedicoAndDataHoraInicioAfter(Medico medico, LocalDateTime data);

    boolean existsByMedicoAndDataHoraInicioBeforeAndDataHoraFimAfterAndStatusNot(
            Medico medico, LocalDateTime inicio, LocalDateTime fim, AgendaStatus status);

    List<Agenda> findByMedicoAndStatusAndDataHoraInicioBetween(
            Medico medico, AgendaStatus status, LocalDateTime inicio, LocalDateTime fim);
    List<Agenda> findByMedicoAndDataHoraInicioBetween(Medico medico, LocalDateTime inicio, LocalDateTime fim);
    @Query("SELECT a FROM Agenda a " +
            "LEFT JOIN FETCH a.consulta c " +
            "LEFT JOIN FETCH c.paciente " +
            "WHERE a.medico.id = :medicoId")
    List<Agenda> buscarComConsultasEPacientes(@Param("medicoId") Long medicoId);
}



