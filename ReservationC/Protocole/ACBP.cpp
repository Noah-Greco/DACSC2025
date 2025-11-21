#include "ACBP.hpp"
#include "CBP.hpp"

#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#ifndef TAILLE_MAX_DATA
#define TAILLE_MAX_DATA 10000
#endif

bool ACBP(char * requete, char * reponse)
{
    char copie[TAILLE_MAX_DATA];
    strncpy(copie, requete, sizeof(copie) - 1);
    copie[sizeof(copie) - 1] = 0;

    char *tag = strtok(copie, "#");
    if (!tag || strcmp(tag, "ACBP") != 0)
    {
        snprintf(reponse, TAILLE_MAX_DATA, "ACBP_BAD_REQUEST");
        return false;
    }

    char *cmd = strtok(NULL, "#");
    if (!cmd)
    {
        snprintf(reponse, TAILLE_MAX_DATA, "ACBP_BAD_REQUEST");
        return false;
    }

    // ===== ALL_CLIENT =====
    if (strcmp(cmd, "ALL_CLIENT") == 0)
    {
        char *res = CBP_All_Client();  // "ip;nom;prenom;no#..."

        if (res == NULL)
        {
            // Vraie erreur interne
            snprintf(reponse, TAILLE_MAX_DATA, "ALL_CLIENT#ko#internal_error");
            return false;
        }

        // Même si res est vide, on considère que c’est "ok" : juste aucun client.
        snprintf(reponse, TAILLE_MAX_DATA, "ALL_CLIENT#ok#%s", res);
        free(res);
        return false; // on garde la connexion admin ouverte
    }

    // Commande inconnue
    snprintf(reponse, TAILLE_MAX_DATA, "ACBP#ko#unknown_command");
    return false;
}
