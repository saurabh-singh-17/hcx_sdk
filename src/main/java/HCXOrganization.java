import org.hl7.fhir.r4.model.*;

import java.util.HashMap;
import java.util.Map;

public class HCXOrganization {

    public static Organization ProviderExample(){
        //Information needed to create coverage eligibility bundle with mandatory fields
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("HOS_Name","Tata Memorial Hospital");
        inputMap.put("HOS_Indetifier","HFR-ID-FOR-TMH");
        //making the hospital org resource
        Organization hos = new Organization();
        hos.setId("Tmh01");
        Meta metaorg1 = new Meta();
        metaorg1.getProfile().add(new CanonicalType("https://nrces.in/ndhm/fhir/r4/StructureDefinition/Organization"));
        hos.setMeta(metaorg1);
        hos.setName((String) inputMap.get("HOS_Name"));
        hos.getIdentifier().add(new Identifier().setSystem("http://abdm.gov.in/facilities").setValue((String) inputMap.get("HOS_Indetifier")).setType(new CodeableConcept(new Coding().setSystem("http://terminology.hl7.org/CodeSystem/v2-0203").setCode("AC").setDisplay("Narayana"))));
        return hos;
    }

    public static Organization InsurerExample(){
        //Information needed to create coverage eligibility bundle with mandatory fields
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("INS_Name","GICOFINDIA");
        inputMap.put("INS_Indetifier","GICOFINDIA");

        //making an organization resource
        Organization org = new Organization();
        Meta metaorg = new Meta();
        metaorg.getProfile().add(new CanonicalType("https://nrces.in/ndhm/fhir/r4/StructureDefinition/Organization"));
        org.setMeta(metaorg);
        org.setId("GICOFINDIA");
        org.setName((String) inputMap.get("INS_Name"));
        org.getIdentifier().add(new Identifier().setSystem("http://irdai.gov.in/insurers").setValue((String) inputMap.get("INS_Indetifier")).setType(new CodeableConcept(new Coding().setSystem("http://terminology.hl7.org/CodeSystem/v2-0203").setCode("AC").setDisplay("GOVOFINDIA"))));
        return org;
    }
}
