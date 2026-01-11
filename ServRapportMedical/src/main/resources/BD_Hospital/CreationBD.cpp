#include <stdio.h>
#include <stdlib.h>
#include <mysql.h>
#include <time.h>
#include <string.h>

typedef struct {
  int  id;
  char name[30];
} SPECIALTY;

typedef struct {
  int  id;
  int  specialty_id;
  char last_name[30];
  char first_name[30];
  char username[30];
  char password[30];
} DOCTOR;

typedef struct {
  int  id;
  char last_name[30];
  char first_name[30];
  char birth_date[20];
  char last_login_ip[15];
} PATIENT;

typedef struct {
  int  id;
  int  doctor_id;
  int  patient_id;
  char date[20];
  char hour[10];
  char reason[100];
} CONSULTATION;

typedef struct {
  int  id;
  int  doctor_id;
  int  patient_id;
  char date[20];
  char description[255]; // Un texte plus long pour le rapport
} REPORT;

SPECIALTY specialties[] = {
  {-1, "Cardiologie"},
  {-1, "Dermatologie"},
  {-1, "Neurologie"},
  {-1, "Ophtalmologie"}
};
int nbSpecialties = 4;

DOCTOR doctors[] = {
  {-1, 1, "Dupont", "Alice", "d1", "pass123"},
  {-1, 2, "Lemoine", "Bernard", "d2", "pass123"},
  {-1, 3, "Martin", "Claire", "d3", "pass123"},
  {-1, 2, "Maboul", "Paul", "d4", "pass123"},
  {-1, 1, "Coptere", "Elie", "d5", "pass123"},
  {-1, 3, "Merad", "Gad", "d6", "pass123"}
};
int nbDoctors = 6;

PATIENT patients[] = {
  {-1, "Durand", "Jean", "1980-05-12", "192.168.1.10"},
  {-1, "Petit", "Sophie", "1992-11-30", "192.168.1.11"},
  {-1, "Samatar", "Abdoulkader", "2005-08-06", "10.51.25.126"},
  {-1, "Eric", "Luis", "1999-12-15", "192.168.3.32"},
  {-1, "Jolie", "Anne", "1965-11-30", "192.168.9.144"}
};
int nbPatients = 5;

CONSULTATION consultations[] = {
  {-1, 3,  1, "2025-10-01", "09:00", "Check-up"},
  {-1, 1,  2, "2025-10-02", "14:30", "Premier rendez-vous"},
  {-1, 2,  1, "2025-10-03", "11:15", "Douleurs persistantes"},
  {-1, 4, -1, "2025-10-04", "9:00", ""},
  {-1, 4, -1, "2025-10-04", "9:30", ""},
  {-1, 4, -1, "2025-10-04", "10:00", ""},
  {-1, 4, -1, "2025-10-04", "10:30", ""},
  {-1, 6, -1, "2025-10-07", "13:00", ""},
  {-1, 6, -1, "2025-10-07", "13:30", ""},
  {-1, 6, -1, "2025-10-07", "14:00", ""},
  {-1, 6, -1, "2025-10-07", "14:30", ""},
};
int nbConsultations = 11;

REPORT reports[] = {
  // Rapports du Docteur 1 (Dupont Alice)
  {-1, 1, 1, "2025-10-01", "Patient se plaint de migraines frequentes. Tension normale."},
  {-1, 1, 2, "2025-10-02", "Examen de routine. Tout est en ordre."},
  {-1, 1, 1, "2025-10-15", "Suivi migraines. Prescription de calmants legers."},
  
  // Rapports du Docteur 2 (Lemoine Bernard)
  {-1, 2, 3, "2025-10-05", "Douleurs thoraciques. ECG realise. Resultats en attente."},
  {-1, 2, 4, "2025-10-06", "Consultation pour fatigue chronique. Prise de sang effectuee."},
  {-1, 2, 3, "2025-10-20", "Analyse des resultats ECG. Rythme cardiaque regulier."},
};
int nbReports = 6;

void finish_with_error(MYSQL *con) {
  fprintf(stderr, "%s\n", mysql_error(con));
  mysql_close(con);
  exit(1);
}

int main() {
  MYSQL* connexion = mysql_init(NULL);
  if (!mysql_real_connect(connexion, "localhost", "Student", "PassStudent1_", "PourStudent", 0, NULL, 0)) {
    finish_with_error(connexion);
  }

  mysql_query(connexion, "DROP TABLE IF EXISTS reports;");
  mysql_query(connexion, "DROP TABLE IF EXISTS consultations;");
  mysql_query(connexion, "DROP TABLE IF EXISTS patients;");
  mysql_query(connexion, "DROP TABLE IF EXISTS doctors;");
  mysql_query(connexion, "DROP TABLE IF EXISTS specialties;");

  if (mysql_query(connexion, "CREATE TABLE specialties (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(30));"))
    finish_with_error(connexion);

  if (mysql_query(connexion, "CREATE TABLE doctors ("
                             "id INT AUTO_INCREMENT PRIMARY KEY, "
                             "specialty_id INT, "
                             "last_name VARCHAR(30), "
                             "first_name VARCHAR(30), "
                             "username VARCHAR(30) UNIQUE NOT NULL, "
                             "password VARCHAR(255) NOT NULL, "
                             "FOREIGN KEY (specialty_id) REFERENCES specialties(id));"))
    finish_with_error(connexion);

  if (mysql_query(connexion, "CREATE TABLE patients ("
                             "id INT AUTO_INCREMENT PRIMARY KEY, "
                             "last_name VARCHAR(30), "
                             "first_name VARCHAR(30), "
                             "birth_date DATE,"
                             "last_login_ip VARCHAR(15));"))
    finish_with_error(connexion);

  if (mysql_query(connexion, "CREATE TABLE consultations ("
                             "id INT AUTO_INCREMENT PRIMARY KEY, "
                             "doctor_id INT NOT NULL, "
                             "patient_id INT NULL, "
                             "date DATE, "
                             "hour VARCHAR(10), "
                             "reason VARCHAR(100), "
                             "FOREIGN KEY (doctor_id) REFERENCES doctors(id), "
                             "FOREIGN KEY (patient_id) REFERENCES patients(id),"

                             "CONSTRAINT UQ_Consultation UNIQUE (doctor_id, date, hour))"))
    finish_with_error(connexion);

  if (mysql_query(connexion, "CREATE TABLE reports ("
                             "id INT AUTO_INCREMENT PRIMARY KEY, "
                             "doctor_id INT NOT NULL, "
                             "patient_id INT NOT NULL, "
                             "date DATE, "
                             "description TEXT, "
                             "FOREIGN KEY (doctor_id) REFERENCES doctors(id), "
                             "FOREIGN KEY (patient_id) REFERENCES patients(id));"))
    finish_with_error(connexion);

  char request[512];
  for (int i = 0; i < nbSpecialties; i++) {
    sprintf(request, "INSERT INTO specialties VALUES (NULL, '%s');", specialties[i].name);
    if (mysql_query(connexion, request)) finish_with_error(connexion);
  }

  for (int i = 0; i < nbDoctors; i++) {
    sprintf(request, "INSERT INTO doctors VALUES (NULL, %d, '%s', '%s', '%s', '%s');",
            doctors[i].specialty_id, doctors[i].last_name, doctors[i].first_name,
            doctors[i].username, doctors[i].password); 
    if (mysql_query(connexion, request)) finish_with_error(connexion);
  }

  for (int i = 0; i < nbPatients; i++) {
    sprintf(request, "INSERT INTO patients VALUES (NULL, '%s', '%s', '%s','%s');",
            patients[i].last_name, patients[i].first_name, patients[i].birth_date, patients[i].last_login_ip);
    if (mysql_query(connexion, request)) finish_with_error(connexion);
  }

  for (int i = 0; i < nbConsultations; i++) {
    if (consultations[i].patient_id == -1) {
      sprintf(request, "INSERT INTO consultations (doctor_id, patient_id, date, hour, reason) "
                       "VALUES (%d, NULL, '%s', '%s', '%s');",
              consultations[i].doctor_id, consultations[i].date, consultations[i].hour, consultations[i].reason);
    } else {
      sprintf(request, "INSERT INTO consultations (doctor_id, patient_id, date, hour, reason) "
                       "VALUES (%d, %d, '%s', '%s', '%s');",
              consultations[i].doctor_id, consultations[i].patient_id, consultations[i].date,
              consultations[i].hour, consultations[i].reason);
    }
    if (mysql_query(connexion, request)) finish_with_error(connexion);
  }
  for (int i = 0; i < nbReports; i++) 
  {
    sprintf(request, "INSERT INTO reports VALUES (NULL, %d, %d, '%s', '%s');",
            reports[i].doctor_id, reports[i].patient_id, 
            reports[i].date, reports[i].description);
            
    if (mysql_query(connexion, request)) finish_with_error(connexion);
  }

  mysql_close(connexion);
  return 0;
}

