package net.codejava;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService {

	@Autowired
	private ProductRepository repo;
	
	@Autowired
	private JdbcTemplate JdbcTemplate;
	
	public List<Product> listAll() {
		List<Product> map = new ArrayList<Product>();
		String sql = "select * from PRODUCT";
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = JdbcTemplate.getDataSource().getConnection();
			pstm = con.prepareStatement(sql);
			rs = pstm.executeQuery();
			while (rs.next()) {
				Product product = new Product();
				product.setCode(rs.getString("CODE"));
				product.setEmail(rs.getString("EMAIL"));
				product.setId(rs.getLong("ID"));
				product.setPackageId(rs.getString("PACKAGE_ID"));
				product.setPhone(rs.getString("PHONE"));
				map.add(product);
			}
		} catch (Exception e) {

		} finally {
			closeResource(con, pstm, rs);
		}
		return map;
	}
	
	public List<Packages> listAllPackages() {
		List<Packages> map = new ArrayList<Packages>();
		String sql = "select * from Packages order by CATEGORY";
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = JdbcTemplate.getDataSource().getConnection();
			pstm = con.prepareStatement(sql);
			rs = pstm.executeQuery();
			while (rs.next()) {
				Packages packages = new Packages();
				packages.setCode(rs.getString("CODE"));
				packages.setName(rs.getString("NAME"));
				packages.setPrice(rs.getString("PRICE"));
				packages.setCategory(rs.getString("CATEGORY"));
				packages.setTime(rs.getString("TIME"));
				packages.setTemplate(rs.getString("TEMPLATE"));
				packages.setId(rs.getLong("ID"));
				packages.setType(rs.getString("TYPE"));
				packages.setSubject(rs.getString("SUBJECT"));
				Locale locale = new Locale("vi","VN");
				packages.setPriceLocale(String.format(locale, "%,d", Integer.parseInt(rs.getString("PRICE"))) );
				map.add(packages);
			}
		} catch (Exception e) {

		} finally {
			closeResource(con, pstm, rs);
		}
		return map;
	}
	
	public List<Code> listAllCode() {
		List<Code> map = new ArrayList<Code>();
		String sql = "select * from CODE where ISACTIVE = 'Y'";
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = JdbcTemplate.getDataSource().getConnection();
			pstm = con.prepareStatement(sql);
			rs = pstm.executeQuery();
			while (rs.next()) {
				Code code = new Code();				
				code.setName(rs.getString("NAME"));
				code.setPrice(rs.getString("PRICE"));
				code.setId(rs.getLong("ID"));

				map.add(code);
			}
		} catch (Exception e) {

		} finally {
			closeResource(con, pstm, rs);
		}
		return map;
	}
	
	public void updatePaymentLink(String paymentLink, String vaNumber, String invoiceNo) {
		String sql = "update PAYMENT set PAYMENT_LINK = ?, VA_NUMBER = ? WHERE INVOICE_NO = ?";
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		
		try {
			con = JdbcTemplate.getDataSource().getConnection();
			pstm = con.prepareStatement(sql);
			pstm.setString(1, paymentLink);
			pstm.setString(2, vaNumber);
			pstm.setString(3, invoiceNo);
			int updateCount = pstm.executeUpdate();	
			System.out.print(updateCount);
		} catch (Exception e) {
			System.out.print(e);
		} finally {
			closeResource(con, pstm, rs);
		}
	}
	
	private void closeResource(Connection con, PreparedStatement pstm, ResultSet rs) {
		try {
			if (con != null) {
				con.close();
			}
			if (pstm != null) {
				pstm.close();
			}
			if (rs != null) {
				rs.close();
			}
		} catch (Exception e) {

		}
	}
	
	public void save(Product product) {
		String sql = "INSERT INTO PRODUCT (EMAIL, PHONE, PACKAGE_ID, CODE, INVOICE_NO, PAYMENT_LINK, VA_NUMBER) SELECT ?, ?, ?, ?, ?, ?, ? FROM dual";
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		
		try {
			con = JdbcTemplate.getDataSource().getConnection();
			pstm = con.prepareStatement(sql);
			pstm.setString(1, product.getEmail());
			pstm.setString(2, product.getPhone());
			pstm.setString(3, product.getPackageId());
			pstm.setString(4, product.getCode());
			pstm.setString(5, product.getInvoiceNo());
			pstm.setString(6, product.getPaymentLink());
			pstm.setString(7, product.getVaNumber());
			
			int updateCount = pstm.executeUpdate();	
			System.out.print(updateCount);
		} catch (Exception e) {
			System.out.print(e);
		} finally {
			closeResource(con, pstm, rs);
		}
	}
	
	public void updateUseCountCode(String code) {
		String sql = "update CODE set USECOUNT = USECOUNT + 1 WHERE NAME = ?";
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		
		try {
			con = JdbcTemplate.getDataSource().getConnection();
			pstm = con.prepareStatement(sql);
			pstm.setString(1, code);

			int updateCount = pstm.executeUpdate();	
			System.out.print(updateCount);
		} catch (Exception e) {
			System.out.print(e);
		} finally {
			closeResource(con, pstm, rs);
		}
	}
	
	public void insertUser(Product product) {
		String sql = "INSERT INTO USERS (EMAIL, PHONE, PASSWORD) SELECT ?, ?, '123456' FROM dual where not exists(select * from USERS where (email = ?))";
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		
		try {
			con = JdbcTemplate.getDataSource().getConnection();
			pstm = con.prepareStatement(sql);
			pstm.setString(1, product.getEmail());
			pstm.setString(2, product.getPhone());
			pstm.setString(3, product.getEmail());
			int updateCount = pstm.executeUpdate();	
			System.out.print(updateCount);
		} catch (Exception e) {
			System.out.print(e);
		} finally {
			closeResource(con, pstm, rs);
		}
	}
	
	public void insertMap(Product product) {
		String sql = "INSERT INTO MAP (USER_ID,PACKAGE_ID,IS_CANCEL,PAY_DATE,NEXT_PAY_DATE,TOTAL_AMOUNT,CODE,MACHINE_ID) SELECT (SELECT ID FROM USERS WHERE EMAIL = ? LIMIT 1), ?, 'N', ?,? ,?,?,? FROM dual";
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		
		try {
			con = JdbcTemplate.getDataSource().getConnection();
			pstm = con.prepareStatement(sql);
			pstm.setString(1, product.getEmail());
			pstm.setString(2, product.getPackageId());
			Date date = new Date();
			Calendar c = Calendar.getInstance(); 
			c.setTime(date); 
			try{
	            int number = Integer.parseInt(product.getTime());
	            System.out.println(number); 
	            c.add(Calendar.DATE, number);
	        }
	        catch (NumberFormatException ex){
	            ex.printStackTrace();
	        }
			
			Date dt = c.getTime();
			pstm.setDate  (3, new java.sql.Date(date.getTime()));
			pstm.setDate  (4, new java.sql.Date(dt.getTime()));
			pstm.setString(5, product.getPrice());
			pstm.setString(6, product.getCode());
			pstm.setString(7, product.getMachineId());
			int updateCount = pstm.executeUpdate();	
			System.out.print(updateCount);
		} catch (Exception e) {
			System.out.print(e);
		} finally {
			closeResource(con, pstm, rs);
		}
	}
	
	public void insertPayment(Product product) {
		String sql = "INSERT INTO PAYMENT (MAP_ID,PRICE,EPAY_DATE,INVOICE_NO,STATUS) SELECT (SELECT ID FROM MAP WHERE USER_ID = (SELECT ID FROM USERS WHERE EMAIL = ? LIMIT 1) AND PACKAGE_ID = ? AND IS_CANCEL = 'N' LIMIT 1), ?, ?, ?,? FROM dual";
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		
		try {
			con = JdbcTemplate.getDataSource().getConnection();
			pstm = con.prepareStatement(sql);
			pstm.setString(1, product.getEmail());
			pstm.setString(2, product.getPackageId());
			
			pstm.setString(3, product.getPrice());
			Date date = new Date();
			pstm.setDate  (4, new java.sql.Date(date.getTime()));			
			
			pstm.setString(5, product.getInvoiceNo());
			pstm.setString(6, product.getStatus());
			int updateCount = pstm.executeUpdate();	
			System.out.print(updateCount);
		} catch (Exception e) {
			System.out.print(e);
		} finally {
			closeResource(con, pstm, rs);
		}
	}
	
	public boolean checkExist(Product product) {
		String sql = "select * from MAP where USER_ID = (SELECT ID FROM USERS WHERE EMAIL = ? LIMIT 1) AND PACKAGE_ID = ? AND IS_CANCEL = 'N'";
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = JdbcTemplate.getDataSource().getConnection();
			pstm = con.prepareStatement(sql);
			pstm.setString(1, product.getEmail());
			pstm.setString(2, product.getPackageId());
			rs = pstm.executeQuery();
			while (rs.next()) {
				return true;
			}
		} catch (Exception e) {
			return false;
		} finally {
			closeResource(con, pstm, rs);
		}
		return false;
	}
	
	public Product get(long id) {
		return repo.findById(id).get();
	}
	
	public void delete(long id) {
		repo.deleteById(id);
	}
	
	public void sendEmail(String content, String subject,String listRec) {
		Properties commonConfig = new Properties();
		
		commonConfig.setProperty("email.host", "smtp.gmail.com");		
		commonConfig.setProperty("email.port", "465");
		commonConfig.setProperty("email.auth", "true");
		commonConfig.setProperty("email.socketFactory.port", "465");
		commonConfig.setProperty("email.timeout", "120000");
		commonConfig.setProperty("email.ssl.enable", "true");
		commonConfig.setProperty("email.starttls.enable", "true");
		commonConfig.setProperty("email.from", "alert@megapay.vn");
		commonConfig.setProperty("email.recipient", "alert-megapay@vnptepay.com.vn");
		commonConfig.setProperty("email.username", "support@orderflow.vn");
		commonConfig.setProperty("email.password", "qajpmsmnvgxflacz");
		commonConfig.setProperty("email.subject", "[core - local] Megapay alert");
    	Thread sendMailThread = new Thread(new SendEmailThread(content,subject,listRec, commonConfig));
		sendMailThread.start();
	}
}
