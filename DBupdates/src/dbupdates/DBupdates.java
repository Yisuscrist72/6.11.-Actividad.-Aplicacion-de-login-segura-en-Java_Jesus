package dbupdates;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;


public class DBupdates {

    static final String DB_URL = "jdbc:mysql://localhost:3306/seguridad_db";
    static final String DB_USER = "root";
    static final String DB_PASSWORD = "root";
    
    
    public static void main(String[] args) {
               
        //Declaración de Variables
        Connection connection = null;
        PreparedStatement pstmt = null;
        String sql = "";
        String encriptedPassword;
        try {
            // Cargar el driver de MySQL.
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver de la base de datos cargado.");
     
     
            // Conectarme a la base de datos
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Conexión Correcta");
     
           
            // Modificar la tabla users.
            //A?adir la columna failed_attempts
            sql = "alter table users add column failed_attempts int default 0";
            pstmt = connection.prepareStatement(sql);
            pstmt.executeUpdate();
            System.out.println("Columna failed_attempts a?adida.");
            
            //A?adir la columna last_attempt
            sql = "alter table users add column last_attempt timestamp";
            pstmt = connection.prepareStatement(sql);
            pstmt.executeUpdate();
            System.out.println("Columna last_attempt a?adida.");
      
     
            // Encriptar las contrase?as.
            // Contrase?a de admin
            encriptedPassword = BCrypt.hashpw("admin123", BCrypt.gensalt(12));
            System.out.println("La contrase?a 'admin123' encriptada es: " + encriptedPassword);
            
            sql = "update users set password = ? where username = ?";
            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, encriptedPassword);
            pstmt.setString(2, "admin");
            pstmt.executeUpdate();
            System.out.println("Contrase?a del usuario 'admin' actualizado.");
            
            // Contrase?a de usuario
            encriptedPassword = BCrypt.hashpw("pass123", BCrypt.gensalt(12));
            System.out.println("La contrase?a 'pass123' encriptada es: " + encriptedPassword);
            
            sql = "update users set password = ? where username = ?";
            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, encriptedPassword);
            pstmt.setString(2, "usuario");
            pstmt.executeUpdate();
            System.out.println("Contrase?a del usuario 'usuario' actualizado.");
            
             
             
             
             
             
     
            } catch (ClassNotFoundException ex) {
            System.err.println("Error al cargar el Driver de la base de datos." 
                    + ex.getMessage());
            } catch (SQLException ex) {
            System.err.println("Error al conectar a la base de datos o al ejecutar el SQL: " 
                    + ex.getMessage());
            } finally {
            try {
                if(pstmt != null){pstmt.close();}
                if(connection != null){connection.close();} 
            } catch (SQLException ex){
                System.err.println("Error al cerrar la conexión." + ex.getMessage()) ;
                System.err.println("Error al cerrar el PreparedStatement" + ex.getMessage());
                }
            }
    
    
    
    
    }
}
