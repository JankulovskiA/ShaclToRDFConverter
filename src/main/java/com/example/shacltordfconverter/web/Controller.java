package com.example.shacltordfconverter.web;

import com.example.shacltordfconverter.service.FlowHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.ModelMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@org.springframework.stereotype.Controller
@RequestMapping("/controller")
public class Controller {

    private final FlowHandler flowHandler;

    public Controller(FlowHandler flowHandler) {
        this.flowHandler = flowHandler;
    }

    @GetMapping(value = {"/inputForm", "/", ""})
    public String getInputForm() {
        return "inputForm";
    }

    @PostMapping("/getShacl")
    public String createRDF(@RequestParam("shaclFile") MultipartFile shaclFile,
                            @RequestParam("rdfInstances") Integer rdfInstances,
                            ModelMap model) throws IOException {
        Model factoryModel = ModelFactory.createDefaultModel();
        String rdf = flowHandler.convertData(factoryModel.read(shaclFile.getInputStream(), null, "TTL"), rdfInstances);
        model.addAttribute("rdf", rdf);
        return "RDFdisplay";
    }
}
