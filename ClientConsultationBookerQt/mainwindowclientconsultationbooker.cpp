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
    string requete = string(LOGIN) + diez + prenom + diez + nom + diez + to_string(patientId) + diez + (nouveauPatient ? "OUI" : "NON");
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
    std::string requete = GET_SPECIALTIES;
    std::string reponse;
    if (!envoyerRequete(requete, reponse)) return false;

    const std::string prefix = std::string(GET_SPECIALTIES) + "#" + std::string(OK) + "#";
    if (reponse.rfind(prefix, 0) != 0) {
        std::cout << "Erreur chargement spécialités: " << reponse << std::endl;
        return false;
    }

    std::string payload = reponse.substr(prefix.size()); // "id;nom#id;nom#..."
    clearComboBoxSpecialties();
    addComboBoxSpecialties(TOUTES);

    std::stringstream ss(payload);
    std::string item;
    while (std::getline(ss, item, '#')) {
        if (item.empty()) continue;
        size_t p = item.find(';');
        if (p == std::string::npos || p+1 >= item.size()) continue;
        // id: item.substr(0, p)  // si besoin plus tard
        std::string nom = item.substr(p+1);
        if (!nom.empty()) addComboBoxSpecialties(nom);
    }
    return true;
}


bool MainWindowClientConsultationBooker::chargerDocteurs()
{
    std::string requete = GET_DOCTORS;
    std::string reponse;
    if (!envoyerRequete(requete, reponse)) return false;

    const std::string prefix = std::string(GET_DOCTORS) + "#" + std::string(OK) + "#";
    if (reponse.rfind(prefix, 0) != 0) {
        std::cout << "Erreur chargement médecins: " << reponse << std::endl;
        return false;
    }

    std::string payload = reponse.substr(prefix.size()); // "id;last;first;spec#..."
    clearComboBoxDoctors();
    addComboBoxDoctors(TOUS);

    std::stringstream ss(payload);
    std::string item;
    while (std::getline(ss, item, '#')) {
        if (item.empty()) continue;

        // Découper "id;last;first;spec"
        size_t p1 = item.find(';');
        if (p1 == std::string::npos) continue;
        size_t p2 = item.find(';', p1 + 1);
        if (p2 == std::string::npos) continue;
        size_t p3 = item.find(';', p2 + 1);
        if (p3 == std::string::npos) continue;

        // id: item.substr(0, p1)          // si besoin
        std::string last  = item.substr(p1 + 1, p2 - (p1 + 1));
        std::string first = item.substr(p2 + 1, p3 - (p2 + 1));
        // specialty_id: item.substr(p3 + 1) // si besoin

        if (!last.empty() || !first.empty()) {
            addComboBoxDoctors(last + " " + first);
        }
    }
    return true;
}


bool MainWindowClientConsultationBooker::rechercherConsultations(const std::string& specialiteUi,
                                                                 const std::string& docteurUi,
                                                                 const std::string& dateDebut,
                                                                 const std::string& dateFin)
{
    // Normalisation vers le protocole
    std::string spec = (specialiteUi.rfind("---",0)==0 ? "*" : specialiteUi);
    std::string doc  = (docteurUi.rfind("---",0)==0 ? "*" : docteurUi);

    // Optionnel: si doc = "Nom Prénom", n’envoyer que le nom de famille
    if (doc != "*" ) {
        size_t sp = doc.find(' ');
        if (sp != std::string::npos) doc = doc.substr(0, sp);
    }

    std::string requete = std::string(SEARCH_CONSULTATIONS) + diez
                        + spec + diez + doc + diez + dateDebut + diez + dateFin;

    std::string reponse;
    if (!envoyerRequete(requete, reponse)) return false;

    const std::string okPrefix = std::string(SEARCH_CONSULTATIONS) + "#" + std::string(OK) + "#";
    const std::string koPrefix = std::string(SEARCH_CONSULTATIONS) + "#ko";

    clearTableConsultations();

    if (reponse.rfind(okPrefix, 0) == 0) {
        std::string payload = reponse.substr(okPrefix.size()); // "id#docteur#spec#date#heure|..."
        if (payload.empty()) return true;

        size_t start = 0;
        while (start <= payload.size()) {
            size_t bar = payload.find('|', start);
            std::string rec = payload.substr(start, (bar==std::string::npos)?std::string::npos:bar-start);
            if (!rec.empty()) {
                size_t p1 = rec.find('#'); if (p1==std::string::npos) goto next;
                size_t p2 = rec.find('#',p1+1); if (p2==std::string::npos) goto next;
                size_t p3 = rec.find('#',p2+1); if (p3==std::string::npos) goto next;
                size_t p4 = rec.find('#',p3+1); if (p4==std::string::npos) goto next;

                std::string id    = rec.substr(0, p1);
                std::string docNm = rec.substr(p1+1, p2-(p1+1));
                std::string specN = rec.substr(p2+1, p3-(p2+1));
                std::string date  = rec.substr(p3+1, p4-(p3+1));
                std::string heure = rec.substr(p4+1);

                if (!id.empty())
                    addTupleTableConsultations(std::atoi(id.c_str()), specN, docNm, date, heure);
            }
        next:
            if (bar==std::string::npos) break;
            start = bar + 1;
        }
        return true;
    }

    // Traite "ko" comme zéro résultat pour éviter la pop-up d’erreur si la requête est valide mais vide
    if (reponse.rfind(koPrefix, 0) == 0) {
        return true; // pas d’erreur, juste aucun résultat
    }

    std::cout << "Erreur recherche consultations: " << reponse << std::endl;
    return false;
}



bool MainWindowClientConsultationBooker::reserverConsultation(int consultationId, const std::string& raison, int patientId) {
    std::string requete = std::string(BOOK_CONSULTATION) + diez
                        + std::to_string(consultationId) + diez
                        + raison + diez
                        + std::to_string(patientId);

    std::string reponse;
    if (!envoyerRequete(requete, reponse)) return false;

    const std::string okPrefix = std::string(BOOK_CONSULTATION) + "#" + std::string(OK);
    if (reponse.rfind(okPrefix, 0) == 0) return true;

    std::cout << "Erreur réservation: " << reponse << std::endl;
    return false;
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

    int patientId = this->getPatientId();
    if (reserverConsultation(consultationId, raison, patientId))
    {
        dialogMessage("Réservation", "Consultation réservée avec succès");
        on_pushButtonRechercher_clicked();
    }
    else
    {
        dialogError("Erreur", "Échec de la réservation");
    }
}

int MainWindowClientConsultationBooker::getSelectedConsultationId()
{
    int row = getSelectionIndexTableConsultations();
    if (row == -1) return -1;

    QTableWidgetItem *item = ui->tableWidgetConsultations->item(row, 0);
    if (!item) return -1;

    return item->text().toInt();
}