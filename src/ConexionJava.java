import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ConexionJava extends JFrame {

    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JTextField txtServidor;
    private JTextField txtPuerto;
    private JComboBox<String> cbBases;
    private JButton btnConectar;
    private JButton btnCargarTablas;
    private JButton btnActPrecioProd;

    public ConexionJava() {
        setTitle("Conexión MySQL - Northwind");
        setSize(450, 320);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        setLayout(new BorderLayout(10, 10));

        JPanel panelFormulario = new JPanel(new GridLayout(5, 2, 10, 10));
        panelFormulario.setBorder(new EmptyBorder(15, 15, 5, 15));

        panelFormulario.add(new JLabel("Usuario:"));
        txtUsuario = new JTextField("root");
        panelFormulario.add(txtUsuario);

        panelFormulario.add(new JLabel("Contraseña:"));
        txtPassword = new JPasswordField();
        panelFormulario.add(txtPassword);

        panelFormulario.add(new JLabel("Servidor:"));
        txtServidor = new JTextField("127.0.0.1");
        panelFormulario.add(txtServidor);

        panelFormulario.add(new JLabel("Puerto:"));
        txtPuerto = new JTextField("3306");
        panelFormulario.add(txtPuerto);

        panelFormulario.add(new JLabel("Base de datos:"));
        cbBases = new JComboBox<>();
        panelFormulario.add(cbBases);

        JPanel panelBotones = new JPanel(new GridLayout(3, 1, 5, 8));
        panelBotones.setBorder(new EmptyBorder(5, 15, 15, 15));

        btnConectar = new JButton("Conectar");
        btnCargarTablas = new JButton("Mostrar tablas");
        btnActPrecioProd = new JButton("Actualizar precio productos");

        panelBotones.add(btnConectar);
        panelBotones.add(btnCargarTablas);
        panelBotones.add(btnActPrecioProd);

        add(panelFormulario, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);

        btnConectar.addActionListener(e -> conectar());
        btnCargarTablas.addActionListener(e -> cargarTablas());
        btnActPrecioProd.addActionListener(e -> actualizar());
    }

    private void conectar() {
        cbBases.removeAllItems();

        String servidor = txtServidor.getText().trim();
        String puerto = txtPuerto.getText().trim();
        String usuario = txtUsuario.getText().trim();

        if (servidor.isEmpty() || usuario.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Introduce el servidor y el usuario.");
            return;
        }

        try {
            int numeroPuerto = Integer.parseInt(puerto);
            if (numeroPuerto < 1 || numeroPuerto > 45003) {
                JOptionPane.showMessageDialog(this, "El puerto debe estar entre 1 y 45003.");
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Introduce un puerto válido.");
            return;
        }

        String url = "jdbc:mysql://" + servidor + ":" + puerto + "/";

        try (
            Connection con = DriverManager.getConnection(url, usuario, new String(txtPassword.getPassword()));
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SHOW DATABASES")
        ) {
            while (rs.next()) {
                cbBases.addItem(rs.getString(1));
            }

            if (cbBases.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, "No hay bases de datos disponibles.");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar las bases de datos: " + e.getMessage());
        }
    }

    private void cargarTablas() {
        String bd = (String) cbBases.getSelectedItem();

        if (bd == null) {
            JOptionPane.showMessageDialog(this, "Carga las bases de datos y selecciona una.");
            return;
        }

        String url = "jdbc:mysql://"
                + txtServidor.getText().trim()
                + ":"
                + txtPuerto.getText().trim()
                + "/"
                + bd;

        try (
            Connection con = DriverManager.getConnection(
                url,
                txtUsuario.getText().trim(),
                new String(txtPassword.getPassword()))
        ) {
            JOptionPane.showMessageDialog(this, "Tablas cargadas correctamente");
            mostrarTablas(con);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al conectar: " + e.getMessage());
        }
    }

    private void mostrarTablas(Connection con) {
        JFrame ventana = new JFrame("Tablas");
        ventana.setSize(300, 300);
        ventana.setLayout(new BorderLayout());

        DefaultListModel<String> modelo = new DefaultListModel<>();
        JList<String> lista = new JList<>(modelo);

        JButton btnSeleccionar = new JButton("Seleccionar");
        ventana.add(new JScrollPane(lista), BorderLayout.CENTER);
        ventana.add(btnSeleccionar, BorderLayout.SOUTH);

        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SHOW TABLES");

            while (rs.next()) {
                modelo.addElement(rs.getString(1));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar tablas");
        }

        btnSeleccionar.addActionListener(e -> {
            String tabla = lista.getSelectedValue();

            if (tabla == null) {
                JOptionPane.showMessageDialog(ventana, "Selecciona una tabla");
            } else {
                JOptionPane.showMessageDialog(ventana, "Tabla seleccionada: " + tabla);
            }
        });

        ventana.setVisible(true);
    }

    private void actualizar() {
        String bd = (String) cbBases.getSelectedItem();

        if (bd == null || !bd.equalsIgnoreCase("Northwind")) {
            JOptionPane.showMessageDialog(
                this,
                "Debes seleccionar la base de datos Northwind para realizar esta acción."
            );
            return;
        }

        String url = "jdbc:mysql://"
                + txtServidor.getText().trim()
                + ":"
                + txtPuerto.getText().trim()
                + "/"
                + bd;

        try (Connection con = DriverManager.getConnection(
                url,
                txtUsuario.getText().trim(),
                new String(txtPassword.getPassword()))) {
            
            abrirVentanaActualizacionPrecios(url);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Error al conectar con la base de datos: " + e.getMessage()
            );
        }
    }

 private void abrirVentanaActualizacionPrecios(String urlConexion) {
        JFrame ventanaAct = new JFrame("Actualización de precios");
        ventanaAct.setSize(400, 200);
        ventanaAct.setLocationRelativeTo(this);
        ventanaAct.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        ventanaAct.setLayout(new GridLayout(3, 2, 10, 10));
        ((JComponent) ventanaAct.getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        ventanaAct.add(new JLabel("Categoría:"));
        JComboBox<ItemCategoria> cbCategorias = new JComboBox<>();
        ventanaAct.add(cbCategorias);

        ventanaAct.add(new JLabel("Porcentaje (%):"));
        JTextField txtPorcentaje = new JTextField();
        ventanaAct.add(txtPorcentaje);

        JButton btnActualizarCat = new JButton("ACTUALIZAR");
        ventanaAct.add(new JLabel()); 
        ventanaAct.add(btnActualizarCat);

        String sql = "SELECT DISTINCT c.CategoryID, c.CategoryName " +
                     "FROM categories c JOIN products p ON c.CategoryID = p.CategoryID";

        try (Connection con = DriverManager.getConnection(urlConexion, txtUsuario.getText().trim(), new String(txtPassword.getPassword()));
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            boolean hayOpciones = false;
            while (rs.next()) {
                int id = rs.getInt("CategoryID");
                String nombre = rs.getString("CategoryName");
                cbCategorias.addItem(new ItemCategoria(id, nombre));
                hayOpciones = true;
            }

            if (!hayOpciones) {
                JOptionPane.showMessageDialog(ventanaAct, "No existen categorías con productos asociados para actualizar.");
                return; 
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(ventanaAct, "Error al cargar categorías: " + e.getMessage());
            return;
        }

        btnActualizarCat.addActionListener(e -> {
            ItemCategoria categoriaSeleccionada = (ItemCategoria) cbCategorias.getSelectedItem();
            if (categoriaSeleccionada == null) {
                JOptionPane.showMessageDialog(ventanaAct, "Debes seleccionar una categoría válida.");
                return;
            }

            String textoPorcentaje = txtPorcentaje.getText().trim();
            if (textoPorcentaje.isEmpty()) {
                JOptionPane.showMessageDialog(ventanaAct, "El campo de porcentaje no puede estar vacío.");
                return;
            }

            int porcentaje;
            try {
                porcentaje = Integer.parseInt(textoPorcentaje);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(ventanaAct, "El porcentaje debe ser un número entero positivo.");
                return;
            }

            if (porcentaje < 0) {
                JOptionPane.showMessageDialog(ventanaAct, "El porcentaje no puede ser negativo.");
                return;
            }

            if (porcentaje == 0) {
                JOptionPane.showMessageDialog(ventanaAct, "No habrá cambios");
                return;
            }

            int confirmacion = JOptionPane.showConfirmDialog(
                ventanaAct,
                "¿Estás seguro de actualizar los precios de la categoría '" + categoriaSeleccionada.getNombre() + "' con un " + porcentaje + "%?",
                "Confirmación de actualización",
                JOptionPane.YES_NO_OPTION
            );

            if (confirmacion != JOptionPane.YES_OPTION) {
                return; 
            }

            btnActualizarCat.setEnabled(false);

            String sqlUpdate = "UPDATE products SET UnitPrice = ROUND(UnitPrice * (1 + ? / 100), 2) WHERE CategoryID = ?";

            try (Connection con = DriverManager.getConnection(urlConexion, txtUsuario.getText().trim(), new String(txtPassword.getPassword()));
                 PreparedStatement pst = con.prepareStatement(sqlUpdate)) {

                pst.setInt(1, porcentaje);
                pst.setInt(2, categoriaSeleccionada.getId());

                int filas = pst.executeUpdate();

                if (filas > 0) {
                    JOptionPane.showMessageDialog(ventanaAct, "Se han actualizado " + filas + " productos correctamente.");
                    ventanaAct.dispose();
                } else {
                    JOptionPane.showMessageDialog(ventanaAct, "No se ha modificado ningún producto.");
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(ventanaAct, "Error en Base de Datos: " + ex.getMessage());
            } finally {
                btnActualizarCat.setEnabled(true);
            }
        });

        ventanaAct.setVisible(true);
    }

    private static class ItemCategoria {
        private int id;
        private String nombre;

        public ItemCategoria(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public int getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }

        @Override
        public String toString() {
            return id + " - " + nombre; 
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ConexionJava().setVisible(true));
    }
}