<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ConsultationDAO, type Consultation } from '../model/ConsultationDAO';
import { ReferenceDAO, type Doctor, type Specialty } from '../model/ReferenceDAO';

// --- VARIABLES ---
const myConsultations = ref<Consultation[]>([]);
const searchResults = ref<Consultation[]>([]);
const doctors = ref<Doctor[]>([]);
const specialties = ref<Specialty[]>([]);

const showSearchComponent = ref(false);
const selectedSpecialtyId = ref<number>(0);
const selectedDoctorId = ref<number>(0);

const currentPatientId = Number(localStorage.getItem('patientId'));
const currentPatientName = localStorage.getItem('patientName') || 'Patient';

const router = useRouter();
const consultationDAO = new ConsultationDAO();
const referenceDAO = new ReferenceDAO();

// --- INITIALISATION ---
onMounted(async () => {
    if (!currentPatientId) {
        router.push('/');
        return;
    }
    await loadMyConsultations();
    doctors.value = await referenceDAO.getDoctors();
    specialties.value = await referenceDAO.getSpecialties();
});

async function loadMyConsultations() {
    myConsultations.value = await consultationDAO.getForPatient(currentPatientId);
}

const filteredDoctors = computed(() => {
    if (selectedSpecialtyId.value === 0) return doctors.value;
    return doctors.value.filter(d => d.specialtyId === selectedSpecialtyId.value);
});

// --- ACTIONS ---
function handleLogout() {
    localStorage.removeItem('patientId');
    router.push('/');
}

async function handleCancel(rdvId: number) {
    if (confirm("Voulez-vous vraiment supprimer ce rendez-vous ?")) {
        const success = await consultationDAO.cancel(rdvId);
        if (success) {
            await loadMyConsultations();
            if (showSearchComponent.value && selectedDoctorId.value !== 0) {
                handleSearch();
            }
        }
    }
}

function openSearch() {
    showSearchComponent.value = true;
    searchResults.value = [];
    selectedDoctorId.value = 0;
}

async function handleSearch() {
    if (selectedDoctorId.value === 0) {
        alert("Choisissez un médecin.");
        return;
    }
    const results = await consultationDAO.search(selectedDoctorId.value);
    searchResults.value = results.filter(c => c.patientId === null);
}

async function handleBook(rdvId: number) {
    const reason = prompt("Motif du rendez-vous ?");
    if (reason) {
        const success = await consultationDAO.book(rdvId, currentPatientId, reason);
        if (success) {
            alert("Rendez-vous confirmé !");
            showSearchComponent.value = false;
            await loadMyConsultations();
        } else {
            alert("Erreur : Créneau probablement déjà pris.");
            handleSearch();
        }
    }
}

function closeSearch() {
    showSearchComponent.value = false;
}

// Helpers
function getDoctorName(id: number) {
    const doc = doctors.value.find(d => d.id === id);
    return doc ? `Dr. ${doc.lastName} ${doc.firstName}` : 'Inconnu';
}
function getSpecialtyNameByDoctorId(doctorId: number) {
    const doc = doctors.value.find(d => d.id === doctorId);
    if (!doc) return '-';
    const spec = specialties.value.find(s => s.id === doc.specialtyId);
    return spec ? spec.name : '-';
}
</script>

<template>
  <div class="container">
    
    <header class="page-header">
      <h1>Bonjour {{ currentPatientName }} 👋</h1>
      <button class="btn-logout" @click="handleLogout">Déconnexion</button>
    </header>

    <div class="section-box">
        <h2>🗓️ Mes Rendez-vous confirmés</h2>
        
        <table v-if="myConsultations.length > 0">
            <thead>
                <tr>
                    <th>Date</th>
                    <th>Heure</th>
                    <th>Médecin</th>
                    <th>Spécialité</th>
                    <th>Motif</th>
                    <th>Action</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="rdv in myConsultations" :key="rdv.id">
                    <td>{{ rdv.date }}</td>
                    <td>{{ rdv.hour }}</td>
                    <td>{{ getDoctorName(rdv.doctorId) }}</td>
                    <td>{{ getSpecialtyNameByDoctorId(rdv.doctorId) }}</td>
                    <td>{{ rdv.reason }}</td>
                    <td>
                        <button class="btn-cancel" @click="handleCancel(rdv.id)">
                            Annuler
                        </button>
                    </td>
                </tr>
            </tbody>
        </table>
        <p v-else class="empty-msg">Vous n'avez aucun rendez-vous prévu.</p>

        <div class="action-bar" v-if="!showSearchComponent">
            <button class="btn-primary" @click="openSearch">
                ➕ Prendre un autre rendez-vous
            </button>
        </div>
    </div>

    <div v-if="showSearchComponent" class="section-box search-section">
        <h3>🔍 Rechercher une disponibilité</h3>
        
        <div class="filters">
            <div class="filter-group">
                <label>Spécialité :</label>
                <select v-model="selectedSpecialtyId" @change="selectedDoctorId = 0">
                    <option :value="0">-- Toutes --</option>
                    <option v-for="s in specialties" :key="s.id" :value="s.id">{{ s.name }}</option>
                </select>
            </div>
            <div class="filter-group">
                <label>Médecin :</label>
                <select v-model="selectedDoctorId">
                    <option :value="0">-- Choisir --</option>
                    <option v-for="d in filteredDoctors" :key="d.id" :value="d.id">Dr. {{ d.lastName }}</option>
                </select>
            </div>
            <button class="btn-search" @click="handleSearch">Rechercher</button>
        </div>

        <table v-if="searchResults.length > 0">
            <thead>
                <tr>
                    <th>Date</th>
                    <th>Heure</th>
                    <th>Médecin</th>
                    <th>Action</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="rdv in searchResults" :key="rdv.id">
                    <td>{{ rdv.date }}</td>
                    <td>{{ rdv.hour }}</td>
                    <td>{{ getDoctorName(rdv.doctorId) }}</td>
                    <td>
                        <button class="btn-book" @click="handleBook(rdv.id)">
                            Réserver
                        </button>
                    </td>
                </tr>
            </tbody>
        </table>
        <p v-else-if="selectedDoctorId !== 0" class="no-result">
            Aucun créneau libre trouvé pour ce médecin.
        </p>

        <div class="action-bar">
            <button class="btn-secondary" @click="closeSearch">Annuler la recherche</button>
        </div>
    </div>
  </div>
</template>

<style src="../assets/consultation.css"></style>