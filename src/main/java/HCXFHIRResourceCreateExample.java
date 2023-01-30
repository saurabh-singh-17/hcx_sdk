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

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("protocolBasePath", "http://staging-hcx.swasth.app/api/v0.7");
        configMap.put("participantCode", "1-521eaec7-8cb9-4b6c-8b4e-4dba300af6f4");
        configMap.put("authBasePath", "http://a9dd63de91ee94d59847a1225da8b111-273954130.ap-south-1.elb.amazonaws.com:8080/auth/realms/swasth-health-claim-exchange/protocol/openid-connect/token");
        configMap.put("username", "swasth_mock_provider@swasthapp.org");
        configMap.put("password", "Opensaber@123");
        configMap.put("encryptionPrivateKey", certificate);
        configMap.put("igUrl", "https://ig.hcxprotocol.io/v0.7.1");
        HCXIntegrator.init(configMap);

        /**
         * Swasth IG version : 0.7.1
         */

        /**
         * We will now try to create a Coverage Eligibility Request Bundle as per
         * Resource Profile: HCX CoverageEligibility Request Bundle
         * Defining URL:	https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-CoverageEligibilityRequestBundle.html
         * Version:	0.7.1
         * Name:	CovereageEligibilityRequestBundle
         * Title:	Covereage Eligibility Request Bundle
         * Definition:
         * Coverage Eligibility Request constraints on Bundle resource ensuring that a coverage eligibility request resource is present.
         * While creating a bundle resource we need provide a mandatory BundleType. We will be using
         * BundleTYpe as Document as per the resource link shared above. We will be using Bundle.BundleType.COLLECTION
         */

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
         * into the CoverageEligibilityRequest object.
         */

        /**
         * We can now create bundle as per the https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-CoverageEligibilityRequestBundle.html
         * To create the bundle we can use resourceToBundle function.In the below example, we are using "ce", a coverage eligibility resource
         * as the main object and an array of other resources which we need in the bundle
         */
        List<DomainResource> domList = List.of(hos,org,pat,cov);
        Bundle bundleTest = new Bundle();
        try{
            bundleTest = HCXFHIRUtils.resourceToBundle(ce, domList, Bundle.BundleType.COLLECTION, "https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-CoverageEligibilityRequestBundle.html");
            System.out.println("reosurceToBundle \n" + p.encodeResourceToString(bundleTest));
        }catch (Exception e){
            System.out.println("Error message " + e.getMessage());
        }




        /**
         * CoverageEligibilityRequest bundle can be converted into a CoverageEligibilityRequest object using the
         * bundleToResource
         */
        DomainResource covRes = HCXFHIRUtils.getPrimaryResource(bundleTest, "https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-CoverageEligibilityRequest.html");
        System.out.println("getPrimaryResource \n" + p.encodeResourceToString(covRes));


        /**
         * CoverageEligibilityRequest bundle can be converted into a CoverageEligibilityRequest object using the
         * bundleToResource
 */
        List<DomainResource> covRef = HCXFHIRUtils.getReferencedResource(bundleTest);
        System.out.println("getReferencedResource \n" + covRef);


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
        try {
            outgoing = new HCXOutgoingRequest();
            outgoing.generate(p.encodeResourceToString(bundleTest), Operations.COVERAGE_ELIGIBILITY_CHECK, "1-29482df3-e875-45ef-a4e9-592b6f565782",outmap);
            System.out.println("generated payload "+ outmap);
        } catch (Exception e) {
            System.out.println("Error in generating outgoing payload");
            throw new RuntimeException(e);
        }




        //Processing an incoming request
        HCXIncomingRequest hcxIncomingRequest;
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
