package packagesystem;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Vector;

/**
 *
 * @author frsy
 */
public class DBConn {
    
    public final static int PACKAGE_STATE_IN = 0;
    public final static int PACKAGE_STATE_OUT = 1;
    public final static int SELECT_MODE_DESC = 0;
    public final static int SELECT_MODE_ASC = 1;
    public final static int SELECT_MODE_BEFORE = 0;
    public final static int SELECT_MODE_AFTER = 1;
    public final static int SELECT_MODE_INDATE = 0;
    public final static int SELECT_MODE_OUTDATE = 1;
    
    
    
    public static Connection getConnection(){
        try{
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String conUrl="jdbc:sqlserver://localhost:1433;databaseName=PackageSystem";
            Connection conn=DriverManager.getConnection(conUrl,"sa","20000126");
            System.out.println("连接成功");
            return conn;
        }catch(ClassNotFoundException | SQLException e){
            e.printStackTrace();
            return null;
        }
    }
    
    public static Vector queryPackageById(String pkgId){
        String sltSql = "select * from Package where pkgId like ?";
        Vector vPkg = new Vector();
        try{
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sltSql);
            ps.setString(1,"%"+pkgId+"%");
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                Vector line = new Vector();
                line.add(rs.getString("pkgId"));
                line.add(rs.getString("SOutCode"));
                line.add(rs.getString("position"));
                line.add(rs.getString("sinDatetime"));
                line.add(rs.getString("soutDatetime")!=null?rs.getString("soutDatetime"):"未取件");
                line.add(rs.getString("userNum"));
                vPkg.add(line);
            }
            rs.close();
            ps.close();
            conn.close();
            return vPkg;
        }catch(SQLException e){
            e.printStackTrace();
            return null;
        }
    }
    
    public static Vector queryPackageByNum(String Num){
        String sltSql = "select * from Package where userNum like ?";
        Vector vPkg = new Vector();
        try{
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sltSql);
            ps.setString(1,"%"+Num+"%");
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                Vector line = new Vector();
                line.add(rs.getString("pkgId"));
                line.add(rs.getString("SOutCode"));
                line.add(rs.getString("position"));
                line.add(rs.getString("sinDatetime"));
                line.add(rs.getString("soutDatetime")!=null?rs.getString("soutDatetime"):"未取件");
                line.add(rs.getString("userNum"));
                vPkg.add(line);
            }
            rs.close();
            ps.close();
            conn.close();
            return vPkg;           
        }catch(SQLException e){
            return null;
        }
    }
    
    
    
    
    /**
     * 
     * @param pkgCode 快递取件码
     * @return 取件成功返回该快递的位置，否则返回空
     */
    public static String stkOutPackage(String pkgCode){
        String selectSql="select * from Package where SOutCode = ? and soutDatetime is null";
        String updateSql="update Package set position = ? , soutDatetime = ? where SOutCode = ?";
        try{
        Connection conn=getConnection();
        PreparedStatement sltps=conn.prepareStatement(selectSql);
        sltps.setString(1, pkgCode);
        ResultSet rs=sltps.executeQuery();
        if(rs.next()){
            System.out.println(rs.getString("SOutCode"));
            String pos = rs.getString("position");
            String newpos = pos.replace('(', '[').replace(')',']');
            PreparedStatement udps=conn.prepareStatement(updateSql);
            udps.setString(1, newpos);
            LocalDateTime dt=LocalDateTime.now();
            DateTimeFormatter dtf=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            udps.setString(2, dt.format(dtf));
            udps.setString(3, pkgCode);
            udps.execute();
            rs.close();
            sltps.close();
            udps.close();
            conn.close();
            return pos;
        }else{
            return null;
        }      
        }catch(SQLException e){
           return null;
        }
        
    }
    
    public static ExpPackage stkInPackage(String pkgId,String userNum){
        String pkgCode = Utils.formPackageCode();
        String pos = Utils.fromPackagePosition();
        String insertSql = "insert into Package values (?,?,?,?,NULL,?)";
        LocalDateTime dt = LocalDateTime.now();
        DateTimeFormatter dtf=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs;
            while((rs = stmt.executeQuery(String.format("select * from Package where SOutCode = '%s' and soutDatetime is null",pkgCode))).next()){
                pkgCode = Utils.formPackageCode();
            }
            while((rs = stmt.executeQuery(String.format("select * from Package where position = '%s' and soutDatetime is null",pos))).next()){
                pos = Utils.fromPackagePosition();
            }
            PreparedStatement ps = conn.prepareStatement(insertSql);
            ps.setString(1,pkgId);
            ps.setString(2,pkgCode);
            ps.setString(3,pos);
            ps.setString(4,dt.format(dtf));
            ps.setString(5,userNum);
            ps.execute();
            int count = ps.getUpdateCount();
            rs.close();
            ps.close();
            conn.close();
            if(count!=0){
                ExpPackage pkg = new ExpPackage(pkgId, pkgCode, pos, dt.format(dtf), "未出柜", userNum);
                return pkg;
            }else{
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 
     * @param user 用户名
     * @param pwd 密码
     * @return 登录成功返回真，否则返回假
     */
    public static boolean sysLogin(String user,String pwd){
        String md5Pwd;
        String sql = "select password from Admin where userName = ?";
        try{           
            md5Pwd=Utils.md5Encode(pwd);
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, user);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                String curPwd = rs.getString("password");
                System.out.println(curPwd);
                if(curPwd.equals(md5Pwd)){
                    return true;
                }else{
                    return false;
                }
            }else{
                return false;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
    
    public static boolean register(String user,String pwd){ 
        Connection conn=getConnection(); //连接数据库
        String sql = "insert into Admin values(?,?)";
        String md5Pwd;
        try{           
             PreparedStatement ps = conn.prepareStatement(sql); 
            md5Pwd=Utils.md5Encode(pwd);
            ps.setString(1, user);
            ps.setString(2,Utils.md5Encode(pwd));
            ps.execute();
            if(ps.getUpdateCount()>0){
                return true;
            }else{
                return false;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
     }
    
    /**
     * 
     * @return 
     */
    public static Vector queryPackage(){
        String sltSql = "select * from Package";       
        Vector vPkg = new Vector();
        try{
            Connection conn=getConnection();
            PreparedStatement ps = conn.prepareStatement(sltSql);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                Vector line = new Vector();
                line.add(rs.getString("pkgId"));
                line.add(rs.getString("SOutCode"));
                line.add(rs.getString("position"));
                line.add(rs.getString("sinDatetime"));
                line.add(rs.getString("soutDatetime")!=null?rs.getString("soutDatetime"):"未取件");
                line.add(rs.getString("userNum"));
                vPkg.add(line);
            }
            rs.close();
            ps.close();
            conn.close();
            return vPkg;
            
        }catch(SQLException e){
            e.printStackTrace();
            return null;
        }
    }
    
    public static Vector queryPackage(String colName,int selectMode){
        String mode;
        if(selectMode==SELECT_MODE_ASC){
            mode = "asc";
        }else{
            mode = "desc";
        }
        String sltSql = String.format("select * from Package order by %s %s",colName,mode);
        Vector vPkg = new Vector();
        try{
            Connection conn=getConnection();
            PreparedStatement ps = conn.prepareStatement(sltSql);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                Vector line = new Vector();
                line.add(rs.getString("pkgId"));
                line.add(rs.getString("SOutCode"));
                line.add(rs.getString("position"));
                line.add(rs.getString("sinDatetime"));
                line.add(rs.getString("soutDatetime")!=null?rs.getString("soutDatetime"):"未取件");
                line.add(rs.getString("userNum"));
                vPkg.add(line);
            }
            rs.close();
            ps.close();
            conn.close();
            return vPkg;           
        }catch(SQLException e){
            e.printStackTrace();
            return null;
        }
    }
    
    public static Vector queryPackageByDate(String date,int datemode,int sltmode){
        String[] dMode = {"sinDatetime","soutDatetime"};
        String[] sMode = {"<=",">="};
        String sltSql = String.format("select * from Package where %s %s ?",dMode[datemode],sMode[sltmode]);
        Vector vPkg = new Vector();
        try{
            Connection conn=getConnection();
            PreparedStatement ps = conn.prepareStatement(sltSql);
            ps.setString(1, date);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                Vector line = new Vector();
                line.add(rs.getString("pkgId"));
                line.add(rs.getString("SOutCode"));
                line.add(rs.getString("position"));
                line.add(rs.getString("sinDatetime"));
                line.add(rs.getString("soutDatetime")!=null?rs.getString("soutDatetime"):"未取件");
                line.add(rs.getString("userNum"));
                vPkg.add(line);
            }
            rs.close();
            ps.close();
            conn.close();
            return vPkg;           
        }catch(SQLException e){
            e.printStackTrace();
            return null;
        }
    }
    
    public static boolean deletePackage(String pkgId){
        String delSql = "delete from Package where pkgId = ?";
        try{
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(delSql);
            ps.setString(1, pkgId);
            ps.execute();
            if(ps.getUpdateCount()>0){
                return true;
            }else{
                return false;
            }
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean updateUserNum(String pkgId,String nNum){
        String utSql = "update Package set userNum = ? where pkgId = ?";
        try{
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(utSql);
            ps.setString(1, nNum);
            ps.setString(2, pkgId);
            ps.execute();
            if(ps.getUpdateCount()>0){
                return true;
            }else{
                return false;
            }
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean renewPackageCode(String pkgId){
        String nCode = Utils.formPackageCode();
        String utSql = "update Package set SOutCode = ? where pkgId = ?";
        try{
             Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(utSql);
            ps.setString(1, nCode);
            ps.setString(2, pkgId);
            ps.execute();
            if(ps.getUpdateCount()>0){
                return true;
            }else{
                return false;
            }
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }
    
    public static Vector queryPackageByState(int pkgState){
        String sltSql;
        if(pkgState==PACKAGE_STATE_IN){
            System.out.println("in");
            sltSql = "select * from Package where soutDatetime is NULL";
        }else{
            sltSql = "select * from Package where soutDatetime is not null";
        }
        Vector vPkg = new Vector();
        try{
            Connection conn=getConnection();
            PreparedStatement ps = conn.prepareStatement(sltSql);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                Vector line = new Vector();
                line.add(rs.getString("pkgId"));
                line.add(rs.getString("SOutCode"));
                line.add(rs.getString("position"));
                line.add(rs.getString("sinDatetime"));
                line.add(rs.getString("soutDatetime")!=null?rs.getString("soutDatetime"):"未取件");
                line.add(rs.getString("userNum"));
                vPkg.add(line);
            }
            rs.close();
            ps.close();
            conn.close();
            System.out.println(vPkg);
            return vPkg;           
        }catch(SQLException e){
            e.printStackTrace();
            return null;
        }
    }
    
    public static int queryPackageCount(){
        String sltSql = "select * from Package where soutDatetime is NULL";
        int count = 0;
        try{
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sltSql);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                count++;
            }
            return count;
        }catch(SQLException e){
            e.printStackTrace();
            return -1;
        }
    }
    
}
