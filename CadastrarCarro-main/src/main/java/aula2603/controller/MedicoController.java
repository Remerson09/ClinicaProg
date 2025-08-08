package aula2603.controller;

import aula2603.model.entity.*;
import aula2603.repository.AgendaRepository;
import aula2603.repository.ConsultaRepository;
import aula2603.repository.MedicoRepository;
import aula2603.repository.PacienteRepository; // PacienteRepository pode ser necessário no futuro
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


@Controller
@RequestMapping("/medicos" )
public class
MedicoController {
    @Autowired
    private AgendaRepository agendaRepository;
    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("medicos", medicoRepository.findAll());
        return "medico/list";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("medico", new Medico());
        return "medico/form";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("medico") Medico medico,
                         BindingResult result,
                         RedirectAttributes redirectAttributes,
                         Model model) {

        // Validação adicional: verificar se CRM já existe
        if (medico.getCrm() != null && !medico.getCrm().trim().isEmpty()) {
            Medico existente = medicoRepository.findByCrm(medico.getCrm().trim().toUpperCase());
            if (existente != null) {
                result.rejectValue("crm", "error.medico.crm.exists", "Este CRM já está cadastrado no sistema");
            }
        }

        if (result.hasErrors()) {
            return "medico/form";
        }

        try {
            if (medico.getId() != null) {
                result.rejectValue("id", "medico.id.notnull", "ID deve ser nulo para novo médico");
                return "medico/form";
            }

            // Formatar CRM antes de salvar
            if (medico.getCrm() != null) {
                medico.setCrm(medico.getCrm().trim().toUpperCase());
            }

            medicoRepository.save(medico);
            redirectAttributes.addFlashAttribute("success", "Médico cadastrado com sucesso!");
            return "redirect:/medicos";
        } catch (Exception e) {
            model.addAttribute("error", "Erro inesperado ao cadastrar: " + e.getMessage());
            return "medico/form";
        }
    }

    @GetMapping("/editar/{id}")
    public String editarForm(@PathVariable Long id, Model model) {
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Médico não encontrado: " + id));
        model.addAttribute("medico", medico);
        return "medico/form";
    }

    @PostMapping("/atualizar/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("medico") Medico medicoAtualizado,
                            BindingResult result,
                            RedirectAttributes redirectAttributes,
                            Model model) {

        // Validação adicional: verificar se CRM já existe (exceto para o próprio médico)
        if (medicoAtualizado.getCrm() != null && !medicoAtualizado.getCrm().trim().isEmpty()) {
            Medico existente = medicoRepository.findByCrm(medicoAtualizado.getCrm().trim().toUpperCase());
            if (existente != null && !existente.getId().equals(id)) {
                result.rejectValue("crm", "error.medico.crm.exists", "Este CRM já está cadastrado no sistema");
            }
        }

        if (result.hasErrors()) {
            medicoAtualizado.setId(id);
            return "medico/form";
        }

        try {
            if (!medicoRepository.existsById(id)) {
                throw new IllegalArgumentException("Médico não encontrado para atualização: " + id);
            }

            medicoAtualizado.setId(id);

            // Formatar CRM antes de salvar
            if (medicoAtualizado.getCrm() != null) {
                medicoAtualizado.setCrm(medicoAtualizado.getCrm().trim().toUpperCase());
            }

            medicoRepository.save(medicoAtualizado);
            redirectAttributes.addFlashAttribute("success", "Médico atualizado com sucesso!");
            return "redirect:/medicos";
        } catch (Exception e) {
            medicoAtualizado.setId(id);
            model.addAttribute("error", "Erro inesperado ao atualizar: " + e.getMessage());
            return "medico/form";
        }
    }

    @GetMapping("/consultas/{id}")
    public String consultasMedico(@PathVariable Long id, Model model) {
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Médico não encontrado: " + id));

        List<Consulta> consultas = consultaRepository.findByMedicoIdWithPaciente(id);

        model.addAttribute("medico", medico);
        model.addAttribute("consultas", consultas);
        return "medico/consulta";
    }

    @GetMapping("/excluir/{id}")
    public String excluirMedico(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (!medicoRepository.existsById(id)) {
                throw new IllegalArgumentException("Médico não encontrado para exclusão: " + id);
            }

            // Verificar se médico possui consultas ou agendas
            long consultasCount = consultaRepository.countByMedicoId(id);
            if (consultasCount > 0) {
                redirectAttributes.addFlashAttribute("error",
                        "Não é possível excluir o médico pois ele possui consultas cadastradas.");
                return "redirect:/medicos";
            }

            medicoRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Médico excluído com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao excluir médico: " + e.getMessage());
        }
        return "redirect:/medicos";
    }

    @GetMapping("/buscar")
    public String buscarMedicos(@RequestParam(required = false) String nome,
                                @RequestParam(required = false) String crm,
                                Model model) {
        List<Medico> medicos;

        if ((nome == null || nome.isBlank()) && (crm == null || crm.isBlank())) {
            medicos = medicoRepository.findAll();
        } else if (crm != null && !crm.isBlank()) {
            // Busca por CRM (exato ou parcial)
            String crmFormatted = crm.trim().toUpperCase();
            if (!crmFormatted.startsWith("CRM-")) {
                crmFormatted = "CRM-" + crmFormatted;
            }
            medicos = medicoRepository.findByCrmContainingIgnoreCase(crmFormatted);
        } else {
            // Busca por nome
            medicos = medicoRepository.findByNomeContainingIgnoreCase(nome.trim());
        }

        model.addAttribute("medicos", medicos);
        model.addAttribute("nome", nome);
        model.addAttribute("crm", crm);
        return "medico/list";
    }

    // Endpoint para validar CRM via AJAX (opcional)
    @GetMapping("/validar-crm")
    @ResponseBody
    public String validarCrm(@RequestParam String crm, @RequestParam(required = false) Long id) {
        if (crm == null || crm.trim().isEmpty()) {
            return "CRM é obrigatório";
        }

        String crmFormatted = crm.trim().toUpperCase();

        // Validar formato
        if (!crmFormatted.matches("^CRM-\\d{3}$")) {
            return "Formato inválido. Use: CRM-XXX (exemplo: CRM-123)";
        }

        // Verificar se já existe
        Medico existente = medicoRepository.findByCrm(crmFormatted);
        if (existente != null && (id == null || !existente.getId().equals(id))) {
            return "Este CRM já está cadastrado";
        }

        return "OK";
    }

    // ***** MÉTODO ADICIONADO E CORRIGIDO *****
    @GetMapping("/{id}/agenda")
    public String verAgendaDoMedico(@PathVariable Long id, Model model) {
        // 1. Busca o médico no banco de dados ou lança um erro 404 se não encontrar
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Médico não encontrado: " + id));

        // 2. Busca todos os horários (passados e futuros) associados a esse médico
        //    O método findByMedico já existe no seu AgendaRepository por padrão (JpaRepository)
        List<Agenda> agendaDoMedico = agendaRepository.findByMedico(medico);

        // 3. Adiciona o médico e a lista de horários ao modelo para a view
        model.addAttribute("medico", medico);
        model.addAttribute("agendaDoMedico", agendaDoMedico);

        // 4. Retorna o nome do arquivo HTML que vai exibir as informações
        return "medico/agenda"; // Caminho: /resources/templates/medico/agenda.html
    }

    @GetMapping("/agendar/{idMedico}")
    public String mostrarAgendaParaAgendamento(@PathVariable Long idMedico,
                                               @RequestParam(required = false) String dataAgendamento,
                                               Model model) {
        Medico medico = medicoRepository.findById(idMedico)
                .orElseThrow(() -> new IllegalArgumentException("Médico não encontrado"));

        LocalDate data;
        if (dataAgendamento != null && !dataAgendamento.isBlank()) {
            data = LocalDate.parse(dataAgendamento);
        } else {
            data = LocalDate.now(); // data padrão: hoje
        }

        LocalTime start = LocalTime.of(8, 30);
        LocalTime end = LocalTime.of(11, 30);

        List<LocalTime> horariosPossiveis = new ArrayList<>();
        for (LocalTime time = start; time.isBefore(end); time = time.plusMinutes(30)) {
            horariosPossiveis.add(time);
        }

        List<Agenda> agendasOcupadas = agendaRepository.findByMedicoAndDataHoraInicioBetween(
                medico,
                LocalDateTime.of(data, start),
                LocalDateTime.of(data, end)
        );

        List<LocalTime> horariosDisponiveis = horariosPossiveis.stream()
                .filter(horario -> agendasOcupadas.stream()
                        .noneMatch(a -> a.getDataHoraInicio().toLocalTime().equals(horario)))
                .toList();

        model.addAttribute("medico", medico);
        model.addAttribute("horariosDisponiveis", horariosDisponiveis);
        model.addAttribute("dataAgendamento", data);
        model.addAttribute("horaInicio", start);
        model.addAttribute("horaFim", end);

        return "agendamento/form";
    }
}
