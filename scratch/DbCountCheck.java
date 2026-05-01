import java.sql.*;
public class DbCountCheck {
    public static void main(String[] args) {
        String url = "jdbc:sqlserver://gym-db.c9o2u8u8u8u8.us-east-1.rds.amazonaws.com:1433;databaseName=GymDB;user=admin;password=GymPass123;encrypt=true;trustServerCertificate=true";
        try (Connection c = DriverManager.getConnection(url);
             Statement s = c.createStatement()) {
            
            ResultSet rs1 = s.executeQuery("SELECT COUNT(*) FROM sinif_programlari");
            if (rs1.next()) System.out.println("Program Count: " + rs1.getInt(1));
            
            ResultSet rs2 = s.executeQuery("SELECT COUNT(*) FROM sinif_rezervasyonlari");
            if (rs2.next()) System.out.println("Rezervasyon Count: " + rs2.getInt(1));

            ResultSet rs3 = s.executeQuery("SELECT program_id, gun, baslangic_saati FROM sinif_programlari");
            System.out.println("--- Programs ---");
            while(rs3.next()) {
                System.out.println("ID: " + rs3.getInt(1) + ", Day: " + rs3.getString(2) + ", Time: " + rs3.getString(3));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
