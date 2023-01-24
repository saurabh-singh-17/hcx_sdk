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




We will now create a    CoverageEligibilityRequest example below, codes are also available in file HCXCoverageEligibility.java

        /**
        * We will now try to create a Coverage Eligibility Request as per
        * https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-CoverageEligibilityRequest.html
        */

        CoverageEligibilityRequest ce = new CoverageEligibilityRequest();
        ce.setId("dc82673b-8c71-48c2-8a17-16dcb3b035f6");

        /**
         * Meta is not a mandatory field as per the definitions, but we need to include HCX profile links in resource field in meta
         * to ensure that the resource is validated against given HCX FHIR profile. In below case, as we are creating a coverage eligibility
         * request as per https://ig.hcxprotocol.io/v0.7/StructureDefinition-CoverageEligibilityRequest.html so we need to give the
         * same link the Meta resource
         */
        Meta meta = new Meta();
        meta.getProfile().add(new CanonicalType("https://ig.hcxprotocol.io/v0.7/StructureDefinition-CoverageEligibilityRequest.html"));
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

We have now created a CoverageEligibilityRequest object "ce". In order to visualize the object in JSON format we can use the below code

`System.out.println("main res with contained \n" + p.encodeResourceToString(ce));`

CoverageEligibilityRequest Bundle must contain a CoverageEligibilityRequest resource. Users can import and explore a CoverageEligibilityRequest example already created and code is available in file "HCXCoverageEligibility.java. We will also be importing other reference profiles used in CoverageEligibilityRequest example already created
        
        CoverageEligibilityRequest ce = HCXCoverageEligibility.coverageEligibilityRequestExample();
        Organization hos = HCXOrganization.providerOrganizationExample();
        Organization org = HCXOrganization.insurerOrganizationExample();
        Patient pat = HCXPatient.patientExample();
        Coverage cov = HCXCoverage.coverageExample();


We can now convert the CoverageEligibilityRequest object into a CoverageEligibilityRequest bundle using the resourceToBundle

        Bundle bundleTest = HCXFHIRUtils.resourceToBundle(ce, new DomainResource[]{hos,org,pat,cov}, Bundle.BundleType.COLLECTION, "https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-CoverageEligibilityRequestBundle.html");
        System.out.println("reosurceToBundle \n" + p.encodeResourceToString(bundleTest));

CoverageEligibilityRequest bundle can be converted into a CoverageEligibilityRequest object using the bundleToResource. All referenced resource will be in the "contained" field

        DomainResource covRes = HCXFHIRUtils.bundleToResource(bundleTest);
        System.out.println("bundleToResource \n" + p.encodeResourceToString(covRes));