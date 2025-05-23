package uk.ac.man.cs.eventlite.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api", produces = {MediaType.APPLICATION_JSON_VALUE})
public class HomeControllerApi {
    @GetMapping
    public RepresentationModel<?> getHome() {
        RepresentationModel<?> model = new RepresentationModel<>();

//         Add links to venues, events and profile
        model.add(linkTo(VenuesControllerApi.class).withRel("venues"));
        model.add(linkTo(EventsControllerApi.class).withRel("events"));
//        There isn't a profile controller yet, uncomment this line when there is
//        model.add(linkTo(ProfileControllerApi.class).withRel("profile"));
//        And comment this one out, this is temporary ----
        model.add(linkTo(HomeControllerApi.class).slash("profile").withRel("profile"));
//        ------------------------------------------------

        return model;
    }
}

