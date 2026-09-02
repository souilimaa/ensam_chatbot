package com.ensam.chatbot.service;

import com.ensam.chatbot.dto.ChatResponse;
import com.ensam.chatbot.intent.Intent;
import com.ensam.chatbot.intent.IntentType;
import com.ensam.chatbot.repository.CompanyCount;
import com.ensam.chatbot.repository.ProfileAnalyticsRepository;
import com.ensam.chatbot.repository.ProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ProfileRepository repo;
    private final ProfileAnalyticsRepository analyticsRepo;
    private final IntentDetectionService intentDetectionService;

    public ChatService(
            ProfileRepository repo,
            ProfileAnalyticsRepository analyticsRepo,
            IntentDetectionService intentDetectionService
    ) {
        this.repo = repo;
        this.analyticsRepo = analyticsRepo;
        this.intentDetectionService = intentDetectionService;
    }

    public ChatResponse ask(String question) {
        Intent intent = intentDetectionService.detect(question);

        normalizeIntent(question, intent);

        switch (intent.getIntent() == null ? IntentType.UNKNOWN : intent.getIntent()) {

            case PERSON_PFE -> {
                if (isBlank(intent.getPersonName()))
                    return new ChatResponse("Merci de préciser le nom complet de la personne.", "mongodb");

                var p = repo.findFirstByFullNameIgnoreCase(intent.getPersonName());
                if (p.isEmpty() || p.get().getPfe() == null || p.get().getPfe().getCompany() == null)
                    return new ChatResponse("Je n’ai pas trouvé le PFE de cette personne dans la base.", "mongodb");

                var pfe = p.get().getPfe();
                return new ChatResponse("PFE: " + pfe.getCompany() + " (" + safe(pfe.getLocation()) + ")", "mongodb");
            }

            case PERSON_MAJOR -> {
                if (isBlank(intent.getPersonName()))
                    return new ChatResponse("Merci de préciser le nom complet de la personne.", "mongodb");

                var p = repo.findFirstByFullNameIgnoreCase(intent.getPersonName());
                if (p.isEmpty() || p.get().getMainEducation() == null)
                    return new ChatResponse("Je n’ai pas trouvé la formation ENSAM de cette personne.", "mongodb");

                return new ChatResponse("Major: " + safe(p.get().getMainEducation().getMajorRaw()), "mongodb");
            }

            case PERSON_SKILLS -> {
                if (isBlank(intent.getPersonName()))
                    return new ChatResponse("Merci de préciser le nom complet de la personne.", "mongodb");

                var p = repo.findFirstByFullNameIgnoreCase(intent.getPersonName());
                if (p.isEmpty() || p.get().getSkills() == null || p.get().getSkills().isEmpty())
                    return new ChatResponse("Je n’ai pas trouvé des skills pour cette personne.", "mongodb");

                String skills = p.get().getSkills().stream().limit(15).collect(Collectors.joining(", "));
                return new ChatResponse("Skills: " + skills, "mongodb");
            }

            case PERSON_CURRENT_JOB -> {
                if (isBlank(intent.getPersonName()))
                    return new ChatResponse("Merci de préciser le nom complet de la personne.", "mongodb");

                var p = repo.findFirstByFullNameIgnoreCase(intent.getPersonName());
                if (p.isEmpty() || p.get().getExperiences() == null)
                    return new ChatResponse("Je n’ai pas trouvé l’expérience de cette personne.", "mongodb");

                var current = p.get().getExperiences().stream()
                        .filter(e -> "JOB".equalsIgnoreCase(e.getType()) && Boolean.TRUE.equals(e.getJobStillWorking()))
                        .findFirst()
                        .orElse(null);

                if (current == null) return new ChatResponse("Je n’ai pas trouvé son job actuel.", "mongodb");
                return new ChatResponse("Job actuel: " + safe(current.getTitle()) + " @ " + safe(current.getCompany()), "mongodb");
            }

            case ANALYTICS_TOP -> {
                int topK = boundedTopK(intent.getTopK());
                Integer promo = intent.getPromoYear();
                String majorNorm = intent.getMajorNorm();

                List<String> metrics = intent.getMetrics();
                if (metrics == null || metrics.isEmpty()) {
                    metrics = List.of("CURRENT_EMPLOYERS");
                }

                StringBuilder out = new StringBuilder();

                for (String m : metrics) {
                    List<CompanyCount> rows = switch (m) {
                        case "PFE_COMPANIES" -> analyticsRepo.topPfeCompanies(promo, majorNorm, topK);
                        case "JOB_TITLES" -> analyticsRepo.topCurrentJobTitles(promo, majorNorm, topK);
                        case "SKILLS" -> analyticsRepo.topSkills(promo, majorNorm, topK);
                        case "CURRENT_EMPLOYERS" -> analyticsRepo.topCurrentEmployers(promo, majorNorm, topK);
                        default -> null;
                    };

                    if (rows == null || rows.isEmpty()) continue;

                    String title = switch (m) {
                        case "PFE_COMPANIES" -> "Top PFE companies";
                        case "CURRENT_EMPLOYERS" -> "Top employeurs actuels";
                        case "JOB_TITLES" -> "Top job titles actuels";
                        case "SKILLS" -> "Top skills";
                        default -> "Résultats";
                    };

                    out.append(title)
                            .append(promo != null ? (" (promo " + promo + ")") : "")
                            .append(majorNorm != null ? (" (major " + majorNorm + ")") : "")
                            .append(":\n")
                            .append(formatTop(rows))
                            .append("\n\n");
                }

                if (out.length() == 0) return new ChatResponse("Aucun résultat pour ces filtres.", "mongodb");
                return new ChatResponse(out.toString().trim(), "mongodb");
            }


            default -> {
                return new ChatResponse("Je n’ai pas compris la question (UNKNOWN).", "mongodb");
            }
        }
    }

    private void normalizeIntent(String question, Intent intent) {
        IntentType type = intent.getIntent() == null ? IntentType.UNKNOWN : intent.getIntent();
        boolean noPerson = isBlank(intent.getPersonName());
        String q = (question == null ? "" : question).toLowerCase();

        if (type.name().startsWith("PERSON_") && noPerson) {
            intent.setIntent(IntentType.ANALYTICS_TOP);
            intent.setTopK(intent.getTopK() == null ? 10 : intent.getTopK());

            // set default metrics based on keywords
            if (q.contains("pfe") || q.contains("stage")) {
                intent.setMetrics(List.of("PFE_COMPANIES"));
            } else if (q.contains("skill") || q.contains("skills") || q.contains("competence") || q.contains("compétence")) {
                intent.setMetrics(List.of("SKILLS"));
            } else if (q.contains("title") || q.contains("poste") || q.contains("job title")) {
                intent.setMetrics(List.of("JOB_TITLES"));
            } else {
                intent.setMetrics(List.of("CURRENT_EMPLOYERS"));
            }
        }

        if (intent.getIntent() == IntentType.ANALYTICS_TOP) {
            if (intent.getMetrics() == null || intent.getMetrics().isEmpty()) {

                // If question asks multiple things, you can auto add both:
                boolean askSkills = q.contains("skill") || q.contains("skills") || q.contains("competence") || q.contains("compétence");
                boolean askPfe = q.contains("pfe") || q.contains("stage");

                if (askSkills && askPfe) {
                    intent.setMetrics(List.of("SKILLS", "PFE_COMPANIES"));
                } else if (askPfe) {
                    intent.setMetrics(List.of("PFE_COMPANIES"));
                } else if (askSkills) {
                    intent.setMetrics(List.of("SKILLS"));
                } else if (q.contains("title") || q.contains("poste") || q.contains("job title")) {
                    intent.setMetrics(List.of("JOB_TITLES"));
                } else {
                    intent.setMetrics(List.of("CURRENT_EMPLOYERS"));
                }

                intent.setTopK(intent.getTopK() == null ? 10 : intent.getTopK());
            }
        }
    }


    private String formatTop(List<CompanyCount> rows) {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (CompanyCount r : rows) {
            sb.append(i++).append(") ").append(r.getId()).append(" — ").append(r.getCount()).append("\n");
        }
        return sb.toString().trim();
    }

    private String safe(String s) { return s == null ? "" : s; }
    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private int boundedTopK(Integer topK) {
        return topK == null ? 10 : Math.max(1, Math.min(topK, 50));
    }
}
