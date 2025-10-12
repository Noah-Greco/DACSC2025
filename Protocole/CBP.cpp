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

int clients[NB_MAX_CLIENTS];
int nbClients = 0;

pthread_mutex_t mutexClients = PTHREAD_MUTEX_INITIALIZER;

bool CBP(char* requete, char* reponse,int socket)
{
	char *ptr = strtok(requete,"#");

	if(strcmp(ptr,"LOGIN") == 0)
	{
		char firstName[50], lastName[50], NoPatient[10];

		strcpy(firstName,strtok(NULL,"#"));
		strcpy(lastName,strtok(NULL,"#"));
		strcpy(NoPatient, strtok(NULL, "#"));

		printf("\t[THREAD %p] LOGIN de %s\n",pthread_self(),firstName);

		if (estPresent(socket) >= 0) // client déjà loggé
		{
			sprintf(reponse,"LOGIN#ko#Client déjà loggé !");
			return false;
		}

		else
		{
			if (CBP_Login(firstName, lastName, NoPatient))
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
		CBP_Logout();
	}
	
	if(strcmp(ptr, "GET_SPECIALTIES") == 0)
	{
		CBP_Get_Specialties();
	}

	if(strcmp(ptr, "GET_DOCTORS") == 0)
	{
		CBP_Get_Doctors();
	}

	//on envoie le nom du docteur puis on récupère son ID ou on envoie son id directement ?
	//on doit récupérer date de fin mais à quoi elle sert ?
	if(strcmp(ptr, "SEARCH_CONSULTATIONS") == 0) 
	{
		char * specialties = strtok(NULL, "#");
		char * doctors = strtok(NULL, "#");
		char * startDate = strtok(NULL, "#");
		char * endDate = strtok(NULL, "#");

		CBP_Search_Consultations(specialties, , startDate, endDate);
	}

	if(strcmp(ptr, "BOOK_CONSULTATION") == 0)
	{
		char * cons_id = strtok(NULL, "#");
		char * reason = strtok(NULL, "#");

		CBP_Book_Consultation(cons_id, reason);
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

char CBP_Login(const char* firstName,const char* lastName, const char * NoPatient)
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
			char sql_cmd[500];
			sprintf(sql_cmd, "select * from patients where last_name like %s and first_name like %s;", lastName, firstName);
			
			if(mysql_query(connection, sql_cmd))
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

void CBP_Logout()
{
	printf("\t[THREAD %p] LOGOUT\n",pthread_self());
	retire(socket);
}

char CBP_Get_Specialties()
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

char CBP_Get_Doctors()
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

char CBP_Search_Consultations(const char* specialties, char* id, char* dateDeb, char* dateFin)
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
			char sql_cmd[500];

			sprintf(sql_cmd, "SELECT * FROM consultations c inner join doctors d on (c.doctor_id = d.id) inner join specialties s on 
				(d.specialty_id = s.id) where c.doctor_id like %s and c.date like %s and s.name like %s;", doctors, dateDeb, specialties);

			if(mysql_query(connection, sql_cmd))
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

char CBP_Book_Consultation(char* consultationId, char* reason)
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
			if(mysql_query(connection, ""))
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