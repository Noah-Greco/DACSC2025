# ==== Config ====
TARGET     := ClientTest
CXX        ?= g++
BUILD_DIR  := build

CXXFLAGS   ?= -std=c++17 -Wall -Wextra -O2 -I./Librairie
LDFLAGS    ?=
LDLIBS     ?=

SRC        := ClientTest.cpp \
              Librairie/Librairie.cpp

OBJ        := $(addprefix $(BUILD_DIR)/,$(SRC:.cpp=.o))
DEPS       := $(OBJ:.o=.d)

# ==== Règles principales ====
.PHONY: all debug clean run

all: $(TARGET)

debug: CXXFLAGS := -std=c++17 -Wall -Wextra -g3 -O0 -I./Librairie
debug: $(TARGET)

$(TARGET): $(OBJ)
	@mkdir -p $(dir $@)
	$(CXX) $(LDFLAGS) $^ $(LDLIBS) -o $@

# Compilation avec génération auto des dépendances
$(BUILD_DIR)/%.o: %.cpp
	@mkdir -p $(dir $@)
	$(CXX) $(CXXFLAGS) -MMD -MP -c $< -o $@

run: $(TARGET)
	./$(TARGET) 127.0.0.1 5000

clean:
	@echo ">> nettoyage"
	@rm -rf $(BUILD_DIR) $(TARGET)

# Inclure les .d si présents (ne casse pas si absents)
-include $(DEPS)