package com.example.CargoAssign.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;

import com.example.CargoAssign.Model.User;
import com.example.CargoAssign.repo.DriverVerificationRepo;
import com.example.CargoAssign.repo.ShipperVerificationRepo;

import jakarta.servlet.http.HttpSession;

@Controller
public class PageController {

	//home page
    @GetMapping("/")
    public String home() {
        return "HomePage";
    }
    //login page
    @GetMapping("/login")
    public String loginPage() {
        return "LoginPage";
    }
    //register page
    @GetMapping("/register")
    public String registerPage() {
        return "registerPage";
    }
    
    //logout 
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
    
    //dashboard
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        User user = (User) session.getAttribute("LOGGED_USER");

        if (user == null) {
            return "redirect:/login";
        }
        
        // If admin, redirect to admin page directly
        if ("ADMIN".equals(user.getRole())) {
            return "redirect:/admin";
        }
        
        return "mainPage";
    }

	
	//driver-verification
	@Autowired
	private DriverVerificationRepo driverVerificationRepo;
	@GetMapping("/driver-verification")
	public String driverVerification(HttpSession session) {

	    User user = (User) session.getAttribute("LOGGED_USER");

	    if (user == null) {
	        return "redirect:/login";
	    }

	    Long userId = user.getId();

	    if (driverVerificationRepo.existsByUser_Id(userId)) {
	        return "redirect:/dashboard";
	    }
	    return "DriverVerificationPage";
	}

    //shipper-verifiaction
	@Autowired
	private ShipperVerificationRepo shipperVerificationRepo;
    @GetMapping("/shipper-verification")
    public String shipperVerification( HttpSession session) {
    	User user = (User) session.getAttribute("LOGGED_USER");

	    if (user == null) {
	        return "redirect:/login";
	    }

	    Long userId = user.getId();

	    if (shipperVerificationRepo.existsByUser_Id(userId)) {
	        return "redirect:/dashboard";
	    }
    	
        return "ShipperVerificationPage";
    }
    @GetMapping("/shipper/post-load")
    public String postLoad(HttpSession session) {
    	User user = (User) session.getAttribute("LOGGED_USER");

	    if (user == null) {
	        return "redirect:/login";
	    }
        if (!"SHIPPER".equalsIgnoreCase(user.getRole())) {
            return "redirect:/dashboard";
        }
        if (!shipperVerificationRepo.existsByUser_IdAndStatus(user.getId(), "APPROVED")) {
            return "redirect:/shipper-verification?kycRequired=post-load";
        }

	    	return "shipper/post-load";
    }  
    @GetMapping("/shipper/myActivities")
    public String myActivity(HttpSession session) {
    	User user = (User) session.getAttribute("LOGGED_USER");
    	
    	if (user == null) {
    		return "redirect:/login";
    	}
    	
    	
    	return "shipper/myActivities";
    }  
    @GetMapping("/shipper/load-history")
    public String loadHistory(HttpSession session) {
    	User user = (User) session.getAttribute("LOGGED_USER");
    	
    	if (user == null) {
    		return "redirect:/login";
    	}
    	
    	
    	return "shipper/loadHistory";
    }  
    @GetMapping("/driver/my-trips")
    public String myTrips(HttpSession session) {
    	User user = (User) session.getAttribute("LOGGED_USER");
    	
    	if (user == null) {
    		return "redirect:/login";
    	}
    	
    	
    	return "driver/myTrips";
    }  
    @GetMapping("/driver/active-trips")
    public String activeTrips(HttpSession session) {
    	User user = (User) session.getAttribute("LOGGED_USER");
    	
    	if (user == null) {
    		return "redirect:/login";
    	}
    	
    	
    	return "driver/activeTrips";
    }  
   
 
    @GetMapping("/tracking")
    public String trackingPage(HttpSession session) {
        User user = (User) session.getAttribute("LOGGED_USER");

        if (user == null) {
            return "redirect:/login";
        }

        return "trackingPage";
    }
    
    @GetMapping("/contact")
    public String contactPage(HttpSession session) {
    	User user = (User) session.getAttribute("LOGGED_USER");
    	
    	if (user == null) {
    		return "redirect:/login";
    	}
    	
    	return "contact";
    }

    @GetMapping({"/reports", "/Reports"})
    public String reportsPage(HttpSession session) {
        User user = (User) session.getAttribute("LOGGED_USER");

        if (user == null) {
            return "redirect:/login";
        }

        return "reports";
    }

    @GetMapping({"/payment-invoice", "/Payment&Invoice"})
    public String paymentInvoicePage(HttpSession session) {
        User user = (User) session.getAttribute("LOGGED_USER");

        if (user == null) {
            return "redirect:/login";
        }

        return "paymentInvoice";
    }

    // Admin page
    @GetMapping("/admin")
    public String adminPage(HttpSession session) {
        User user = (User) session.getAttribute("LOGGED_USER");

        if (user == null) {
            return "redirect:/login";
        }

        // Only allow admin role
        if (!"ADMIN".equals(user.getRole())) {
            return "redirect:/dashboard";
        }

        return "admin";
    }
}
