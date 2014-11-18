/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.xforms;

import static org.openmrs.module.xforms.XformBuilder.ATTRIBUTE_BIND;
import static org.openmrs.module.xforms.XformBuilder.ATTRIBUTE_CONCEPT_ID;
import static org.openmrs.module.xforms.XformBuilder.ATTRIBUTE_CONSTRAINT;
import static org.openmrs.module.xforms.XformBuilder.ATTRIBUTE_ID;
import static org.openmrs.module.xforms.XformBuilder.ATTRIBUTE_MESSAGE;
import static org.openmrs.module.xforms.XformBuilder.ATTRIBUTE_NODESET;
import static org.openmrs.module.xforms.XformBuilder.ATTRIBUTE_OPENMRS_CONCEPT;
import static org.openmrs.module.xforms.XformBuilder.ATTRIBUTE_TYPE;
import static org.openmrs.module.xforms.XformBuilder.ATTRIBUTE_UUID;
import static org.openmrs.module.xforms.XformBuilder.CONTROL_INPUT;
import static org.openmrs.module.xforms.XformBuilder.CONTROL_REPEAT;
import static org.openmrs.module.xforms.XformBuilder.CONTROL_SELECT;
import static org.openmrs.module.xforms.XformBuilder.CONTROL_SELECT1;
import static org.openmrs.module.xforms.XformBuilder.DATA_TYPE_BASE64BINARY;
import static org.openmrs.module.xforms.XformBuilder.DATA_TYPE_BOOLEAN;
import static org.openmrs.module.xforms.XformBuilder.DATA_TYPE_DATE;
import static org.openmrs.module.xforms.XformBuilder.DATA_TYPE_DATETIME;
import static org.openmrs.module.xforms.XformBuilder.DATA_TYPE_DECIMAL;
import static org.openmrs.module.xforms.XformBuilder.DATA_TYPE_TEXT;
import static org.openmrs.module.xforms.XformBuilder.DATA_TYPE_TIME;
import static org.openmrs.module.xforms.XformBuilder.INSTANCE_ID;
import static org.openmrs.module.xforms.XformBuilder.MODEL_ID;
import static org.openmrs.module.xforms.XformBuilder.NAMESPACE_XFORMS;
import static org.openmrs.module.xforms.XformBuilder.NAMESPACE_XML_INSTANCE;
import static org.openmrs.module.xforms.XformBuilder.NAMESPACE_XML_SCHEMA;
import static org.openmrs.module.xforms.XformBuilder.NODE_BIND;
import static org.openmrs.module.xforms.XformBuilder.NODE_GROUP;
import static org.openmrs.module.xforms.XformBuilder.NODE_HINT;
import static org.openmrs.module.xforms.XformBuilder.NODE_INSTANCE;
import static org.openmrs.module.xforms.XformBuilder.NODE_LABEL;
import static org.openmrs.module.xforms.XformBuilder.NODE_MODEL;
import static org.openmrs.module.xforms.XformBuilder.NODE_SCHEMA;
import static org.openmrs.module.xforms.XformBuilder.NODE_VALUE;
import static org.openmrs.module.xforms.XformBuilder.NODE_XFORMS;
import static org.openmrs.module.xforms.XformBuilder.PREFIX_XFORMS;
import static org.openmrs.module.xforms.XformBuilder.PREFIX_XML_INSTANCES;
import static org.openmrs.module.xforms.XformBuilder.PREFIX_XML_SCHEMA;
import static org.openmrs.module.xforms.XformBuilder.PREFIX_XML_SCHEMA2;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.Field;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.context.Context;
import org.openmrs.hl7.HL7Constants;
import org.openmrs.module.xforms.RelativeBuilder;
import org.openmrs.module.xforms.XformBuilder;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.formentry.FormEntryWrapper;
import org.openmrs.module.xforms.formentry.FormSchemaFragment;
import org.openmrs.module.xforms.util.XformsUtil;
import org.openmrs.util.FormConstants;
import org.openmrs.util.FormUtil;

/**
 * This is a clone of the Xforms module XformBuilderEx class, allowing us to tinker with the view
 * creation code separately from the module itself.
 */
public class BuendiaXformBuilder {

    private static Element bodyNode;
    public static Hashtable<String, Element> bindings;
    private static Hashtable<FormField, Element> formFields;
    private static Hashtable<FormField, String> fieldTokens;
    private static boolean useConceptIdAsHint = false;
    
    /**
     * Builds an xform for an given an openmrs form.
     * 
     * @param form - the form object.
     * @return - the xml content of the xform.
     */
    public static String buildXform(Form form) throws Exception {
        
        bindings = new Hashtable<String, Element>();
        formFields = new Hashtable<FormField, Element>();
        fieldTokens = new Hashtable<FormField, String>();
        useConceptIdAsHint = "true".equalsIgnoreCase(Context.getAdministrationService().getGlobalProperty("xforms.useConceptIdAsHint"));
        
        boolean includeRelationshipNodes = !"false".equals(Context.getAdministrationService()
            .getGlobalProperty(XformConstants.GLOBAL_PROP_KEY_INCLUDE_PATIENT_RELATIONSHIPS));
        
        //String schemaXml = XformsUtil.getSchema(form);
        String templateXml = FormEntryWrapper.getFormTemplate(form);
        
        //Add relationship data node
        if (includeRelationshipNodes) {
            templateXml = templateXml.replace("</patient>", "  <patient_relative>\n      <patient_relative.person/>\n      <patient_relative.relationship/>\n    </patient_relative>\n  </patient>");
        }
        
        Element formNode = (Element) XformBuilder.getDocument(new StringReader(templateXml)).getRootElement();
        formNode.setAttribute(null, ATTRIBUTE_UUID, form.getUuid());
        
        Document doc = new Document();
        doc.setEncoding(XformConstants.DEFAULT_CHARACTER_ENCODING);
        
        Element xformsNode = doc.createElement(NAMESPACE_XFORMS, NODE_XFORMS);
        xformsNode.setPrefix(PREFIX_XFORMS, NAMESPACE_XFORMS);
        xformsNode.setPrefix(PREFIX_XML_SCHEMA, NAMESPACE_XML_SCHEMA);
        xformsNode.setPrefix(PREFIX_XML_SCHEMA2, NAMESPACE_XML_SCHEMA);
        xformsNode.setPrefix(PREFIX_XML_INSTANCES, NAMESPACE_XML_INSTANCE);
        
        xformsNode.setPrefix("jr", "http://openrosa.org/javarosa");
        
        doc.addChild(org.kxml2.kdom.Element.ELEMENT, xformsNode);
        
        Element modelNode = doc.createElement(NAMESPACE_XFORMS, NODE_MODEL);
        modelNode.setAttribute(null, ATTRIBUTE_ID, MODEL_ID);
        xformsNode.addChild(Element.ELEMENT, modelNode);
        
        Element groupNode = doc.createElement(NAMESPACE_XFORMS, NODE_GROUP);
        Element labelNode = doc.createElement(NAMESPACE_XFORMS, NODE_LABEL);
        labelNode.addChild(Element.TEXT, "Page1");
        groupNode.addChild(Element.ELEMENT, labelNode);
        xformsNode.addChild(Element.ELEMENT, groupNode);
        bodyNode = groupNode;
        
        Element instanceNode = doc.createElement(NAMESPACE_XFORMS, NODE_INSTANCE);
        instanceNode.setAttribute(null, ATTRIBUTE_ID, INSTANCE_ID);
        modelNode.addChild(Element.ELEMENT, instanceNode);
        
        instanceNode.addChild(Element.ELEMENT, formNode);
        
        Document xformSchemaDoc = new Document();
        xformSchemaDoc.setEncoding(XformConstants.DEFAULT_CHARACTER_ENCODING);
        Element xformSchemaNode = doc.createElement(NAMESPACE_XML_SCHEMA, null);
        xformSchemaNode.setName(NODE_SCHEMA);
        xformSchemaDoc.addChild(org.kxml2.kdom.Element.ELEMENT, xformSchemaNode);
        
        //TODO This block should be replaced with using database field items instead of
        //     parsing the template document.
        Hashtable<String, String> problemList = new Hashtable<String, String>();
        Hashtable<String, String> problemListItems = new Hashtable<String, String>();
        XformBuilder.parseTemplate(modelNode, formNode, formNode, bindings, groupNode, problemList, problemListItems, 0);
        
        buildUInodes(form);
        
        //find all conceptId attributes in the document and replace their value with a mapped concept
        String prefSourceName = Context.getAdministrationService().getGlobalProperty(
            XformConstants.GLOBAL_PROP_KEY_PREFERRED_CONCEPT_SOURCE);
        //we only use the mappings if the global property is set
        if (StringUtils.isNotBlank(prefSourceName)) {
            for (int i = 0; i < formNode.getChildCount(); i++) {
                Element childElement = formNode.getElement(i);
                if (childElement != null) {
                    for (int j = 0; j < childElement.getChildCount(); j++) {
                        if (childElement.getElement(j) != null) {
                            Element grandChildElement = childElement.getElement(j);
                            String value = grandChildElement.getAttributeValue(null, ATTRIBUTE_OPENMRS_CONCEPT);
                            if (StringUtils.isNotBlank(value))
                                XformBuilder.addConceptMapAttributes(grandChildElement, value);
                        }
                    }
                }
            }
        }
        
        if (includeRelationshipNodes) {
            RelativeBuilder.build(modelNode, groupNode, formNode);
        }
        
        bindings.clear();
        formFields.clear();
        fieldTokens.clear();
        
        return XformBuilder.fromDoc2String(doc);
    }        
    
    private static Element addUInode(String token, Concept concept, String dataType, String controlName, Locale locale, Element bodyNode){
        String bindName = token;
        
        Element controlNode = bodyNode.createElement(NAMESPACE_XFORMS, controlName);
        controlNode.setAttribute(null, ATTRIBUTE_BIND, bindName);
        
        Element bindNode = (Element) bindings.get(bindName);
        if (bindNode == null) {
            throw new IllegalArgumentException("No bind node for bindName " + bindName);
        }
        
        bindNode.setAttribute(null, ATTRIBUTE_TYPE, dataType);
        
        //create the label
        Element labelNode = bodyNode.createElement(NAMESPACE_XFORMS, NODE_LABEL);
        ConceptName name = concept.getName(locale, false);
        if (name == null) {
            name = concept.getName();
        }
        
        labelNode.addChild(Element.TEXT, name.getName());
        controlNode.addChild(Element.ELEMENT, labelNode);
        
        addHintNode(labelNode, concept);
        
        XformBuilder.addControl(bodyNode, controlNode);
        
        if(concept instanceof ConceptNumeric) {
            ConceptNumeric numericConcept = (ConceptNumeric)concept;
            if(numericConcept.isPrecise()){
                Double minInclusive = numericConcept.getLowAbsolute();
                Double maxInclusive = numericConcept.getHiAbsolute();
                
                if(!(minInclusive == null && maxInclusive == null)){
                    String lower = (minInclusive == null ? "" : FormSchemaFragment.numericToString(minInclusive, numericConcept.isPrecise()));
                    String upper = (maxInclusive == null ? "" : FormSchemaFragment.numericToString(maxInclusive, numericConcept.isPrecise()));
                    bindNode.setAttribute(null, ATTRIBUTE_CONSTRAINT, ". >= " + lower + " and . <= " + upper);
                    bindNode.setAttribute(null, (XformsUtil.isJavaRosaSaveFormat() ? "jr:constraintMsg" : ATTRIBUTE_MESSAGE),
                        "value should be between " + lower + " and " + upper + " inclusive");
                }
            }
        }
        
        return controlNode;
    }
    
    private static void addCodedUInodes(boolean multiplSel, Element controlNode, Collection<ConceptAnswer> answerList, Concept concept, String dataType, String controlName, Locale locale){
        for (ConceptAnswer answer : answerList) {
            String conceptName = answer.getAnswerConcept().getName(locale).getName();
            String conceptValue;
            
            if (answer.getAnswerConcept().getConceptClass().getConceptClassId()
                    .equals(HL7Constants.CLASS_DRUG)
                    && answer.getAnswerDrug() != null) {
                
                conceptName = answer.getAnswerDrug().getName();
                
                if(multiplSel)
                    conceptValue = FormUtil.getXmlToken(conceptName);
                else {
                    conceptValue = StringEscapeUtils.escapeXml(FormUtil.conceptToString(answer.getAnswerConcept(),
                                    locale)) + "^" + FormUtil.drugToString(answer.getAnswerDrug());
                }
            } else {
                if(multiplSel)
                    conceptValue = FormUtil.getXmlToken(conceptName);
                else
                    conceptValue = StringEscapeUtils.escapeXml(FormUtil.conceptToString(answer.getAnswerConcept(), locale));
            }
            
            Element itemNode = controlNode.createElement(NAMESPACE_XFORMS, XformBuilder.NODE_ITEM);
            itemNode.setAttribute(null, ATTRIBUTE_CONCEPT_ID, concept.getConceptId().toString());
            controlNode.addChild(Element.ELEMENT, itemNode);
            
            Element itemLabelNode = controlNode.createElement(NAMESPACE_XFORMS, NODE_LABEL);
            itemLabelNode.addChild(Element.TEXT, conceptName);
            itemNode.addChild(Element.ELEMENT, itemLabelNode);
            
            //TODO This will make sense after the form designer's OptionDef implements
            //the xforms hint.
            //addHintNode(itemLabelNode, answer.getAnswerConcept());
            
            Element itemValNode = controlNode.createElement(NAMESPACE_XFORMS, NODE_VALUE);
            itemValNode.addChild(Element.TEXT, conceptValue);
            itemNode.addChild(Element.ELEMENT, itemValNode);
        }
    }
    
    private static Element getParentNode(FormField formField, Locale locale){
        formField = formField.getParent();
        if(formField == null){
            return bodyNode; //is this problem list?
        }
        if(formField.getParent() == null){
            return bodyNode;
        }
        else{
            Element node = formFields.get(formField);
            if(node != null)
                return node;
            
            String token = fieldTokens.get(formField);
            
            Element groupNode = bodyNode.createElement(NAMESPACE_XFORMS, NODE_GROUP);
            bodyNode.addChild(Element.ELEMENT, groupNode);
            
            Element labelNode = groupNode.createElement(NAMESPACE_XFORMS, NODE_LABEL);
            labelNode.addChild(Element.TEXT, formField.getField().getConcept().getName(locale, false).getName());
            groupNode.addChild(Element.ELEMENT, labelNode);
            
            addHintNode(labelNode, formField.getField().getConcept());
            
            if (formField.getMaxOccurs() != null && formField.getMaxOccurs() == -1) {
                Element repeatControl = bodyNode.createElement(NAMESPACE_XFORMS, CONTROL_REPEAT);
                repeatControl.setAttribute(null, ATTRIBUTE_BIND, token);
                groupNode.addChild(Element.ELEMENT, repeatControl);
                
                formFields.put(formField, repeatControl);
                return repeatControl;
            }
            else {
                groupNode.setAttribute(null, ATTRIBUTE_ID, token);
                formFields.put(formField, groupNode);
                return groupNode;
            }
        }
    }
    
    public static void addProblemList(String token, Concept concept, boolean required,
                                    Locale locale, FormField formField) {
        
        Element groupNode = bodyNode.createElement(NAMESPACE_XFORMS, NODE_GROUP);
        bodyNode.addChild(Element.ELEMENT, groupNode);
        
        Element labelNode = groupNode.createElement(NAMESPACE_XFORMS, NODE_LABEL);
        labelNode.addChild(Element.TEXT, formField.getField().getConcept().getName(locale, false).getName());
        groupNode.addChild(Element.ELEMENT, labelNode);
        
        addHintNode(labelNode, concept);
        
        Element repeatControl = bodyNode.createElement(NAMESPACE_XFORMS, CONTROL_REPEAT);
        repeatControl.setAttribute(null, ATTRIBUTE_BIND, token);
        groupNode.addChild(Element.ELEMENT, repeatControl);

        //add the input node.
        Element controlNode = repeatControl.createElement(NAMESPACE_XFORMS, CONTROL_INPUT);
        
        String nodeset = "problem_list/" + token + "/value";
        String id = nodeset.replace('/', '_');
        controlNode.setAttribute(null, ATTRIBUTE_BIND, id);
        
        repeatControl.addChild(Element.ELEMENT, controlNode);
        
        //add the label.
        labelNode = controlNode.createElement(NAMESPACE_XFORMS, NODE_LABEL);
        labelNode.addChild(Element.TEXT, token + " value");
        controlNode.addChild(Element.ELEMENT, labelNode);
        
        addHintNode(labelNode, concept);
        
        //create bind node
        Element bindNode = controlNode.createElement(NAMESPACE_XFORMS, NODE_BIND);
        bindNode.setAttribute(null, ATTRIBUTE_ID, id);
        bindNode.setAttribute(null, ATTRIBUTE_NODESET, "/form/" + nodeset);
        bindNode.setAttribute(null, ATTRIBUTE_TYPE, DATA_TYPE_TEXT);
        
        ((Element)bindings.get(token).getParent()).addChild(Element.ELEMENT, bindNode);
    }
    
    private static void buildUInodes(Form form) {   
        Locale locale = Context.getLocale();
        TreeMap<Integer, TreeSet<FormField>> formStructure = FormUtil.getFormStructure(form);
        buildUInodes(form, formStructure, 0, locale);
    }
    
    private static void buildUInodes(Form form, TreeMap<Integer, TreeSet<FormField>> formStructure, Integer sectionId, Locale locale) { 
        
        if (!formStructure.containsKey(sectionId))
            return;
        
        TreeSet<FormField> section = formStructure.get(sectionId);
        if (section == null || section.size() < 1)
            return;
        
        Vector<String> tagList = new Vector<String>();
        
        for(FormField formField : section){ 
            Integer subSectionId = formField.getFormFieldId();
            String sectionName = FormUtil.getXmlToken(formField.getField().getName());
            String name = FormUtil.getNewTag(sectionName, tagList);

            if(formField.getParent() != null && fieldTokens.values().contains(name)){
                String parentName = fieldTokens.get(formField.getParent());
                String token = parentName + "_" + name;
                
                if(!bindings.containsKey(token)) {
                    token = FormUtil.getNewTag(FormUtil.getXmlToken(formField.getParent().getField().getName()),  new Vector<String>());
                    token = token + "_" + name;
                }
                
                name = token;
            }
            
            fieldTokens.put(formField, name);

            Field field = formField.getField();
            boolean required = formField.isRequired();
            
            if (field.getFieldType().getFieldTypeId().equals(FormConstants.FIELD_TYPE_CONCEPT)) {
                Concept concept = field.getConcept();
                ConceptDatatype datatype = concept.getDatatype();
                
                if ( (name.contains("problem_added") || name.contains("problem_resolved")) &&
                        formField.getParent() != null &&
                        (formField.getParent().getField().getName().contains("PROBLEM LIST")) ){
                    
                    addProblemList(name, concept, required, locale, formField);
                }
                else {
                    switch (datatype.getHl7Abbreviation()) {
                        case HL7Constants.HL7_BOOLEAN:
                            addUInode(name, concept, DATA_TYPE_BOOLEAN, CONTROL_INPUT, locale, getParentNode(formField, locale));
                            break;
                        case HL7Constants.HL7_DATE:
                            addUInode(name, concept, DATA_TYPE_DATE, CONTROL_INPUT, locale, getParentNode(formField, locale));
                            break;
                        case HL7Constants.HL7_DATETIME:
                            addUInode(name, concept, DATA_TYPE_DATETIME, CONTROL_INPUT, locale, getParentNode(formField, locale));
                            break;
                        case HL7Constants.HL7_TIME:
                            addUInode(name, concept, DATA_TYPE_TIME, CONTROL_INPUT, locale, getParentNode(formField, locale));
                            break;
                        case HL7Constants.HL7_TEXT:
                            addUInode(name, concept, DATA_TYPE_TEXT, CONTROL_INPUT, locale, getParentNode(formField, locale));
                            break;
                        case HL7Constants.HL7_NUMERIC:
                            ConceptNumeric conceptNumeric = Context.getConceptService().getConceptNumeric(concept.getConceptId());
                            addUInode(name, conceptNumeric, DATA_TYPE_DECIMAL, CONTROL_INPUT, locale, getParentNode(formField, locale));
                            break;
                        case HL7Constants.HL7_CODED:
                        case HL7Constants.HL7_CODED_WITH_EXCEPTIONS:
                            addCodedField(name, formField, field, required, concept, locale);
                            break;
                        case "ED": // This isn't in HL7Constants as far as I can tell.
                            addUInode(name, concept, DATA_TYPE_BASE64BINARY, CONTROL_INPUT, locale, getParentNode(formField, locale));
                            break;
                    }
                }
            }
            
            if (formStructure.containsKey(subSectionId)) {
                buildUInodes(form, formStructure, subSectionId, locale);
            }
        }
    }

    private static void addCodedField(String name, FormField formField, Field field,
            boolean required, Concept concept, Locale locale) {
        if (formField.getMaxOccurs() != null && formField.getMaxOccurs().intValue() == -1) {
            addProblemList(name, concept, required, locale, formField);
        }
        else {
            //Collection<ConceptAnswer> answers = concept.getAnswers(false);
            List<ConceptAnswer> answers = new ArrayList<ConceptAnswer>(concept.getAnswers(false));
            Collections.sort(answers);
            
            String controlName = field.getSelectMultiple() ? CONTROL_SELECT : CONTROL_SELECT1;
            Element controlNode = addUInode(name, concept, DATA_TYPE_TEXT, controlName, locale, getParentNode(formField, locale));
            if (controlNode != null) {
                addCodedUInodes(true, controlNode, answers, concept, DATA_TYPE_TEXT, CONTROL_SELECT, locale);
            }
        }
    }
    
    private static void addHintNode(Element labelNode, Concept concept) {
        String hint = null;
        if(concept.getDescription() != null) {
            hint = concept.getDescription().getDescription();
        }
        
        if(useConceptIdAsHint) {
            hint = (hint != null ? hint + " [" + concept.getConceptId() + "]" : concept.getConceptId().toString());
        }
        
        if(hint != null) {
            Element hintNode = labelNode.createElement(NAMESPACE_XFORMS, NODE_HINT);
            hintNode.addChild(Element.TEXT, hint);
            labelNode.getParent().addChild(Element.ELEMENT, hintNode);
        }
    }
}
