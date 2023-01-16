import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Enumeration;

import java.util.*;
public class HCXCoverageEligibility {

    public static CoverageEligibilityRequest coverageEligibilityRequestExample() {

        //Creating coverage eligibility request
        CoverageEligibilityRequest ce = new CoverageEligibilityRequest();
        ce.setId("dc82673b-8c71-48c2-8a17-16dcb3b035f6");
        Meta meta = new Meta();
        meta.getProfile().add(new CanonicalType("https://ig.hcxprotocol.io/v0.7/StructureDefinition-CoverageEligibilityRequest.html"));
        ce.setMeta(meta);
        ce.getIdentifier().add(new Identifier().setValue("req_70e02576-f5f5-424f-b115-b5f1029704d4"));
        ce.setStatus(CoverageEligibilityRequest.EligibilityRequestStatus.ACTIVE);
        ce.setPriority(new CodeableConcept(new Coding().setSystem("http://terminology.hl7.org/CodeSystem/processpriority").setCode("normal")));

        EnumFactory<CoverageEligibilityRequest.EligibilityRequestPurpose> fact = new CoverageEligibilityRequest.EligibilityRequestPurposeEnumFactory();
        ce.setPurpose(List.of((Enumeration) new Enumeration<>(fact).setValue(CoverageEligibilityRequest.EligibilityRequestPurpose.BENEFITS)));

        ce.setPatient(new Reference("Patient/RVH1003"));
        ce.getServicedPeriod().setStart(new Date(System.currentTimeMillis())).setEnd(new Date(System.currentTimeMillis()));
        ce.setCreated(new Date(System.currentTimeMillis()));
        ce.setEnterer(new Reference("http://abcd.com/Tmh01"));
        ce.setProvider(new Reference("Organization/GICOFINDIA"));
        ce.setInsurer(new Reference( "Organization/Tmh01"));
        ce.setFacility(ce.getFacility().setReference("http://sgh.com.sa/Location/4461281"));
        ce.getInsurance().add(new CoverageEligibilityRequest.InsuranceComponent(new Reference("Coverage/COVERAGE1")));
        return ce;
    }

    public static CoverageEligibilityResponse coverageEligibilityResponseExample() {
        //Creating the coverage eligibility response
        CoverageEligibilityResponse covelires = new CoverageEligibilityResponse();
        Meta metaResponse = new Meta();
        metaResponse.getProfile().add(new CanonicalType("https://ig.hcxprotocol.io/v0.7/StructureDefinition-CoverageEligibilityResponse.html"));
        metaResponse.setLastUpdated(new Date());
        covelires.setMeta(metaResponse);
        covelires.addIdentifier(new Identifier().setSystem("http://identifiersystem.com").setValue("IdentifierValue"));
        covelires.setStatus(CoverageEligibilityResponse.EligibilityResponseStatus.ACTIVE);
        covelires.setPatient(new Reference("Patient/RVH1003"));
        covelires.setCreated(new Date());
        covelires.setInsurer(new Reference("Organization/GICOFINDIA"));
        covelires.setRequest(new Reference("CoverageEligibilityRequest/dc82673b-8c71-48c2-8a17-16dcb3b035f6"));
        covelires.setRequestor(new Reference("Organization/Tmh01"));
        covelires.setOutcome(Enumerations.RemittanceOutcome.COMPLETE);
        return covelires;
    }
}
