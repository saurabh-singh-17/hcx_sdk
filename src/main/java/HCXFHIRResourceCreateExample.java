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
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Claim;
import org.hl7.fhir.r4.model.Enumeration;

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

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("protocolBasePath", "http://staging-hcx.swasth.app/api/v0.7");
        configMap.put("participantCode", "1-ab535902-cc4d-4300-9e15-56c12cd939c0");
        configMap.put("authBasePath", "http://a9dd63de91ee94d59847a1225da8b111-273954130.ap-south-1.elb.amazonaws.com:8080/auth/realms/swasth-health-claim-exchange/protocol/openid-connect/token");
        configMap.put("username", "sanjit.vimal@narayanahealth.org");
        configMap.put("password", "Opensaber@123");
        configMap.put("encryptionPrivateKey", "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCG+XLPYiCxrZq71IX+w7uoDGxGI7qy7XaDbL3BJE33ju7rjdrP7wsAOWRvM8BIyWuRZZhl9xG+u7l/7OsZAzGoqI7p+32x+r9IJVzboLDajk6tp/NPg1csc7f2M5Bu6rkLEvrKLz3dgy3Q928rMsD3rSmzBLelfKTo+aDXvCOiw1dMWsZZdkEpCTJxH39Nb2K4S59kO/R2GtSU/QMLq65m34XcMZpDtatA1u1S8JdZNNeMCO+NuFKBzIfvXUCQ8jkf7h612+UP1AYhoyCMFpzUZ9b7liQF9TYpX1Myr/tT75WKuRlkFlcALUrtVskL8KA0w6sA0nX5fORVsuVehVeDAgMBAAECggEAX1n1y5/M7PhxqWO3zYTFGzC7hMlU6XZsFOhLHRjio5KsImgyPlbm9J+W3iA3JLR2c17MTKxAMvg3UbIzW5YwDLAXViC+aW90like8mEQzzVdS7ysXG2ytcqCGUHQNStI0hP0a8T39XbodQl31ZKjU9VW8grRGe12Kse+4ukcW6yRVES+CkyO5BQB+vs3voZavodRGsk/YSt00PtIrFPJgkDuyzzcybKJD9zeJk5W3OGVK1z0on+NXKekRti5FBx/uEkT3+knkz7ZlTDNcyexyeiv7zSL/L6tcszV0Fe0g9vJktqnenEyh4BgbqABPzQR++DaCgW5zsFiQuD0hMadoQKBgQC+rekgpBHsPnbjQ2Ptog9cFzGY6LRGXxVcY7hKBtAZOKAKus5RmMi7Uv7aYJgtX2jt6QJMuE90JLEgdO2vxYG5V7H6Tx+HqH7ftCGZq70A9jFBaba04QAp0r4TnD6v/LM+PGVT8FKtggp+o7gZqXYlSVFm6YzI37G08w43t2j2aQKBgQC1Nluxop8w6pmHxabaFXYomNckziBNMML5GjXW6b0xrzlnZo0p0lTuDtUy2xjaRWRYxb/1lu//LIrWqSGtzu+1mdmV2RbOd26PArKw0pYpXhKFu/W7r6n64/iCisoMJGWSRJVK9X3D4AjPaWOtE+jUTBLOk0lqPJP8K6yiCA6ZCwKBgDLtgDaXm7HdfSN1/Fqbzj5qc3TDsmKZQrtKZw5eg3Y5CYXUHwbsJ7DgmfD5m6uCsCPa+CJFl/MNWcGxeUpZFizKn16bg3BYMIrPMao5lGGNX9p4wbPN5J1HDD1wnc2jULxupSGmLm7pLKRmVeWEvWl4C6XQ+ykrlesef82hzwcBAoGBAKGY3v4y4jlSDCXaqadzWhJr8ffdZUrQwB46NGb5vADxnIRMHHh+G8TLL26RmcET/p93gW518oGg7BLvcpw3nOZaU4HgvQjT0qDvrAApW0V6oZPnAQUlarTU1Uk8kV9wma9tP6E/+K5TPCgSeJPg3FFtoZvcFq0JZoKLRACepL3vAoGAMAUHmNHvDI+v0eyQjQxlmeAscuW0KVAQQR3OdwEwTwdFhp9Il7/mslN1DLBddhj6WtVKLXu85RIGY8I2NhMXLFMgl+q+mvKMFmcTLSJb5bJHyMz/foenGA/3Yl50h9dJRFItApGuEJo/30cG+VmYo2rjtEifktX4mDfbgLsNwsI=\n-----END PRIVATE KEY-----");
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
         * We can use the addContainedToResource fuction from Utils to add the referenced resources in the main resource
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
        Map<String,Object> outmap = new HashMap<>();
        {
            try {
                outgoing = new HCXOutgoingRequest();
                //Creating empty map to store output
                String commonFhirPayload = "{ \"resourceType\": \"Bundle\", \"id\": \"d4484cdd-1aae-4d21-a92e-8ef749d6d366\", \"meta\": { \"lastUpdated\": \"2022-02-08T21:49:55.458+05:30\" }, \"identifier\": { \"system\": \"https://www.tmh.in/bundle\", \"value\": \"d4484cdd-1aae-4d21-a92e-8ef749d6d366\" }, \"type\": \"document\", \"timestamp\": \"2022-02-08T21:49:55.458+05:30\", \"entry\": [{ \"fullUrl\": \"Composition/42ff4a07-3e36-402f-a99e-29f16c0c9eee\", \"resource\": { \"resourceType\": \"Composition\", \"id\": \"42ff4a07-3e36-402f-a99e-29f16c0c9eee\", \"identifier\": { \"system\": \"https://www.tmh.in/hcx-documents\", \"value\": \"42ff4a07-3e36-402f-a99e-29f16c0c9eee\" }, \"status\": \"final\", \"type\": { \"coding\": [{ \"system\": \"https://www.hcx.org/document-type\", \"code\": \"HcxCoverageEligibilityRequest\", \"display\": \"Coverage Eligibility Request Doc\" }] }, \"subject\": { \"reference\": \"Patient/RVH1003\" }, \"date\": \"2022-02-08T21:49:55+05:30\", \"author\": [{ \"reference\": \"Organization/Tmh01\" }], \"title\": \"Coverage Eligibility Request\", \"section\": [{ \"title\": \"# Eligibility Request\", \"code\": { \"coding\": [{ \"system\": \"https://fhir.loinc.org/CodeSystem/$lookup?system=http://loinc.org&code=10154-3\", \"code\": \"CoverageEligibilityRequest\", \"display\": \"Coverage Eligibility Request\" }] }, \"entry\": [{ \"reference\": \"CoverageEligibilityRequest/dc82673b-8c71-48c2-8a17-16dcb3b035f6\" }] }] } }, { \"fullUrl\": \"Organization/Tmh01\", \"resource\": { \"resourceType\": \"Organization\", \"id\": \"Tmh01\", \"identifier\": [{ \"system\": \"http://abdm.gov.in/facilities\", \"value\": \"HFR-ID-FOR-TMH\" }, { \"system\": \"http://irdai.gov.in/facilities\", \"value\": \"IRDA-ID-FOR-TMH\" } ], \"name\": \"Tata Memorial Hospital\", \"alias\": [ \"TMH\", \"TMC\" ], \"telecom\": [{ \"system\": \"phone\", \"value\": \"(+91) 022-2417-7000\" }], \"address\": [{ \"line\": [ \"Dr Ernest Borges Rd, Parel East, Parel, Mumbai, Maharashtra 400012\" ], \"city\": \"Mumbai\", \"state\": \"Maharashtra\", \"postalCode\": \"400012\", \"country\": \"INDIA\" }], \"endpoint\": [{ \"reference\": \"https://www.tmc.gov.in/\", \"display\": \"Website\" }] } }, { \"fullUrl\": \"Patient/RVH1003\", \"resource\": { \"resourceType\": \"Patient\", \"id\": \"RVH1003\", \"identifier\": [{ \"type\": { \"coding\": [{ \"system\": \"http://terminology.hl7.org/CodeSystem/v2-0203\", \"code\": \"SN\", \"display\": \"Subscriber Number\" }] }, \"system\": \"http://gicofIndia.com/beneficiaries\", \"value\": \"BEN-101\" }, { \"system\": \"http://abdm.gov.in/patients\", \"value\": \"hinapatel@abdm\" } ], \"name\": [{ \"text\": \"Hina Patel\" }], \"gender\": \"female\" } }, { \"fullUrl\": \"CoverageEligibilityRequest/dc82673b-8c71-48c2-8a17-16dcb3b035f6\", \"resource\": { \"resourceType\": \"CoverageEligibilityRequest\", \"id\": \"dc82673b-8c71-48c2-8a17-16dcb3b035f6\", \"identifier\": [{ \"system\": \"https://www.tmh.in/coverage-eligibility-request\", \"value\": \"dc82673b-8c71-48c2-8a17-16dcb3b035f6\" }], \"status\": \"active\", \"purpose\": [ \"discovery\" ], \"patient\": { \"reference\": \"Patient/RVH1003\" }, \"servicedPeriod\": { \"start\": \"2022-02-07T21:49:56+05:30\", \"end\": \"2022-02-09T21:49:56+05:30\" }, \"created\": \"2022-02-08T21:49:56+05:30\", \"provider\": { \"reference\": \"Organization/Tmh01\" }, \"insurer\": { \"reference\": \"Organization/GICOFINDIA\" }, \"insurance\": [{ \"focal\": true, \"coverage\": { \"reference\": \"Coverage/dadde132-ad64-4d18-8c18-1d52d7e86abc\" } }] } }, { \"fullUrl\": \"Organization/GICOFINDIA\", \"resource\": { \"resourceType\": \"Organization\", \"id\": \"GICOFINDIA\", \"identifier\": [{ \"system\": \"http://irdai.gov.in/insurers\", \"value\": \"112\" }], \"name\": \"General Insurance Corporation of India\" } }, { \"fullUrl\": \"Coverage/dadde132-ad64-4d18-8c18-1d52d7e86abc\", \"resource\": { \"resourceType\": \"Coverage\", \"id\": \"dadde132-ad64-4d18-8c18-1d52d7e86abc\", \"identifier\": [{ \"system\": \"https://www.gicofIndia.in/policies\", \"value\": \"policy-RVH1003\" }], \"status\": \"active\", \"subscriber\": { \"reference\": \"Patient/RVH1003\" }, \"subscriberId\": \"SN-RVH1003\", \"beneficiary\": { \"reference\": \"Patient/RVH1003\" }, \"relationship\": { \"coding\": [{ \"system\": \"http://terminology.hl7.org/CodeSystem/subscriber-relationship\", \"code\": \"self\" }] }, \"payor\": [{ \"reference\": \"Organization/GICOFINDIA\" }] } } ] }";

                outgoing.generate(commonFhirPayload, Operations.COVERAGE_ELIGIBILITY_CHECK, "1-29482df3-e875-45ef-a4e9-592b6f565782",outmap);
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
