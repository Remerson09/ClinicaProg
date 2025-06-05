package aula2603.controller;

import aula2603.model.entity.Consulta;
import aula2603.model.entity.Medico;
import aula2603.model.entity.Paciente;
import aula2603.repository.ConsultaRepository;
import aula2603.repository.MedicoRepository;
import aula2603.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/consultas")
public class ConsultaController {

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("consultas", consultaRepository.findAllWithPacienteAndMedico());
        return "consulta/list";
    }

    @GetMapping("/nova")
    public String novoForm(Model model) {
        prepararForm(model, new Consulta());
        return "consulta/form";
    }

    @GetMapping("/nova/{id}")
    public String novoFormComPaciente(@PathVariable Long id, Model model) {
        Consulta consulta = new Consulta();
        consulta.setPaciente(pacienteRepository.findById(id).orElseThrow());
        prepararForm(model, consulta);
        return "consulta/form";
    }

    private void prepararForm(Model model, Consulta consulta) {
        model.addAttribute("consulta", consulta);
        model.addAttribute("pacientes", pacienteRepository.findAll());
        model.addAttribute("medicos", medicoRepository.findAllByOrderByNomeAsc());
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Consulta consulta,
                         BindingResult result,
                         RedirectAttributes redirectAttributes,
                         Model model) { // Adicionado Model como parâmetro

        if (result.hasErrors()) {
            prepararForm(model, consulta); // Usando o model real agora
            return "consulta/form";
        }

        try {
            if (consulta.getPaciente() == null || consulta.getPaciente().getId() == null) {
                throw new IllegalArgumentException("Paciente não informado");
            }

            if (consulta.getMedico() == null || consulta.getMedico().getId() == null) {
                throw new IllegalArgumentException("Médico não selecionado");
            }

            // Corrigido para usar getData() em vez de getDataConsulta()
            if (consulta.getData() == null) {
                throw new IllegalArgumentException("Data e hora da consulta não informadas");
            }

            consultaRepository.save(consulta);
            redirectAttributes.addFlashAttribute("success", "Consulta agendada com sucesso");
            return "redirect:/pacientes/consultas/" + consulta.getPaciente().getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao agendar consulta: " + e.getMessage());
            return "redirect:/consultas/nova/" + (consulta.getPaciente() != null ? consulta.getPaciente().getId() : "");
        }
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Consulta consulta = consultaRepository.findByIdWithPacienteAndMedico(id)
                .orElseThrow(() -> new IllegalArgumentException("Consulta não encontrada"));
        prepararForm(model, consulta);
        return "consulta/editar";
    }

    @PostMapping("/atualizar/{id}")
    public String atualizar(@PathVariable Long id, @ModelAttribute Consulta consulta,
                            BindingResult result, Model model) {
        if (result.hasErrors()) {
            prepararForm(model, consulta);
            return "consulta/editar";
        }

        consulta.setId(id);
        consultaRepository.save(consulta);
        return "redirect:/pacientes/consultas/" + consulta.getPaciente().getId();
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Consulta consulta = consultaRepository.findByIdWithPacienteAndMedico(id)
                    .orElseThrow(() -> new IllegalArgumentException("Consulta não encontrada"));
            Long pacienteId = consulta.getPaciente().getId();
            consultaRepository.delete(consulta);
            redirectAttributes.addFlashAttribute("success", "Consulta excluída com sucesso");
            return "redirect:/pacientes/consultas/" + pacienteId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao excluir consulta: " + e.getMessage());
            return "redirect:/consultas";
        }
    }

    @GetMapping("/buscar-por-data")
    public String buscarPorData(@RequestParam("data")
                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                LocalDate data, Model model) {
        List<Consulta> consultas = consultaRepository.findByData(data); // Atualizado para findByData
        model.addAttribute("consultas", consultas);
        model.addAttribute("dataSelecionada", data);
        return "consulta/list";
    }
}