/*package aula2603.model.entity;

import jakarta.persistence.*;
import org.antlr.v4.runtime.misc.NotNull;


import java.time.LocalDateTime;

@Entity
public class Agenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "A data e hora de início são obrigatórias.")
    @FutureOrPresent(message = "A data de início não pode ser no passado.")
    @Column(nullable = false)
    private LocalDateTime dataHoraInicio;

    // Poderíamos adicionar dataHoraFim se a duração for variável, mas vamos manter simples por enquanto
    // private LocalDateTime dataHoraFim;

    @NotNull(message = "O médico é obrigatório.")
    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    @NotNull(message = "O status da agenda é obrigatório.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgendaStatus status;

    // Relacionamento opcional com a Consulta (quando status for AGENDADO)
    @OneToOne(mappedBy = "agenda", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Consulta consulta;

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

    public Medico getMedico() {
        return medico;
    }

    public void setMedico(Medico medico) {
        this.medico = medico;
    }

    public AgendaStatus getStatus() {
        return status;
    }

    public void setStatus(AgendaStatus status) {
        this.status = status;
    }

    public Consulta getConsulta() {
        return consulta;
    }

    public void setConsulta(Consulta consulta) {
        this.consulta = consulta;
    }
}
*/