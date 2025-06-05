/*package aula2603.controller;

import aula2603.model.entity.*;

import aula2603.repository.AgendaRepository;
import aula2603.repository.ConsultaRepository;
import aula2603.repository.MedicoRepository;
import aula2603.repository.PacienteRepository;

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

    // a. Cadastrar disponibilidade na agenda (Formulário)
    @GetMapping("/nova")
    public String novaDisponibilidadeForm(Model model) {
        model.addAttribute("agenda", new Agenda());
        model.addAttribute("medicos", medicoRepository.findAll()); // Precisa da lista de médicos
        return "agenda/form-disponibilidade";
    }

    // a. Cadastrar disponibilidade na agenda (Salvar)
    @PostMapping("/salvar")
    public String salvarDisponibilidade(@Valid @ModelAttribute("agenda") Agenda agenda,
                                        BindingResult result,
                                        RedirectAttributes redirectAttributes,
                                        Model model) {

        // Validação adicional: Verificar conflito de horário para o mesmo médico
        List<Agenda> conflitos = agendaRepository.findByMedicoAndDataHoraInicioBetween(
                agenda.getMedico(),
                agenda.getDataHoraInicio().minusMinutes(59), // Assume duração de 1h, ajustar se necessário
                agenda.getDataHoraInicio().plusMinutes(59)  // Assume duração de 1h, ajustar se necessário
        );
        if (!conflitos.isEmpty()) {
            result.rejectValue("dataHoraInicio", "error.agenda", "Já existe um agendamento ou disponibilidade neste horário para o médico selecionado.");
        }

        if (result.hasErrors()) {
            model.addAttribute("agenda", agenda);
            model.addAttribute("medicos", medicoRepository.findAll());
            return "agenda/form-disponibilidade";
        }

        try {
            agenda.setStatus(AgendaStatus.DISPONIVEL);
            agenda.setConsulta(null); // Garante que não há consulta associada
            agendaRepository.save(agenda);
            redirectAttributes.addFlashAttribute("success", "Disponibilidade cadastrada com sucesso!");
            return "redirect:/agendas/disponiveis"; // Redireciona para a lista de disponíveis
        } catch (Exception e) {
            model.addAttribute("agenda", agenda);
            model.addAttribute("medicos", medicoRepository.findAll());
            model.addAttribute("error", "Erro ao cadastrar disponibilidade: " + e.getMessage());
            return "agenda/form-disponibilidade";
        }
    }

    // b. Visualizar horários disponíveis
    @GetMapping("/disponiveis")
    public String visualizarDisponiveis(Model model) {
        // Busca agendas disponíveis a partir de agora, com médico carregado
        List<Agenda> disponiveis = agendaRepository.findDisponiveisComMedico(AgendaStatus.DISPONIVEL, LocalDateTime.now());
        model.addAttribute("agendasDisponiveis", disponiveis);
        return "agenda/list-disponiveis";
    }

    // c. Agendar uma consulta (Formulário)
    @GetMapping("/agendar/{agendaId}")
    public String agendarConsultaForm(@PathVariable Long agendaId, Model model) {
        Agenda agenda = agendaRepository.findById(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("Agenda não encontrada: " + agendaId));

        if (agenda.getStatus() != AgendaStatus.DISPONIVEL) {
            // Idealmente, tratar isso com uma mensagem de erro mais amigável
            throw new IllegalStateException("Este horário não está mais disponível para agendamento.");
        }

        Consulta consulta = new Consulta();
        consulta.setAgenda(agenda); // Pré-associa a agenda

        model.addAttribute("consulta", consulta);
        model.addAttribute("pacientes", pacienteRepository.findAll()); // Precisa da lista de pacientes
        model.addAttribute("agenda", agenda); // Passa a agenda para exibir detalhes
        return "agenda/form-agendamento";
    }

    // c. Agendar uma consulta (Salvar)
    @PostMapping("/agendar")
    public String processarAgendamento(@Valid @ModelAttribute("consulta") Consulta consulta,
                                       BindingResult result,
                                       @RequestParam("agendaId") Long agendaId, // Pega o ID da agenda do form
                                       RedirectAttributes redirectAttributes,
                                       Model model) {

        Agenda agenda = agendaRepository.findById(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("Agenda não encontrada: " + agendaId));

        // Re-valida se a agenda ainda está disponível
        if (agenda.getStatus() != AgendaStatus.DISPONIVEL) {
            redirectAttributes.addFlashAttribute("error", "Este horário não está mais disponível.");
            return "redirect:/agendas/disponiveis";
        }

        consulta.setAgenda(agenda); // Garante a associação correta

        if (result.hasErrors()) {
            model.addAttribute("consulta", consulta);
            model.addAttribute("pacientes", pacienteRepository.findAll());
            model.addAttribute("agenda", agenda);
            return "agenda/form-agendamento";
        }

        try {
            // Atualiza o status da Agenda
            agenda.setStatus(AgendaStatus.AGENDADO);
            agenda.setConsulta(consulta); // Associa a consulta à agenda

            // Salva a Consulta (que por cascade deve salvar/atualizar a Agenda também se configurado)
            // Ou salva ambos explicitamente em uma transação
            consultaRepository.save(consulta); // Salva a consulta primeiro
            // agendaRepository.save(agenda); // Se o cascade não estiver configurado ou para garantir

            redirectAttributes.addFlashAttribute("success", "Consulta agendada com sucesso!");
            // Redirecionar para uma página de confirmação ou lista de consultas do paciente/médico
            return "redirect:/consultas"; // Ajustar o redirect conforme necessário

        } catch (Exception e) {
            model.addAttribute("consulta", consulta);
            model.addAttribute("pacientes", pacienteRepository.findAll());
            model.addAttribute("agenda", agenda);
            model.addAttribute("error", "Erro ao agendar consulta: " + e.getMessage());
            return "agenda/form-agendamento";
        }
    }

    // d. Cancelar horário/consulta
    @GetMapping("/cancelar/{agendaId}")
    public String cancelarHorario(@PathVariable Long agendaId, RedirectAttributes redirectAttributes) {
        Agenda agenda = agendaRepository.findById(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("Agenda não encontrada: " + agendaId));

        try {
            if (agenda.getStatus() == AgendaStatus.AGENDADO) {
                // Se estava agendado, precisa remover a consulta associada
                Consulta consultaAssociada = agenda.getConsulta();
                if (consultaAssociada != null) {
                    agenda.setConsulta(null); // Remove a associação
                    consultaRepository.delete(consultaAssociada); // Exclui a consulta
                }
            }

            // Atualiza o status da agenda para CANCELADO
            agenda.setStatus(AgendaStatus.CANCELADO);
            agendaRepository.save(agenda);

            redirectAttributes.addFlashAttribute("success", "Horário/Consulta cancelado com sucesso!");
            // Redirecionar para onde for mais apropriado (lista de agendas, etc.)
            return "redirect:/agendas/disponiveis"; // Ou outra página relevante

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao cancelar horário: " + e.getMessage());
            return "redirect:/agendas/disponiveis"; // Ou outra página relevante
        }
    }

    // TODO: Adicionar listagem geral de agendas (opcional)
    // TODO: Adicionar edição de disponibilidade (opcional)
}*/
