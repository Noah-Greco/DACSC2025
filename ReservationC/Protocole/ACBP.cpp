#include "ACBP.hpp"
#include "CBP.hpp"

#include <string.h>
#include <string>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <pthread.h>
#include <mysql/mysql.h>

bool ACBP(char* requete, char* reponse);
char * LIST_CLIENTS();

bool ACBP(char * requete, char * reponse)
{
    char *ptr = strtok(NULL,"#");

    if(strcmp(ptr, "ALL_CLIENT") == 0)
    {
        char * res = CBP_All_Client();

        if (res) 
    	{
        	snprintf(reponse, 200, "ALL_CLIENT#ok#%s", res);
        	free(res);
            return true;
    	} 
        else 
	    {
	        snprintf(reponse, 200, "ALL_CLIENT#ko");
            return false;
	    }
    }

    else
    {
        strcpy(reponse, "ACBP_BAD_REQUEST");
        return false;
    }
}