#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <signal.h>
#include <pthread.h>

#include "../Librairie/Librairie.hpp"
#include "../Protocole/CBP.hpp"
#include "../Protocole/ACBP.hpp"
#include "../param/param.h"

#define TAILLE_MAX_DATA 10000

void HandlerSIGINT(int s);
void TraitementConnexion(int sService);
void* FctThreadClient(void* p);
int LoadConf(const char* nomFichier);

void* ThreadAdminAcceptor(void*); 
void* ThreadAdminService(void* p);
bool Dispatch(char* requete, char* reponse, int sService); 

int sEcoute;
int sEcouteAdmin;

int PORT_RESERVATION = -1;
int PORT_ADMIN = -1;
int NB_THREADS_POOL = -1;
int TAILLE_FILE_ATTENTE = -1;

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

	printf("PORT_RESERVATION = %d\n", PORT_RESERVATION);
	printf("PORT_ADMIN       = %d\n", PORT_ADMIN);
	printf("NB_THREADS_POOL  = %d\n", NB_THREADS_POOL);
	printf("TAILLE_FILE_ATTENTE = %d\n", TAILLE_FILE_ATTENTE);

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

	// Creation de la socket d'écoute client
	if ((sEcoute = ServerSocket(PORT_RESERVATION)) == -1)
	{
		perror("Erreur de ServeurSocket");
		exit(1);
	}

	//Admin
	if ((sEcouteAdmin = ServerSocket(PORT_ADMIN)) == -1)
	{
		perror("Erreur de ServeurSocket Admin");
		exit(1);
	}


	// Creation du pool de threads
	printf("Création du pool de threads.\n");

	pthread_t th;
	for (int i=0 ; i<NB_THREADS_POOL ; i++)
		pthread_create(&th,NULL,FctThreadClient,NULL);

	pthread_t thAdmin;
	pthread_create(&thAdmin, NULL, ThreadAdminAcceptor, NULL);
	pthread_detach(thAdmin);

    printf("%d", PORT_ADMIN);


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

		while (socketsAcceptees[indiceEcriture] != -1)
    		pthread_cond_wait(&condSocketsAcceptees, &mutexSocketsAcceptees);

		socketsAcceptees[indiceEcriture] = sService; 

		indiceEcriture = (indiceEcriture + 1) % TAILLE_FILE_ATTENTE;

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
		indiceLecture = (indiceLecture + 1) % TAILLE_FILE_ATTENTE;
		
		pthread_mutex_unlock(&mutexSocketsAcceptees);

		pthread_cond_signal(&condSocketsAcceptees); // réveille un producteur éventuel

		// Traitement de la connexion (consommation de la tâche)
		printf("\t[THREAD %p] Je m'occupe de la socket %d\n", pthread_self(),sService);

		TraitementConnexion(sService);
	}
}
void* ThreadAdminAcceptor(void*)
{
    char ip[IP_STR_LEN] = DEFAULT_SERVER_IP;
    while (1)
    {
        int sAdmin = Accept(sEcouteAdmin, ip);
        if (sAdmin == -1) { perror("Accept admin"); continue; }
        pthread_t th;

        // un thread par connexion admin = “à la demande”
        int* arg = (int*)malloc(sizeof(int));
        *arg = sAdmin;
        pthread_create(&th, NULL, ThreadAdminService, arg);
        pthread_detach(th);
    }
    return NULL;
}

/*void* ThreadAdminService(void* p)
{
    int sService = *(int*)p; free(p);
    char req[200], rep[200];
    while (1)
    {
        int n = Receive(sService, req);
        if (n <= 0) { close(sService); return NULL; }
        req[n] = 0;

        char tmp[200]; strncpy(tmp, req, sizeof(tmp)-1); tmp[sizeof(tmp)-1]=0;
        char* tag = strtok(tmp, "#");
        if (!tag || strcmp(tag, "ACBP") != 0) {
            snprintf(rep, sizeof(rep), "#ko#admin_only_acbp");
            Send(sService, rep, strlen(rep));
            close(sService);
            return NULL;
        }

        bool fermer = ACBP(req, rep);
        if (Send(sService, rep, strlen(rep)) < 0) { close(sService); return NULL; }
        if (fermer) { close(sService); return NULL; }
    }
}*/

void* ThreadAdminService(void* p)
{
    int sService = *(int*)p;
    free(p);

    char req[TAILLE_MAX_DATA];
    char rep[TAILLE_MAX_DATA];

    printf("[ADMIN] Nouveau client admin sur socket %d\n", sService);

    while (1)
    {
        int n = Receive(sService, req);
        printf("[ADMIN] Receive retourne %d octets sur socket %d\n", n, sService);

        if (n <= 0) {
            printf("[ADMIN] Fin connexion admin socket %d\n", sService);
            close(sService);
            return NULL;
        }

        // Sécurisation : ne JAMAIS écrire au-delà du buffer
        if (n >= (int)sizeof(req)) n = (int)sizeof(req) - 1;
        req[n] = 0;

        printf("[ADMIN] Requête brute = '%s'\n", req);

        // On copie pour tester le tag sans détruire la requête originale
        char tmp[TAILLE_MAX_DATA];
        strncpy(tmp, req, sizeof(tmp) - 1);
        tmp[sizeof(tmp) - 1] = 0;

        char* tag = strtok(tmp, "#");
        if (!tag || strcmp(tag, "ACBP") != 0) {
            snprintf(rep, sizeof(rep), "#ko#admin_only_acbp");
            Send(sService, rep, strlen(rep));
            printf("[ADMIN] Proto invalide, fermeture socket %d\n", sService);
            close(sService);
            return NULL;
        }

        bool fermer = ACBP(req, rep);
        printf("[ADMIN] Réponse ACBP = '%s' (fermer=%d)\n", rep, fermer);

        int lenRep = (int)strlen(rep);
        if (Send(sService, rep, lenRep) < 0) {
            perror("[ADMIN] Erreur Send");
            close(sService);
            return NULL;
        }

        if (fermer) {
            printf("[ADMIN] ACBP demande fermeture socket %d\n", sService);
            close(sService);
            return NULL;
        }
    }
}




void HandlerSIGINT(int s)
{
	printf("\nArret du serveur.\n");
	close(sEcoute);
	close(sEcouteAdmin);

	pthread_mutex_lock(&mutexSocketsAcceptees);

	for (int i=0 ; i<TAILLE_FILE_ATTENTE ; i++)
		if (socketsAcceptees[i] != -1) close(socketsAcceptees[i]);
	
	pthread_mutex_unlock(&mutexSocketsAcceptees);

	CBP_Logout(sEcoute); //a modif car fonctionne pas pour ACBP

	exit(0);
}
void TraitementConnexion(int sService)
{
	char requete[TAILLE_MAX_DATA], reponse[TAILLE_MAX_DATA];
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

			return;
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
		
		bool fermer = Dispatch(requete, reponse, sService);

		//Envoi de la reponse
		if ((nbEcrits = Send(sService,reponse,strlen(reponse))) < 0)
		{
			perror("Erreur de Send");
			close(sService);
			return;
		}

		printf("\t[THREAD %p] Reponse envoyee = %s\n",pthread_self(),reponse);

		if (fermer)
        {
            printf("\t[THREAD %lu] Fin de connexion de la socket %d\n", (unsigned long)pthread_self(), sService);
            close(sService);
            return;
        }
	}
}

bool Dispatch(char* requete, char* reponse, int sService)
{
    // copie locale car strtok modifie la chaîne
    char tmp[200]; strncpy(tmp, requete, sizeof(tmp)-1); tmp[sizeof(tmp)-1]=0;

    char* tag = strtok(tmp, "#");     // "CBP" ou "ACBP"
    if (!tag) { snprintf(reponse,200,"#ko#bad_request"); return false; }
    else
	{   
		if (strcmp(tag, "CBP") == 0)      // == 0 quand égal
	    	return CBP(requete, reponse, sService);
	    else
	    {
	    	if (strcmp(tag, "ACBP") == 0)
	        	return ACBP(requete, reponse);

	        else
	        {
	        	snprintf(reponse,200,"#ko#unknown_proto");}
    			return false;
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
			else if (strcmp(cle, "PORT_ADMIN") == 0) {
				PORT_ADMIN = atoi(valeur);
			}

        }
    }
    
    fclose(fichier);
    return 0;
}