<script setup>
import { useData } from 'vitepress'
import { onMounted, onUnmounted } from 'vue'

const { frontmatter, isDark } = useData()
let preparado = false
let estabaOscuro = false
const abiertos = []

// Al imprimir: modo claro y bloques plegables abiertos. Al terminar, se restaura.
function antes() {
  if (preparado) return
  preparado = true
  estabaOscuro = isDark.value
  if (estabaOscuro) isDark.value = false
  document.querySelectorAll('.vp-doc details:not([open])').forEach((d) => {
    d.open = true
    abiertos.push(d)
  })
}
function despues() {
  if (!preparado) return
  preparado = false
  if (estabaOscuro) isDark.value = true
  abiertos.splice(0).forEach((d) => (d.open = false))
}

onMounted(() => {
  window.addEventListener('beforeprint', antes)
  window.addEventListener('afterprint', despues)
})
onUnmounted(() => {
  window.removeEventListener('beforeprint', antes)
  window.removeEventListener('afterprint', despues)
})

function descargar() {
  antes()
  setTimeout(() => window.print(), 150) // deja que se repinte en modo claro
}
</script>

<template>
  <div v-if="frontmatter.tema" class="barra-tema">
    <button class="boton-pdf" type="button" title="Guardar este tema como PDF" @click="descargar">
      <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
        <path fill="currentColor" d="M12 16 7 11l1.4-1.45 2.6 2.6V4h2v8.15l2.6-2.6L17 11l-5 5Zm-6 4q-.825 0-1.413-.588T4 18v-3h2v3h12v-3h2v3q0 .825-.588 1.413T18 20H6Z" />
      </svg>
      Descargar PDF
    </button>
  </div>
</template>
