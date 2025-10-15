package com.bolaneradar.backend.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

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
     * Bankens namn (Exempelvis: Swedbank)
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Bankens webbplats (Exempelvis: https://www.swedbank.se)
     */
    private String website;

    /**
     * En bank kan ha många räntor kopplade till sig
     * Motsvarar fältet 'bank' i MortgageRate.
     *
     * mappedBy = "bank" betyder att MortgageRate äger relationen (Det är där foreignkey finns)
     * cascade = All betyder om du sparar en bank med nya räntor så sparas de automatiskt
     * orphanRemoval = true betyder om du tar bort en ränta från lsitan så tas den även bort ur databasen
     */
    @OneToMany(mappedBy = "bank", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonManagedReference
    private List<MortgageRate> mortgageRates = new ArrayList<>();

    public Bank() {}

    public Bank(String name, String website) {
        this.name = name;
        this.website = website;
    }

    // Getters & setters
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public List<MortgageRate> getMortgageRates() { return mortgageRates; }
    public void setMortgageRates(List<MortgageRate> mortgageRates) { this.mortgageRates = mortgageRates; }

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
