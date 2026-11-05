package UD01.Examenes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Examen_01_FicherosTexto {

        private static final Path FICHERO_ORIGEN = Path.of("datos/ventas.txt");
        private static final Path CARPETA_SALIDA = Path.of("salida");
        private static final Path CARPETA_BACKUP = Path.of("backup");

        public static void main(String[] args) {
            // Apartado 1: Preparación del entorno
            if (!prepararEntorno()) {
                return;
            }

            // Apartado 2: Lectura y procesamiento
            List<Venta> ventasValidas = new ArrayList<>();
            List<String> errores = new ArrayList<>();
            procesarFichero(ventasValidas, errores);

            // Apartados 3 y 4: Generación de informes y registro de errores
            generarInformes(ventasValidas, errores);

            // Apartado 5: Listado de directorios
            listarDirectorio(CARPETA_BACKUP);
            listarDirectorio(CARPETA_SALIDA);
        }

        private static boolean prepararEntorno() {
            if (Files.notExists(FICHERO_ORIGEN) || !Files.isReadable(FICHERO_ORIGEN)) {
                System.err.println("Error: El fichero de origen no existe o no tiene permisos de lectura.");
                return false;
            }

            try {
                System.out.println("Ruta absoluta: " + FICHERO_ORIGEN.toAbsolutePath());
                System.out.println("Tamaño del fichero: " + Files.size(FICHERO_ORIGEN) + " bytes\n");

                Files.createDirectories(CARPETA_SALIDA);
                Files.createDirectories(CARPETA_BACKUP);

                Path ficheroBackup = CARPETA_BACKUP.resolve("ventas_backup.txt");
                Files.copy(FICHERO_ORIGEN, ficheroBackup, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("-> Copia de seguridad realizada con éxito en " + ficheroBackup);
                return true;

            } catch (IOException e) {
                System.err.println("Error en la gestión de directorios/ficheros: " + e.getMessage());
                return false;
            }
        }

        private static void procesarFichero(List<Venta> ventasValidas, List<String> errores) {
            try (BufferedReader br = Files.newBufferedReader(FICHERO_ORIGEN, StandardCharsets.UTF_8)) {
                String linea;
                int numLinea = 0;

                while ((linea = br.readLine()) != null) {
                    numLinea++;

                    // Ignorar líneas vacías o comentarios
                    if (linea.isBlank() || linea.startsWith("#")) {
                        continue;
                    }

                    String[] campos = linea.split(";");

                    // Validar número de campos
                    if (campos.length != 5) {
                        errores.add("Línea " + numLinea + " | Nº de campos incorrecto | " + linea);
                        continue;
                    }

                    try {
                        String codigo = campos[0].trim();
                        String producto = campos[1].trim();
                        String categoria = campos[2].trim();
                        int unidades = Integer.parseInt(campos[3].trim());
                        double precio = Double.parseDouble(campos[4].trim());

                        // Validar rangos lógicos
                        if (unidades <= 0 || precio <= 0) {
                            errores.add("Línea " + numLinea + " | Valores numéricos fuera de rango | " + linea);
                        } else {
                            ventasValidas.add(new Venta(codigo, producto, categoria, unidades, precio));
                        }

                    } catch (NumberFormatException e) {
                        errores.add("Línea " + numLinea + " | Error de conversión numérica | " + linea);
                    }
                }
                System.out.println("-> Fichero procesado. Ventas válidas: " + ventasValidas.size() + " | Errores: " + errores.size());

            } catch (IOException e) {
                System.err.println("Error al leer el fichero: " + e.getMessage());
            }
        }

        private static void generarInformes(List<Venta> ventasValidas, List<String> errores) {
            Path rutaInforme = CARPETA_SALIDA.resolve("informe_ventas.txt");
            Path rutaErrores = CARPETA_SALIDA.resolve("errores.txt");

            // Generar informe principal (Apartado 3)
            try (BufferedWriter bw = Files.newBufferedWriter(rutaInforme, StandardCharsets.UTF_8)) {
                bw.write("INFORME DE VENTAS - TIENDA INFORMAPLUS");
                bw.newLine();
                bw.write("=========================================================================");
                bw.newLine();
                bw.write(String.format("%-10s %-25s %-15s %-10s %-10s%n", "CÓDIGO", "PRODUCTO", "CATEGORÍA", "UDS", "PRECIO"));
                bw.write("=========================================================================");
                bw.newLine();

                double totalGlobal = 0;
                for (Venta v : ventasValidas) {
                    bw.write(String.format("%-10s %-25s %-15s %-10d %-10.2f%n",
                            v.getCodigo(), v.getProducto(), v.getCategoria(), v.getUnidades(), v.getPrecio()));
                    totalGlobal += (v.getUnidades() * v.getPrecio());
                }

                bw.newLine();
                bw.write(String.format("IMPORTE TOTAL ACUMULADO: %.2f €%n", totalGlobal));
                System.out.println("-> Informe de ventas generado en " + rutaInforme);

            } catch (IOException e) {
                System.err.println("Error al escribir el informe de ventas: " + e.getMessage());
            }

            // Generar registro de errores (Apartado 4)
            try (BufferedWriter bw = Files.newBufferedWriter(rutaErrores, StandardCharsets.UTF_8)) {
                if (errores.isEmpty()) {
                    bw.write("Sin errores");
                } else {
                    for (String error : errores) {
                        bw.write(error);
                        bw.newLine();
                    }
                }
                System.out.println("-> Registro de errores generado en " + rutaErrores + "\n");

            } catch (IOException e) {
                System.err.println("Error al escribir el registro de errores: " + e.getMessage());
            }
        }

        private static void listarDirectorio(Path directorio) {
            System.out.println("Contenido del directorio: " + directorio.getFileName());
            try (Stream<Path> stream = Files.list(directorio)) {
                stream.sorted().forEach(p -> {
                    try {
                        if (Files.isDirectory(p)) {
                            System.out.printf("  <DIRECTORIO>  %s%n", p.getFileName());
                        } else {
                            System.out.printf("  <FICHERO> %s (%d bytes)%n", p.getFileName(), Files.size(p));
                        }
                    } catch (IOException e) {
                        System.err.println("  No se pudo leer la información de: " + p.getFileName());
                    }
                });
            } catch (IOException e) {
                System.err.println("Error al listar el directorio " + directorio + ": " + e.getMessage());
            }
        }
    }
}
