#include "ACBP.hpp"

#include <string.h>
#include <string>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <pthread.h>
#include <mysql/mysql.h>

bool ACBP(char* requete, char* reponse,int socket);
char * LIST_CLIENTS();

bool ACBP(char * requete, char * reponse, int socket)
{
    
}