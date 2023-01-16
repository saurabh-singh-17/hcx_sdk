# hcx_sdk

hcx_sdk repository uses hcx-integrator-sdk and hapi fhir sdks to construct examples of different FHIR resources as per 
standards published in IG https://ig.hcxprotocol.io/v0.7.1/index.html. This repository establishes a connection with HCX
using hcx-integrator-sdk and contains examples to send and receive API calls for Swasth HCX instance.

### List of FHIR resource examples available
1. CoverageEligibilityRequest
2. CoverageEligibilityResponse
3. Claim
4. ClaimResponse
5. Coverage
6. Patient
7. Organization
8. Communication
9. CommunicationRequest
10. PaymentReconciliation
11. PaymentNotice




We will now create a Composition example below, codes are also available in file HCXFHIRResourceCreateExample.java

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

We have now created a Composition object "comp". In order to visualize the object in JSON format we can use the below code

`System.out.println("main res with contained \n" + p.encodeResourceToString(ce));`

CoverageEligibilityRequest Bundle must contain a CoverageEligibilityRequest resource. Users can import and explore a CoverageEligibilityRequest example already created and code is available in file "HCXCoverageEligibility.java. We will also be importing other reference profiles used in CoverageEligibilityRequest example already created
        
        CoverageEligibilityRequest ce = HCXCoverageEligibility.coverageEligibilityRequestExample();
        Organization hos = HCXOrganization.providerOrganizationExample();
        Organization org = HCXOrganization.insurerOrganizationExample();
        Patient pat = HCXPatient.patientExample();
        Coverage cov = HCXCoverage.coverageExample();

Now, we need to add the referenced resources in CoverageEligibilityRequest resource such as Patient, Organizations, Coverage into the CoverageEligibilityRequest object. We can achieve it by using "contained" field in the CoverageEligibilityRequest structure definition. We add need to add the Composition object we crated earlier. We should ensure that composition object is the fist element in "contained" array
         

        
We can use the addContainedToResource function from Utils to add the referenced resources in the main resource. We have to pass Primary resource as the first argument and then all the referenced resources as an array of DomainResource
         
        HCXFHIRUtils.addContainedToResource(ce,new DomainResource[]{comp,hos,org,pat,cov});
        System.out.println("main res with contained \n" + p.encodeResourceToString(ce));

We can now convert the CoverageEligibilityRequest object into a CoverageEligibilityRequest bundle using the resourceToBundle

        Bundle bundleTest = HCXFHIRUtils.resourceToBundle(ce, Bundle.BundleType.DOCUMENT);
        System.out.println("reosurceToBundle \n" + p.encodeResourceToString(bundleTest));

CoverageEligibilityRequest bundle can be converted into a CoverageEligibilityRequest object using the bundleToResource

        DomainResource covRes = HCXFHIRUtils.bundleToResource(bundleTest);
        System.out.println("bundleToResource \n" + p.encodeResourceToString(covRes));