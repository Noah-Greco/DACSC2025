<script setup lang="ts">
import { ref, onMounted } from 'vue'
import LoginPanel from './components/LoginPanel.vue'
import MyAppointments from './components/MyAppointments.vue'
import AvailableConsultations from './components/AvailableConsultations.vue'

import { ConsultationDAO, type Consultation } from './model/ConsultationDAO'
import { ReferenceDAO, type Doctor, type Specialty } from './model/ReferenceDAO'

const patientId = ref<number | null>(null)
const patientName = ref<string>('')

const myConsultations = ref<Consultation[]>([])
const doctors = ref<Doctor[]>([])
const specialties = ref<Specialty[]>([])
const showSearch = ref(false)

const consultationDAO = new ConsultationDAO()
const referenceDAO = new ReferenceDAO()

async function reloadReferences() {
  doctors.value = await referenceDAO.getDoctors()
  specialties.value = await referenceDAO.getSpecialties()
}

async function loadMyConsultations() {
  if (!patientId.value) return
  myConsultations.value = await consultationDAO.getForPatient(patientId.value)
}

function getDoctorName(id: number) {
  const d = doctors.value.find(x => x.id === id)
  return d ? `Dr. ${d.lastName} ${d.firstName}` : 'Inconnu'
}

function getSpecialtyNameByDoctorId(doctorId: number) {
  const d = doctors.value.find(x => x.id === doctorId)
  if (!d) return '-'
  const s = specialties.value.find(x => x.id === d.specialtyId)
  return s ? s.name : '-'
}

async function onLoggedIn(payload: { patientId: number, firstName: string }) {
  patientId.value = payload.patientId
  patientName.value = payload.firstName
  showSearch.value = false

  await reloadReferences()
  await loadMyConsultations()
}

function logout() {
  patientId.value = null
  patientName.value = ''
  myConsultations.value = []
  showSearch.value = false
}

async function deleteAppointment(id: number) {
  if (!confirm('Supprimer ce rendez-vous ?')) return
  const ok = await consultationDAO.cancel(id)
  if (ok) {
    await loadMyConsultations()
    if (showSearch.value) {
    }
  }
}

async function book(id: number) {
  if (!patientId.value) return
  const reason = prompt('Motif du rendez-vous ?')
  if (!reason) return
  const ok = await consultationDAO.book(id, patientId.value, reason)
  if (ok) {
    showSearch.value = false
    await loadMyConsultations()
  } else {
    alert('Créneau probablement déjà pris')
  }
}

async function performSearch(
    filters: { specId: number, docId: number },
    cb: (data: Consultation[]) => void
) {
  const doc = doctors.value.find(d => d.id === filters.docId)
  const spec = specialties.value.find(s => s.id === filters.specId)

  const data = await consultationDAO.search({
    doctor: doc ? doc.lastName : null,
    specialty: spec ? spec.name : null
  })
  cb(data)
}

onMounted(async () => {
  await reloadReferences()
})
</script>

<template>
  <!-- ÉCRAN LOGIN SEUL -->
  <LoginPanel
      v-if="!patientId"
      @logged-in="onLoggedIn"
  />

  <!-- APP NORMALE APRÈS LOGIN -->
  <div v-else class="container">
    <h1>My Online Doctor</h1>
    <h2>Bonjour {{ patientName }}</h2>

    <MyAppointments
        :appointments="myConsultations"
        :getDocName="getDoctorName"
        :getSpecName="getSpecialtyNameByDoctorId"
        @logout="logout"
        @delete="deleteAppointment"
        @showSearch="showSearch = true"
    />

    <AvailableConsultations
        v-if="showSearch"
        :doctors="doctors"
        :specialties="specialties"
        :getDocName="getDoctorName"
        @book="book"
        @cancelSearch="showSearch = false"
        @performSearch="performSearch"
    />
  </div>
</template>

