Architecture : Séparation propre en couches (api, server.dao, server.util, common.entity).

Base de données : Connexion MySQL (DatabaseConnector) et toutes les requêtes SQL dans les DAO (ConsultationDAO, PatientDAO, DoctorDAO, SpecialtyDAO).

API REST : Les 4 routes demandées par le prof sont actives et répondent aux normes :

GET /api/consultations (Liste & Filtres)

POST /api/consultations (Ajout dispo) & PUT (Réservation) & DELETE (Annulation/Libération)

POST /api/patients (Login/Inscription)

GET /api/doctors & /api/specialties (Listes pour menus déroulants)

Refactoring : Code nettoyé avec HttpUtils pour éviter les répétitions (DRY).

Tests :  http (externe) sur la VM Oracle.
        -sudo dnf install httpie
        -http --version
        -http GET localhost:8080/api/doctors
        -http GET localhost:8080/api/specialties
    --creation patient
        -http POST localhost:8080/api/patients \
                    lastName=Dupont \
                    firstName=Jean \
                    birthDate=1990-01-01 \
                    newPatient:=true
    --login au patient cree 
        -http POST localhost:8080/api/patients \
                  lastName=Dupont \
                  firstName=Jean \
                  birthDate=1990-01-01 \
                  newPatient:=false
