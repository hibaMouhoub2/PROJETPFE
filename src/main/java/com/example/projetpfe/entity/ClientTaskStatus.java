package com.example.projetpfe.entity;

public enum ClientTaskStatus {
    PENDING("En attente"),
    TO_CALLBACK("À rappeler"),
    COMPLETED("Traité"),
    CANCELLED("Annulé");

    private final String displayName;

    ClientTaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
