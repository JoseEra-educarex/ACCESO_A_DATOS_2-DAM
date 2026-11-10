import mediumZoom from 'medium-zoom'
import { useRoute } from 'vitepress'
import DefaultTheme from 'vitepress/theme'
import { h, nextTick, onMounted, watch } from 'vue'
import BotonPDF from './components/BotonPDF.vue'
import './custom.css'

export default {
  extends: DefaultTheme,

  // Botón «Descargar PDF» encima de cada tema
  Layout: () => h(DefaultTheme.Layout, null, { 'doc-before': () => h(BotonPDF) }),

  setup() {
    // Ampliar las imágenes al pulsarlas
    const route = useRoute()
    const zoom = () => mediumZoom('.vp-doc img:not(.no-zoom)', { background: 'rgba(0,0,0,.8)' })
    onMounted(zoom)
    watch(() => route.path, () => nextTick(zoom))
  },
}
