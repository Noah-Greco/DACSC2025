bool CBP(char* requete, char* reponse,int socket);
char CBP_Login(const char* user,const char* password);
void CBP_Logout();
char CBP_Get_Specialties();
char CBP_Get_Doctors();
char CBP_Search_Consultations(const char* specialties, char* id, char* dateDeb, char* dateFin);
char CBP_Book_Consultation(char* consultationId, char* reason);

bool CBP(char* requete, char* reponse,int socket)