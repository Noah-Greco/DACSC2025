import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import ConsultationView from '../views/ConsultationView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',          // L'URL de base (http://localhost:5173/)
      name: 'login',
      component: LoginView // Affiche notre page de login
    },
    {
      path: '/consultations', // L'URL où on ira après (on créera la page demain)
      name: 'consultations',
      // Pour l'instant, on redirige vers login car la page n'existe pas encore
      component: ConsultationView 
    }
  ]
})

export default router