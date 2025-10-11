#include "CBP.h"
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <pthread.h>
#include <mysql.h>

bool CBP(char* requete, char* reponse,int socket);
char CBP_Login(const char* user,const char* password);
char CBP_Logout();
char CBP_Get_Specialties();
char CBP_Get_Doctors();
char CBP_Search_Consultations(const char* specialties, char* id, char* dateDeb, char* dateFin);
char CBP_Book_Consultation(char* consultationId, char* reason);

int estPresent(int socket);

bool CBP(char* requete, char* reponse,int socket)
{
	char *ptr = strtok(requete,"#");

	if(strcmp(ptr,"LOGIN") == 0)
	{
		char user[50], password[50];

		strcpy(user,strtok(NULL,"#"));
		strcpy(password,strtok(NULL,"#"));

		printf("\t[THREAD %p] LOGIN de %s\n",pthread_self(),user);

		if (estPresent(socket) >= 0) // client déjà loggé
		{
			sprintf(reponse,"LOGIN#ko#Client déjà loggé !");

			return false;
		}

		else
		{
			if (CBP_Login(user,password))
			{
				sprintf(reponse,"LOGIN#ok");
				ajoute(socket);
			}
			else
			{
				sprintf(reponse,"LOGIN#ko#Mauvais identifiants !");
				return false;
			}
		}

	}
	if (strcmp(ptr,"LOGOUT") == 0)
	{
		printf("\t[THREAD %p] LOGOUT\n",pthread_self());
		retire(socket);
		sprintf(reponse,"LOGOUT#ok");
		return false;

	}
	//Pas encore eu le temps de les compléter. Il faudrait implémenter la lecture dans la base de données
	//Soit tu peux faire la lecture dans la BD soit tu peux continuer les serveurs ou clients réservation.
	//Pour lire dans la bd j'ai mis toute les infos de la bd dans le readme et chat dis qu'on peut faire des lectures dans la bd depuis le code 
	//donc ce serait ce qu'il faudrait faire. Si tu fais Client ou serveur réservation j'ai pas encore regarder mais si c ce que tu fais fais moi une note
	//pour m'expliquer ce que tu as fait 
	if(strcmp(ptr, "GET_SPECIALTIES") == 0)
	{
		MYSQL * connection;
		connection = mysql_init(NULL);

		if(!connection)
		{
			fprintf(stderr, "mysql_init failed\n");
		}

		else
		{
			if(mysql_real_connect(connection, "localhost","Student","PassStudent1_","PourStudent",0,NULL,0) == NULL)
			{
				fprintf(stderr, "connect : %s\n", mysql_error(connection));
			}

			else
			{
				if(mysql_query(connection, "SELECT * FROM specialties;"))
				{
					fprintf(stderr, "Query : %s\n", mysql_error(connection));
					mysql_close(connection);
				}

				else
				{
					MYSQL_RES * res = mysql_store_result(connection);

					if(!res)
					{
						fprintf(stderr, "store_result : %s\n", mysql_error(connection));
						mysql_close(connection);
					}

					else
					{
						MYSQL_ROW row;

						while((row = mysql_fetch_row(res)) != NULL)
						{
							printf("Specialité : %s\n", row[0]);
						}

						mysql_free_result(res);
						mysql_close(connection);
					}
				}

			}
		}


	}

	if(strcmp(ptr, "GET_DOCTORS") == 0)
	{
		MYSQL * connection;
		connection = mysql_init(NULL);

		if(!connection)
		{
			fprintf(stderr, "mysql_init failed\n");
		}

		else
		{
			if(mysql_real_connect(connection, "localhost","Student","PassStudent1_","PourStudent",0,NULL,0) == NULL)
			{
				fprintf(stderr, "connect : %s\n", mysql_error(connection));
			}

			else
			{
				if(mysql_query(connection, "SELECT * FROM doctors;"))
				{
					fprintf(stderr, "Query : %s\n", mysql_error(connection));
					mysql_close(connection);
				}

				else
				{
					MYSQL_RES * res = mysql_store_result(connection);

					if(!res)
					{
						fprintf(stderr, "store_result : %s\n", mysql_error(connection));
						mysql_close(connection);
					}

					else
					{
						MYSQL_ROW row;

						while((row = mysql_fetch_row(res)) != NULL)
						{
							printf("Specialité : %s\n", row[0]);
						}

						mysql_free_result(res);
						mysql_close(connection);
					}
				}

			}
		}
	}

	if(strcmp(ptr, "SEARCH_CONSULTATIONS") == 0)
	{
		
	}

	if(strcmp(ptr, "BOOK_CONSULTATION") == 0)
	{
		
	}
}

int estPresent(int socket)
{
	int indice = -1;
	pthread_mutex_lock(&mutexClients);
	for(int i=0 ; i<nbClients ; i++)
	{
		if (clients[i] == socket) 
		{ 
			indice = i; break; 
		}
	}

	pthread_mutex_unlock(&mutexClients);
	return indice;
}

void ajoute(int socket)
{
	pthread_mutex_lock(&mutexClients);
	clients[nbClients] = socket;
	nbClients++;
	pthread_mutex_unlock(&mutexClients);
}

void retire(int socket)
{
	int pos = estPresent(socket);
	if (pos == -1) return;
	pthread_mutex_lock(&mutexClients);
	for (int i=pos ; i<=nbClients-2 ; i++)
		clients[i] = clients[i+1];
	nbClients--;
	pthread_mutex_unlock(&mutexClients);
}