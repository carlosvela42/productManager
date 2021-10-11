package net.codejava;

import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AppController {

	@Autowired
	private ProductService service; 
	
	@RequestMapping("/")
	public String viewHomePage(Model model) {
		List<Packages> listPackages = service.listAllPackages();
		
		LinkedHashMap<String, List<Packages>> hashMap = new LinkedHashMap<String, List<Packages>>();
		for (Packages element : listPackages) {
			if (!hashMap.containsKey(element.getCategory())) {
			    List<Packages> list = new ArrayList<Packages>();
			    list.add(element);

			    hashMap.put(element.getCategory(), list);
			} else {
			    hashMap.get(element.getCategory()).add(element);
			}
		}
		List<Code> listCode = service.listAllCode();
		model.addAttribute("listPackages", hashMap);
		model.addAttribute("listCode", listCode);
		Product product = new Product();
		model.addAttribute("product", product);
		
		return "index";
	}
	
	@RequestMapping("/new")
	public String showNewProductPage(Model model) {
		Product product = new Product();
		model.addAttribute("product", product);
		
		return "new_product";
	}
	
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public ModelAndView saveProduct(@ModelAttribute("product") Product product, Model model) {
		Date date = new Date();
		DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss"); 
		String fullDate = df.format(date);
		product.setInvoiceNo("EPAY000001" + fullDate);
		
		service.insertUser(product);
		product.setStatus("0");
		//if(service.checkExist(product)) {
		if(false) {
			product.setErrorMsg("1");
			List<Packages> listPackages = service.listAllPackages();
			
			LinkedHashMap<String, List<Packages>> hashMap = new LinkedHashMap<String, List<Packages>>();
			for (Packages element : listPackages) {
				if (!hashMap.containsKey(element.getCategory())) {
				    List<Packages> list = new ArrayList<Packages>();
				    list.add(element);

				    hashMap.put(element.getCategory(), list);
				} else {
				    hashMap.get(element.getCategory()).add(element);
				}
			}
			List<Code> listCode = service.listAllCode();
			model.addAttribute("listPackages", hashMap);
			model.addAttribute("listCode", listCode);
			
			model.addAttribute("product", product);
			ModelAndView mav = new ModelAndView("index");
			return mav;
		} else {
			service.insertMap(product);
			service.updateUseCountCode(product.getCode());
			if("0".equals(product.getPrice())) {		    	
		    //if(true) {	
				product.setErrorMsg("0");				
				service.insertPayment(product);
				List<Packages> listPackages = service.listAllPackages();
				
				LinkedHashMap<String, List<Packages>> hashMap = new LinkedHashMap<String, List<Packages>>();
				for (Packages element : listPackages) {
					if (!hashMap.containsKey(element.getCategory())) {
					    List<Packages> list = new ArrayList<Packages>();
					    list.add(element);

					    hashMap.put(element.getCategory(), list);
					} else {
					    hashMap.get(element.getCategory()).add(element);
					}
					if(product.getPackageId().equals(element.getId() + "")) {
						service.sendEmail(element.getTemplate(), element.getSubject(), product.getEmail());
					}
				}
				List<Code> listCode = service.listAllCode();
				model.addAttribute("listPackages", hashMap);
				model.addAttribute("listCode", listCode);
				
				model.addAttribute("product", product);
				ModelAndView mav = new ModelAndView("index");
				return mav;
			} else {
				service.insertPayment(product);
				ModelAndView mav = new ModelAndView("payment");
				mav.addObject("product", product);
				return mav;
			}			
		}		
	}
	
	@RequestMapping("/edit/{id}")
	public ModelAndView showEditProductPage(@PathVariable(name = "id") int id) {
		ModelAndView mav = new ModelAndView("edit_product");
		Product product = service.get(id);
		mav.addObject("product", product);
		
		return mav;
	}
	
	@RequestMapping("/delete/{id}")
	public String deleteProduct(@PathVariable(name = "id") int id) {
		service.delete(id);
		return "redirect:/";		
	}
	
	@RequestMapping(value = "/callback")
	public ModelAndView callback(HttpServletRequest request) throws Exception {
		String requestDecode = URLDecoder.decode(request.getQueryString(), "UTF-8");
		Map<String, String> requestMap = getQueryMap(requestDecode);
		String resultMsg = requestMap.get("resultMsg");
		String merchantToken = requestMap.get("merchantToken");
		
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(requestMap.get("resultCd"));
		stringBuilder.append(requestMap.get("timeStamp"));
		stringBuilder.append(requestMap.get("merTrxId"));
		stringBuilder.append(requestMap.get("trxId"));
		stringBuilder.append(requestMap.get("merId"));
		stringBuilder.append(requestMap.get("amount"));
		
		if("ORDERFLOW2".equals(requestMap.get("merId"))){
			stringBuilder.append("B9NmWS0lxV3AmgcbBmRWJ2maT3ew8S3en2MTxz4/TSRM0mDlHwUvjckYkGLI+yVgJfNx9/PxcARhFJQO+Wsd0w==");
		}else{
			stringBuilder.append(requestMap.get("payToken"));
			stringBuilder.append("qxVTXsZIpyWu5FEm7Asmk6i+Pr8aeiMCxpb1KOyE1a94P1MBnFtgkJgxYLaxDoOgwdcVW1TG+cDLT5koJbjYqA==");
		}

		String result = encrypt(stringBuilder.toString());
				
		ModelAndView mav = new ModelAndView("success");
		Product product = new Product();
		product.setPaymentLink("");
		product.setErrorMsg(resultMsg);
		if (result.equals(merchantToken)) {
			service.updatePayment(requestMap.get("payToken"), requestMap.get("merTrxId"), requestMap.get("trxId"), requestMap.get("resultCd"), requestMap.get("invoiceNo"));
		} else {
			product.setErrorMsg("Merchant token không khớp");
		}
		
		mav.addObject("product", product);
		return mav;		
	}
	
	@RequestMapping(value = "/ipn")
	public ModelAndView ipn(HttpServletRequest request) throws Exception {
		//tuong lai code
		String requestDecode = URLDecoder.decode(request.getQueryString(), "UTF-8");
		Map<String, String> requestMap = getQueryMap(requestDecode);
		String resultMsg = requestMap.get("resultMsg");
		String link = "";
		
		ModelAndView mav = new ModelAndView("success");
		Product product = new Product();
		product.setPaymentLink(link);
		product.setErrorMsg(resultMsg);
		mav.addObject("product", product);
		return mav;		
	}
	
	public static Map<String, String> getQueryMap(String query) {  
	    String[] params = query.split("&");  
	    Map<String, String> map = new HashMap<String, String>();

	    for (String param : params) {  
	        String name = param.split("=")[0];
	        String value = "";
	        if(param.split("=").length > 1) {
	        	value = param.split("=")[1]; 
	        }
	        
	        map.put(name, value);  
	    }  
	    return map;  
	}
	
	public static String encrypt(String str){
		String SHA = ""; 
		try{
			MessageDigest sh = MessageDigest.getInstance("SHA-256"); 
			sh.update(str.getBytes()); 
			byte byteData[] = sh.digest();
			StringBuffer sb = new StringBuffer(); 
			for(int i = 0 ; i < byteData.length ; i++){
				sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
			}
			SHA = sb.toString();
			
		}catch(NoSuchAlgorithmException e){
			e.printStackTrace(); 
			SHA = null; 
		}
		return SHA;
	}
}
