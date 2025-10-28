--Quand on modifie le CMake ou qu'on supprimer le build
cmake -S . -B build-release -DCMAKE_BUILD_TYPE=Release

--pour compiler
cmake --build build-release -j
