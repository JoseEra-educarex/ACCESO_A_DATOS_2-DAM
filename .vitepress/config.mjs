import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { defineConfig } from 'vitepress'
import { withMermaid } from 'vitepress-plugin-mermaid'

// ─────────────────────────────────────────────────────────────────────────────
// Web de los apuntes con VitePress.
// Los .md no se tocan: todo lo que hace falta para publicarlos está aquí.
// ─────────────────────────────────────────────────────────────────────────────
const REPO = 'avalverdec07/ACCESO_A_DATOS_2-DAM'
const TITULO = 'ACCESO A DATOS 2ºDAM'
const RAIZ = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')

// Las unidades se leen de los archivos Tema_N_….md de la raíz: un tema nuevo sale solo en el menú.
// Las páginas que viven dentro de Tema_N_imagenes/ se cuelgan de su tema.
const natural = (a, b) => a.localeCompare(b, 'es', { numeric: true })
const tituloDe = (rel) => {
  const m = fs.readFileSync(path.join(RAIZ, rel), 'utf8').match(/^#\s+(.+?)\s*$/m)
  return m ? m[1].replace(/\.$/, '') : rel.replace(/\.md$/, '').replace(/_/g, ' ')
}
function menu() {
  const temas = fs.readdirSync(RAIZ).filter((f) => /^Tema_\d+.*\.md$/.test(f)).sort(natural)
  const items = []
  for (const f of temas) {
    items.push({ text: tituloDe(f), link: '/' + f.replace(/\.md$/, '') })
    const n = f.match(/^Tema_(\d+)/)[1]
    const carpeta = path.join(RAIZ, `Tema_${n}_imagenes`)
    if (fs.existsSync(carpeta)) {
      for (const g of fs.readdirSync(carpeta).filter((x) => x.endsWith('.md')).sort(natural)) {
        const rel = `Tema_${n}_imagenes/${g}`
        items.push({ text: tituloDe(rel), link: '/' + rel.replace(/\.md$/, '') })
      }
    }
  }
  return [{ text: 'Inicio', link: '/' }, { text: 'Unidades didácticas', items }]
}

// Anclas como las crea GitHub (minúsculas, con tildes, sin signos): así los índices
// escritos a mano en los temas (#1-introducción…) siguen funcionando.
const anclaGitHub = (s) =>
  s.trim().toLowerCase().replace(/<[^>]+>/g, '').replace(/[^\p{L}\p{N}\s_-]/gu, '').replace(/\s/g, '-')

// Imagen que no está junto al .md: se busca desde la raíz del repositorio.
// Si tampoco está, aviso al construir y la página se publica igual.
const IMAGEN = /\.(png|jpe?g|gif|svg|webp|avif)$/i
const FALTA = '\0imagen-que-falta'
function imagenes() {
  return {
    name: 'imagenes',
    enforce: 'pre',
    resolveId(origen, importador) {
      if (!importador || !importador.endsWith('.md') || !IMAGEN.test(origen) || origen.startsWith('/')) return null
      const junto = path.resolve(path.dirname(importador), decodeURIComponent(origen))
      if (fs.existsSync(junto)) return null
      const desdeRaiz = path.resolve(RAIZ, decodeURIComponent(origen))
      if (fs.existsSync(desdeRaiz)) return desdeRaiz
      console.warn(`⚠ Imagen no encontrada: ${origen} (en ${path.relative(RAIZ, importador)})`)
      return FALTA
    },
    load(id) {
      return id === FALTA ? 'export default ""' : null
    },
  }
}

export default withMermaid(
  defineConfig({
    lang: 'es-ES',
    title: TITULO,
    description: 'Apuntes y ejercicios del módulo de Acceso a Datos (2º DAM)',
    base: '/ACCESO_A_DATOS_2-DAM/',
    lastUpdated: true,
    // Lo que no son apuntes: el código Java, la configuración de Jekyll y la del IDE
    srcExclude: ['src/**', '_layouts_old/**', '.idea/**', 'README.md'],

    transformPageData(pageData) {
      const fm = pageData.frontmatter
      if (fm.layout === 'default') delete fm.layout // la portada venía de Jekyll
      if (/^Tema_\d+/.test(path.basename(pageData.relativePath))) fm.tema ??= true // botón «Descargar PDF»
    },

    markdown: {
      anchor: { slugify: anclaGitHub },
      codeCopyButtonTitle: 'Copiar código',
      container: {
        tipLabel: 'Consejo',
        infoLabel: 'Información',
        warningLabel: 'Atención',
        dangerLabel: 'Importante',
        detailsLabel: 'Ver más',
      },
    },

    themeConfig: {
      nav: [{ text: 'Inicio', link: '/' }],
      sidebar: menu(),
      outline: { level: [2, 3], label: 'En esta página' },
      search: {
        provider: 'local',
        options: {
          translations: {
            button: { buttonText: 'Buscar', buttonAriaLabel: 'Buscar' },
            modal: {
              noResultsText: 'Sin resultados para',
              resetButtonTitle: 'Borrar búsqueda',
              backButtonTitle: 'Cerrar',
              displayDetails: 'Ver detalles',
              footer: { selectText: 'abrir', navigateText: 'moverse', closeText: 'cerrar' },
            },
          },
        },
      },
      editLink: { pattern: `https://github.com/${REPO}/edit/main/:path`, text: 'Editar esta página en GitHub' },
      lastUpdated: { text: 'Última actualización', formatOptions: { dateStyle: 'long', forceLocale: true } },
      docFooter: { prev: 'Anterior', next: 'Siguiente' },
      darkModeSwitchLabel: 'Apariencia',
      lightModeSwitchTitle: 'Cambiar a modo claro',
      darkModeSwitchTitle: 'Cambiar a modo oscuro',
      sidebarMenuLabel: 'Menú',
      returnToTopLabel: 'Volver arriba',
      notFound: {
        title: 'PÁGINA NO ENCONTRADA',
        quote: 'Puede que el tema haya cambiado de nombre.',
        linkText: 'Volver al inicio',
      },
      socialLinks: [{ icon: 'github', link: `https://github.com/${REPO}` }],
    },

    vite: { plugins: [imagenes()] },

    mermaid: { theme: 'neutral' },
  }),
)
