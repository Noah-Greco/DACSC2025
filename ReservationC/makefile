
# Compiler
CXX = g++

# Compiler flags
CXXFLAGS = -Wall -Wextra -std=c++11

# Directories
BD_DIR = BD_Hospital
CLIENT_DIR = ClientConsultationBookerQt
LIBRARY_DIR = Librairie
SERVEUR_DIR = serveurReservation
PROTOCOLE_DIR = Protocole
PARAM_DIR = param

# Source files
BD_SRC = $(BD_DIR)/CreationBD.cpp
CLIENT_SRC = $(CLIENT_DIR)/main.cpp $(CLIENT_DIR)/mainwindowclientconsultationbooker.cpp $(CLIENT_DIR)/moc_mainwindowclientconsultationbooker.cpp $(LIBRARY_DIR)/Librairie.cpp
SOCKET_SRC = $(LIBRARY_DIR)/Librairie.cpp
SERVEUR_SRC = $(SERVEUR_DIR)/Serveur.cpp $(LIBRARY_DIR)/Librairie.cpp $(PROTOCOLE_DIR)/CBP.cpp $(PROTOCOLE_DIR)/ACBP.cpp

# Header files
SOCKET_HEADERS = $(LIBRARY_DIR)/Librairie.hpp
PROTOCOLE_HEADERS = $(PROTOCOLE_DIR)/CBP.hpp $(PROTOCOLE_DIR)/ACBP.hpp
UTIL_HEADERS = $(PARAM_DIR)/param.h

# Output binaries
BD_BIN = $(BD_DIR)/CreationBD
CLIENT_BIN = $(CLIENT_DIR)/ClientConsultationBooker
SERVEUR_BIN = $(SERVEUR_DIR)/serveur

# MySQL flags (headers + lib)
MYSQL_CFLAGS = -I/usr/include/mysql
MYSQL_LIBS = -lmysqlclient -lpthread -lz -lm -lrt -lssl -lcrypto -ldl

# Qt flags (adjust if needed)
QT_FLAGS = `pkg-config --cflags --libs Qt5Widgets`

# Default target
all: bin $(BD_BIN) $(CLIENT_BIN) $(SERVEUR_BIN)
	@echo "============================================"
	@echo "Compilation terminée avec succès !"
	@echo "Binaires créés :"
	@echo "  - $(BD_BIN) (Creation de la base de données)"
	@echo "  - $(CLIENT_BIN) (Client Qt)"
	@echo "  - $(SERVEUR_BIN) (Serveur TCP)"
	@echo "============================================"

# Create bin directory
bin:

$(BD_BIN): $(BD_SRC)
	@echo "Compilation de CreationBD..."
	$(CXX) $(CXXFLAGS) -o $@ $< $(MYSQL_CFLAGS) -m64 -L/usr/lib64/mysql $(MYSQL_LIBS)
	@echo "✓ $(BD_BIN) créé"

$(BD_CLEAN_BIN): $(BD_CLEAN_SRC)
	@echo "Compilation de CleanBD..."
	$(CXX) $(CXXFLAGS) -o $@ $< $(MYSQL_CFLAGS) -m64 -L/usr/lib64/mysql $(MYSQL_LIBS)
	@echo "✓ $(BD_CLEAN_BIN) créé"

$(CLIENT_BIN): $(CLIENT_SRC) $(SOCKET_HEADERS) $(UTIL_HEADERS)
	@echo "Compilation du client Qt..."
	$(CXX) $(CXXFLAGS) -fPIC -o $@ $(CLIENT_SRC) $(QT_FLAGS)
	@echo "✓ $(CLIENT_BIN) créé"

$(SERVEUR_BIN): $(SERVEUR_SRC) $(SOCKET_HEADERS) $(PROTOCOLE_HEADERS) $(UTIL_HEADERS)
	@echo "Compilation du serveur..."
	$(CXX) $(CXXFLAGS) -o $@ $(SERVEUR_SRC) -lpthread $(MYSQL_CFLAGS) -m64 -L/usr/lib64/mysql $(MYSQL_LIBS)
	@echo "✓ $(SERVEUR_BIN) créé"

# Initialize database with sample data
init-db: $(BD_BIN)
	@echo "============================================"
	@echo "Initialisation de la base de données..."
	@echo "Création des tables et insertion des données..."
	$(BD_BIN)
	@echo "✓ Base de données initialisée avec succès"
	@echo "Tables créées :"
	@echo "  - specialties (spécialités médicales)"
	@echo "  - doctors (médecins)"
	@echo "  - patients (patients)"
	@echo "  - consultations (consultations)"
	@echo "============================================"

clean:
	@echo "============================================"
	@echo "Nettoyage en cours..."
	@echo "Suppression des binaires..."
	rm -f $(BD_BIN) $(CLIENT_BIN) $(SERVEUR_BIN)
	@echo "✓ Binaires supprimés"
	@echo "============================================"
	@echo "Nettoyage terminé."

.PHONY: all clean bin init-db