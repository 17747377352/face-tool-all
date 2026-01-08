import { createRouter, createWebHistory } from 'vue-router'
import FaceRecognitionPage from '../views/FaceRecognitionPage.vue'

const routes = [
  {
    path: '/',
    redirect: '/face-recognition'
  },
  {
    path: '/face-recognition',
    name: 'FaceRecognition',
    component: FaceRecognitionPage
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
