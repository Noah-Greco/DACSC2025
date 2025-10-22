#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <signal.h>
#include <pthread.h>

#include "../Librairie/Librairie.hpp"
#include "../Protocole/CBP.hpp"
#include "../param/param.h"

void HandlerSIGINT(int s);
void TraitementConnexion(int sService);
void* FctThreadClient(void* p);
int LoadConf(const char* nomFichier);

int sEcoute;

int PORT_RESERVATION;
int NB_THREADS_POOL;
int TAILLE_FILE_ATTENTE;

int* socketsAcceptees;
int indiceEcriture=0, indiceLecture=0;

pthread_mutex_t mutexSocketsAcceptees;
pthread_cond_t condSocketsAcceptees;

int main(int argc,char* argv[])
{
	const char* fichierConfig = "Serveur.conf";

	printf("Chargement du fichier de configuration : %s\n", fichierConfig);

	if(LoadConf(fichierConfig) != 0)
	{
		printf("Erreur lors de la lecture du chargement\n");
		printf("%s\n", argv[0]);
		exit(1);
	}

	//Aloue la file qui stockera les socket en attente 
	socketsAcceptees = (int*)malloc(TAILLE_FILE_ATTENTE * sizeof(int));
    if (socketsAcceptees == NULL)
    {
        printf("Erreur d'allocation mémoire\n");
        exit(1);
    }

	// Initialisation socketsAcceptees
	pthread_mutex_init(&mutexSocketsAcceptees,NULL);
	pthread_cond_init(&condSocketsAcceptees,NULL);

	for (int i=0 ; i<TAILLE_FILE_ATTENTE ; i++)
		socketsAcceptees[i] = -1; //vide

	// Armement des signaux
	struct sigaction A;
	A.sa_flags = 0;
	sigemptyset(&A.sa_mask);
	A.sa_handler = HandlerSIGINT;

	if (sigaction(SIGINT,&A,NULL) == -1)
	{
		perror("Erreur de sigaction");
		exit(1);
	}

	// Creation de la socket d'écoute
	if ((sEcoute = ServerSocket(PORT_RESERVATION)) == -1)
	{
		perror("Erreur de ServeurSocket");
		exit(1);
	}

	// Creation du pool de threads
	printf("Création du pool de threads.\n");
	pthread_t th;

	for (int i=0 ; i<NB_THREADS_POOL ; i++)
		pthread_create(&th,NULL,FctThreadClient,NULL);

	// Mise en boucle du serveur
	int sService;
	char ipClient[IP_STR_LEN] = DEFAULT_SERVER_IP;

	printf("Demarrage du serveur.\n");

	while(1)
	{
		printf("Attente d'une connexion...\n");
		if ((sService = Accept(sEcoute,ipClient)) == -1)
		{
			perror("Erreur de Accept");
			close(sEcoute);
			CBP_Logout(sEcoute);
			exit(1);
		}

		printf("Connexion acceptée : IP=%s socket=%d\n",ipClient,sService);

		// Insertion en liste d'attente et réveil d'un thread du pool
		// (Production d'une tâche)
		pthread_mutex_lock(&mutexSocketsAcceptees);

		socketsAcceptees[indiceEcriture] = sService; 

		indiceEcriture++; //avance dans tab

		if (indiceEcriture == TAILLE_FILE_ATTENTE) //si fin
			indiceEcriture = 0; //revenir debut

		pthread_mutex_unlock(&mutexSocketsAcceptees);

		pthread_cond_signal(&condSocketsAcceptees);
	}
}
void* FctThreadClient(void* p)
{
	int sService;

	while(1)
	{
		printf("\t[THREAD %lu] Attente socket...\n", (unsigned long) pthread_self());

		// Attente d'une tâche
		pthread_mutex_lock(&mutexSocketsAcceptees);

		while (indiceEcriture == indiceLecture) //si file vide
			pthread_cond_wait(&condSocketsAcceptees,&mutexSocketsAcceptees);

		sService = socketsAcceptees[indiceLecture];//recup sckt a traiter
		socketsAcceptees[indiceLecture] = -1; //libère case
		indiceLecture++;//passe a suivante 

		if (indiceLecture == TAILLE_FILE_ATTENTE) //debut si vide
			indiceLecture = 0;
		
		pthread_mutex_unlock(&mutexSocketsAcceptees);

		// Traitement de la connexion (consommation de la tâche)
		printf("\t[THREAD %p] Je m'occupe de la socket %d\n", pthread_self(),sService);

		TraitementConnexion(sService);
	}
}
void HandlerSIGINT(int s)
{
	printf("\nArret du serveur.\n");
	close(sEcoute);
	pthread_mutex_lock(&mutexSocketsAcceptees);

	for (int i=0 ; i<TAILLE_FILE_ATTENTE ; i++)
		if (socketsAcceptees[i] != -1) close(socketsAcceptees[i]);
	
	pthread_mutex_unlock(&mutexSocketsAcceptees);

	CBP_Logout(sEcoute); 

	exit(0);
}
void TraitementConnexion(int sService)
{
	char requete[200], reponse[200];
	int nbLus, nbEcrits;
	int status = SUCCES;

	while(true)
	{
		printf("\t[THREAD %lu] Attente requete...\n",(unsigned long)pthread_self());

		//Reception Requete
		if ((nbLus = Receive(sService,requete)) < 0)
		{
			perror("Erreur de Receive");
			close(sService);

			HandlerSIGINT(0);
		}

		//Fin de connexion
		if (nbLus == 0)
		{
			printf("\t[THREAD %lu] Fin de connexion du client.\n",(unsigned long)pthread_self());
			close(sService);
			return;
		}

		requete[nbLus] = 0; // \O

		printf("\t[THREAD %lu] Requete recue = %s\n",(unsigned long)pthread_self(),requete);

		//Traitement de la requete
		status = CBP(requete,reponse,sService);

		//Envoi de la reponse
		if ((nbEcrits = Send(sService,reponse,strlen(reponse))) < 0)
		{
			perror("Erreur de Send");
			close(sService);
			HandlerSIGINT(0);
		}

		printf("\t[THREAD %p] Reponse envoyee = %s\n",pthread_self(),reponse);

		if (status == FERMER_CONNEXION)
        {
            printf("\t[THREAD %lu] Fin de connexion de la socket %d\n", (unsigned long)pthread_self(), sService);
            close(sService);
            return;
        }
	}
}
//Charge serv.conf
int LoadConf(const char* nomFichier)
{
    FILE* fichier = fopen(nomFichier, "r");
    if (fichier == NULL)
    {
        printf("Impossible d'ouvrir le fichier de configuration: %s\n", nomFichier);
        return -1;
    }
    
    char ligne[256];
    char cle[64], valeur[64];
    
    while (fgets(ligne, sizeof(ligne), fichier) != NULL)
    {
        if (ligne[0] == '#' || ligne[0] == '\n' || ligne[0] == '\r')
            continue;//passer tour de boucle suivant
         
      //lis 63 caractere max jusqua trouver = , tt avant il met dans clé et apres dans valeur    
        if (sscanf(ligne, "%63[^=]=%63s", cle, valeur) == 2) //
        {
        	//compare ce qu'on a avec ce qu'on attends
            if (strcmp(cle, "PORT_RESERVATION") == 0)
            {
                PORT_RESERVATION = atoi(valeur);
            }
            else if (strcmp(cle, "NB_THREADS_POOL") == 0)
            {
                NB_THREADS_POOL = atoi(valeur);
            }
            else if (strcmp(cle, "TAILLE_FILE_ATTENTE") == 0)
            {
                TAILLE_FILE_ATTENTE = atoi(valeur);
            }
        }
    }
    
    fclose(fichier);
    return 0;
}