import java.sql.*;

public class TestQuery {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:sqlserver://localhost;databaseName=fitzone;integratedSecurity=true;encrypt=true;trustServerCertificate=true;";
        Connection conn = DriverManager.getConnection(url);

        String baseQ = "SELECT a.abonelik_id,k.ad,k.soyad,p.plan_adi," +
                    "a.baslangic_tarihi,a.bitis_tarihi,a.otomatik_yenile,a.durum, u.kullanici_id " +
                    "FROM uye_abonelikleri a " +
                    "JOIN uyeler u ON a.uye_id=u.uye_id " +
                    "JOIN kullanicilar k ON u.kullanici_id=k.kullanici_id " +
                    "JOIN uye_planlari p ON a.plan_id=p.plan_id ";
        
        PreparedStatement stmt = conn.prepareStatement(baseQ);
        ResultSet rs = stmt.executeQuery();
        StringBuilder json=new StringBuilder("[");
        boolean first=true;
        while(rs.next()) {
            if (!first) json.append(",");
            json.append(String.format(
                "{\"kullanici\":%d,\"id\":%d,\"uye\":\"%s %s\",\"plan\":\"%s\"," +
                "\"baslangic\":\"%s\",\"bitis\":\"%s\",\"otomatik\":%s,\"durum\":\"%s\"}",
                rs.getInt("kullanici_id"),
                rs.getInt("abonelik_id"),rs.getString("ad").trim(),rs.getString("soyad").trim(),
                rs.getString("plan_adi"),rs.getString("baslangic_tarihi"),rs.getString("bitis_tarihi"),
                rs.getBoolean("otomatik_yenile")?"true":"false",rs.getString("durum")));
            first=false;
        }
        json.append("]");
        System.out.println(json.toString());
    }
}
