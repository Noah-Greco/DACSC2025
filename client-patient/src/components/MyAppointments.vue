<script setup lang="ts">
import { computed } from 'vue'
import type { Consultation } from '../model/ConsultationDAO'

const props = defineProps<{
  appointments: Consultation[]
  getDocName: (id: number) => string
  getSpecName: (doctorId: number) => string
}>()

const emit = defineEmits<{
  (e: 'logout'): void
  (e: 'delete', id: number): void
  (e: 'showSearch'): void
}>()

const hasAppointments = computed(() => props.appointments.length > 0)
</script>

<template>
  <div class="section-box">
    <div class="section-head">
      <h2>Mes rendez-vous</h2>
      <div class="head-actions">
        <button class="btn-secondary" @click="emit('logout')">Logout</button>
        <button class="btn-primary" @click="emit('showSearch')">Nouveau rendez-vous</button>
      </div>
    </div>

    <table v-if="hasAppointments" class="table">
      <thead>
      <tr>
        <th>Date</th>
        <th>Heure</th>
        <th>Médecin</th>
        <th>Spécialité</th>
        <th>Motif</th>
        <th></th>
      </tr>
      </thead>

      <tbody>
      <tr v-for="rdv in appointments" :key="rdv.id">
        <td>{{ rdv.date }}</td>
        <td>{{ rdv.hour }}</td>
        <td>{{ getDocName(rdv.doctorId) }}</td>
        <td>{{ getSpecName(rdv.doctorId) }}</td>
        <td>{{ rdv.reason }}</td>
        <td class="col-action">
          <button class="btn-cancel" @click="emit('delete', rdv.id)">Supprimer</button>
        </td>
      </tr>
      </tbody>
    </table>

    <p v-else class="no-result">Aucun rendez-vous prévu.</p>
  </div>
</template>
