#include "CBP.hpp"
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <pthread.h>
#include <mysql/mysql.h>

bool CBP(char* requete, char* reponse,int socket);
char * CBP_Login(const char* firstName,const char* lastName, const char * NoPatient, const char * NvPatient, int socket);
void CBP_Logout(int socket);
const char * CBP_Get_Specialties();
const char * CBP_Get_Doctors();
const char * CBP_Search_Consultations(const char* specialties, char* id, char* dateDeb, char* dateFin);
void CBP_Book_Consultation(char* consultationId, char* reason, int id);

int estPresent(int socket);
void ajoute(int socket);
void retire(int socket, unsigned long long id);

int clients[NB_MAX_CLIENTS];
int nbClients = 0;

pthread_mutex_t mutexClients = PTHREAD_MUTEX_INITIALIZER;

bool CBP(char* requete, char* reponse,int socket)
{
	char *ptr = strtok(requete,"#");

	if(strcmp(ptr,"LOGIN") == 0)
	{
		char firstName[50], lastName[50], NoPatient[10], NvPatient[10];

		strcpy(firstName,strtok(NULL,"#"));
		strcpy(lastName,strtok(NULL,"#"));
		strcpy(NoPatient, strtok(NULL, "#"));
		strcpy(NvPatient, strtok(NULL, "#"));

		printf("\t[THREAD %lu] LOGIN de %s\n",(unsigned long)pthread_self(),firstName);

		if (estPresent(socket) >= 0) // client déjà loggé
		{
			sprintf(reponse,"LOGIN#ko#Client déjà loggé !");
			return false;
		}

		else
		{
			char* res = CBP_Login(firstName, lastName, NoPatient, NvPatient, socket);

			snprintf(reponse, 200, "%s", res ? res : "NON");

			if (res && strcmp(res, "NON") != 0) free(res);  // si CBP_Login a alloué

		}
	}

	if (strcmp(ptr,"LOGOUT") == 0)
	{
		CBP_Logout(socket);
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

		CBP_Search_Consultations(specialties, doctors, startDate, endDate);
	}

	if(strcmp(ptr, "BOOK_CONSULTATION") == 0)
	{
		char * cons_id = strtok(NULL, "#");
		char * reason = strtok(NULL, "#");
		char * id = strtok(NULL, "#");
		int ID = atoi(id);

		CBP_Book_Consultation(cons_id, reason, ID);
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

void ajoute(int socket, unsigned long long id)
{
	pthread_mutex_lock(&mutexClients);
	clients[nbClients] = socket;
	nbClients++;
	pthread_mutex_unlock(&mutexClients);
}

void retire(int socket)
{
	int pos = estPresent(socket);

	if (pos == -1) 
		return;

	pthread_mutex_lock(&mutexClients);

	for (int i=pos ; i<=nbClients-2 ; i++)
		clients[i] = clients[i+1];

	nbClients--;

	pthread_mutex_unlock(&mutexClients);
}

char * CBP_Login(const char* firstName,const char* lastName, const char * NoPatient, const char * NvPatient, int socket)
{
	MYSQL * connection;
	connection = mysql_init(NULL);

	bool NewPatient;

	if(strcmp(NvPatient, "OUI") == 0)
	{
		NewPatient = true;
	}
	else
	{
		NewPatient = false;
	}

	if(NewPatient) 
	{
		if(!connection)
		{
			fprintf(stderr, "mysql_init failed\n");
			return strdup("LOGIN#ko");
		}

		else
		{
			if(mysql_real_connect(connection, "localhost","Student","PassStudent1_","PourStudent",0,NULL,0) == NULL)
			{
				fprintf(stderr, "connect : %s\n", mysql_error(connection));
				return strdup("LOGIN#ko");
			}

			else
			{
				char sql_cmd[500];
				sprintf(sql_cmd, "insert into patients (id, last_name, first_name) values (%s, '%s', '%s');", NoPatient, lastName, firstName);
				if(mysql_query(connection, sql_cmd))
				{
					fprintf(stderr, "Query 1 : %s\n", mysql_error(connection));
					mysql_close(connection);
					return strdup("LOGIN#ko");
				}

				unsigned long long id = mysql_insert_id(connection);   

				printf("Création du patient avec l'id %llu. Connection OK", id);
				ajoute(socket, id);

				char* valRet = static_cast<char*>(std::malloc(32));
				if (!valRet) { mysql_close(connection); return strdup("LOGIN#ko"); }

				snprintf(valRet, 32, "LOGIN#ok#%llu", id);

				mysql_query(connection, "commit;");

				mysql_close(connection);
				return valRet;


			}
		}
	}
	else
	{	
		if(!connection)
		{
			fprintf(stderr, "mysql_init failed\n");
			return strdup("LOGIN#ko");
		}

		else
		{
			if(mysql_real_connect(connection, "localhost","Student","PassStudent1_","PourStudent",0,NULL,0) == NULL)
			{
				fprintf(stderr, "connect : %s\n", mysql_error(connection));
				return "LOGIN#ko";
			}
			else
			{
				char sql_cmd[500];
				sprintf(sql_cmd, "select * from patients where last_name like '%s' and first_name like '%s';", lastName, firstName);
				if(mysql_query(connection, sql_cmd))
				{
					fprintf(stderr, "Query : %s\n", mysql_error(connection));
					mysql_close(connection);
					return strdup("LOGIN#ko");
				}

				else
				{
					MYSQL_RES * res = mysql_store_result(connection);

					if(!res)
					{
						fprintf(stderr, "store_result : %s\n", mysql_error(connection));
						mysql_close(connection);
						return strdup("LOGIN#ko");
					}

					else
					{
						MYSQL_ROW row;

						row = mysql_fetch_row(res);

						char id[20];
						int NumPatient;

						NumPatient = atoi(NoPatient);

						sprintf(id, "%d", NumPatient);

						if(strcmp(id, row[0]) == 0)
						{
							printf("Le NoPatient est bon. Connection OK");
							mysql_free_result(res);
							mysql_close(connection);
							return strdup("LOGIN#ok");
						}
						else
						{
							mysql_free_result(res);
							mysql_close(connection);
							return strdup("LOGIN#ko");
						}

							
					}
				}

			}
		}
	}
		
}


void CBP_Logout(int socket)
{
	printf("\t[THREAD %lu] LOGOUT\n",(unsigned long)pthread_self());

	retire(socket);
}

const char * CBP_Get_Specialties()
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

					char valRet[500];

					row = mysql_fetch_row(res);

					strcpy(valRet, row[0]);
					strcat(valRet, ";");
					strcat(valRet, row[1]);

					while((row = mysql_fetch_row(res)) != NULL)
					{
						strcat(valRet, "#");
						strcat(valRet, row[0]);
						strcat(valRet, ";");
						strcat(valRet, row[1]);
					}


					mysql_free_result(res);
					mysql_close(connection);

					return valRet;
				}
			}
		}
	}
}

const char * CBP_Get_Doctors()
{
	MYSQL * connection;
	connection = mysql_init(NULL);

	if(!connection)
	{
		fprintf(stderr, "mysql_init failed\n");
	}

	else
	{						// connexion db , ip     ,  user    , mdp           ,   bduse,      
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
					MYSQL_ROW row; //var de type ligne bdd 
					char DocRet[500];
					row = mysql_fetch_row(res);
					//permet de faire en sorte d'arriver au 1er
					strcpy(DocRet, row[0]);
					strcat(DocRet, ";");
					strcat(DocRet, row[1]);
					strcat(DocRet, ";");
					strcat(DocRet, row[2]);
					strcat(DocRet, ";");
					strcat(DocRet, row[3]);


					while((row = mysql_fetch_row(res)) != NULL)
					{
						strcat(DocRet, "#");
						strcat(DocRet, row[0]);
						strcat(DocRet, ";");
						strcat(DocRet, row[1]);
						strcat(DocRet, ";");
						strcat(DocRet, row[2]);
						strcat(DocRet, ";");
						strcat(DocRet, row[3]);
					}

					mysql_free_result(res);
					mysql_close(connection);

					return DocRet; 
				}
			}

		}
	}
}

const char * CBP_Search_Consultations(const char* specialties, char* id, char* dateDeb, char* dateFin)
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

			sprintf(sql_cmd, 
            "select consultations.id, specialties.name, CONCAT(doctors.last_name, ' ', doctors.first_name), DATE_FORMAT(consultations.date, '%%Y-%%m-%%d'), hour from consultations "
            "inner join doctors on consultations.doctor_id = doctors.id inner join specialties on doctors.specialty_id = specialties.id where patient_id is NULL and (specialties.name = '%s' "
            "or doctors.last_name = '%s') and date between '%s' and '%s';", specialties, id, dateDeb, dateFin);

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
					char ConsRet[500];
					row = mysql_fetch_row(res);
					strcpy(ConsRet, row[0]);
					strcat(ConsRet, ";");
					strcat(ConsRet, row[1]);
					strcat(ConsRet, ";");
					strcat(ConsRet, row[2]);
					strcat(ConsRet, ";");
					strcat(ConsRet, row[3]);
					strcat(ConsRet, ";");
					strcat(ConsRet, row[4]);
					strcat(ConsRet, ";");
					strcat(ConsRet, row[5]);
					while((row = mysql_fetch_row(res)) != NULL)
					{
						strcat(ConsRet, "#");
						strcat(ConsRet, row[0]);
						strcat(ConsRet, ";");
						strcat(ConsRet, row[1]);
						strcat(ConsRet, ";");
						strcat(ConsRet, row[2]);
						strcat(ConsRet, ";");
						strcat(ConsRet, row[3]);
						strcat(ConsRet, ";");
						strcat(ConsRet, row[4]);
						strcat(ConsRet, ";");
						strcat(ConsRet, row[5]);
					}

					mysql_free_result(res);
					mysql_close(connection);
					return ConsRet;
				}
			}

		}
	}
}

void CBP_Book_Consultation(char* consultationId, char* reason, int id)
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

			int consID = atoi(consultationId);

			sprintf(sql_cmd, "update consultations set patient_id = %d reason = '%s' where id = %d and patient_id is NULL;", id, reason, consID);
			if(mysql_query(connection, sql_cmd))
			{
				fprintf(stderr, "Query : %s\n", mysql_error(connection));
				mysql_close(connection);
			}

			mysql_close(connection);

		}
	}
}