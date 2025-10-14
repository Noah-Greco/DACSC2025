// 1 = coché 0 = Non coché

//requete             				sens      		data1           data2   		data3			data4			réponse1		réponse2		réponse3
//LOGIN							CL -> S -> CL 		Nom				Prénom			No Patient		1 ou 0			oui ou non		No Pat si nv	/
//LOGOUT						CL -> S -> CL 		 /					/				/				/				/			/				/
//GET_SPECIALTIES				S -> CL 			/					/			/					/			Id Spé			Nom Spé			/
//GET_DOCTORS					S -> CL 			/					/			/					/			Id Med			Nom Med			Prénom Med
//SEARCH_CONSULTATIONS			CL -> S -> CL 		Spé Med			Date deb		Date Fin 						Liste Consultation  /			/
//BOOK_CONSULTATION				CL -> S -> CL 		Consult ID		Raison				/				/			Oui ou Non			/			/

#ifndef CBP_H
#define CBP_H

#define NB_MAX_CLIENTS 100

bool CBP(char* requete, char* reponse,int socket);
char CBP_Login(const char* user,const char* password);
void CBP_Logout();
char CBP_Get_Specialties();
char CBP_Get_Doctors();
char CBP_Search_Consultations(const char* specialties, char* id, char* dateDeb, char* dateFin);
char CBP_Book_Consultation(char* consultationId, char* reason);
int estPresent(int socket);
void ajoute(int socket);
void retire(int socket, unsigned long long id);

#endif