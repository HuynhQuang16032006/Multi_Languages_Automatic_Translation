package vn.edu.sgu.audiotour.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    @GetMapping({"/next", "/next/", "/next/explore", "/next/places/{id}", "/next/player/{id}", "/next/map", "/next/qr", "/next/about", "/next/admin", "/next/admin/{page}"})
    public String touristApp() {
        return "forward:/next/index.html";
    }
}
