package com.example.shacltordfconverter.service;

import com.example.shacltordfconverter.model.ShaclShape;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RdfGenerator {

    private static final Map<String, String> prefixMap = new HashMap<>();

    static {
        prefixMap.put("http://www.w3.org/ns/shacl", "sh");
        prefixMap.put("http://example.com/ns", "ex");
        prefixMap.put("http://www.w3.org/2001/XMLSchema", "xsd");
    }

    public Model createMockRdf(ShaclShape shaclShape) {
        Model model = ModelFactory.createDefaultModel();
        prefixMap.forEach((key, value) -> model.setNsPrefix(value, key));
        String target = shaclShape.getTargetClass();
        String[] splitTarget = target.split("#");
        Resource targetClass = model.createResource(prefixMap.get(splitTarget[0]) + ":" + DistinguishRestrictions.generateRandomString(0, 10));
        Resource targetClassType = model.createResource(prefixMap.get(splitTarget[0]) + ":" + splitTarget[1]);
        model.add(targetClass, RDF.type, targetClassType);
        List<Node> paths = shaclShape.getRestrictionsMap().keySet().stream().toList();
        for (Node path : paths) {
            Map<Node, List<Node>> restrictionsForPath = shaclShape.getRestrictionsMap().get(path);

            Integer maxCount = getMax(restrictionsForPath);
            Integer minCount = getMin(restrictionsForPath);
            if (minCount == null && maxCount == null) {
                Node value = DistinguishRestrictions.analyzePathRestrictions(restrictionsForPath, prefixMap);
                String[] parts = path.toString().split("#");
                Property property = model.createProperty(prefixMap.get(parts[0]) + ":" + path.toString().split("#")[1]);
                boolean isUrl = prefixMap.values().stream().anyMatch(v -> value.toString().startsWith(v));
                if (isUrl) {
                    model.add(targetClass, property, model.createResource(value.toString()));
                } else {
                    model.add(targetClass, property, value.toString().replace("\"", ""));
                }
            } else if (maxCount == null) {
                // If only minCalls is provided.
                for (int i = 0; i < minCount; i++) {
                    Node value = DistinguishRestrictions.analyzePathRestrictions(restrictionsForPath, prefixMap);
                    String[] parts = path.toString().split("#");
                    Property property = model.createProperty(prefixMap.get(parts[0]), path.toString().split("#")[1]);
                    model.add(targetClass, property, value.toString().replace("\"", ""));
                }
            } else if (minCount == null) {
                // If only maxCalls is provided.
                Random random = new Random();
                int numCalls = random.nextInt(maxCount + 1);
                for (int i = 0; i < numCalls; i++) {
                    Node value = DistinguishRestrictions.analyzePathRestrictions(restrictionsForPath, prefixMap);
                    String[] parts = path.toString().split("#");
                    Property property = model.createProperty(prefixMap.get(parts[0]), path.toString().split("#")[1]);
                    model.add(targetClass, property, value.toString().replace("\"", ""));
                }
            } else {
                // If both minCalls and maxCalls are provided.
                Random random = new Random();
                int numCalls = random.nextInt(maxCount - minCount + 1) + minCount;
                for (int i = 0; i < numCalls; i++) {
                    Node value = DistinguishRestrictions.analyzePathRestrictions(restrictionsForPath, prefixMap);
                    String[] parts = path.toString().split("#");
                    Property property = model.createProperty(prefixMap.get(parts[0]) + ":", path.toString().split("#")[1]);
                    model.add(targetClass, property, value.toString().replace("\"", ""));
                }
            }
        }
        return model;
    }

    private Integer getMax(Map<Node, List<Node>> pathRestrictions) {
        List<Node> restrictionTypes = pathRestrictions.keySet().stream().toList();
        Optional<Node> max = restrictionTypes.stream()
                .filter(restrictionType -> restrictionType.toString().contains("http://www.w3.org/ns/shacl#maxCount"))
                .findFirst();
        if (max.isPresent()) {
            return Integer.parseInt(
                    max.map(node -> pathRestrictions.get(node)
                                    .get(0)
                                    .toString()
                                    .split("\\^\\^")[0]
                                    .replace("\"", ""))
                            .get());
        }
        return null;
    }

    private Integer getMin(Map<Node, List<Node>> pathRestrictions) {
        List<Node> restrictionTypes = pathRestrictions.keySet().stream().toList();
        Optional<Node> min = restrictionTypes.stream()
                .filter(restrictionType -> restrictionType.toString().contains("http://www.w3.org/ns/shacl#minCount"))
                .findFirst();
        if (min.isPresent()) {
            return Integer.parseInt(
                    min.map(node -> pathRestrictions
                                    .get(node)
                                    .get(0)
                                    .toString()
                                    .split("\\^\\^")[0]
                                    .replace("\"", ""))
                            .get());
        }
        return null;
    }
}
