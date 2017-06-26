package app.controllers;

import app.pojo.Book;
import app.services.SlopeOne;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class MainController {
    @Autowired
    private SlopeOne slopeOne;

    @RequestMapping(value = "/create/mark/for-book/{isbn}/value/{value}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public void hello(@PathVariable("isbn") String isbn, @PathVariable("value") String value) {
        slopeOne.addNewRating(isbn, Integer.parseInt(value));
    }

    @RequestMapping(value = "/recommendations")
    public String recommendations(Map<String, Object> model) {
        slopeOne.fillDevForCurrentUser();
        model.put("books", slopeOne.predictBest(slopeOne.getCurrentUserId(), 9));
        return "Recommendations";
    }

    @RequestMapping(value = "/")
    public String welcome(Map<String, Object> model) {
        model.put("books", slopeOne.showInitialBooks());
        return "MainPage";
    }

}