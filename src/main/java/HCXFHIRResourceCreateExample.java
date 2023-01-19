import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import io.hcxprotocol.impl.HCXIncomingRequest;
import io.hcxprotocol.impl.HCXOutgoingRequest;
import io.hcxprotocol.init.HCXIntegrator;
import io.hcxprotocol.utils.JSONUtils;
import io.hcxprotocol.utils.Operations;
import io.hcxprotocol.validator.HCXFHIRValidator;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Claim;
import org.hl7.fhir.r4.model.Enumeration;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HCXFHIRResourceCreateExample {

    public static void main(String[] args) throws Exception {

        /**
         * Initializing FHIR HAPI parser for R4 version as Swasth IGs follow R4 HL7 FHIR standards
         * Parser context is later used to covert FHIR objects in JSON and other formats for easy readability and data transfer
        */
        IParser p = FhirContext.forR4().newJsonParser().setPrettyPrint(true);

        /**
         * Initializing hcx_sdk to use helper functions and FHIR validator
         * Documentation is available at https://github.com/Swasth-Digital-Health-Foundation/hcx-platform/releases/tag/hcx-integrator-sdk-1.0.0
         */
        String publicKeyUrl = "https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-29/demo-app/server/resources/keys/x509-private-key.pem";
        String certificate = IOUtils.toString(new URL(publicKeyUrl), StandardCharsets.UTF_8.toString());
        System.out.println("certificate " + certificate);

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("protocolBasePath", "http://staging-hcx.swasth.app/api/v0.7");
        configMap.put("participantCode", "1-521eaec7-8cb9-4b6c-8b4e-4dba300af6f4");
        configMap.put("authBasePath", "http://a9dd63de91ee94d59847a1225da8b111-273954130.ap-south-1.elb.amazonaws.com:8080/auth/realms/swasth-health-claim-exchange/protocol/openid-connect/token");
        configMap.put("username", "swasth_mock_provider@swasthapp.org");
        configMap.put("password", "Opensaber@123");
        configMap.put("encryptionPrivateKey", certificate);
        configMap.put("igUrl", "https://ig.hcxprotocol.io/v0.7");
        HCXIntegrator.init(configMap);


        /**
         * We will now try to create a Coverage Eligibility Request Bundle as per
         * https://www.hl7.org/fhir/bundle.html
         * While creating a bundle resource we need provide a mandatory BundleType. We will be using
         * BundleTYpe as Document as per the resource link shared above. We will be using Bundle.BundleType.DOCUMENT
         */

        /**
         * HCX FHIR implementation requires BundleType to be "Document" and in such cases a Composition resource is mandatory in
         * the bundle. We will create a Composition resource for our CoverageEligibilityRequest below
         * Composition resource structure definition is available at "https://ig.hcxprotocol.io/v0.7/StructureDefinition-CoverageEligibilityRequestDocument.html"
         * While creating a Composition resource we need provide a Status. We will be using Status as final
         * Users can use other values for Status as per the business scenarios which is available in the bundle definition
         * We will create a Composition below for CoverageEligibilityRequest
         */
        Composition comp = new Composition();

        /**
         * Meta is not a mandatory field as per the definitions, but we need to include HCX profile links in resource field in meta
         * to ensure that the resource is validated against given HCX FHIR profile. In below case, as we are creating a coverage eligibility
         * composition as per https://ig.hcxprotocol.io/v0.7/StructureDefinition-CoverageEligibilityRequestDocument.html so we need to give the
         * same link the Meta information
         */
        Meta metaComp = new Meta();
        metaComp.getProfile().add(new CanonicalType("https://ig.hcxprotocol.io/v0.7/StructureDefinition-CoverageEligibilityRequestDocument.html"));
        //comp.setMeta(metaComp);
        /**
         * We will now be providing other mandatory fields as oer the HCX FHIR standards.
         * We are using sample values in most places. Please replace the values as per your needs.
         */
        comp.setId("COMPCOVELEREQ");
        comp.setStatus(Composition.CompositionStatus.FINAL);
        comp.setTitle("Coverage Eligibility Request");
        /**
         * Identifiers are mandatory in almost all resource definitions.
         * Identifier has two main components, a System and a Code. We need to provide a System which is a URL that
         * contains the organization or participants identifier code such as Rohini ID etc.
         */
        comp.setIdentifier(new Identifier().setSystem("https://www.tmh.in/hcx-documents").setValue(UUID.randomUUID().toString()));
        comp.setType(new CodeableConcept(new Coding().setSystem("https://www.hcx.org/document-type").setCode("HcxCoverageEligibilityRequest").setDisplay("Coverage Eligibility Request Doc")));
        comp.setSubject(new Reference("Patient/RVH1003"));
        comp.setDate(new Date());
        comp.getAuthor().add(new Reference("Organization/Tmh01"));
        /**
         * We need to add a reference to the CoverageEligibilityRequest object in the Section
         */
        comp.getSection().add(new Composition.SectionComponent().setTitle("# Eligibility Request").setCode(new CodeableConcept(new Coding().setSystem("https://fhir.loinc.org/CodeSystem/$lookup?system=http://loinc.org&code=10154-3").setCode( "CoverageEligibilityRequest").setDisplay("Coverage Eligibility Request"))).addEntry(new Reference("CoverageEligibilityRequest/dc82673b-8c71-48c2-8a17-16dcb3b035f6")));

        /**
         * CoverageEligibilityRequest Bundle must contain a CoverageEligibilityRequest resource
         * Users can import and explore a CoverageEligibilityRequest example already created and code is available
         * in file "HCXCoverageEligibility.java"
         * We will also be importing other reference profiles used in CoverageEligibilityRequest example already created
         */
        CoverageEligibilityRequest ce = HCXCoverageEligibility.coverageEligibilityRequestExample();
        Organization hos = HCXOrganization.providerOrganizationExample();
        Organization org = HCXOrganization.insurerOrganizationExample();
        Patient pat = HCXPatient.patientExample();
        Coverage cov = HCXCoverage.coverageExample();

        /**
         * Now, we need to add the referenced resources in CoverageEligibilityRequest resource such as Patient, Organizations, Coverage
         * into the CoverageEligibilityRequest object. We can achieve it by using "contained" field in the CoverageEligibilityRequest
         * structure definition. We add need to add the Composition object we crated earlier. We should ensure that
         * composition object is the fist element in "contained" array
         */

        /**
         * We can use the addContainedToResource function from Utils to add the referenced resources in the main resource
         * We have to pass Primary resource as the first argument and then all the referenced resources as an array of DomainResource
         */
        HCXFHIRUtils.addContainedToResource(ce,new DomainResource[]{comp,hos,org,pat,cov});
        System.out.println("main res with contained \n" + p.encodeResourceToString(ce));

        /**
         * We can now convert the CoverageEligibilityRequest object into a CoverageEligibilityRequest bundle using the
         * resourceToBundle
         */
        Bundle bundleTest = HCXFHIRUtils.resourceToBundle(ce, Bundle.BundleType.DOCUMENT);
        //System.out.println("reosurceToBundle \n" + p.encodeResourceToString(bundleTest));

        /**
         * CoverageEligibilityRequest bundle can be converted into a CoverageEligibilityRequest object using the
         * bundleToResource
         */
        DomainResource covRes = HCXFHIRUtils.bundleToResource(bundleTest);
        //System.out.println("bundleToResource \n" + p.encodeResourceToString(covRes));


        /**
         * All the resources in the bundle can be validated using the HCX FHIR validator available in the HCX SDK
         */
        FhirValidator validator = HCXFHIRValidator.getValidator();
        ValidationResult result1 = validator.validateWithResult(bundleTest);
        for (SingleValidationMessage next : result1.getMessages()) {
            System.out.println(next.getSeverity() + " -- " + next.getLocationString() + " -- " + next.getMessage());
        }


        //Creating the payload for action calls
        HCXOutgoingRequest outgoing;
        //Creating empty map to store output
        Map<String,Object> outmap = new HashMap<>();
        {
            try {
                outgoing = new HCXOutgoingRequest();
                outgoing.generate(p.encodeResourceToString(bundleTest), Operations.COVERAGE_ELIGIBILITY_CHECK, "1-29482df3-e875-45ef-a4e9-592b6f565782",outmap);
                System.out.println("generated payload "+ outmap);
            } catch (Exception e) {
                System.out.println("Error in generating outgoing payload");
                throw new RuntimeException(e);
            }
        }




        //Processing an incoming request
        HCXIncomingRequest hcxIncomingRequest;

        {
            try {
                hcxIncomingRequest = new HCXIncomingRequest();
                Map<String, Object> output = new HashMap<>();
                Map<String, Object> abcd = new HashMap<>();
                abcd.put("payload",outmap.get("payload"));
                hcxIncomingRequest.process(JSONUtils.serialize(abcd), Operations.COVERAGE_ELIGIBILITY_CHECK, output);
                System.out.println("output of incoming request " + output);
            } catch (Exception e) {
                System.out.println("Error with processing incoming payload");
                throw new RuntimeException(e);
            }
        }


    }



}
