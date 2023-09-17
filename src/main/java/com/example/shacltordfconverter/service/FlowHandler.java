package com.example.shacltordfconverter.service;

import com.example.shacltordfconverter.model.ShaclShape;
import org.apache.jena.rdf.model.Model;
import org.springframework.stereotype.Service;

import java.io.StringWriter;

@Service
public class FlowHandler {
    private final ShaclMapper shaclMapper;
    private final RdfGenerator rdfGenerator;
    public FlowHandler(ShaclMapper shaclMapper, RdfGenerator rdfGenerator) {
        this.shaclMapper = shaclMapper;
        this.rdfGenerator = rdfGenerator;
    }

    public String convertData(Model model, Integer instances) {
        StringWriter stringWriter = new StringWriter();
        for (int i = 0; i < instances; i++) {
            ShaclShape shaclShape = shaclMapper.mapTTLFileToShaclShape(model);
            Model rdfModel = rdfGenerator.createMockRdf(shaclShape,i);
            rdfModel.write(stringWriter, "TTL");
            stringWriter.append("\n");
        }
        return stringWriter.toString();
    }
}

