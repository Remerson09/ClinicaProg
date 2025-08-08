package aula2603.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Table(name = "agenda")
public class Agenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "A data/hora de início é obrigatória")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "data_hora_inicio", nullable = false)
    private LocalDateTime dataHoraInicio;

    @NotNull(message = "A data/hora de fim é obrigatória")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "data_hora_fim", nullable = false)
    private LocalDateTime dataHoraFim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgendaStatus status = AgendaStatus.DISPONIVEL;

    @NotNull(message = "O médico deve ser selecionado")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    @OneToOne(mappedBy = "agenda", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Consulta consulta;

    // Construtores
    public Agenda() {}

    public Agenda(LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim, Medico medico) {
        this.dataHoraInicio = dataHoraInicio;
        this.dataHoraFim = dataHoraFim;
        this.medico = medico;
        this.status = AgendaStatus.DISPONIVEL;
    }

    public Agenda(LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim, AgendaStatus status, Medico medico) {
        this.dataHoraInicio = dataHoraInicio;
        this.dataHoraFim = dataHoraFim;
        this.status = status;
        this.medico = medico;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDataHoraInicio() {
        return dataHoraInicio;
    }

    public void setDataHoraInicio(LocalDateTime dataHoraInicio) {
        this.dataHoraInicio = dataHoraInicio;
    }

    public LocalDateTime getDataHoraFim() {
        return dataHoraFim;
    }

    public void setDataHoraFim(LocalDateTime dataHoraFim) {
        this.dataHoraFim = dataHoraFim;
    }

    public AgendaStatus getStatus() {
        return status;
    }

    public void setStatus(AgendaStatus status) {
        this.status = status;
    }

    public Medico getMedico() {
        return medico;
    }

    public void setMedico(Medico medico) {
        this.medico = medico;
    }

    public Consulta getConsulta() {
        return consulta;
    }

    public void setConsulta(Consulta consulta) {
        this.consulta = consulta;
    }

    // Métodos auxiliares
    public boolean isDisponivel() {
        return this.status == AgendaStatus.DISPONIVEL;
    }

    public boolean isAgendado() {
        return this.status == AgendaStatus.AGENDADO;
    }

    public boolean isCancelado() {
        return this.status == AgendaStatus.CANCELADO;
    }

    /**
     * Verifica se este horário conflita com outro horário
     */
    public boolean conflitaCom(LocalDateTime inicio, LocalDateTime fim) {
        return (inicio.isBefore(this.dataHoraFim) && fim.isAfter(this.dataHoraInicio));
    }

    /**
     * Verifica se este horário está no passado
     */
    public boolean isPassado() {
        return this.dataHoraInicio.isBefore(LocalDateTime.now());
    }

    /**
     * Retorna a duração em minutos
     */
    public long getDuracaoMinutos() {
        return java.time.Duration.between(dataHoraInicio, dataHoraFim).toMinutes();
    }

    /**
     * Formata o período para exibição
     */
    public String getPeriodoFormatado() {
        return dataHoraInicio.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) +
                " às " +
                dataHoraFim.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
    }

    /**
     * Retorna informações resumidas da agenda
     */
    public String dados() {
        return "Agenda: " + getPeriodoFormatado() +
                " | Médico: " + (medico != null ? medico.getNome() : "N/A") +
                " | Status: " + status;
    }

    // Validações de negócio
    @PrePersist
    @PreUpdate
    private void validarDatas() {
        if (dataHoraInicio != null && dataHoraFim != null) {
            if (dataHoraFim.isBefore(dataHoraInicio) || dataHoraFim.isEqual(dataHoraInicio)) {
                throw new IllegalArgumentException("A data/hora de fim deve ser posterior à data/hora de início");
            }
        }
    }

    @Override
    public String toString() {
        return "Agenda{" +
                "id=" + id +
                ", dataHoraInicio=" + dataHoraInicio +
                ", dataHoraFim=" + dataHoraFim +
                ", status=" + status +
                ", medico=" + (medico != null ? medico.getNome() : "null") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agenda agenda = (Agenda) o;
        return id != null && id.equals(agenda.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

