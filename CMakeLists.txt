cmake_minimum_required(VERSION 3.16)

# Projet C++
project(HospitalReservation LANGUAGES CXX)

# Standard & warnings
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)
add_compile_options(-Wall -Wextra -Wpedantic)

# Qt (Qt6 si dispo, sinon Qt5)
set(QT_REQUIRED_COMPONENTS Widgets)
find_package(Qt6 COMPONENTS ${QT_REQUIRED_COMPONENTS} QUIET)
if(Qt6_FOUND)
  set(QT_PACKAGE Qt6)
else()
  find_package(Qt5 REQUIRED COMPONENTS ${QT_REQUIRED_COMPONENTS})
  set(QT_PACKAGE Qt5)
endif()

# Threads pour le serveur
find_package(Threads REQUIRED)

# Où sortir les binaires
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)

# Dossiers communs d'en-têtes (headers only)
# param/ contient pram.h (headers only)
include_directories(
  ${CMAKE_SOURCE_DIR}/param
  ${CMAKE_SOURCE_DIR}/Protocole
  ${CMAKE_SOURCE_DIR}/Librairie
)

# --- Libraries internes -------------------------------------------------

# 1) protocole -> Protocole/CBP.cpp
add_library(protocole STATIC
  ${CMAKE_SOURCE_DIR}/Protocole/CBP.cpp
)
target_include_directories(protocole PUBLIC ${CMAKE_SOURCE_DIR}/Protocole)

# 2) librairie -> tous les .cpp de Librairie/
# (on GLOB ici pour t’éviter d’énumérer; si tu préfères, liste-les explicitement)
file(GLOB LIBRAIRIE_SOURCES
  "${CMAKE_SOURCE_DIR}/Librairie/*.cpp"
)
add_library(librarie STATIC ${LIBRAIRIE_SOURCES})
target_include_directories(librarie PUBLIC ${CMAKE_SOURCE_DIR}/Librairie)

# --- Serveur ------------------------------------------------------------

# Source unique déclaré : serveurReservation/Serveur.cpp
add_executable(reservation_server
  ${CMAKE_SOURCE_DIR}/serveurReservation/Serveur.cpp
)

# Lier aux libs internes + pthreads
target_link_libraries(reservation_server
  PRIVATE protocole librarie Threads::Threads
)

# Définir un chemin de conf par défaut (install + run local)
target_compile_definitions(reservation_server PRIVATE
  DEFAULT_CONF_PATH="/etc/hospital-reservation/Serveur.conf"
  FALLBACK_CONF_PATH="${CMAKE_SOURCE_DIR}/serveurReservation/Serveur.conf"
)

# Copie la conf à côté du binaire après build (pratique en dev)
add_custom_command(TARGET reservation_server POST_BUILD
  COMMAND ${CMAKE_COMMAND} -E make_directory ${CMAKE_RUNTIME_OUTPUT_DIRECTORY}
  COMMAND ${CMAKE_COMMAND} -E copy_if_different
          ${CMAKE_SOURCE_DIR}/serveurReservation/Serveur.conf
          ${CMAKE_RUNTIME_OUTPUT_DIRECTORY}/Serveur.conf
  COMMENT "Copie de serveurReservation/Serveur.conf -> build/bin/"
)

# Install optionnelle
install(TARGETS reservation_server RUNTIME DESTINATION bin)
install(FILES ${CMAKE_SOURCE_DIR}/serveurReservation/Serveur.conf
        DESTINATION etc/hospital-reservation)

# --- Client Qt ----------------------------------------------------------

# Activer les outils Qt auto
set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTOUIC ON)
set(CMAKE_AUTORCC ON)

# On prend tous les .cpp du dossier client, en excluant les fichiers générés moc_*
file(GLOB CLIENT_SOURCES
  "${CMAKE_SOURCE_DIR}/ClientConsultationBookerQt/*.cpp"
)
# Filtrer les moc_*.cpp s’ils existent déjà dans le dossier
list(FILTER CLIENT_SOURCES EXCLUDE REGEX ".*/moc_.*\\.cpp$")

add_executable(ClientConsultationBookerQt
  ${CLIENT_SOURCES}
)

# Inclure le dossier client pour trouver mainwindow*.h
target_include_directories(ClientConsultationBookerQt
  PRIVATE ${CMAKE_SOURCE_DIR}/ClientConsultationBookerQt
)

# Lier Qt Widgets + tes libs internes si le client en dépend (au besoin)
target_link_libraries(ClientConsultationBookerQt
  PRIVATE ${QT_PACKAGE}::Widgets protocole librarie
)

# Pour certaines distros, nécessaire pour l’ICO/ICU au runtime Qt
if (UNIX AND NOT APPLE)
  target_compile_definitions(ClientConsultationBookerQt PRIVATE QT_NO_KEYWORDS)
endif()
