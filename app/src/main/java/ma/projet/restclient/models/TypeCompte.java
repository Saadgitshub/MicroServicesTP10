package ma.projet.restclient.models;

import com.google.gson.annotations.SerializedName;

public enum TypeCompte {
    @SerializedName("COURANT")
    COURANT,

    @SerializedName("EPARGNE")
    EPARGNE
}

