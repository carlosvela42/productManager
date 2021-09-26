package net.codejava;

import java.net.URLDecoder;
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
			//if("0".equals(product.getPrice())) {		    	
		    if(true) {	
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
		String paymentLink = requestMap.get("paymentLink");
		String link = "";
		if(paymentLink != null) {
			byte[] decoded = org.apache.commons.codec.binary.Base64.decodeBase64(paymentLink);
			link = new String(decoded, "UTF-8");
			service.updatePaymentLink(link, requestMap.get("vaNumber"), requestMap.get("invoiceNo"));
		}
		ModelAndView mav = new ModelAndView("success");
		Product product = new Product();
		product.setPaymentLink(link);
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
}
