package ma.projet.restclient.models;

import com.google.gson.annotations.SerializedName;

public class Compte {
    @SerializedName("id")
    private Long id;

    @SerializedName("solde")
    private double solde;

    @SerializedName("dateCreation")
    private String dateCreation;

    @SerializedName("type")
    private TypeCompte type;

    // Constructeur par défaut
    public Compte() {
    }

    // Constructeur complet
    public Compte(Long id, double solde, String dateCreation, TypeCompte type) {
        this.id = id;
        this.solde = solde;
        this.dateCreation = dateCreation;
        this.type = type;
    }

    // Constructeur sans ID (pour création)
    public Compte(double solde, String dateCreation, TypeCompte type) {
        this.solde = solde;
        this.dateCreation = dateCreation;
        this.type = type;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getSolde() {
        return solde;
    }

    public void setSolde(double solde) {
        this.solde = solde;
    }

    public String getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(String dateCreation) {
        this.dateCreation = dateCreation;
    }

    public TypeCompte getType() {
        return type;
    }

    public void setType(TypeCompte type) {
        this.type = type;
    }
}

