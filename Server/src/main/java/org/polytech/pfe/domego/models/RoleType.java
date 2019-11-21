package org.polytech.pfe.domego.models;

public enum RoleType {
    NON_DEFINI("NONE",0),
    MAITRE_D_OUVRAGE("MANAGER", 1),
    MAITRE_D_OEUVRE("ARCHITECT",2),
    BUREAU_D_ETUDE("OFFICER",3),
    BUREAU_DE_CONTROLE("CONTROLER",4),
    ENTREPRISE_GROS_OEUVRE("",5),
    ENTREPRISE_CORPS_ETAT_SECONDAIRE("SECONDARY",6);

    private String name;
    private int id;

    RoleType(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public static int getNumberOfRole(){
        return RoleType.values().length -1;
    }
}
