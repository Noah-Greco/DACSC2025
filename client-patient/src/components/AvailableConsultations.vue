<script setup lang="ts">
import { ref, computed } from 'vue';
import type { Doctor, Specialty } from '../model/ReferenceDAO';
import type { Consultation } from '../model/ConsultationDAO';

// Props typées avec les interfaces
const props = defineProps<{ 
  doctors: Doctor[], 
  specialties: Specialty[], 
  getDocName: (id: number) => string 
}>();

const emit = defineEmits<{
  (e: 'book', id: number): void,
  (e: 'cancelSearch'): void,
  // Callback pattern pour la recherche
  (e: 'performSearch', filters: { specId: number, docId: number }, callback: (data: Consultation[]) => void): void
}>();

// Variables locales pour le formulaire
const selSpec = ref(0);
const selDoc = ref(0);
const searchResults = ref<Consultation[]>([]);

// Filtrage dynamique de la liste des médecins dans le select
const filteredDoctors = computed(() => {
  if (selSpec.value === 0) return props.doctors;
  return props.doctors.filter(d => d.specialtyId === selSpec.value);
});

// Action de recherche
function onSearch() {
  // On émet l'événement vers le parent, qui fera l'appel API
  emit('performSearch', { specId: selSpec.value, docId: selDoc.value }, (data) => {
    searchResults.value = data;
  });
}
</script>

<template>
  <div class="section-box search-section">
    <h3>🔍 Rechercher une disponibilité</h3>
    
    <div class="filters">
      <div class="filter-group">
        <label>Spécialité :</label>
        <select v-model="selSpec" @change="selDoc = 0">
          <option :value="0">-- Toutes --</option>
          <option v-for="s in specialties" :key="s.id" :value="s.id">{{ s.name }}</option>
        </select>
      </div>

      <div class="filter-group">
        <label>Médecin :</label>
        <select v-model="selDoc">
          <option :value="0">-- Choisir --</option>
          <option v-for="d in filteredDoctors" :key="d.id" :value="d.id">
            Dr. {{ d.lastName }}
          </option>
        </select>
      </div>

      <button class="btn-search" @click="onSearch">Rechercher</button>
    </div>

    <table v-if="searchResults.length > 0">
      <thead>
        <tr><th>Date</th><th>Heure</th><th>Médecin</th><th>Action</th></tr>
      </thead>
      <tbody>
        <tr v-for="rdv in searchResults" :key="rdv.id">
          <td>{{ rdv.date }}</td>
          <td>{{ rdv.hour }}</td>
          <td>{{ getDocName(rdv.doctorId) }}</td>
          <td>
            <button class="btn-book" @click="emit('book', rdv.id)">Réserver</button>
          </td>
        </tr>
      </tbody>
    </table>
    
    <p v-else class="no-result">Aucun résultat affiché.</p>

    <div class="action-bar">
      <button class="btn-secondary" @click="emit('cancelSearch')">Annuler</button>
    </div>
  </div>
</template>