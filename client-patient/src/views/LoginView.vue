<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { PatientDAO } from '../model/PatientDAO';

const lastName = ref('');
const firstName = ref('');
const birthDate = ref('');
const isNew = ref(false);

const router = useRouter();
const dao = new PatientDAO();

async function handleLogin() {
    if (!lastName.value || !firstName.value || !birthDate.value) {
        alert("Veuillez remplir tous les champs !");
        return;
    }

    try {
        const id = await dao.login(lastName.value, firstName.value, birthDate.value, isNew.value);
        localStorage.setItem('patientId', id.toString());
        localStorage.setItem('patientName', firstName.value);
        alert("Connexion réussie ! ID Patient : " + id);
        router.push('/consultations');
    } catch (error: any) {
        alert("Erreur : " + error.message);
    }
}
</script>

<template>
  <main>
    <div class="login-box">
      <h1>Bienvenue Doctolib'Light</h1>
      
      <div class="form-group">
        <label>Nom :</label>
        <input type="text" v-model="lastName" placeholder="Ex: Dupont" />
      </div>

      <div class="form-group">
        <label>Prénom :</label>
        <input type="text" v-model="firstName" placeholder="Ex: Jean" />
      </div>

      <div class="form-group">
        <label>Date de naissance :</label>
        <input type="date" v-model="birthDate" />
      </div>

      <div class="form-group checkbox">
        <input type="checkbox" id="new" v-model="isNew" />
        <label for="new">Je suis un nouveau patient</label>
      </div>

      <button @click="handleLogin">Se connecter</button>
    </div>
  </main>
</template>