# Conexión Java a MySQL

Aplicación de escritorio en Java que permite conectarse a un servidor MySQL,
cargar las bases de datos disponibles para el usuario y elegir una desde un
menú desplegable.

## Requisitos

- Java instalado.
- MySQL Server en ejecución.
- El conector MySQL Connector/J incluido en `lib`.

## Ejecutar en Visual Studio Code

Abre esta carpeta en VS Code y ejecuta la configuración **Launch ConexionJava**.
La configuración incluye el conector JDBC necesario para la conexión.

En la aplicación, introduce el usuario, la contraseña, el servidor y el puerto
de MySQL. Pulsa **CARGAR** para consultar las bases de datos disponibles,
selecciona una y pulsa **CONECTAR**.

La conexión inicial usa `localhost:3306` como servidor y puerto. El usuario y
la contraseña deben corresponder a una cuenta válida de MySQL con permisos
para ver y acceder a la base de datos seleccionada.