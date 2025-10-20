#include "mainwindowclientconsultationbooker.h"
#include "ui_mainwindowclientconsultationbooker.h"

#include <QInputDialog>
#include <QMessageBox>
#include <iostream>
#include <vector>
#include <sstream>

using namespace std;

MainWindowClientConsultationBooker::MainWindowClientConsultationBooker(QWidget *parent)
    : QMainWindow(parent)
    , ui(new Ui::MainWindowClientConsultationBooker), socketServeur(-1), connecte(false)
{
    ui->setupUi(this);
    logoutOk();

    if (!connecterServeur(ipServeur, portServeur))
    {
        dialogError("Erreur", "Impossible de se connecter au serveur");
    }

    // Configuration de la table des employes (Personnel Garage)
    ui->tableWidgetConsultations->setColumnCount(5);
    ui->tableWidgetConsultations->setRowCount(0);
    QStringList labelsTableConsultations;
    labelsTableConsultations << "Id" << "Spécialité" << "Médecin" << "Date" << "Heure";
    ui->tableWidgetConsultations->setHorizontalHeaderLabels(labelsTableConsultations);
    ui->tableWidgetConsultations->setSelectionMode(QAbstractItemView::SingleSelection);
    ui->tableWidgetConsultations->setSelectionBehavior(QAbstractItemView::SelectRows);
    ui->tableWidgetConsultations->setEditTriggers(QAbstractItemView::NoEditTriggers);
    ui->tableWidgetConsultations->horizontalHeader()->setVisible(true);
    ui->tableWidgetConsultations->horizontalHeader()->setStretchLastSection(true);
    ui->tableWidgetConsultations->verticalHeader()->setVisible(false);
    ui->tableWidgetConsultations->horizontalHeader()->setStyleSheet("background-color: lightyellow");
    int columnWidths[] = {40, 150, 200, 150, 100};
    for (int col = 0; col < 5; ++col)
        ui->tableWidgetConsultations->setColumnWidth(col, columnWidths[col]);
}

MainWindowClientConsultationBooker::~MainWindowClientConsultationBooker()
{
    deconnecterServeur();
    delete ui;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///// Fonctions utiles Table des livres encodés (ne pas modifier) ////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void MainWindowClientConsultationBooker::addTupleTableConsultations(int id,
                                                                    string specialty,
                                                                    string doctor,
                                                                    string date,
                                                                    string hour)
{
    int nb = ui->tableWidgetConsultations->rowCount();
    nb++;
    ui->tableWidgetConsultations->setRowCount(nb);
    ui->tableWidgetConsultations->setRowHeight(nb-1,10);

    // id
    QTableWidgetItem *item = new QTableWidgetItem;
    item->setTextAlignment(Qt::AlignCenter);
    item->setText(QString::number(id));
    ui->tableWidgetConsultations->setItem(nb-1,0,item);

    // specialty
    item = new QTableWidgetItem;
    item->setTextAlignment(Qt::AlignCenter);
    item->setText(QString::fromStdString(specialty));
    ui->tableWidgetConsultations->setItem(nb-1,1,item);

    // doctor
    item = new QTableWidgetItem;
    item->setTextAlignment(Qt::AlignCenter);
    item->setText(QString::fromStdString(doctor));
    ui->tableWidgetConsultations->setItem(nb-1,2,item);

    // date
    item = new QTableWidgetItem;
    item->setTextAlignment(Qt::AlignCenter);
    item->setText(QString::fromStdString(date));
    ui->tableWidgetConsultations->setItem(nb-1,3,item);

    // hour
    item = new QTableWidgetItem;
    item->setTextAlignment(Qt::AlignCenter);
    item->setText(QString::fromStdString(hour));
    ui->tableWidgetConsultations->setItem(nb-1,4,item);
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
void MainWindowClientConsultationBooker::clearTableConsultations() {
    ui->tableWidgetConsultations->setRowCount(0);
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
int MainWindowClientConsultationBooker::getSelectionIndexTableConsultations() const
{
    QModelIndexList list = ui->tableWidgetConsultations->selectionModel()->selectedRows();
    if (list.size() == 0) return -1;
    QModelIndex index = list.at(0);
    int ind = index.row();
    return ind;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///// Fonctions utiles des comboboxes (ne pas modifier) //////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void MainWindowClientConsultationBooker::addComboBoxSpecialties(string specialty) {
    ui->comboBoxSpecialties->addItem(QString::fromStdString(specialty));
}

string MainWindowClientConsultationBooker::getSelectionSpecialty() const {
    return ui->comboBoxSpecialties->currentText().toStdString();
}

void MainWindowClientConsultationBooker::clearComboBoxSpecialties() {
    ui->comboBoxSpecialties->clear();
    this->addComboBoxSpecialties("--- TOUTES ---");
}

void MainWindowClientConsultationBooker::addComboBoxDoctors(string doctor) {
    ui->comboBoxDoctors->addItem(QString::fromStdString(doctor));
}

string MainWindowClientConsultationBooker::getSelectionDoctor() const {
    return ui->comboBoxDoctors->currentText().toStdString();
}

void MainWindowClientConsultationBooker::clearComboBoxDoctors() {
    ui->comboBoxDoctors->clear();
    this->addComboBoxDoctors("--- TOUS ---");
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///// Fonction utiles de la fenêtre (ne pas modifier) ////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
string MainWindowClientConsultationBooker::getLastName() const {
    return ui->lineEditLastName->text().toStdString();
}

string MainWindowClientConsultationBooker::getFirstName() const {
    return ui->lineEditFirstName->text().toStdString();
}

int MainWindowClientConsultationBooker::getPatientId() const {
    return ui->spinBoxId->value();
}

void MainWindowClientConsultationBooker::setLastName(string value) {
    ui->lineEditLastName->setText(QString::fromStdString(value));
}

string MainWindowClientConsultationBooker::getStartDate() const {
    return ui->dateEditStartDate->date().toString("yyyy-MM-dd").toStdString();
}

string MainWindowClientConsultationBooker::getEndDate() const {
    return ui->dateEditEndDate->date().toString("yyyy-MM-dd").toStdString();
}

void MainWindowClientConsultationBooker::setFirstName(string value) {
    ui->lineEditFirstName->setText(QString::fromStdString(value));
}

void MainWindowClientConsultationBooker::setPatientId(int value) {
    if (value > 0) ui->spinBoxId->setValue(value);
}

bool MainWindowClientConsultationBooker::isNewPatientSelected() const {
    return ui->checkBoxNewPatient->isChecked();
}

void MainWindowClientConsultationBooker::setNewPatientChecked(bool state) {
    ui->checkBoxNewPatient->setChecked(state);
}

void MainWindowClientConsultationBooker::setStartDate(string date) {
    QDate qdate = QDate::fromString(QString::fromStdString(date), "yyyy-MM-dd");
    if (qdate.isValid()) ui->dateEditStartDate->setDate(qdate);
}

void MainWindowClientConsultationBooker::setEndDate(string date) {
    QDate qdate = QDate::fromString(QString::fromStdString(date), "yyyy-MM-dd");
    if (qdate.isValid()) ui->dateEditEndDate->setDate(qdate);
}

void MainWindowClientConsultationBooker::loginOk() {
    ui->lineEditLastName->setReadOnly(true);
    ui->lineEditFirstName->setReadOnly(true);
    ui->spinBoxId->setReadOnly(true);
    ui->checkBoxNewPatient->setEnabled(false);
    ui->pushButtonLogout->setEnabled(true);
    ui->pushButtonLogin->setEnabled(false);
    ui->pushButtonRechercher->setEnabled(true);
    ui->pushButtonReserver->setEnabled(true);
}

void MainWindowClientConsultationBooker::logoutOk() {
    ui->lineEditLastName->setReadOnly(false);
    setLastName("");
    ui->lineEditFirstName->setReadOnly(false);
    setFirstName("");
    ui->spinBoxId->setReadOnly(false);
    setPatientId(1);
    ui->checkBoxNewPatient->setEnabled(true);
    setNewPatientChecked(false);
    ui->pushButtonLogout->setEnabled(false);
    ui->pushButtonLogin->setEnabled(true);
    ui->pushButtonRechercher->setEnabled(false);
    ui->pushButtonReserver->setEnabled(false);
    setStartDate("2025-09-15");
    setEndDate("2025-12-31");
    clearComboBoxDoctors();
    clearComboBoxSpecialties();
    clearTableConsultations();
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///// Fonctions permettant d'afficher des boites de dialogue (ne pas modifier) ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void MainWindowClientConsultationBooker::dialogMessage(const string& title,const string& message) {
   QMessageBox::information(this,QString::fromStdString(title),QString::fromStdString(message));
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
void MainWindowClientConsultationBooker::dialogError(const string& title,const string& message) {
   QMessageBox::critical(this,QString::fromStdString(title),QString::fromStdString(message));
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
string MainWindowClientConsultationBooker::dialogInputText(const string& title,const string& question) {
    return QInputDialog::getText(this,QString::fromStdString(title),QString::fromStdString(question)).toStdString();
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
int MainWindowClientConsultationBooker::dialogInputInt(const string& title,const string& question) {
    return QInputDialog::getInt(this,QString::fromStdString(title),QString::fromStdString(question));
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///// Fonctions gestion des boutons (TO DO) //////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
/*void MainWindowClientConsultationBooker::on_pushButtonLogin_clicked()
{
    string lastName = this->getLastName();
    string firstName = this->getFirstName();
    int patientId = this->getPatientId();
    bool newPatient = this->isNewPatientSelected();

    cout << "lastName = " << lastName << endl;
    cout << "FirstName = " << firstName << endl;
    cout << "patientId = " << patientId << endl;
    cout << "newPatient = " << newPatient << endl;

    loginOk();
}

void MainWindowClientConsultationBooker::on_pushButtonLogout_clicked()
{
    logoutOk();
}

void MainWindowClientConsultationBooker::on_pushButtonRechercher_clicked()
{
    string specialty = this->getSelectionSpecialty();
    string doctor = this->getSelectionDoctor();
    string startDate = this->getStartDate();
    string endDate = this->getEndDate();

    cout << "specialty = " << specialty << endl;
    cout << "doctor = " << doctor << endl;
    cout << "startDate = " << startDate << endl;
    cout << "endDate = " << endDate << endl;
}

void MainWindowClientConsultationBooker::on_pushButtonReserver_clicked()
{
    int selectedTow = this->getSelectionIndexTableConsultations();

    cout << "selectedRow = " << selectedTow << endl;
}
*/

bool MainWindowClientConsultationBooker::connecterServeur(const string& ipServeur, int port)
{
    this->ipServeur = ipServeur;
    this->portServeur = port;
    
    socketServeur = ClientSocket(ipServeur.c_str(), port);
    if (socketServeur == -1)
    {
        connecte = false;
        return connecte;
    }
    
    connecte = true;
    cout << "Connexion au serveur " << ipServeur << ":" << port << " réussie" << endl;
    return connecte;
}

void MainWindowClientConsultationBooker::deconnecterServeur()
{
    if (connecte && socketServeur != -1)
    {
        closeSocket(socketServeur);
        socketServeur = -1;
        connecte = false;
        cout << "Déconnexion du serveur" << endl;
    }
}

bool MainWindowClientConsultationBooker::envoyerRequete(const string& requete, string& reponse)
{
    if (!connecte || socketServeur == -1)
    {
        cout << "Erreur: Pas connecté au serveur" << endl;
        return false;
    }
    
    if (Send(socketServeur, requete.c_str(), requete.length()) < 0)
    {
        cout << "Erreur envoi requête" << endl;
        return false;
    }
    
    char buffer[1024];
    int nbRecu = Receive(socketServeur, buffer);
    if (nbRecu < 0)
    {
        cout << "Erreur réception réponse" << endl;
        return false;
    }
    
    reponse = string(buffer);
    cout << "Requête: " << requete << endl;
    cout << "Réponse: " << reponse << endl;
    return true;
}

void MainWindowClientConsultationBooker::closeEvent(QCloseEvent* event)
{
    deconnecterServeur();
    QMainWindow::closeEvent(event);
}

bool MainWindowClientConsultationBooker::estConnecte() const
{
    return connecte;
}

bool MainWindowClientConsultationBooker::loginPatient(const string& nom, const string& prenom, int patientId, bool nouveauPatient)
{
    string requete = string(LOGIN) + diez + nom + diez + prenom + diez + to_string(patientId) + diez + (nouveauPatient ? "1" : "0");
    string reponse;
    
    if (!envoyerRequete(requete, reponse))
        return false;
    
    if (reponse.find(string(LOGIN) + diez + string(OK)) == 0)
    {
        cout << "Login réussi" << endl;
        return true;
    }
    else
    {
        cout << "Erreur login: " << reponse << endl;
        return false;
    }
}

void MainWindowClientConsultationBooker::logoutPatient()
{
    string requete = LOGOUT;
    string reponse;
    
    envoyerRequete(requete, reponse);
    cout << "Logout effectué" << endl;
}

bool MainWindowClientConsultationBooker::chargerSpecialties()
{
    string requete = GET_SPECIALTIES;
    string reponse;
    
    if (!envoyerRequete(requete, reponse))
        return false;
    
    if (reponse.find(string(GET_SPECIALTIES) + diez + string(OK)) == 0)
    {
        clearComboBoxSpecialties();
        addComboBoxSpecialties(TOUTES);
        
        // Format attendu: GET_SPECIALTIES#ok##nom1|nom2|nom3...
        string data = reponse.substr(strlen(GET_SPECIALTIES) + 4); // Enlever "GET_SPECIALTIES#ok#"
        
        // Enlever le premier # s'il existe
        if (!data.empty() && data[0] == '#') {
            data = data.substr(1);
        }
        
        // Parser les spécialités séparées par |
        stringstream ss(data);
        string specialite;
        while (getline(ss, specialite, '|')) {
            if (!specialite.empty()) {
                addComboBoxSpecialties(specialite);
            }
        }
        return true;
    }
    else
    {
        cout << "Erreur chargement spécialités: " << reponse << endl;
        return false;
    }
}

bool MainWindowClientConsultationBooker::chargerDocteurs()
{
    string requete = GET_DOCTORS;
    string reponse;
    
    if (!envoyerRequete(requete, reponse))
        return false;
    
    if (reponse.find(string(GET_DOCTORS) + diez + string(OK)) == 0)
    {
        clearComboBoxDoctors();
        addComboBoxDoctors(TOUS);
        
        // Format attendu: GET_DOCTORS#ok##id1#nom1#specialite1|id2#nom2#specialite2|...
        string data = reponse.substr(strlen(GET_DOCTORS) + 4); // Enlever "GET_DOCTORS#ok#"
        
        // Enlever le premier # s'il existe
        if (!data.empty() && data[0] == '#') {
            data = data.substr(1);
        }
        
        // Parser les docteurs séparés par |
        stringstream ss(data);
        string docteurInfo;
        while (getline(ss, docteurInfo, '|')) {
            if (!docteurInfo.empty()) {
                // Extraire le nom (deuxième champ) de "id#nom#specialite"
                stringstream ssDocteur(docteurInfo);
                string id, nom, specialite;
                if (getline(ssDocteur, id, '#') && getline(ssDocteur, nom, '#')) {
                    addComboBoxDoctors(nom);
                }
            }
        }
        return true;
    }
    else
    {
        cout << "Erreur chargement médecins: " << reponse << endl;
        return false;
    }
}

bool MainWindowClientConsultationBooker::rechercherConsultations(const string& specialite, const string& docteur, 
                                                               const string& dateDebut, const string& dateFin)
{
    string requete = string(SEARCH_CONSULTATIONS) + diez + specialite + diez + docteur + diez + dateDebut + diez + dateFin;
    string reponse;
    
    if (!envoyerRequete(requete, reponse))
        return false;
    
    if (reponse.find(string(SEARCH_CONSULTATIONS) + diez + string(OK)) == 0)
    {
        clearTableConsultations();
        
        // Format attendu: SEARCH_CONSULTATIONS#ok##id1#docteur1#specialite1#date1#heure1|id2#docteur2#specialite2#date2#heure2|...
        string data = reponse.substr(strlen(SEARCH_CONSULTATIONS) + 4); // Enlever "SEARCH_CONSULTATIONS#ok#"
        
        // Enlever le premier # s'il existe
        if (!data.empty() && data[0] == '#') {
            data = data.substr(1);
        }
        
        // Exemple de format attendu pour 'data' :
        // "12#Dr. Martin#Cardiologie#2025-10-15#09:00|15#Dr. Dupont#Dermatologie#2025-10-16#11:30|"
        // Chaque consultation est séparée par '|', chaque champ par '#'
        // id#docteur#specialite#date#heure|id#docteur#specialite#date#heure|...
        //
        // Après le parsing, on obtient pour chaque consultation :
        //   id = 12, docteur = Dr. Martin, specialite = Cardiologie, date = 2025-10-15, heure = 09:00
        //   id = 15, docteur = Dr. Dupont, specialite = Dermatologie, date = 2025-10-16, heure = 11:30
        stringstream ss(data); // Utiliser stringstream pour le parsing
        string consultation;
        while (getline(ss, consultation, '|')) {
            if (!consultation.empty()) {
                // Pour chaque consultation, on extrait les champs séparés par '#'
                stringstream ssConsultation(consultation);
                string id, docteur, specialite, date, heure;
                if (getline(ssConsultation, id, '#') && 
                    getline(ssConsultation, docteur, '#') && 
                    getline(ssConsultation, specialite, '#') && 
                    getline(ssConsultation, date, '#') && 
                    getline(ssConsultation, heure, '#')) {
                    // Ici, on peut utiliser les valeurs extraites pour afficher dans le tableau
                    if (!id.empty()) {
                        addTupleTableConsultations(atoi(id.c_str()), specialite, docteur, date, heure);
                    }
                }
            }
        } // Fin du parsing des consultations
        return true;
    }
    else
    {
        cout << "Erreur recherche consultations: " << reponse << endl;
        return false;
    }
}

bool MainWindowClientConsultationBooker::reserverConsultation(int consultationId, const string& raison)
{
    string requete = string(BOOK_CONSULTATION) + diez + to_string(consultationId) + diez + raison;
    string reponse;
    
    if (!envoyerRequete(requete, reponse))
        return false;
    
    if (reponse.find(string(BOOK_CONSULTATION) + diez + string(OK)) == 0)
    {
        cout << "Consultation réservée avec succès" << endl;
        return true;
    }
    else
    {
        cout << "Erreur réservation: " << reponse << endl;
        return false;
    }
}

void MainWindowClientConsultationBooker::on_pushButtonLogin_clicked()
{
    string lastName = this->getLastName();
    string firstName = this->getFirstName();
    int patientId = this->getPatientId();
    bool newPatient = this->isNewPatientSelected();

    cout << "lastName = " << lastName << endl;
    cout << "FirstName = " << firstName << endl;
    cout << "patientId = " << patientId << endl;
    cout << "newPatient = " << newPatient << endl;

    if (!estConnecte())
    {
        dialogError("Erreur", "Pas connecté au serveur");
        return;
    }

    if (loginPatient(lastName, firstName, patientId, newPatient))
    {
        loginOk();
        chargerSpecialties();
        chargerDocteurs();
    }
    else
    {
        dialogError("Erreur", "Échec de la connexion");
    }
}

void MainWindowClientConsultationBooker::on_pushButtonLogout_clicked()
{
    if (estConnecte())
    {
        logoutPatient();
    }
    logoutOk();
}

void MainWindowClientConsultationBooker::on_pushButtonRechercher_clicked()
{
    string specialty = this->getSelectionSpecialty();
    string doctor = this->getSelectionDoctor();
    string startDate = this->getStartDate();
    string endDate = this->getEndDate();

    cout << "specialty = " << specialty << endl;
    cout << "doctor = " << doctor << endl;
    cout << "startDate = " << startDate << endl;
    cout << "endDate = " << endDate << endl;

    if (!estConnecte())
    {
        dialogError("Erreur", "Pas connecté au serveur");
        return;
    }

    if (rechercherConsultations(specialty, doctor, startDate, endDate))
    {
        dialogMessage("Recherche", "Recherche effectuée avec succès");
    }
    else
    {
        dialogError("Erreur", "Échec de la recherche");
    }
}

void MainWindowClientConsultationBooker::on_pushButtonReserver_clicked()
{
    int selectedRow = this->getSelectionIndexTableConsultations();

    cout << "selectedRow = " << selectedRow << endl;

    if (selectedRow < 0)
    {
        dialogError("Erreur", "Veuillez sélectionner une consultation");
        return;
    }

    if (!estConnecte())
    {
        dialogError("Erreur", "Pas connecté au serveur");
        return;
    }

    string raison = dialogInputText("Réservation", "Raison de la consultation:");
    if (raison.empty())
    {
        dialogError("Erreur", "Raison de consultation requise");
        return;
    }

    int consultationId = ui->tableWidgetConsultations->item(selectedRow, 0)->text().toInt();

    if (reserverConsultation(consultationId, raison))
    {
        dialogMessage("Réservation", "Consultation réservée avec succès");
        on_pushButtonRechercher_clicked();
    }
    else
    {
        dialogError("Erreur", "Échec de la réservation");
    }
}