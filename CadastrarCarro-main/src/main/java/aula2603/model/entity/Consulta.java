package aula2603.model.entity;

import aula2603.model.entity.Medico;
import aula2603.model.entity.Paciente;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Entity
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "A data e hora da consulta não podem estar em branco")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "data_consulta")
    private LocalDateTime data;

    @Min(value = 0, message = "O valor da consulta não pode ser negativo")
    private double valor;

    private String observacao;

    @NotNull(message = "O paciente deve ser selecionado")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @NotNull(message = "O médico deve ser selecionado")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id")
    private Medico medico;

    // Relacionamento com Agenda
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agenda_id")
    private Agenda agenda;

    // Construtores
    public Consulta() {}

    public Consulta(LocalDateTime data, double valor, String observacao, Paciente paciente, Medico medico) {
        this.data = data;
        this.valor = valor;
        this.observacao = observacao;
        this.paciente = paciente;
        this.medico = medico;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Medico getMedico() {
        return medico;
    }

    public void setMedico(Medico medico) {
        this.medico = medico;
    }

    public Agenda getAgenda() {
        return agenda;
    }

    public void setAgenda(Agenda agenda) {
        this.agenda = agenda;
    }

    // Métodos auxiliares
    public String dados() {
        return "Consulta em " + data +
                " | Valor: R$" + valor +
                " | Paciente: " + (paciente != null ? paciente.getNome() : "N/A") +
                " | Médico: " + (medico != null ? medico.getNome() : "N/A");
    }

    @Override
    public String toString() {
        return "Consulta{" +
                "id=" + id +
                ", data=" + data +
                ", valor=" + valor +
                ", paciente=" + (paciente != null ? paciente.getNome() : "null") +
                ", medico=" + (medico != null ? medico.getNome() : "null") +
                '}';
    }
}

