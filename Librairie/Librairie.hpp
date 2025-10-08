#ifndef LIBRAIRIE_HPP
#define LIBRAIRIE_HPP

#include <cstddef>   // size_t

#define TAILLE_MAX_DATA 10000

//Appelés par le processus serveur --> Celui qui attent une connexion --> prends en parametre le port sur le quel le serveur attends et retourne le socket d'écoute
int ServerSocket(int port);

//Appelée par le processus serveur --> prends en para la socket crée par serversocket() et retourne la socket de service obtenue grâce a la connexion d'un client
int Accept(int sckt_serveur,char *ipClient);

//appelés par le processus client -->  prends en entrée l'ip sous forme de chaine de cara et le port du server sur le quel on veut se connecter retourne 
//socket service qui permettra de communiquer avec le serveur 
int ClientSocket(char* ipServeur,int portServeur);

//appelés par le processus client et le serveur --> recoit en parametre la socket de service, l'adresse mémoire d'un paquet de byte que l'on desire en voyer et 
//la taille du packet de byte et retourne le nb de byte envoyés
int Send(int sSocket,char* data,int taille);

//appelés par le processus client et serveur --> recois paquet de donnée envoyer par send() recoit en para la socket de service et  l’adresse d’un buffer de réception 
//qui va revoir les données lues sur le réseau et retourne le nb de bytes lu
int Receive(int sSocket,char* data);

#endif