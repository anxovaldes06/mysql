import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

public class ConexionJava extends JFrame {

    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JTextField txtServidor;
    private JTextField txtPuerto;
    private JComboBox<String> cbBasesDatos;
    private JLabel lblEstadoBasesDatos;
    private JButton btnConectar;

    public ConexionJava() {
        setTitle("MySQL");
        setSize(400, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Usuario:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        txtUsuario = new JTextField("root", 15);
        add(txtUsuario, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        add(new JLabel("Contraseña:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        txtPassword = new JPasswordField(15);
        add(txtPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        add(new JLabel("Servidor:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 1;
        txtServidor = new JTextField("127.0.0.1", 10);
        add(txtServidor, gbc);

        gbc.gridx = 2;
        txtPuerto = new JTextField("3306", 5);
        add(txtPuerto, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        add(new JLabel("Base de datos:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 1;
        cbBasesDatos = new JComboBox<>();
        add(cbBasesDatos, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 3;
        lblEstadoBasesDatos = new JLabel(" ");
        add(lblEstadoBasesDatos, gbc);

        gbc.gridx = 1; gbc.gridy = 5; gbc.gridwidth = 1;
        gbc.insets = new Insets(20, 8, 8, 8);
        btnConectar = new JButton("CONECTAR");
        add(btnConectar, gbc);

    
        btnConectar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validarCamposSintacticos()) {
                    realizarConexion();
                } else {
                    mostrarResultadoConexion(false);
                }
            }
        });

        FocusAdapter cargarBasesListener = new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                cargarBasesDeDatos();
            }
        };
        txtUsuario.addFocusListener(cargarBasesListener);
        txtPassword.addFocusListener(cargarBasesListener);
        txtServidor.addFocusListener(cargarBasesListener);
        txtPuerto.addFocusListener(cargarBasesListener);

        cargarBasesDeDatos();
    }


    private boolean validarCamposSintacticos() {
        String usuario = txtUsuario.getText().trim();
        String servidor = txtServidor.getText().trim();
        String puertoStr = txtPuerto.getText().trim();

        if (usuario.isEmpty() || servidor.isEmpty() || puertoStr.isEmpty()) {
            return false;
        }

        try {
            int puerto = Integer.parseInt(puertoStr);
            if (puerto < 0 || puerto > 45003) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }


    private void cargarBasesDeDatos() {
        if (!validarCamposSintacticos()) {
            lblEstadoBasesDatos.setText("Revisa usuario, servidor y puerto.");
            return;
        }

        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());
        String servidor = txtServidor.getText().trim();
        String puerto = txtPuerto.getText().trim();
        String baseSeleccionada = (String) cbBasesDatos.getSelectedItem();

        String url = "jdbc:mysql://" + servidor + ":" + puerto + "/?useSSL=false&serverTimezone=UTC";

        cbBasesDatos.removeAllItems();
        lblEstadoBasesDatos.setText("Cargando bases de datos...");

        try (Connection con = DriverManager.getConnection(url, usuario, password);
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SHOW DATABASES")) {

            while (rs.next()) {
                cbBasesDatos.addItem(rs.getString(1));
            }

            if (cbBasesDatos.getItemCount() == 0) {
                lblEstadoBasesDatos.setText("El servidor no devolvió bases de datos.");
                return;
            }

            String northwind = buscarBaseDeDatos("northwind");
            String baseAnterior = baseSeleccionada == null ? null : buscarBaseDeDatos(baseSeleccionada);
            if (northwind != null) {
                cbBasesDatos.setSelectedItem(northwind);
            } else if (baseAnterior != null) {
                cbBasesDatos.setSelectedItem(baseAnterior);
            } else {
                cbBasesDatos.setSelectedIndex(0);
            }
            lblEstadoBasesDatos.setText(cbBasesDatos.getItemCount() + " bases de datos disponibles.");

        } catch (SQLException e) {
            lblEstadoBasesDatos.setText("No se pudieron cargar las bases de datos.");
        }
    }

    private String buscarBaseDeDatos(String nombre) {
        for (int i = 0; i < cbBasesDatos.getItemCount(); i++) {
            String base = cbBasesDatos.getItemAt(i);
            if (nombre.equalsIgnoreCase(base)) {
                return base;
            }
        }
        return null;
    }

  
    private void realizarConexion() {
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());
        String servidor = txtServidor.getText().trim();
        String puerto = txtPuerto.getText().trim();
        String bd = (String) cbBasesDatos.getSelectedItem();

        if (bd == null || bd.isEmpty()) {
            mostrarResultadoConexion(false);
            return;
        }

        String url = "jdbc:mysql://" + servidor + ":" + puerto + "/" + bd + "?useSSL=false&serverTimezone=UTC";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection con = DriverManager.getConnection(url, usuario, password)) {
            }
        } catch (ClassNotFoundException | SQLException e) {
            mostrarResultadoConexion(false);
            return;
        }
        mostrarResultadoConexion(true);
    }

    private void mostrarResultadoConexion(boolean conectado) {
        String mensaje = conectado ? "Operación exitosa: conectado." : "No se pudo conectar.";
        String titulo = conectado ? "Éxito" : "Conexión fallida";
        int tipo = conectado ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE;
        JOptionPane.showMessageDialog(this, mensaje, titulo, tipo);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ConexionJava().setVisible(true);
        });
    }
}