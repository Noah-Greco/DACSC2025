#ifndef ACBP_HPP
#define ACBP_HPP

#define NB_MAX_CLIENTS 100

bool ACBP(char* requete, char* reponse,int socket);
char * LIST_CLIENTS();

#endif