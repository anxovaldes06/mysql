
import java.io.*;
import java.sql.*;
import java.util.*;


public class northwind {

    private static final String DEFAULT_SERVIDOR = "127.0.0.1";
    private static final String DEFAULT_PUERTO = "3306";
    private static final String DEFAULT_USUARIO = "anxov";
    private static final String BASE_DATOS = "northwind";

    private String usuario;
    private String password;
    private String url;

    private List<Producto> listaProductosA;

    public northwind() {
        listaProductosA = new ArrayList<>();
    }

    public static void main(String[] args) {
        new northwind().iniciar();
    }

    public void iniciar() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("--- Conexión MySQL - Northwind (Consola) ---");
        
        System.out.print("Servidor [" + DEFAULT_SERVIDOR + "]: ");
        String servidorInput = scanner.nextLine().trim();
        String servidor = servidorInput.isEmpty() ? DEFAULT_SERVIDOR : servidorInput;

        System.out.print("Puerto [" + DEFAULT_PUERTO + "]: ");
        String puertoInput = scanner.nextLine().trim();
        String puerto = puertoInput.isEmpty() ? DEFAULT_PUERTO : puertoInput;

        System.out.print("Usuario [" + DEFAULT_USUARIO + "]: ");
        String usuarioInput = scanner.nextLine().trim();
        usuario = usuarioInput.isEmpty() ? DEFAULT_USUARIO : usuarioInput;

        System.out.print("Contraseña: ");
        password = scanner.nextLine(); 

        url = "jdbc:mysql://" + servidor + ":" + puerto + "/" + BASE_DATOS;

        System.out.println("Intentando conectar a " + url + "...");
        try (Connection con = DriverManager.getConnection(url, usuario, password)) {
            System.out.println("¡Conexión exitosa a la base de datos " + BASE_DATOS + "!");
        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
            System.err.println("Por favor, reinicia el programa y verifica tus credenciales.");
            return; 
        }

        mostrarMenu(scanner);
        scanner.close();
    }

    private void mostrarMenu(Scanner scanner) {
        String opcion = "";
        do {
            System.out.println("\n--- MENÚ DE TAREAS ---");
            System.out.println("A. Consultar productos (Categoría 1, Precio >= 18.00)");
            System.out.println("B. Reponer existencias (Producto 1, Categoría 1, +5 unidades)");
            System.out.println("C. Generar informe de texto (Requiere tarea A previa)");
            System.out.println("S. Salir");
            System.out.print("Selecciona una opción: ");
            opcion = scanner.nextLine().trim().toUpperCase();

            switch (opcion) {
                case "A":
                    tareaA_consultarProductos();
                    break;
                case "B":
                    tareaB_reponerExistencias();
                    break;
                case "C":
                    if (!listaProductosA.isEmpty() || !avisoListaVacía_A) {
                         tareaC_generarInforme();
                    } else {
                        System.out.println("Error: Debes ejecutar con éxito la tarea A antes de generar el informe.");
                    }
                    break;
                case "S":
                    System.out.println("Saliendo del programa...");
                    break;
                default:
                    System.out.println("Opción no válida. Inténtalo de nuevo.");
            }

        } while (!opcion.equals("S"));
    }
    
    private boolean avisoListaVacía_A = false; 

  
    private void tareaA_consultarProductos() {
        listaProductosA.clear(); 
        avisoListaVacía_A = false;

        String sql = "SELECT ProductID, ProductName, UnitPrice, UnitsInStock " +
                     "FROM products " +
                     "WHERE CategoryID = ? AND UnitPrice >= ? " +
                     "ORDER BY ProductID ASC";

        System.out.println("\n--- Ejecutando Tarea A: Consultar Productos ---");

        try (Connection con = DriverManager.getConnection(url, usuario, password);
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, 1);     
            pst.setDouble(2, 18.0);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Producto p = new Producto(
                        rs.getInt("ProductID"),
                        rs.getString("ProductName"),
                        rs.getDouble("UnitPrice"),
                        rs.getInt("UnitsInStock")
                    );
                    listaProductosA.add(p);
                }
            }

            if (listaProductosA.isEmpty()) {
                System.out.println("AVISO: No se encontraron productos (Cat 1, Precio >= 18).");
                avisoListaVacía_A = true;
            } else {
                System.out.println("Lista de productos recuperados (" + listaProductosA.size() + "):");
                for (Producto prod : listaProductosA) {
                    System.out.println(prod); 
                }
            }

        } catch (SQLException e) {
            System.err.println("Error en la consulta A: " + e.getMessage());
            e.printStackTrace();
        }
    }

   
    private void tareaB_reponerExistencias() {
        String sql = "UPDATE products SET UnitsInStock = UnitsInStock + ? " +
                     "WHERE ProductID = ? AND CategoryID = ?";

        System.out.println("\n--- Ejecutando Tarea B: Reponer Existencias ---");

        try (Connection con = DriverManager.getConnection(url, usuario, password);
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, 5);
            pst.setInt(2, 1); 
            pst.setInt(3, 1); 

            int filasAfectadas = pst.executeUpdate();

            if (filasAfectadas > 0) {
                System.out.println("EXITO: Se han sumado 5 unidades a las existencias del producto 1.");
                System.out.println("Filas actualizadas: " + filasAfectadas);
            } else {
                System.out.println("AVISO: No se actualizó ningún producto.");
                System.out.println("Verifica que exista el ProductID 1 en la CategoryID 1.");
            }

        } catch (SQLException e) {
            System.err.println("Error en la actualización B: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void tareaC_generarInforme() {
        System.out.println("\n--- Ejecutando Tarea C: Generar Informe ---");
        
        String nombreFichero = "informe_existencias.txt";
        File fichero = new File(nombreFichero);

        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(fichero))) {

            escritor.write("ID|NOMBRE|PRECIO|EXISTENCIAS");
            escritor.newLine();

            if (!listaProductosA.isEmpty()) {
                for (Producto p : listaProductosA) {
                    String linea = String.format("%d|%s|%.2f|%d", 
                        p.getId(), 
                        p.getNombre(), 
                        p.getPrecio(), 
                        p.getExistencias());
                    escritor.write(linea);
                    escritor.newLine();
                }
            }

            System.out.println("Informe generado con éxito en: " + fichero.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("Error al escribir el informe C: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    }
