import org.hl7.fhir.r4.model.*;

import java.util.HashMap;
import java.util.Map;

public class HCXCoverage {

    public static Coverage HCXCoverageExample(){

        //Information needed to create coverage eligibility bundle with mandatory fields
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("COV_Status",Coverage.CoverageStatus.ACTIVE);
        inputMap.put("COV_Identifier_System","https://www.gicofIndia.in/policies");
        inputMap.put("COV_Identifier_Value","policy-RVH1003");
        inputMap.put("COV_SubscriberId","SN-RVH1003");
        inputMap.put("COV_Relationship_System","http://terminology.hl7.org/CodeSystem/subscriber-relationship");
        inputMap.put("COV_Relationship_Value","self");
        
        //making the coverage resource
        Coverage cov = new Coverage();
        cov.setId("COVERAGE1");
        Meta metacov = new Meta();
        metacov.getProfile().add(new CanonicalType("https://ig.hcxprotocol.io/v0.7/StructureDefinition-Coverage.html"));
        cov.setMeta(metacov);
        cov.setStatus((Coverage.CoverageStatus) inputMap.get("COV_Status"));
        cov.getIdentifier().add(new Identifier().setValue((String) inputMap.get("COV_Identifier_Value")).setSystem((String) inputMap.get("COV_Identifier_System")));
        cov.getSubscriber().setReference("Patient/RVH1003");
        cov.setSubscriberId((String) inputMap.get("COV_SubscriberId"));
        cov.getBeneficiary().setReference( "Patient/RVH1003");
        cov.setRelationship(new CodeableConcept(new Coding().setSystem((String) inputMap.get("COV_Relationship_System")).setCode((String) inputMap.get("COV_Relationship_Value"))));
        cov.getPayor().add(new Reference("Organization/GICOFINDIA"));
        return cov;
    }
}
