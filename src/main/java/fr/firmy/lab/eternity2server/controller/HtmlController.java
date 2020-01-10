package fr.firmy.lab.eternity2server.controller;

import fr.firmy.lab.eternity2server.controller.services.GraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "/api/eternity2-server/v1")
public class HtmlController {

    private GraphService graphService;

    @Autowired
    public HtmlController(GraphService graphService) {
        this.graphService = graphService;
    }

    @RequestMapping(value="graph", method = RequestMethod.GET)
    public String getGraph(Model model) {
        model.addAttribute("mermaidDiagram", graphService.createMermaidDiagram() );
        return "graph";
    }
}
