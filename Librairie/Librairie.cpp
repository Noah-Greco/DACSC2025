#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <unistd.h>
#include <signal.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <sys/socket.h>

#include "Librairie.hpp"

#ifndef TAILLE_MAX_DATA
#define TAILLE_MAX_DATA 10000
#endif

// --- ServerSocket ---
// Crée un socket TCP, bind sur 0.0.0.0:port et listen(SOMAXCONN).
int ServerSocket(int port) {
    int s = socket(AF_INET, SOCK_STREAM, 0);
    if (s < 0) {
        return -1;
    }

    // Réutilisation d'adresse pour relancer sans attendre TIME_WAIT
    int opt = 1;
    (void)setsockopt(s, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));

    struct sockaddr_in addr;
    memset(&addr, 0, sizeof(addr));
    addr.sin_family      = AF_INET;
    addr.sin_addr.s_addr = htonl(INADDR_ANY);
    addr.sin_port        = htons((uint16_t)port);

    if (bind(s, (struct sockaddr*)&addr, sizeof(addr)) < 0) {
        close(s);
        return -1;
    }
    if (listen(s, SOMAXCONN) < 0) {
        close(s);
        return -1;
    }
    return s;
}

// --- Accept ---
// Accepte une connexion et écrit l’IP du client dans ipClient si non NULL.
int Accept(int sEcoute, char *ipClient) {
    struct sockaddr_in cli;
    socklen_t len = sizeof(cli);
    int s = accept(sEcoute, (struct sockaddr*)&cli, &len);
    if (s < 0) {
        return -1;
    }
    if (ipClient != NULL) {
        // Espace minimum recommandé : INET_ADDRSTRLEN (16)
        inet_ntop(AF_INET, &cli.sin_addr, ipClient, INET_ADDRSTRLEN);
    }
    return s;
}

// --- ClientSocket ---
// Crée un socket TCP et connecte vers ipServeur:portServeur.
int ClientSocket(char* ipServeur, int portServeur) {
    if (ipServeur == NULL) {
        errno = EINVAL;
        return -1;
    }

    int s = socket(AF_INET, SOCK_STREAM, 0);
    if (s < 0) {
        return -1;
    }

    struct sockaddr_in srv;
    memset(&srv, 0, sizeof(srv));
    srv.sin_family = AF_INET;
    srv.sin_port   = htons((uint16_t)portServeur);

    if (inet_pton(AF_INET, ipServeur, &srv.sin_addr) != 1) {
        close(s);
        errno = EINVAL;
        return -1;
    }

    if (connect(s, (struct sockaddr*)&srv, sizeof(srv)) < 0) {
        close(s);
        return -1;
    }

    return s;
}

// --- Send ---
// Envoie exactement 'taille' octets en bouclant sur send().
// Retourne le nombre total d’octets envoyés ou -1 si échec.
int Send(int sSocket, char* data, int taille) {
    if (data == NULL || taille < 0) {
        errno = EINVAL;
        return -1;
    }

    int total = 0;
    while (total < taille) {
        ssize_t n = send(sSocket, data + total, (size_t)(taille - total), MSG_NOSIGNAL);
        if (n < 0) {
            if (errno == EINTR) continue;   // réessayer si interrompu
            return -1;
        }
        if (n == 0) {
            // Connexion fermée côté pair
            break;
        }
        total += (int)n;
    }
    return total;
}

// --- Receive ---
// Lit au plus TAILLE_MAX_DATA octets en un seul recv().
// Par convention ici, on NUL-terminate si possible pour faciliter l’usage texte.
// Retourne nb d’octets lus (0 si pair fermé), ou -1 si erreur.
int Receive(int sSocket, char* data) {
    if (data == NULL) {
        errno = EINVAL;
        return -1;
    }

    ssize_t n = recv(sSocket, data, (size_t)TAILLE_MAX_DATA, 0);
    if (n < 0) {
        if (errno == EINTR) {
            // Si interrompu, on peut relancer une seule fois
            n = recv(sSocket, data, (size_t)TAILLE_MAX_DATA, 0);
            if (n < 0) return -1;
        } else {
            return -1;
        }
    }

    // NUL-terminate si on a de la place (pratique pour des messages texte)
    if (n >= 0) {
        size_t cap = (size_t)TAILLE_MAX_DATA;
        size_t idx = (size_t)((n < (ssize_t)cap) ? n : (ssize_t)(cap - 1));
        data[idx] = '\0';
    }

    return (int)n;
}
