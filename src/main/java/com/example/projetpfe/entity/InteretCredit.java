package com.example.projetpfe.entity;

public enum InteretCredit {
    OUI("Oui"),
    NON("Non"),
    PEUT_ETRE("Peut-être dans le futur");

    private final String displayName;

    InteretCredit(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}