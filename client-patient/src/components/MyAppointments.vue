<script setup lang="ts">
import type { Consultation } from '../model/ConsultationDAO';

// Définition stricte des Props
defineProps<{ 
  appointments: Consultation[], 
  getDocName: (id: number) => string, 
  getSpecName: (id: number) => string 
}>();

// Définition des Events
const emit = defineEmits<{
  (e: 'logout'): void,
  (e: 'delete', id: number): void,
  (e: 'showSearch'): void
}>();
</script>

<template>
  <div class="section-box">
    <h2>🗓️ Mes Rendez-vous confirmés</h2>
    
    <table v-if="appointments.length > 0">
      <thead>
        <tr>
          <th>Date</th><th>Heure</th><th>Médecin</th><th>Spécialité</th><th>Motif</th><th>Action</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="rdv in appointments" :key="rdv.id">
          <td>{{ rdv.date }}</td>
          <td>{{ rdv.hour }}</td>
          <td>{{ getDocName(rdv.doctorId) }}</td>
          <td>{{ getSpecName(rdv.doctorId) }}</td>
          <td>{{ rdv.reason }}</td>
          <td>
            <button class="btn-cancel" @click="emit('delete', rdv.id)">Supprimer</button>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else>Vous n'avez aucun rendez-vous prévu.</p>

    <div class="action-bar">
      <button class="btn-logout" @click="emit('logout')">Logout</button>
      <button class="btn-primary" @click="emit('showSearch')">Prendre un autre rendez-vous</button>
    </div>
  </div>
</template>