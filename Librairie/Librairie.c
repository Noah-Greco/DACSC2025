#define TAILLE_MAX_DATA 10000
#define _POSIX_C_SOURCE 200112L
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include "Librairie.h"

int ServerSocket(int port) 
{
  int sckt_serveur;
  char portBis[20];
 //Fait un appel à socket() pour créer la socket
  sckt_serveur = socket(AF_INET , SOCK_STREAM , 0); //0 veut dire que l'os prendra le protocol adapté lui mm 
  if(sckt_serveur < 0 )//Erreur de socket se teste par < 0 et pas == 
  {
    perror("Erreur de socket()");
    exit(1);
  }
   printf("socket creee = %d\n",sckt_serveur);

 //construit l’adresse réseau de la socket par appel à getaddrinfo()
 struct addrinfo hints;
 struct addrinfo *results;
 memset(&hints,0,sizeof(struct addrinfo));
 hints.ai_family = AF_INET; //IPV4
 hints.ai_socktype = SOCK_STREAM;//TCP
 hints.ai_flags = AI_PASSIVE | AI_NUMERICSERV; // pour une connexion passive
 sprintf(portBis, "%d", port);
 if (getaddrinfo(NULL,portBis,&hints,&results) != 0)//null veut dire nimp quelle ip
 {
    close(sckt_serveur);
    exit(1);
 } 
 //fait appel à bind() pour lier la socket à l’adresse réseau
 if (bind(sckt_serveur,results->ai_addr,results->ai_addrlen) < 0)
 {
    perror("Erreur de bind()");
    exit(1);
 }
 freeaddrinfo(results);
 printf("bind() reussi !\n");
 pause();

 //fait appel à listen() pour démarrer la machine à états TCP
 if(listen(sckt_serveur,8)== -1)
 {
     perror("Erreur de listen()");
     exit(1);
 }
  printf("listen reussis");
}
int Accept(int sckt_serveur,char *ipClient)
{ 
  int sckt_Service;
  char host[NI_MAXHOST];
  char port[NI_MAXSERV];
 //Fait appel à accept()
 if(sckt_Service=accept(sckt_serveur,NULL,NULL) == -1)
 {
     perror("Erreur de accepte");
     exit(1);
 }
 //Recupérer l'ip du client qui vient de se connecter dans ipClient si c'est non NULL, si c'est NULL pointer vers une zone mémoire capable de recevoir une chaine de cara de la taille de l'ip
 struct sockaddr_in adrClient;
 socklen_t adrClientLen = sizeof(struct sockaddr_in); // nécessaire
 getpeername(sckt_Service,(struct sockaddr*)&adrClient,&adrClientLen);
 getnameinfo((struct sockaddr*)&adrClient,adrClientLen,
 host,NI_MAXHOST,
 port,NI_MAXSERV,
 NI_NUMERICSERV | NI_NUMERICHOST);
 printf("Client connecte --> Adresse IP: %s -- Port: %s\n",host,port);
 pause();

}
int ClientSocket(char* ipServeur,int portServeur)
{
 //Fait appel à socket() pour créer la socket
  char portServeurBis[20];
  int sckt_client;
  sprintf(portServeurBis, "%d", portServeur);
  sckt_client = socket(AF_INET , SOCK_STREAM , 0); 
  if(sckt_client < 0 )
  {
    perror("Erreur de socket()");
    exit(1);
  }
   printf("socket creee = %d\n",sckt_client);
  //construit l’adresse réseau de la socket (avec l’IP et le port du serveur) par appel à la fonction getaddrinfo()
  struct addrinfo hints;
  struct addrinfo *results;
  memset(&hints,0,sizeof(struct addrinfo));
  hints.ai_family = AF_INET; //IPV4
  hints.ai_socktype = SOCK_STREAM;//TCP
  hints.ai_flags = AI_PASSIVE | AI_NUMERICSERV; // pour une connexion passive
 if (getaddrinfo(ipServeur,portServeurBis,&hints,&results) != 0)//null veut dire nimp quelle ip
 {
    close(sckt_client);
    exit(1);
 } 
 //fait appel à connect() pour se connecter sur le serveur
 if(connect(sckt_client,results->ai_addr,results->ai_addrlen)<0)
 {
   perror("Erreur lors de la tentative de connect ()");
   exit(1);
 }
 printf("connect() réussi !");
}
int Send(int sSocket,char* data,int taille)
{
  int nb;
  char buffer[200], tmp[60] = "##//##";
  strcpy(buffer,data);
  
  strcat(buffer, tmp);
  if ((nb = write(sSocket,buffer,taille)) == -1)
  {
    perror("Erreur de write()");  
    close(sSocket);
  }
  printf("nbEcrits = %d\n",nb);
}
int Receive(int sSocket, char* data)
{
    static const char *DELIM = "##//##";
    static const int   DLEN  = 6;

    int pos   = 0;   // position d’écriture dans 'data'
    int match = 0;   // nb de caractères consécutifs du délimiteur déjà reconnus

    char buf[512];
    for (;;)
    {
        ssize_t r = recv(sSocket, buf, sizeof(buf), 0);
        if (r == 0) { // fermeture propre
            if (pos <= TAILLE_MAX_DATA) data[pos] = '\0';
            return 0;
        }
        if (r < 0) {
            return -1; // perror éventuel à l'appelant si tu veux
        }

        for (ssize_t i = 0; i < r; ++i)
        {
            char c = buf[i];

            if (c == DELIM[match]) {
                match++;
                if (match == DLEN) {
                    if (pos > TAILLE_MAX_DATA) { data[TAILLE_MAX_DATA] = '\0'; return -2; }
                    data[pos] = '\0';
                    return pos; // délimiteur trouvé (non recopié)
                }
                continue; // on attend de voir si on complète le délimiteur
            }

            // si on avait un début de délimiteur qui casse, on "rejoue" ce qu'on avait matché
            if (match > 0) {
                for (int k = 0; k < match; ++k) {
                    if (pos >= TAILLE_MAX_DATA) { data[TAILLE_MAX_DATA] = '\0'; return -2; }
                    data[pos++] = DELIM[k];
                }
                match = 0;
            }

            // recopie du caractère courant
            if (pos >= TAILLE_MAX_DATA) { data[TAILLE_MAX_DATA] = '\0'; return -2; }
            data[pos++] = c;

            // ce caractère peut-il démarrer un nouveau délimiteur ?
            if (c == DELIM[0]) match = 1;
        }
    }
}