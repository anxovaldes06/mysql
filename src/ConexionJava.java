import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

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
                ResultSet rs = st.executeQuery("SHOW DATABASES")) {
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

        mostrarTablas(url);
    }

    private Connection abrirConexion(String url) throws SQLException {
        return DriverManager.getConnection(url, txtUsuario.getText().trim(),
                new String(txtPassword.getPassword()));
    }

    private void mostrarTablas(String urlConexion) {
        JFrame ventana = new JFrame("Gestión de Tablas - Northwind");
        ventana.setSize(750, 500);
        ventana.setLocationRelativeTo(this);
        ventana.setLayout(new BorderLayout(10, 10));
        ((JComponent) ventana.getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        DefaultListModel<String> modeloLista = new DefaultListModel<>();
        JList<String> listaTablas = new JList<>(modeloLista);
        listaTablas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableModel modeloTabla = new DefaultTableModel();
        JTable tablaDatos = new JTable(modeloTabla);
        tablaDatos.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(listaTablas),
                new JScrollPane(tablaDatos));
        splitPane.setDividerLocation(200);

        JPanel panelBotones = new JPanel();
        JButton btnSeleccionar = new JButton("Ver Contenido");
        JButton btnImportarProd = new JButton("Importar Productos");
        JButton btnExportarProd = new JButton("Exportar Productos");

        panelBotones.add(btnSeleccionar);
        panelBotones.add(btnImportarProd);
        panelBotones.add(btnExportarProd);

        ventana.add(splitPane, BorderLayout.CENTER);
        ventana.add(panelBotones, BorderLayout.SOUTH);

        try (Connection con = abrirConexion(urlConexion);
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SHOW TABLES")) {

            while (rs.next()) {
                modeloLista.addElement(rs.getString(1));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(ventana, "Error al cargar las tablas: " + e.getMessage());
            ventana.dispose();
            return;
        }

        btnSeleccionar.addActionListener(e -> {
            String tablaSeleccionada = listaTablas.getSelectedValue();

            if (tablaSeleccionada == null) {
                JOptionPane.showMessageDialog(ventana, "Por favor, selecciona una tabla de la lista.");
                return;
            }

            try (Connection con = abrirConexion(urlConexion)) {
                cargarContenidoTablaEnJTable(con, tablaSeleccionada, modeloTabla);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(ventana, "Error al conectar: " + ex.getMessage());
            }
        });

        btnImportarProd.addActionListener(e -> importarProductos(ventana, urlConexion, modeloTabla));
        btnExportarProd.addActionListener(e -> exportarProductos(ventana, urlConexion));
        ventana.setVisible(true);
    }

    private void importarProductos(JFrame ventana, String urlConexion, DefaultTableModel modeloTabla) {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Seleccionar productos_a_importar.txt");
        selector.setFileFilter(new FileNameExtensionFilter("Ficheros de texto (*.txt)", "txt"));

        if (selector.showOpenDialog(ventana) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File fichero = selector.getSelectedFile();
        if (!fichero.getName().toLowerCase().endsWith(".txt")) {
            JOptionPane.showMessageDialog(ventana, "Selecciona un fichero .txt.");
            return;
        }

        String sql = "INSERT INTO products (ProductName, SupplierID, CategoryID, QuantityPerUnit, "
                + "UnitPrice, UnitsInStock, UnitsOnOrder, ReorderLevel, Discontinued) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int insertados = 0;

        try (BufferedReader lector = new BufferedReader(new FileReader(fichero));
                Connection con = abrirConexion(urlConexion);
                PreparedStatement pst = con.prepareStatement(sql)) {

            String linea;
            while ((linea = lector.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }

                String[] datos = linea.split(",", -1);
                if (datos.length != 10) {
                    throw new IOException("La línea no tiene 10 valores: " + linea);
                }

                pst.setString(1, datos[1].trim());
                if (datos[2].trim().equalsIgnoreCase("NULL")) {
                    pst.setNull(2, Types.INTEGER);
                } else {
                    pst.setInt(2, Integer.parseInt(datos[2].trim()));
                }
                pst.setInt(3, Integer.parseInt(datos[3].trim()));
                pst.setString(4, datos[4].trim());
                pst.setDouble(5, Double.parseDouble(datos[5].trim()));
                pst.setInt(6, Integer.parseInt(datos[6].trim()));
                pst.setInt(7, Integer.parseInt(datos[7].trim()));
                pst.setInt(8, Integer.parseInt(datos[8].trim()));
                pst.setBoolean(9, datos[9].trim().equals("1"));
                insertados += pst.executeUpdate();
            }

            JOptionPane.showMessageDialog(ventana, "Productos importados: " + insertados);
            cargarContenidoTablaEnJTable(con, "products", modeloTabla);

        } catch (IOException | SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(ventana, "Error al importar productos: " + ex.getMessage());
        }
    }

    private void exportarProductos(JFrame ventana, String urlConexion) {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Guardar productos exportados (.txt)");
        selector.setFileFilter(new FileNameExtensionFilter("Ficheros de texto (*.txt)", "txt"));
        
        selector.setSelectedFile(new File("productos_exportados.txt"));

        int resultado = selector.showSaveDialog(ventana);
        if (resultado != JFileChooser.APPROVE_OPTION) {
            return; 
        }

        File fichero = selector.getSelectedFile();
        
        if (!fichero.getName().toLowerCase().endsWith(".txt")) {
            fichero = new File(fichero.getAbsolutePath() + ".txt");
        }

        if (fichero.exists()) {
            int opcion = JOptionPane.showConfirmDialog(
                ventana,
                "El archivo ya existe. ¿Deseas sobreescribirlo?",
                "Confirmar sobreescritura",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            if (opcion != JOptionPane.YES_OPTION) {
                return; 
            }
        }

        String sql = "SELECT ProductID, ProductName, SupplierID, CategoryID, QuantityPerUnit, "
                   + "UnitPrice, UnitsInStock, UnitsOnOrder, ReorderLevel, Discontinued "
                   + "FROM products";
        
        int exportados = 0;

        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(fichero));
             Connection con = abrirConexion(urlConexion);
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                String id = String.valueOf(rs.getInt("ProductID"));
                String productName = rs.getString("ProductName");
                
                int supplierID = rs.getInt("SupplierID");
                String supStr = rs.wasNull() ? "NULL" : String.valueOf(supplierID);
                
                String categoryID = String.valueOf(rs.getInt("CategoryID"));
                String quantityPerUnit = rs.getString("QuantityPerUnit");
                String unitPrice = String.valueOf(rs.getDouble("UnitPrice"));
                String unitsInStock = String.valueOf(rs.getInt("UnitsInStock"));
                String unitsOnOrder = String.valueOf(rs.getInt("UnitsOnOrder"));
                String reorderLevel = String.valueOf(rs.getInt("ReorderLevel"));
                
                boolean discontinued = rs.getBoolean("Discontinued");
                String discStr = discontinued ? "1" : "0";

                String linea = id + "," + 
                               (productName != null ? productName : "") + "," + 
                               supStr + "," + 
                               categoryID + "," + 
                               (quantityPerUnit != null ? quantityPerUnit : "") + "," + 
                               unitPrice + "," + 
                               unitsInStock + "," + 
                               unitsOnOrder + "," + 
                               reorderLevel + "," + 
                               discStr;

                escritor.write(linea);
                escritor.newLine(); 
                exportados++;
            }

            JOptionPane.showMessageDialog(ventana, "Productos exportados con éxito: " + exportados);

        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(ventana, "Error al exportar productos: " + ex.getMessage());
        }
    }


    private void cargarContenidoTablaEnJTable(Connection con, String nombreTabla, DefaultTableModel modeloTabla) {
        try (Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM " + nombreTabla)) {

            modeloTabla.setRowCount(0);
            modeloTabla.setColumnCount(0);

            ResultSetMetaData metaData = rs.getMetaData();
            int numeroColumnas = metaData.getColumnCount();

            for (int i = 1; i <= numeroColumnas; i++) {
                modeloTabla.addColumn(metaData.getColumnName(i));
            }

            while (rs.next()) {
                Object[] fila = new Object[numeroColumnas];
                for (int i = 1; i <= numeroColumnas; i++) {
                    fila[i - 1] = rs.getObject(i);
                }
                modeloTabla.addRow(fila);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al refrescar la tabla en la interfaz: " + e.getMessage());
        }
    }

    private void actualizar() {
        String bd = (String) cbBases.getSelectedItem();

        if (bd == null || !bd.equalsIgnoreCase("Northwind")) {
            JOptionPane.showMessageDialog(
                    this,
                    "Debes seleccionar la base de datos Northwind para realizar esta acción.");
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
                    "Error al conectar con la base de datos: " + e.getMessage());
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

        try (Connection con = DriverManager.getConnection(urlConexion, txtUsuario.getText().trim(),
                new String(txtPassword.getPassword()));
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
                JOptionPane.showMessageDialog(ventanaAct,
                        "No existen categorías con productos asociados para actualizar.");
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
                    "¿Estás seguro de actualizar los precios de la categoría '" + categoriaSeleccionada.getNombre()
                            + "' con un " + porcentaje + "%?",
                    "Confirmación de actualización",
                    JOptionPane.YES_NO_OPTION);

            if (confirmacion != JOptionPane.YES_OPTION) {
                return;
            }

            btnActualizarCat.setEnabled(false);

            String sqlUpdate = "UPDATE products SET UnitPrice = ROUND(UnitPrice * (1 + ? / 100), 2) WHERE CategoryID = ?";

            try (Connection con = DriverManager.getConnection(urlConexion, txtUsuario.getText().trim(),
                    new String(txtPassword.getPassword()));
                    PreparedStatement pst = con.prepareStatement(sqlUpdate)) {

                pst.setInt(1, porcentaje);
                pst.setInt(2, categoriaSeleccionada.getId());

                int filas = pst.executeUpdate();

                if (filas > 0) {
                    JOptionPane.showMessageDialog(ventanaAct,
                            "Se han actualizado " + filas + " productos correctamente.");
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