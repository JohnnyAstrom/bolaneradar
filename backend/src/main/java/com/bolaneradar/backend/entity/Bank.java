package com.bolaneradar.backend.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

/**
 * Representerar en bank i systemet.
 * Varje bank kan ha flera olika bolåneräntor.
 */
@Entity
@Table(name = "banks")
public class Bank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Bankens namn
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Bankens webbplats
     */
    private String website;

    /**
     * En bank kan ha många räntor kopplade till sig.
     * Motsvarar fältet 'bank' i MortgageRate.
     * <p>
     * mappedBy = "bank" → MortgageRate äger relationen (foreign key finns där).
     * cascade = ALL → sparar eller tar bort räntor automatiskt med banken.
     * orphanRemoval = true → tar bort räntor som inte längre hör till någon bank.
     */
    @OneToMany(mappedBy = "bank", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<MortgageRate> mortgageRates = new ArrayList<>();

    /**
     * Standardkonstruktor krävs av JPA.
     * Initierar listan för att undvika NullPointerExceptions.
     */
    public Bank() {
        this.mortgageRates = new ArrayList<>();
    }

    /**
     * Skapar en ny bank med namn och webbplats.
     * Listan med räntor initieras automatiskt.
     */
    public Bank(String name, String website) {
        this.name = name;
        this.website = website;
        this.mortgageRates = new ArrayList<>();
    }

    // --- Getters & setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public List<MortgageRate> getMortgageRates() { return mortgageRates; }

    /**
     * Säker setter för räntor.
     * Behåller kopplingar mellan bank och ränta, även vid uppdateringar.
     */
    public void setMortgageRates(List<MortgageRate> mortgageRates) {
        this.mortgageRates.clear();
        if (mortgageRates != null) {
            mortgageRates.forEach(this::addMortgageRate);
        }
    }

    /**
     * Hjälpmetod för att lägga till en ränta till banken.
     * Sätter även bank-fältet på räntan så relationen hålls synkad.
     */
    public void addMortgageRate(MortgageRate rate) {
        mortgageRates.add(rate);
        rate.setBank(this);
    }

    /**
     * Hjälpmetod för att ta bort en ränta från banken.
     * Tar bort kopplingen åt båda håll.
     */
    public void removeMortgageRate(MortgageRate rate) {
        mortgageRates.remove(rate);
        rate.setBank(null);
    }
}