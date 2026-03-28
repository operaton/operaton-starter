import { createRouter, createWebHistory } from 'vue-router'
import GalleryView from '../views/GalleryView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'gallery',
      component: GalleryView
    },
    {
      path: '/configure',
      name: 'configure',
      component: () => import('../views/ConfigureView.vue')
    }
  ]
})

export default router
