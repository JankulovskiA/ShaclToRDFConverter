package com.example.shacltordfconverter.service;

import com.example.shacltordfconverter.model.ShaclShape;
import org.apache.jena.rdf.model.Model;
import org.springframework.stereotype.Service;

@Service
public class FlowHandler {
    private final ShaclMapper shaclMapper;
    private final RdfGenerator rdfGenerator;
    private final DistinguishRestrictions distinguishRestrictions;

    public FlowHandler(ShaclMapper shaclMapper, RdfGenerator rdfGenerator, DistinguishRestrictions distinguishRestrictions) {
        this.shaclMapper = shaclMapper;
        this.rdfGenerator = rdfGenerator;
        this.distinguishRestrictions = distinguishRestrictions;
    }

    public String convertData(Model model) {
        ShaclShape shaclShape = shaclMapper.mapTTLFileToShaclShape(model);
        Model rdfModel = rdfGenerator.createMockRdf(shaclShape);
        rdfModel.write(System.out,"TTL");
        return "";
    }
}

