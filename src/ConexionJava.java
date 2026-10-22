import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class ConexionJava extends JFrame {

    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JTextField txtServidor;
    private JTextField txtPuerto;
    private JComboBox<String> cbBases;
    private JButton btnCargarBases;
    private JButton btnConectar;

    public ConexionJava() {

        setTitle("Conexión MySQL");
        setSize(400, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 2, 5, 5));

        add(new JLabel("Usuario:"));
        txtUsuario = new JTextField("root");
        add(txtUsuario);

        add(new JLabel("Contraseña:"));
        txtPassword = new JPasswordField();
        add(txtPassword);

        add(new JLabel("Servidor:"));
        txtServidor = new JTextField("127.0.0.1");
        add(txtServidor);

        add(new JLabel("Puerto:"));
        txtPuerto = new JTextField("3306");
        add(txtPuerto);

        add(new JLabel("Base de datos:"));
        cbBases = new JComboBox<>();
        add(cbBases);

        btnCargarBases = new JButton("Cargar bases");
        add(btnCargarBases);

        btnConectar = new JButton("Conectar");
        add(btnConectar);

        btnCargarBases.addActionListener(e -> cargarBases());
        btnConectar.addActionListener(e -> conectar());
    }

    private void cargarBases() {

        cbBases.removeAllItems();

        String servidor = txtServidor.getText().trim();
        String puerto = txtPuerto.getText().trim();
        String usuario = txtUsuario.getText().trim();

        if (servidor.isEmpty() || usuario.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Introduce el servidor y el usuario."
            );
            return;
        }

        try {
            int numeroPuerto = Integer.parseInt(puerto);

            if (numeroPuerto < 1 || numeroPuerto > 45003) {
                JOptionPane.showMessageDialog(
                        this,
                        "El puerto debe estar entre 1 y 45003."
                );
                return;
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Introduce un puerto válido."
            );
            return;
        }

        String url = "jdbc:mysql://" + servidor + ":" + puerto + "/";

        try (
                Connection con = DriverManager.getConnection(
                        url,
                        usuario,
                        new String(txtPassword.getPassword())
                );

                Statement st = con.createStatement();

                ResultSet rs = st.executeQuery("SHOW DATABASES")
        ) {

            while (rs.next()) {
                cbBases.addItem(rs.getString(1));
            }

            if (cbBases.getItemCount() == 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "No hay bases de datos disponibles."
                );
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "No se pudieron cargar las bases de datos: "
                            + e.getMessage()
            );
        }
    }

    private void conectar() {

        String bd = (String) cbBases.getSelectedItem();

        if (bd == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Carga las bases de datos y selecciona una."
            );
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
                        new String(txtPassword.getPassword())
                )
        ) {

            JOptionPane.showMessageDialog(
                    this,
                    "Conexión realizada correctamente"
            );

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error al conectar: " + e.getMessage()
            );
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() ->
                new ConexionJava().setVisible(true)
        );
    }
}