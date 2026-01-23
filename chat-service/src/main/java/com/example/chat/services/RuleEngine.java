package com.example.chat.services;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class RuleEngine {
    /**
     * 10 Predefined Customer Support Rules
     * These rules are matched against user messages to provide automated responses
     */
    private final Map<String, String> rules = new HashMap<>();

    public RuleEngine() {
        // =============== 10 PRIMARY RULES ===============
        // Rule 1: Program/Hours
        rules.put("program", "Programul suport este L-V 09:00-18:00.");
        
        // Rule 2: Operating Hours (alternative keyword)
        rules.put("orar", "Suntem disponibili L-V 09:00-18:00.");
        
        // Rule 3: Energy Consumption
        rules.put("consum", "Poti vedea consumul in sectiunea Energy Monitoring.");
        
        // Rule 4: Password Reset
        rules.put("parola", "Pentru resetarea parolei, foloseste optiunea 'Forgot Password'.");
        
        // Rule 5: Invoices
        rules.put("factura", "Facturile se genereaza lunar si pot fi descarcate din sectiunea 'My Invoices'.");
        
        // Rule 6: Alerts
        rules.put("alerta", "Primesti alerte de supraconsum prin notificari in timp real pe platforma.");
        
        // Rule 7: Device Management
        rules.put("device", "Poti adauga dispozitive noi din sectiunea 'My Devices' cu codul dispozitivului.");
        
        // Rule 8: Cost Calculation
        rules.put("cost", "Costul energiei este calculat pe baza tarifelor actuale din contract.");
        
        // Rule 9: Logout
        rules.put("logout", "Pentru a te deconecta, apasa butonul 'Logout' din meniul principal.");
        
        // Rule 10: Profile Management
        rules.put("profil", "Poti actualiza profilul tau din 'Account Settings' inclusiv date de contact si preferinte.");
        
        // =============== ADDITIONAL SUPPORTING RULES (complementary to the 10 main rules) ===============
        rules.put("notificare", "Notificarile in timp real sunt trimise pentru alerte de consum si mesaje importante.");
        rules.put("resetare", "Pentru a reseta parola, acceseaza 'Forgot Password' pe pagina de login.");
        rules.put("monitorizare", "Monitorizarea consumului de energie se realizeaza in timp real pentru fiecare dispozitiv.");
        rules.put("suport", "Pentru probleme tehnice, contacteaza echipa noastra de suport L-V 09:00-18:00.");
    }

    /**
     * Match user message against predefined rules
     * @param message the user's message
     * @return Optional containing the matching rule response
     */
    public Optional<String> matchResponse(String message) {
        if (message == null || message.isBlank()) return Optional.empty();
        String lower = message.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, String> e : rules.entrySet()) {
            if (lower.contains(e.getKey())) {
                return Optional.of(e.getValue());
            }
        }
        return Optional.empty();
    }

    /**
     * Get total number of primary rules
     * @return number of rules
     */
    public int getPrimaryRulesCount() {
        return 10;
    }

    /**
     * Get all rules
     * @return map of rules
     */
    public Map<String, String> getAllRules() {
        return new HashMap<>(rules);
    }
}
