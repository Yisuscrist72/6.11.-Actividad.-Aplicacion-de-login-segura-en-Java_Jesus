package loginapp;

import java.sql.*;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.mindrot.jbcrypt.BCrypt;

public class LoginAPP {

    static final String DB_URL = "jdbc:mysql://localhost:3306/seguridad_db";
    static final String DB_USER = "root";
    static final String DB_PASSWORD = "root";
    
    public static void main(String[] args) {
        // Declaracion de variables
        Scanner sc = new Scanner(System.in);
        String user = "";
        String password = "";
        Connection connection = null;
        PreparedStatement pstmt = null;
        String sql = "";
        ResultSet rs;
        boolean succesful;
        boolean terminar = false;
        
        try {
            //Conectar con la base de datos.
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver cargado correctamente.");
            
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Conexión Correcta");
            
            // Bucle mientras no haya superado el numero de intentos fallidos.
            while(!terminar){
                //Solicitar datos al usuario
                System.out.print("Insertar su nombre de usuario: ");
                user = sc.nextLine();
                
                System.out.print("Insertar su contrase?a: ");
                password = sc.nextLine();
                
                // El uso de consultas preparadas en la construcción de las sentencia SQL, para evitar el peligro de inyección SQL.
                sql = "SELECT password, failed_attempts, last_attempt FROM users WHERE username = ?";
                pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, user);
                System.out.println("Consulta SQL:\n" + sql);
                
                // Mostrar resultados
                rs = pstmt.executeQuery();
                succesful = false;
                
                if(rs.next()){
                    // Bloqueo de intentos
                    int failed_attempts = rs.getInt("failed_attempts");
                    Timestamp lastAttemptTimestamp = rs.getTimestamp("last_attempt");
                    LocalDateTime now = LocalDateTime.now();
                    
                    // Si ha fallado 3 o más veces, verificar si ha pasado el tiempo de bloqueo
                    if(failed_attempts >= 3){
                        if(lastAttemptTimestamp != null) {
                            LocalDateTime lastAttempt = lastAttemptTimestamp.toLocalDateTime();
                            long minutesPassed = ChronoUnit.MINUTES.between(lastAttempt, now);
                            
                            if(minutesPassed < 5) {
                                // Aún no ha pasado el tiempo de bloqueo
                                long minutesRemaining = 5 - minutesPassed;
                                System.out.println("Cuenta bloqueada. Intente nuevamente en " + minutesRemaining + " minutos.");
                                continue; // Saltar a la siguiente iteración del bucle while
                            } else {
                                // Ya pasó el tiempo de bloqueo, resetear los intentos
                                System.out.println("El bloqueo ha expirado. Puede intentar de nuevo.");
                                failed_attempts = 0;
                                
                                // Actualizar en la base de datos el reset de intentos
                                sql = "UPDATE users SET failed_attempts = 0 WHERE username = ?";
                                pstmt = connection.prepareStatement(sql);
                                pstmt.setString(1, user);
                                pstmt.executeUpdate();
                                System.out.println("Intentos fallidos reseteados por expiración de bloqueo");
                            }
                        }
                    }
                    
                    // Verificar la contrase?a si no está bloqueado
                    String rowEncriptedPassword = rs.getString("password");
                    succesful = BCrypt.checkpw(password, rowEncriptedPassword);
                    
                    // Actualizar numero de intentos fallidos
                    if(!succesful){
                        // Incrementar el numero de intentos fallidos
                        failed_attempts += 1;
                        System.out.println("Incrementando intentos fallidos a: " + failed_attempts);
                        
                        // Registrar la hora del intento fallido
                        sql = "UPDATE users SET failed_attempts = ?, last_attempt = ? WHERE username = ?";
                        pstmt = connection.prepareStatement(sql);
                        pstmt.setInt(1, failed_attempts);
                        pstmt.setTimestamp(2, Timestamp.valueOf(now));
                        pstmt.setString(3, user);
                        pstmt.executeUpdate();
                        
                        // Informar si se ha bloqueado la cuenta
                        if(failed_attempts >= 3){
                            System.out.println("Ha superado el número máximo de intentos. Su cuenta ha sido bloqueada por 5 minutos.");
                        }
                    } else {
                        // Si el login es exitoso, resetear los intentos fallidos
                        sql = "UPDATE users SET failed_attempts = 0 WHERE username = ?";
                        pstmt = connection.prepareStatement(sql);
                        pstmt.setString(1, user);
                        pstmt.executeUpdate();
                        System.out.println("Intentos fallidos reseteados");
                        
                        System.out.println("Inicio de sesión correcto");
                        terminar = true;
                    }
                } else {
                    // Usuario no encontrado
                    System.out.println("Usuario o contrase?a incorrectos");
                }
            }
        } catch (ClassNotFoundException ex) {
            System.err.println("Error al cargar el Driver del SGBD: " + ex.getMessage());
        } catch (SQLException ex) {
            System.err.println("Error al conectar a la base de datos: " + ex.getMessage() + "\n" + ex.getSQLState());
            ex.printStackTrace();
        } finally {
            // Cerrar recursos
            try {
                if (pstmt != null) pstmt.close();
                if (connection != null) connection.close();
                sc.close();
            } catch (SQLException ex) {
                System.err.println("Error al cerrar recursos: " + ex.getMessage());
            }
        }
    }
}
    /*
    public static void main(String[] args) {
        // Declaracion de variables
        Scanner sc = new Scanner(System.in);
        String user = "";
        String password = "";
        Connection connection = null;
        Statement stmt = null;
        String sql = "";
        ResultSet rs;
        boolean succesful;
        
        //Solicitar datos al usuario
        
        System.out.print("Insertar su nombre de usuario: ");
        user = sc.nextLine();
        
        System.out.print("Insertar su constrase?a: ");
        password = sc.nextLine();
        
        try {
        //Conectar con la base de datos.
        Class.forName("com.mysql.cj.jdbc.Driver");
        System.out.println("Driver cargado correctamente.");
        
        connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        System.out.println("Conexión Correcta");
        
        
        // Realizar la consulta SQL.
        stmt = connection.createStatement();
        sql = "select * from users where username = '" + user + "'" + " and password = '" + password + "'";
        System.out.println("Consulta SQL:\n" + sql);
        rs = stmt.executeQuery(sql);
        succesful = false;
        while(rs.next()){
            succesful = true;
            int rowID = rs.getInt("id");
            String rowUsername = rs.getString("username");
            String rowPassword = rs.getString("password");
            System.out.println(rowID + "\t" + rowUsername +"\t" + rowPassword);
        }
        if(succesful){
            System.out.println("Inicio de sesion correcto");
        }else{
            System.out.println("Usuario o contrase?a incorrecto");
        }
                
                
                
        }  catch (ClassNotFoundException ex) {
            System.err.println("Error al conectar a la base de datos: " + ex.getMessage());
        } catch (SQLException ex) {
            System.err.println("Error al cargar el Driver del SGBD");
        }    
            

        
        
        }
        
    */

    
