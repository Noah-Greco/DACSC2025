# ==== Config ====
APPS       := ClientTest ServeurTest ClientConsultationBookerQtApp
CXX        ?= g++
BUILD_DIR  := build

CXXFLAGS   ?= -std=c++17 -Wall -Wextra -O2 -I./Librairie
LDFLAGS    ?=
LDLIBS     ?=

# ==== Détection Qt (Qt6 puis Qt5) ====
QT_PKG :=
ifneq ($(shell pkg-config --exists Qt6Widgets && echo yes),)
  QT_PKG := Qt6Widgets
else ifneq ($(shell pkg-config --exists Qt5Widgets && echo yes),)
  QT_PKG := Qt5Widgets
endif

QT_CFLAGS := $(if $(QT_PKG),$(shell pkg-config --cflags $(QT_PKG)))
QT_LIBS   := $(if $(QT_PKG),$(shell pkg-config --libs $(QT_PKG)))

# ==== Sources communes (lib) ====
SRCS_COMMON := Librairie/Librairie.cpp

# ==== Sources spécifiques ====
SRCS_ClientTest   := ClientTest.cpp   $(SRCS_COMMON)
SRCS_ServeurTest  := ServeurTest.cpp  $(SRCS_COMMON)

SRCS_ClientConsultationBookerQt := \
  ClientConsultationBookerQt/main.cpp \
  ClientConsultationBookerQt/mainwindowclientconsultationbooker.cpp \
  ClientConsultationBookerQt/moc_mainwindowclientconsultationbooker.cpp

# ==== Objets ====
OBJ_ClientTest   := $(addprefix $(BUILD_DIR)/,$(SRCS_ClientTest:.cpp=.o))
OBJ_ServeurTest  := $(addprefix $(BUILD_DIR)/,$(SRCS_ServeurTest:.cpp=.o))
OBJ_ClientConsultationBookerQt := $(addprefix $(BUILD_DIR)/,$(SRCS_ClientConsultationBookerQt:.cpp=.o))

DEPS := \
  $(OBJ_ClientTest:.o=.d) \
  $(OBJ_ServeurTest:.o=.d) \
  $(OBJ_ClientConsultationBookerQt:.o=.d)

# ==== Règles principales ====
.PHONY: all debug clean run run-client run-serveur run-qt

all: $(APPS)

debug: CXXFLAGS := -std=c++17 -Wall -Wextra -g3 -O0 -I./Librairie
debug: $(APPS)

# ---- Editions de liens ----
ClientTest: $(OBJ_ClientTest)
	@mkdir -p $(dir $@)
	$(CXX) $(LDFLAGS) $^ $(LDLIBS) -o $@

ServeurTest: $(OBJ_ServeurTest)
	@mkdir -p $(dir $@)
	$(CXX) $(LDFLAGS) $^ $(LDLIBS) -o $@

# exécutable renommé pour éviter la collision avec le dossier ClientConsultationBookerQt/
ClientConsultationBookerQtApp: $(OBJ_ClientConsultationBookerQt)
	@mkdir -p $(dir $@)
	$(if $(QT_PKG),,@echo ">> ATTENTION: Qt non détecté (Qt5/Qt6). Installe Qt ou exporte PKG_CONFIG_PATH."; exit 2)
	$(CXX) $(LDFLAGS) $^ $(LDLIBS) $(QT_LIBS) -o $@

# ---- Compilation générique ----
$(BUILD_DIR)/%.o: %.cpp
	@mkdir -p $(dir $@)
	$(CXX) $(CXXFLAGS) -MMD -MP -c $< -o $@

# ---- Compilation Qt (flags Qt + -fPIC) ----
$(BUILD_DIR)/ClientConsultationBookerQt/%.o: ClientConsultationBookerQt/%.cpp
	@mkdir -p $(dir $@)
	$(CXX) $(CXXFLAGS) $(QT_CFLAGS) -I./ClientConsultationBookerQt -fPIC -MMD -MP -c $< -o $@

# ---- Raccourcis d'exécution ----
run: run-client

run-client: ClientTest
	./ClientTest 127.0.0.1 5000

run-serveur: ServeurTest
	./ServeurTest 5000

run-qt: ClientConsultationBookerQtApp
	./ClientConsultationBookerQtApp

clean:
	@echo ">> nettoyage"
	@rm -rf $(BUILD_DIR) $(APPS)

# Inclure les .d si présents (ne casse pas si absents)
-include $(DEPS)
