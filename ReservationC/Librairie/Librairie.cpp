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

#define END_MARKER "##//##"
#define END_MARKER_LEN 6

#endif

int ServerSocket(int port) {
    int s = socket(AF_INET, SOCK_STREAM, 0); // IP Type Protocol
    if (s < 0) {
        return -1;
    }

    // Réutilisation d'adresse pour relancer sans attendre TIME_WAIT
    int opt = 1;
    //modif param sckt
    (void)setsockopt(s, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));

    struct sockaddr_in addr;
    memset(&addr, 0, sizeof(addr));
    addr.sin_family      = AF_INET;
    addr.sin_addr.s_addr = htonl(INADDR_ANY);
    addr.sin_port        = htons((uint16_t)port);

    //lier une sckt à ip et port
    if (bind(s, (struct sockaddr*)&addr, sizeof(addr)) < 0) {
        close(s);
        return -1;
    }
    //att connexion
    if (listen(s, SOMAXCONN) < 0) {
        close(s);
        return -1;
    }
    return s;
}

int Accept(int sEcoute, char *ipClient) {
    struct sockaddr_in cli;
    socklen_t len = sizeof(cli);
    int s = accept(sEcoute, (struct sockaddr*)&cli, &len);
    if (s < 0) {
        return -1;
    }
    if (ipClient != NULL) {
        //convertit ip en chaine                    //taille min
        inet_ntop(AF_INET, &cli.sin_addr, ipClient, INET_ADDRSTRLEN);
    }
    return s;
}

// Crée un socket TCP et connecte vers serv
int ClientSocket(const char* ipServeur, int portServeur) {
    //Validation d'entrée
    if (ipServeur == NULL) {
        errno = EINVAL;
        return -1;
    }

    int s = socket(AF_INET, SOCK_STREAM, 0);
    if (s < 0) {
        return -1;
    }
//Prepare l'adresse réseau
    struct sockaddr_in srv;
    memset(&srv, 0, sizeof(srv));
    srv.sin_family = AF_INET;
    srv.sin_port   = htons((uint16_t)portServeur);
//lis l'ip et remplie ipServeur
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

int Send(int sSocket, const char* data, int taille) {
    if (data == NULL || taille < 0) {
        errno = EINVAL;
        return -1;
    }

    // buffer complet = data + délimiteur
    int totalSize = taille + END_MARKER_LEN;
    char buffer[TAILLE_MAX_DATA];

    if (totalSize >= TAILLE_MAX_DATA) {
        errno = EMSGSIZE;
        return -1;
    }

    memcpy(buffer, data, taille);
    memcpy(buffer + taille, END_MARKER, END_MARKER_LEN);

    int total = 0;
    while (total < totalSize) {
        ssize_t n = send(sSocket, buffer + total, totalSize - total, MSG_NOSIGNAL);
        if (n < 0) {
            if (errno == EINTR) continue;
            return -1;
        }
        if (n == 0) break;

        total += n;
    }

    return total;
}


int Receive(int sSocket, char* data) {
    if (data == NULL) {
        errno = EINVAL;
        return -1;
    }

    char buffer[1024];
    int total = 0;
    data[0] = '\0';

    while (1) {
        ssize_t n = recv(sSocket, buffer, sizeof(buffer)-1, 0);

        if (n < 0) {
            if (errno == EINTR) continue;
            return -1;
        }
        if (n == 0) {
            // déconnexion du client
            return 0;
        }

        buffer[n] = '\0';

        // vérifier si on déborde
        if (total + n >= TAILLE_MAX_DATA) {
            errno = EMSGSIZE;
            return -1;
        }

        memcpy(data + total, buffer, n);
        total += n;
        data[total] = '\0';

        // vérifie si on a reçu le marqueur
        char* pos = strstr(data, END_MARKER);
        if (pos != NULL) {
            *pos = '\0'; // coupe au marqueur
            return (int)(pos - data); // taille utile
        }
    }
}


int closeSocket(int sSocket)
{
    if (close(sSocket) < 0)
    {
        return -1;
    }
    return 0;
}