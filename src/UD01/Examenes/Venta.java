package UD01.Examenes;

public class Venta {
        private String codigo;
        private String producto;
        private String categoria;
        private int unidades;
        private double precio;

        public Venta(String codigo, String producto, String categoria, int unidades, double precio) {
            this.codigo = codigo;
            this.producto = producto;
            this.categoria = categoria;
            this.unidades = unidades;
            this.precio = precio;
        }

        public String getCodigo() {
            return codigo;
        }

        public void setCodigo(String codigo) {
            this.codigo = codigo;
        }

        public String getProducto() {
            return producto;
        }

        public void setProducto(String producto) {
            this.producto = producto;
        }

        public String getCategoria() {
            return categoria;
        }

        public void setCategoria(String categoria) {
            this.categoria = categoria;
        }

        public int getUnidades() {
            return unidades;
        }

        public void setUnidades(int unidades) {
            this.unidades = unidades;
        }

        public double getPrecio() {
            return precio;
        }

        public void setPrecio(double precio) {
            this.precio = precio;
        }
}
