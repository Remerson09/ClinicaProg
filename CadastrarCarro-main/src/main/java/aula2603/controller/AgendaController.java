package aula2603.controller;

import aula2603.model.entity.*;
import aula2603.repository.AgendaRepository;
import aula2603.repository.ConsultaRepository;
import aula2603.repository.MedicoRepository;
import aula2603.repository.PacienteRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/agendas")
public class AgendaController {

    @Autowired
    private AgendaRepository agendaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    // -------------------------------
    // FORMULÁRIO E SALVAR DISPONIBILIDADE
    // -------------------------------

    @GetMapping("/nova")
    public String novaDisponibilidadeForm(Model model) {
        List<Medico> medicosAtivos = medicoRepository.findByStatus(StatusMedico.ATIVO);
        model.addAttribute("agenda", new Agenda());
        model.addAttribute("medicos", medicosAtivos);
        return "agenda/form-disponibilidade";
    }

    @PostMapping("/salvar")
    public String salvarDisponibilidade(@Valid @ModelAttribute("agenda") Agenda agenda,
                                        BindingResult result,
                                        RedirectAttributes redirectAttributes,
                                        Model model) {
        if (agenda.getDataHoraFim().isBefore(agenda.getDataHoraInicio())) {
            result.rejectValue("dataHoraFim", "error.agenda",
                    "A data/hora final deve ser após a data/hora inicial");
        }
        List<Agenda> conflitos = agendaRepository.findConflitosDeHorario(
                agenda.getMedico(),
                agenda.getDataHoraInicio(),
                agenda.getDataHoraFim()
        );
        if (!conflitos.isEmpty()) {
            result.rejectValue("dataHoraInicio", "error.agenda",
                    "Conflito de horário com agendamentos existentes");
        }
        if (result.hasErrors()) {
            model.addAttribute("agenda", agenda);
            model.addAttribute("medicos", medicoRepository.findAll());
            return "agenda/form-disponibilidade";
        }
        try {
            agenda.setStatus(AgendaStatus.DISPONIVEL);
            agenda.setConsulta(null);
            agendaRepository.save(agenda);
            redirectAttributes.addFlashAttribute("success", "Disponibilidade cadastrada com sucesso!");
            return "redirect:/agendas/disponiveis";
        } catch (Exception e) {
            model.addAttribute("agenda", agenda);
            model.addAttribute("medicos", medicoRepository.findAll());
            model.addAttribute("error", "Erro ao cadastrar disponibilidade: " + e.getMessage());
            return "agenda/form-disponibilidade";
        }
    }

    // -------------------------------
    // LISTAR DISPONÍVEIS
    // -------------------------------

    @GetMapping("/disponiveis")
    public String visualizarDisponiveis(Model model) {
        List<Agenda> disponiveis = agendaRepository.findDisponiveisComMedicoAtivo(
                AgendaStatus.DISPONIVEL, LocalDateTime.now());
        model.addAttribute("agendasDisponiveis", disponiveis);
        return "agenda/list-disponiveis";
    }

    // -------------------------------
    // AGENDAMENTO DE CONSULTA
    // -------------------------------

    // GET: Mostrar o formulário para agendar (passa agendaId)
    @GetMapping("/agendar/{agendaId}")
    public String mostrarFormularioDeAgendamento(@PathVariable Long agendaId, Model model, RedirectAttributes redirectAttributes) {
        Agenda agenda = agendaRepository.findById(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("Agenda não encontrada: " + agendaId));

        if (agenda.getStatus() != AgendaStatus.DISPONIVEL) {
            redirectAttributes.addFlashAttribute("error", "Este horário não está mais disponível.");
            return "redirect:/agendas/disponiveis";
        }

        List<Paciente> todosOsPacientes = pacienteRepository.findAll();
        if (todosOsPacientes.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "ERRO: Não há pacientes cadastrados. Cadastre um paciente antes de agendar.");
            return "redirect:/agendas/disponiveis";
        }

        Consulta consulta = new Consulta();
        consulta.setAgenda(agenda);

        model.addAttribute("consulta", consulta);
        model.addAttribute("pacientes", todosOsPacientes);
        model.addAttribute("agenda", agenda);
        return "agenda/form-agendamento";
    }

    // POST: Processar o agendamento (formulário de agendamento)
    @PostMapping("/agendar")
    public String processarAgendamento(@Valid @ModelAttribute("consulta") Consulta consulta,
                                       BindingResult result,
                                       @RequestParam("agendaId") Long agendaId,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {

        Agenda agenda = agendaRepository.findById(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("Agenda não encontrada: " + agendaId));

        if (agenda.getStatus() != AgendaStatus.DISPONIVEL) {
            redirectAttributes.addFlashAttribute("error", "Este horário não está mais disponível.");
            return "redirect:/agendas/disponiveis";
        }

        consulta.setAgenda(agenda);
        consulta.setMedico(agenda.getMedico());
        consulta.setData(agenda.getDataHoraInicio());

        if (result.hasErrors()) {
            model.addAttribute("consulta", consulta);
            model.addAttribute("pacientes", pacienteRepository.findAll());
            model.addAttribute("agenda", agenda);
            return "agenda/form-agendamento";
        }

        try {
            agenda.setStatus(AgendaStatus.AGENDADO);
            agenda.setConsulta(consulta);

            consultaRepository.save(consulta);
            agendaRepository.save(agenda);

            redirectAttributes.addFlashAttribute("success", "Sua consulta foi agendada com sucesso!");

            // Redireciona para agenda do médico após agendar
            return "redirect:/medicos/" + agenda.getMedico().getId() + "/agenda";

        } catch (Exception e) {
            model.addAttribute("consulta", consulta);
            model.addAttribute("pacientes", pacienteRepository.findAll());
            model.addAttribute("agenda", agenda);
            model.addAttribute("error", "Erro inesperado ao agendar: " + e.getMessage());
            return "agenda/form-agendamento";
        }
    }

    // -------------------------------
    // CANCELAR AGENDAMENTO OU DISPONIBILIDADE
    // -------------------------------

    @GetMapping("/cancelar/{agendaId}")
    public String cancelarHorario(@PathVariable Long agendaId, RedirectAttributes redirectAttributes) {
        Agenda agenda = agendaRepository.findById(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("Agenda não encontrada: " + agendaId));
        try {
            String nomePaciente = null;

            if (agenda.getStatus() == AgendaStatus.AGENDADO) {
                Consulta consultaAssociada = agenda.getConsulta();
                if (consultaAssociada != null) {
                    if (consultaAssociada.getPaciente() != null) {
                        nomePaciente = consultaAssociada.getPaciente().getNome();
                    }
                    agenda.setConsulta(null);
                    consultaRepository.delete(consultaAssociada);
                }
            }
            agenda.setStatus(AgendaStatus.CANCELADO);
            agendaRepository.save(agenda);

            if (nomePaciente != null) {
                redirectAttributes.addFlashAttribute("success",
                        "Consulta do paciente " + nomePaciente + " cancelada com sucesso!");
            } else {
                redirectAttributes.addFlashAttribute("success", "Horário/Consulta cancelado com sucesso!");
            }

            return "redirect:/agendas/disponiveis";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao cancelar horário: " + e.getMessage());
            return "redirect:/agendas/disponiveis";
        }
    }


    // -------------------------------
    // LISTAR TODAS AGENDAS
    // -------------------------------

    @GetMapping
    public String listarAgendas(Model model) {
        model.addAttribute("agendas", agendaRepository.findAll());
        return "agenda/list";
    }

    // -------------------------------
    // EDITAR DISPONIBILIDADE
    // -------------------------------

    @GetMapping("/editar/{id}")
    public String editarDisponibilidade(@PathVariable Long id, Model model) {
        Agenda agenda = agendaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Agenda não encontrada: " + id));
        if (agenda.getStatus() != AgendaStatus.DISPONIVEL) {
            throw new IllegalStateException("Só é possível editar agendas com status DISPONIVEL.");
        }
        model.addAttribute("agenda", agenda);
        model.addAttribute("medicos", medicoRepository.findAll());
        return "agenda/form-disponibilidade";
    }

    @PostMapping("/atualizar/{id}")
    public String atualizarDisponibilidade(@PathVariable Long id,
                                           @Valid @ModelAttribute("agenda") Agenda agendaAtualizada,
                                           BindingResult result,
                                           RedirectAttributes redirectAttributes,
                                           Model model) {
        if (result.hasErrors()) {
            agendaAtualizada.setId(id);
            model.addAttribute("agenda", agendaAtualizada);
            model.addAttribute("medicos", medicoRepository.findAll());
            return "agenda/form-disponibilidade";
        }
        try {
            if (!agendaRepository.existsById(id)) {
                throw new IllegalArgumentException("Agenda não encontrada para atualização: " + id);
            }
            agendaAtualizada.setId(id);
            agendaAtualizada.setStatus(AgendaStatus.DISPONIVEL);
            agendaAtualizada.setConsulta(null);
            agendaRepository.save(agendaAtualizada);
            redirectAttributes.addFlashAttribute("success", "Disponibilidade atualizada com sucesso!");
            return "redirect:/agendas/disponiveis";
        } catch (Exception e) {
            model.addAttribute("agenda", agendaAtualizada);
            model.addAttribute("medicos", medicoRepository.findAll());
            model.addAttribute("error", "Erro ao atualizar disponibilidade: " + e.getMessage());
            return "agenda/form-disponibilidade";
        }
    }

    // -------------------------------
    // LISTAR AGENDA DE UM MÉDICO (COM CONSULTAS)
    // -------------------------------

    @GetMapping("/medico/{id}")
    public String listarAgendasDoMedico(@PathVariable Long id, Model model) {
        List<Agenda> agendas = agendaRepository.buscarComConsultasEPacientes(id);
        model.addAttribute("agendas", agendas);
        return "agenda/list-medico";
    }

}
