package com.example.projetpfe.entity;

public enum PriseContact {
    CONTACTE("Contacté"),
    ABSENT("Absent"),
    PREMIER_RELANCE("1er relance après appel non fructueux"),
    DEUXIEME_RELANCE("2ème relance après appel non fructueux"),
    REFUS_DISCUSSION("Refus catégorique de discuter");

    private final String displayName;

    PriseContact(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
