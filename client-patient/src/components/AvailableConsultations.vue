<script setup lang="ts">
import { ref, computed } from 'vue'
import type { Doctor, Specialty } from '../model/ReferenceDAO'
import type { Consultation } from '../model/ConsultationDAO'

type Filters = { specId: number; docId: number }

const props = defineProps<{
  doctors: Doctor[]
  specialties: Specialty[]
  getDocName: (id: number) => string
}>()

const emit = defineEmits<{
  (e: 'book', id: number): void
  (e: 'cancelSearch'): void
  (e: 'performSearch', filters: Filters, callback: (data: Consultation[]) => void): void
}>()

const selSpec = ref<number>(0)
const selDoc = ref<number>(0)

const isSearching = ref(false)
const hasSearched = ref(false)
const searchResults = ref<Consultation[]>([])

const filteredDoctors = computed(() => {
  if (selSpec.value === 0) return props.doctors
  return props.doctors.filter(d => d.specialtyId === selSpec.value)
})

const canSearch = computed(() => {
  // tu peux décider ici si un médecin est obligatoire
  return true
})

function resetResults() {
  hasSearched.value = false
  searchResults.value = []
}

function onSpecChange() {
  selDoc.value = 0
  resetResults()
}

function onSearch() {
  if (!canSearch.value) return

  isSearching.value = true
  hasSearched.value = true
  searchResults.value = []

  emit('performSearch', { specId: selSpec.value, docId: selDoc.value }, (data) => {
    searchResults.value = data
    isSearching.value = false
  })
}
</script>

<template>
  <div class="section-box search-section">
    <div class="section-head">
      <h3>Rechercher une disponibilité</h3>
      <button class="btn-secondary" @click="emit('cancelSearch')">Fermer</button>
    </div>

    <div class="filters">
      <div class="filter-group">
        <label>Spécialité</label>
        <select v-model.number="selSpec" @change="onSpecChange">
          <option :value="0">Toutes</option>
          <option v-for="s in specialties" :key="s.id" :value="s.id">
            {{ s.name }}
          </option>
        </select>
      </div>

      <div class="filter-group">
        <label>Médecin</label>
        <select v-model.number="selDoc" @change="resetResults">
          <option :value="0">Tous / non spécifié</option>
          <option v-for="d in filteredDoctors" :key="d.id" :value="d.id">
            Dr. {{ d.lastName }} {{ d.firstName }}
          </option>
        </select>
      </div>

      <button class="btn-primary" :disabled="isSearching || !canSearch" @click="onSearch">
        {{ isSearching ? 'Recherche...' : 'Rechercher' }}
      </button>
    </div>

    <div v-if="isSearching" class="info-line">Chargement...</div>

    <template v-else>
      <table v-if="searchResults.length > 0" class="table">
        <thead>
        <tr>
          <th>Date</th>
          <th>Heure</th>
          <th>Médecin</th>
          <th></th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="rdv in searchResults" :key="rdv.id">
          <td>{{ rdv.date }}</td>
          <td>{{ rdv.hour }}</td>
          <td>{{ getDocName(rdv.doctorId) }}</td>
          <td class="col-action">
            <button class="btn-book" @click="emit('book', rdv.id)">Réserver</button>
          </td>
        </tr>
        </tbody>
      </table>

      <p v-else-if="hasSearched" class="no-result">
        Aucun créneau disponible avec ces filtres.
      </p>

      <p v-else class="hint">
        Sélectionne des filtres puis lance la recherche.
      </p>
    </template>
  </div>
</template>
