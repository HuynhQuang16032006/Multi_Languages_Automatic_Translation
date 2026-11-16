package vn.edu.sgu.audiotour.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class FrontendController {
    @GetMapping("/")
    public RedirectView root(HttpServletRequest request) {
        String venue = request.getParameter("venue");
        boolean validVenue = venue != null && venue.matches("[1-9][0-9]{0,15}")
                && (venue.length() < 16 || venue.compareTo("9007199254740991") <= 0);
        RedirectView redirect = new RedirectView(validVenue
                ? "/next/places/" + venue + "?source=qr" : "/next/explore", true);
        redirect.setPropagateQueryParams(true);
        return redirect;
    }

    @GetMapping({"/next", "/next/", "/next/explore", "/next/places/{id}", "/next/player/{id}", "/next/map", "/next/qr", "/next/about", "/next/admin", "/next/admin/{page}"})
    public String touristApp() {
        return "forward:/next/index.html";
    }
}
