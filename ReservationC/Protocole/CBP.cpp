#include "CBP.hpp"
#include <string.h>
#include <string>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <pthread.h>
#include <mysql/mysql.h>
#include <sys/socket.h>
#include <netdb.h>
#include <arpa/inet.h>


bool CBP(char* requete, char* reponse,int socket);
char * CBP_Login(const char* firstName,const char* lastName, const char * NoPatient, const char * NvPatient, int socket);
void CBP_Logout(int socket);
char * CBP_Get_Specialties();
char * CBP_Get_Doctors();
char * CBP_Search_Consultations(const char* specialties, char* id, char* dateDeb, char* dateFin);
bool CBP_Book_Consultation(char* consultationId, const char* reason, int id);
char * CBP_All_Client();

int estPresent(int socket);
void ajoute(int socket, unsigned long long id, const char* firstName,const char* lastName, const char * NoPatient);
void retire(int socket, unsigned long long id);

typedef struct
{
	char ipClient[30];
	char nomClient[30];
	char prenomClient[30];
	char noClient[30];
}structClient;

structClient clients[NB_MAX_CLIENTS];
int clientSocket[NB_MAX_CLIENTS];
int nbClients = 0;

pthread_mutex_t mutexClients = PTHREAD_MUTEX_INITIALIZER;

bool CBP(char* requete, char* reponse,int socket)
{
	char *ptr = strtok(NULL,"#");

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
		return false;
	}

	if (strcmp(ptr,"LOGOUT") == 0)
	{
		CBP_Logout(socket);
		return false;
	}

	if (strcmp(ptr, "GET_SPECIALTIES") == 0) 
	{
    	char* res = CBP_Get_Specialties();  // "id;nom#..."
    	if (res) 
    	{
        	snprintf(reponse, 200, "GET_SPECIALTIES#ok#%s", res);
        	free(res);
    	} 
	    else 
	    {
	        snprintf(reponse, 200, "GET_SPECIALTIES#ko");
	    }
	    return false;
	}

	if (strcmp(ptr, "GET_DOCTORS") == 0) 
	{
    	char* res = CBP_Get_Doctors();
    	if (res) 
    	{
        	snprintf(reponse, 200, "GET_DOCTORS#ok#%s", res);
        	free(res);
        	return false;
    	} 
	    else 
	    {
	        snprintf(reponse, 200, "GET_DOCTORS#ko");
	    }
	    return false;
	}

    if (strcmp(ptr, "SEARCH_CONSULTATIONS") == 0)
	{
	    char *specialty  = strtok(NULL, "#");
	    char *doctor     = strtok(NULL, "#");
	    char *startDate  = strtok(NULL, "#");
	    char *endDate    = strtok(NULL, "#");

	    // garde-fous simples
	    if (!specialty) specialty = (char*)"*";
	    if (!doctor)    doctor    = (char*)"*";
	    if (!startDate) startDate = (char*)"";
	    if (!endDate)   endDate   = (char*)"";

	    char *data = CBP_Search_Consultations(specialty, doctor, startDate, endDate);

	    // Toujours renvoyer ok#, data possiblement vide
	    snprintf(reponse, 200, "SEARCH_CONSULTATIONS#ok#%s", (data ? data : ""));
	    if (data) free(data);
	    return false;
	}


	if (strcmp(ptr, "BOOK_CONSULTATION") == 0)
    {
        char* cons_id = strtok(NULL, "#");
        char* reason  = strtok(NULL, "#");
        char* idStr   = strtok(NULL, "#");
        if (!cons_id || !reason || !idStr) {
            snprintf(reponse, 200, "BOOK_CONSULTATION#ko#bad_parameters");
            return false;
        }
        int ID = atoi(idStr);
        bool ok = CBP_Book_Consultation(cons_id, reason, ID);
        snprintf(reponse, 200, "BOOK_CONSULTATION#%s", ok ? "ok" : "ko");
        return false;
    }
}

int estPresent(int socket)
{
	int indice = -1;
	pthread_mutex_lock(&mutexClients);
	for(int i=0 ; i<nbClients ; i++)
	{
		if (clientSocket[i] == socket) 
		{ 
			indice = i; break; 
		}
	}

	pthread_mutex_unlock(&mutexClients);
	return indice;
}

void ajoute(int socket, unsigned long long id, const char* firstName,const char* lastName, const char * NoPatient)
{
	char host[NI_MAXHOST] = {0};
    char serv[NI_MAXSERV] = {0};

    struct sockaddr_storage sa;
    socklen_t len = sizeof(sa);

    if (getpeername(socket, (struct sockaddr*)&sa, &len) == 0)
    {
        if (getnameinfo((struct sockaddr*)&sa, len,
                        host, sizeof(host),
                        serv, sizeof(serv),
                        NI_NUMERICHOST | NI_NUMERICSERV) != 0)
        {
	        printf("Erreur d'ajout le client n'a pas pu être connecté");
        }
    }
    else
    {
        printf("Erreur d'ajout le client n'a pas pu être connecté");
    }


	pthread_mutex_lock(&mutexClients);
	clientSocket[nbClients] = socket;

	strncpy(clients[nbClients].ipClient, host, sizeof(clients[nbClients].ipClient) - 1);
	clients[nbClients].ipClient[sizeof(clients[nbClients].ipClient) - 1] = '\0';

	strncpy(clients[nbClients].nomClient,     lastName,  sizeof(clients[nbClients].nomClient) - 1);
	clients[nbClients].nomClient[sizeof(clients[nbClients].nomClient) - 1] = '\0';

	strncpy(clients[nbClients].prenomClient,  firstName, sizeof(clients[nbClients].prenomClient) - 1);
	clients[nbClients].prenomClient[sizeof(clients[nbClients].prenomClient) - 1] = '\0';

	strncpy(clients[nbClients].noClient,      NoPatient, sizeof(clients[nbClients].noClient) - 1);
	clients[nbClients].noClient[sizeof(clients[nbClients].noClient) - 1] = '\0';

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
	{
		clients[i] = clients[i+1];
		clientSocket[i] = clientSocket[i+1];
	}

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
				ajoute(socket, id, firstName, lastName, NoPatient);

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

char* CBP_Get_Specialties()
{
    MYSQL* conn = mysql_init(nullptr);
    if (!conn) return nullptr;
    if (!mysql_real_connect(conn,"localhost","Student","PassStudent1_","PourStudent",0,nullptr,0)) {
        mysql_close(conn); return nullptr;
    }
    if (mysql_query(conn, "SELECT id, name FROM specialties;")) {
        mysql_close(conn); return nullptr;
    }

    MYSQL_RES* res = mysql_store_result(conn);
    if (!res) { mysql_close(conn); return nullptr; }

    std::string out;                // "id;nom#id;nom#..."
    MYSQL_ROW row; bool first = true;
    while ((row = mysql_fetch_row(res)) != nullptr) {
        if (!row[0] || !row[1]) continue;
        if (!first) out.push_back('#');
        out += row[0];
        out.push_back(';');
        out += row[1];
        first = false;
    }

    mysql_free_result(res);
    mysql_close(conn);

    if (out.empty()) return nullptr;
    return strdup(out.c_str());     // free() côté appelant
}

char* CBP_Get_Doctors()
{
    MYSQL* conn = mysql_init(nullptr);
    if (!conn) return nullptr;
    if (!mysql_real_connect(conn,"localhost","Student","PassStudent1_","PourStudent",0,nullptr,0)) {
        mysql_close(conn); return nullptr;
    }
    if (mysql_query(conn, "SELECT id, specialty_id, last_name, first_name FROM doctors;")) {
        mysql_close(conn); return nullptr;
    }

    MYSQL_RES* res = mysql_store_result(conn);
    if (!res) { mysql_close(conn); return nullptr; }

    // Format: id;last_name;first_name;specialty_id#id;last_name;first_name;specialty_id...
    std::string out;
    MYSQL_ROW row; bool first = true;
    while ((row = mysql_fetch_row(res)) != nullptr) {
        if (!row[0] || !row[1] || !row[2] || !row[3]) continue;
        if (!first) out.push_back('#');
        out += row[0];          // id
        out.push_back(';');
        out += row[2];          // last_name
        out.push_back(';');
        out += row[3];          // first_name
        out.push_back(';');
        out += row[1];          // specialty_id
        first = false;
    }

    mysql_free_result(res);
    mysql_close(conn);

    if (out.empty()) return nullptr;
    return strdup(out.c_str());   // free() côté appelant
}

char* CBP_Search_Consultations(const char* specialties, char* doctorKey, char* dateDeb, char* dateFin)
{
    MYSQL *conn = mysql_init(nullptr);
    if (!conn) return nullptr;
    if (!mysql_real_connect(conn,"localhost","Student","PassStudent1_","PourStudent",0,nullptr,0)) {
        mysql_close(conn); return nullptr;
    }

    // Filtre: par spécialité OU par nom de famille du docteur
    // Cols: id, docteur "Last First", spécialité, date, heure
    char sql[512];
    snprintf(sql, sizeof sql,
        "SELECT c.id, CONCAT(d.last_name,' ',d.first_name) AS doctor, "
        "s.name AS spec, DATE_FORMAT(c.date,'%%Y-%%m-%%d') AS d, c.hour AS h "
        "FROM consultations c "
        "JOIN doctors d ON c.doctor_id = d.id "
        "JOIN specialties s ON d.specialty_id = s.id "
        "WHERE c.patient_id IS NULL "
        "AND (s.name = '%s' OR d.last_name = '%s') "
        "AND c.date BETWEEN '%s' AND '%s';",
        specialties, doctorKey, dateDeb, dateFin);

    if (mysql_query(conn, sql)) {
        mysql_close(conn); return nullptr;
    }

    MYSQL_RES *res = mysql_store_result(conn);
    if (!res) { mysql_close(conn); return nullptr; }

    std::string out; // id#docteur#specialite#date#heure|...
    MYSQL_ROW row;
    bool first = true;
    while ((row = mysql_fetch_row(res)) != nullptr) {
        // row[0]=id, row[1]=doctor, row[2]=spec, row[3]=date, row[4]=hour
        if (!row[0] || !row[1] || !row[2] || !row[3] || !row[4]) continue;
        if (!first) out.push_back('|');
        out += row[0]; out.push_back('#');
        out += row[1]; out.push_back('#');
        out += row[2]; out.push_back('#');
        out += row[3]; out.push_back('#');
        out += row[4];
        first = false;
    }

    mysql_free_result(res);
    mysql_close(conn);

    if (out.empty()) return strdup("");   // pas d’erreur, juste vide
    return strdup(out.c_str());           // free() côté appelant
}


bool CBP_Book_Consultation(char* consultationId, const char* reason, int id) 
{
    MYSQL* connection = mysql_init(NULL);
    if (!connection) { fprintf(stderr, "mysql_init failed\n"); return false; }

    if (!mysql_real_connect(connection, "localhost","Student","PassStudent1_","PourStudent",0,NULL,0)) {
        fprintf(stderr, "connect : %s\n", mysql_error(connection));
        mysql_close(connection);
        return false;
    }

    // Échapper reason
    char reasonEsc[2*512+1]; // assez grand pour raison <=512
    unsigned long len = mysql_real_escape_string(connection, reasonEsc, reason, strlen(reason));
    reasonEsc[len] = '\0';

    int consID = atoi(consultationId);
    char sql_cmd[1024];
    // <-- virgule ajoutée entre patient_id et reason
    sprintf(sql_cmd,
            "UPDATE consultations "
            "SET patient_id = %d, reason = '%s' "
            "WHERE id = %d AND patient_id IS NULL;",
            id, reasonEsc, consID);

    if (mysql_query(connection, sql_cmd)) {
        fprintf(stderr, "Query : %s\n", mysql_error(connection));
        mysql_close(connection);
        return false;
    }

    bool ok = (mysql_affected_rows(connection) == 1);
    mysql_close(connection);
    return ok;
}

char * CBP_All_Client()
{
	int i;
	char rep[100];
	strcpy(rep, "ALL_CLIENT#ok#");

	pthread_mutex_lock(&mutexClients);
	for(i = 0; i < NB_MAX_CLIENTS; i++)
	{
		strcat(rep, "#");
		strcat(rep, clients[i].ipClient);
		strcat(rep, ";");
		strcat(rep, clients[i].nomClient);
		strcat(rep, ";");
		strcat(rep, clients[i].prenomClient);
		strcat(rep, ";");
		strcat(rep, clients[i].noClient);
	}
	pthread_mutex_unlock(&mutexClients);

	return rep;
}