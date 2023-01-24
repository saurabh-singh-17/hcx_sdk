import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Enumeration;

import java.util.*;
public class HCXCoverageEligibility {

    public static CoverageEligibilityRequest coverageEligibilityRequestExample() {

        /**
         * We will now try to create a Coverage Eligibility Request as per
         * https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-CoverageEligibilityRequest.html
         */

        //Creating coverage eligibility request
        CoverageEligibilityRequest ce = new CoverageEligibilityRequest();
        ce.setId("dc82673b-8c71-48c2-8a17-16dcb3b035f6");

        /**
         * Meta is not a mandatory field as per the definitions, but we need to include HCX profile links in resource field in meta
         * to ensure that the resource is validated against given HCX FHIR profile. In below case, as we are creating a coverage eligibility
         * request as per https://ig.hcxprotocol.io/v0.7/StructureDefinition-CoverageEligibilityRequest.html so we need to give the
         * same link the Meta resource
         */
        Meta meta = new Meta();
        meta.getProfile().add(new CanonicalType("https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-CoverageEligibilityRequest.html"));
        ce.setMeta(meta);

        /**
         * We will now be providing other mandatory fields as oer the HCX FHIR standards.
         * We are using sample values in most places. Please replace the values as per your needs.
         */

        /**
         * Identifiers are mandatory in almost all resource definitions.
         * Identifier has two main components, a System and a Code. We need to provide a System which is a URL that
         * contains the organization or participants identifier code such as Rohini ID etc.
         */

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
        metaResponse.getProfile().add(new CanonicalType("https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-CoverageEligibilityResponse.html"));
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
