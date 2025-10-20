cmake_minimum_required(VERSION 3.16)

# ===================== Projet =====================
project(HospitalReservation LANGUAGES CXX)

# Standard & warnings
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)
add_compile_options(-Wall -Wextra -Wpedantic)

# Sortie des binaires
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)

# ===================== Dépendances =====================
# Threads POSIX
find_package(Threads REQUIRED)

# Qt (Qt6 si dispo, sinon Qt5)
set(QT_REQUIRED_COMPONENTS Widgets)
find_package(Qt6 COMPONENTS ${QT_REQUIRED_COMPONENTS} QUIET)
if (Qt6_FOUND)
  set(QT_PACKAGE Qt6)
else()
  find_package(Qt5 REQUIRED COMPONENTS ${QT_REQUIRED_COMPONENTS})
  set(QT_PACKAGE Qt5)
endif()

# Headers communs (headers only)
# param/ contient pram.h
include_directories(
  ${CMAKE_SOURCE_DIR}/param
  ${CMAKE_SOURCE_DIR}/Protocole
  ${CMAKE_SOURCE_DIR}/Librairie
  ${CMAKE_SOURCE_DIR}/ClientConsultationBookerQt
)

# ===================== Libraries internes =====================

# --- protocole : Protocole/CBP.cpp ---
add_library(protocole STATIC
  ${CMAKE_SOURCE_DIR}/Protocole/CBP.cpp
)
target_include_directories(protocole PUBLIC ${CMAKE_SOURCE_DIR}/Protocole)

# --- MySQL client via pkg-config (mysqlclient uniquement) ---
find_package(PkgConfig REQUIRED)
pkg_check_modules(MYSQLCLIENT REQUIRED IMPORTED_TARGET mysqlclient)

# Lier la lib MySQL à la target qui utilise mysql_* (protocole)
target_include_directories(protocole PUBLIC ${MYSQLCLIENT_INCLUDE_DIRS})
target_link_libraries(protocole PUBLIC PkgConfig::MYSQLCLIENT)

# --- librairie : tous les .cpp de Librairie/ ---
file(GLOB LIBRAIRIE_SOURCES
  "${CMAKE_SOURCE_DIR}/Librairie/*.cpp"
)
add_library(librarie STATIC ${LIBRAIRIE_SOURCES})
target_include_directories(librarie PUBLIC ${CMAKE_SOURCE_DIR}/Librairie)

# ===================== Serveur =====================
add_executable(reservation_server
  ${CMAKE_SOURCE_DIR}/serveurReservation/Serveur.cpp
)

# Lier aux libs internes + pthreads
target_link_libraries(reservation_server
  PRIVATE protocole librarie Threads::Threads
)

# Définitions (chemin conf)
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

# ===================== Client Qt =====================
# Auto MOC/UIC/RCC (on garde globalement ON)
set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTOUIC ON)
set(CMAKE_AUTORCC ON)

# Sources du client (exclut moc_*.cpp)
file(GLOB CLIENT_SOURCES
  "${CMAKE_SOURCE_DIR}/ClientConsultationBookerQt/*.cpp"
)
list(FILTER CLIENT_SOURCES EXCLUDE REGEX ".*/moc_.*\\.cpp$")

add_executable(ClientConsultationBookerQt
  ${CLIENT_SOURCES}
)

# Désactiver AUTOUIC UNIQUEMENT pour ce binaire (tu as déjà ui_*.h, pas de .ui)
set_target_properties(ClientConsultationBookerQt PROPERTIES AUTOUIC OFF)

# Lier Qt Widgets + libs internes (si besoin du protocole/librairie côté client)
target_link_libraries(ClientConsultationBookerQt
  PRIVATE ${QT_PACKAGE}::Widgets protocole librarie
)

# ===================== Fin =====================
