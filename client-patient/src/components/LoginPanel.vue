<script setup lang="ts">
import { ref } from 'vue'
import { PatientDAO } from '../model/PatientDAO'

const emit = defineEmits<{
  (e: 'logged-in', payload: { patientId: number, firstName: string }): void
}>()

const dao = new PatientDAO()

const lastName = ref('')
const firstName = ref('')
const isNew = ref(false)
const patientId = ref<string>('')      // utilisé si isNew=false
const birthDate = ref<string>('')      // utilisé si isNew=true

async function submit() {
  if (!lastName.value || !firstName.value) {
    alert('Nom et prénom requis')
    return
  }

  try {
    const id =
        await dao.login(
            lastName.value,
            firstName.value,
            isNew.value ? null : Number(patientId.value),
            isNew.value ? birthDate.value : null,
            isNew.value
        )

    emit('logged-in', { patientId: id, firstName: firstName.value })
  } catch (e: any) {
    alert(e.message || 'Erreur login')
  }
}
</script>

<template>
  <div class="login-box">
    <h2>Connexion patient</h2>

    <div class="form-group">
      <label>Nom</label>
      <input v-model="lastName" placeholder="Dupont" />
    </div>

    <div class="form-group">
      <label>Prénom</label>
      <input v-model="firstName" placeholder="Alice" />
    </div>

    <div class="form-group checkbox">
      <input type="checkbox" id="new" v-model="isNew" />
      <label for="new">Nouveau patient</label>
    </div>

    <div class="form-group" v-if="!isNew">
      <label>Numéro patient</label>
      <input v-model="patientId" placeholder="ex: 6" />
    </div>

    <div class="form-group" v-else>
      <label>Date de naissance</label>
      <input type="date" v-model="birthDate" />
    </div>

    <button @click="submit">Login</button>
  </div>
</template>
