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
        String keyUrl = "https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-29/demo-app/server/resources/keys/x509-private-key.pem";
        String certificate = IOUtils.toString(new URL(keyUrl), StandardCharsets.UTF_8.toString());

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
        List<DomainResource> domList = List.of(hos, org, pat, cov);
        Bundle bundleTest = new Bundle();
        try {
            bundleTest = HCXFHIRUtils.resourceToBundle(ce, domList, Bundle.BundleType.COLLECTION, "https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-CoverageEligibilityRequestBundle.html");
            System.out.println("reosurceToBundle \n" + p.encodeResourceToString(bundleTest));
        } catch (Exception e) {
            System.out.println("Error message " + e.getMessage());
        }


        /**
         * CoverageEligibilityRequest bundle can be used to extract the main resource which is CoverageEligibilityRequest
         * in this example using the function getPrimaryResource. In case no URL is provided for the SD, first entry in the bundle will be
         * returned
         */
        DomainResource covRes = HCXFHIRUtils.getPrimaryResource(bundleTest, "https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-CoverageEligibilityRequest.html");
        //System.out.println("getPrimaryResource \n" + p.encodeResourceToString(covRes));


        /**
         * CoverageEligibilityRequest bundle can be used to get all the referenced resources in the main resource from the bundle
         * if present using getReferencedResource. If a URL is passed then, the URL is treated as the main resource and all other
         * resources in the bundle is returned. If no URL is passed then all resources apart from the first entry is returned
         */
        List<DomainResource> covRef = HCXFHIRUtils.getReferencedResource(bundleTest);
        //System.out.println("getReferencedResource \n" + covRef);


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
        Map<String, Object> outmap = new HashMap<>();
        String bundle;
        try {
            outgoing = new HCXOutgoingRequest();
            //bundle = "{\n  \"resourceType\": \"Bundle\",\n  \"id\": \"d4484cdd-1aae-4d21-a92e-8ef749d6d366\",\n  \"meta\": {\n    \"lastUpdated\": \"2022-02-08T21:49:55.458+05:30\"\n  },\n  \"identifier\": {\n    \"system\": \"https://www.tmh.in/bundle\",\n    \"value\": \"d4484cdd-1aae-4d21-a92e-8ef749d6d366\"\n  },\n  \"type\": \"document\",\n  \"timestamp\": \"2022-02-08T21:49:55.458+05:30\",\n  \"entry\": [{\n    \"fullUrl\": \"Composition/42ff4a07-3e36-402f-a99e-29f16c0c9eee\",\n    \"resource\": {\n      \"resourceType\": \"Composition\",\n      \"id\": \"42ff4a07-3e36-402f-a99e-29f16c0c9eee\",\n      \"identifier\": {\n        \"system\": \"https://www.tmh.in/hcx-documents\",\n        \"value\": \"42ff4a07-3e36-402f-a99e-29f16c0c9eee\"\n      },\n      \"status\": \"final\",\n      \"type\": {\n        \"coding\": [{\n          \"system\": \"https://www.hcx.org/document-type\",\n          \"code\": \"HcxCoverageEligibilityRequest\",\n          \"display\": \"Coverage Eligibility Request Doc\"\n        }]\n      },\n      \"subject\": {\n        \"reference\": \"Patient/RVH1003\"\n      },\n      \"date\": \"2022-02-08T21:49:55+05:30\",\n      \"author\": [{\n        \"reference\": \"Organization/Tmh01\"\n      }],\n      \"title\": \"Coverage Eligibility Request\",\n      \"section\": [{\n        \"title\": \"# Eligibility Request\",\n        \"code\": {\n          \"coding\": [{\n            \"system\": \"https://fhir.loinc.org/CodeSystem/$lookup?system=http://loinc.org&code=10154-3\",\n            \"code\": \"CoverageEligibilityRequest\",\n            \"display\": \"Coverage Eligibility Request\"\n          }]\n        },\n        \"entry\": [{\n          \"reference\": \"CoverageEligibilityRequest/dc82673b-8c71-48c2-8a17-16dcb3b035f6\"\n        }]\n      }]\n    }\n  },\n    {\n      \"fullUrl\": \"Organization/Tmh01\",\n      \"resource\": {\n        \"resourceType\": \"Organization\",\n        \"id\": \"Tmh01\",\n        \"identifier\": [{\n          \"system\": \"http://abdm.gov.in/facilities\",\n          \"value\": \"HFR-ID-FOR-TMH\"\n        },\n          {\n            \"system\": \"http://irdai.gov.in/facilities\",\n            \"value\": \"IRDA-ID-FOR-TMH\"\n          }\n        ],\n        \"name\": \"Tata Memorial Hospital\",\n        \"alias\": [\n          \"TMH\",\n          \"TMC\"\n        ],\n        \"telecom\": [{\n          \"system\": \"phone\",\n          \"value\": \"(+91) 022-2417-7000\"\n        }],\n        \"address\": [{\n          \"line\": [\n            \"Dr Ernest Borges Rd, Parel East, Parel, Mumbai, Maharashtra 400012\"\n          ],\n          \"city\": \"Mumbai\",\n          \"state\": \"Maharashtra\",\n          \"postalCode\": \"400012\",\n          \"country\": \"INDIA\"\n        }],\n        \"endpoint\": [{\n          \"reference\": \"https://www.tmc.gov.in/\",\n          \"display\": \"Website\"\n        }]\n      }\n    },\n    {\n      \"fullUrl\": \"Patient/RVH1003\",\n      \"resource\": {\n        \"resourceType\": \"Patient\",\n        \"id\": \"RVH1003\",\n        \"identifier\": [{\n          \"type\": {\n            \"coding\": [{\n              \"system\": \"http://terminology.hl7.org/CodeSystem/v2-0203\",\n              \"code\": \"SN\",\n              \"display\": \"Subscriber Number\"\n            }]\n          },\n          \"system\": \"http://gicofIndia.com/beneficiaries\",\n          \"value\": \"BEN-101\"\n        },\n          {\n            \"system\": \"http://abdm.gov.in/patients\",\n            \"value\": \"hinapatel@abdm\"\n          }\n        ],\n        \"name\": [{\n          \"text\": \"Hina Patel\"\n        }],\n        \"gender\": \"female\"\n      }\n    },\n    {\n      \"fullUrl\": \"CoverageEligibilityRequest/dc82673b-8c71-48c2-8a17-16dcb3b035f6\",\n      \"resource\": {\n        \"resourceType\": \"CoverageEligibilityRequest\",\n        \"id\": \"dc82673b-8c71-48c2-8a17-16dcb3b035f6\",\n        \"identifier\": [{\n          \"system\": \"https://www.tmh.in/coverage-eligibility-request\",\n          \"value\": \"dc82673b-8c71-48c2-8a17-16dcb3b035f6\"\n        }],\n        \"status\": \"active\",\n        \"purpose\": [\n          \"discovery\"\n        ],\n        \"patient\": {\n          \"reference\": \"Patient/RVH1003\"\n        },\n        \"servicedPeriod\": {\n          \"start\": \"2022-02-07T21:49:56+05:30\",\n          \"end\": \"2022-02-09T21:49:56+05:30\"\n        },\n        \"created\": \"2022-02-08T21:49:56+05:30\",\n        \"provider\": {\n          \"reference\": \"Organization/Tmh01\"\n        },\n        \"insurer\": {\n          \"reference\": \"Organization/GICOFINDIA\"\n        },\n        \"insurance\": [{\n          \"focal\": true,\n          \"coverage\": {\n            \"reference\": \"Coverage/dadde132-ad64-4d18-8c18-1d52d7e86abc\"\n          }\n        }]\n      }\n    },\n    {\n      \"fullUrl\": \"Organization/GICOFINDIA\",\n      \"resource\": {\n        \"resourceType\": \"Organization\",\n        \"id\": \"GICOFINDIA\",\n        \"identifier\": [{\n          \"system\": \"http://irdai.gov.in/insurers\",\n          \"value\": \"112\"\n        }],\n        \"name\": \"General Insurance Corporation of India\"\n      }\n    },\n    {\n      \"fullUrl\": \"Coverage/dadde132-ad64-4d18-8c18-1d52d7e86abc\",\n      \"resource\": {\n        \"resourceType\": \"Coverage\",\n        \"id\": \"dadde132-ad64-4d18-8c18-1d52d7e86abc\",\n        \"identifier\": [{\n          \"system\": \"https://www.gicofIndia.in/policies\",\n          \"value\": \"policy-RVH1003\"\n        }],\n        \"status\": \"active\",\n        \"subscriber\": {\n          \"reference\": \"Patient/RVH1003\"\n        },\n        \"subscriberId\": \"SN-RVH1003\",\n        \"beneficiary\": {\n          \"reference\": \"Patient/RVH1003\"\n        },\n        \"relationship\": {\n          \"coding\": [{\n            \"system\": \"http://terminology.hl7.org/CodeSystem/subscriber-relationship\",\n            \"code\": \"self\"\n          }]\n        },\n        \"payor\": [{\n          \"reference\": \"Organization/GICOFINDIA\"\n        }]\n      }\n    }\n  ]\n}";
            outgoing.generate(p.encodeResourceToString(bundleTest), Operations.COVERAGE_ELIGIBILITY_CHECK, "1-29482df3-e875-45ef-a4e9-592b6f565782", outmap);
            System.out.println("generated payload " + outmap);
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
            abcd.put("payload", outmap.get("payload"));
            hcxIncomingRequest.process(JSONUtils.serialize(abcd), Operations.COVERAGE_ELIGIBILITY_CHECK, output);
            System.out.println("output of incoming request " + output);
        } catch (Exception e) {
            System.out.println("Error with processing incoming payload");
            throw new RuntimeException(e);
        }

        //Processing incoming on action request from mock server

        //Testing all other bundles and resouces
        Map<String, String> new1 = new HashMap<>();
        Map<String, Object> out1 = new HashMap<>();
        new1.put("payload","eyJ4LWhjeC1yZWNpcGllbnRfY29kZSI6IjEtNTIxZWFlYzctOGNiOS00YjZjLThiNGUtNGRiYTMwMGFmNmY0IiwieC1oY3gtdGltZXN0YW1wIjoiMjAyMy0wMi0xNlQxODo0MTowMS4wMzArMDU6MzAiLCJ4LWhjeC1zZW5kZXJfY29kZSI6IjEtMjk0ODJkZjMtZTg3NS00NWVmLWE0ZTktNTkyYjZmNTY1NzgyIiwieC1oY3gtY29ycmVsYXRpb25faWQiOiI5NTQ2YWVjZC1lNzBhLTQ4YTItODM2My04NDFmZDRiNTIzZWUiLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiUlNBLU9BRVAtMjU2IiwieC1oY3gtYXBpX2NhbGxfaWQiOiI4MWU4NGM4Ny05MGNiLTRjN2ItODVjYy01ZWVhYjc3NjBiYTQiLCJ4LWhjeC1zdGF0dXMiOiJyZXNwb25zZS5jb21wbGV0ZSJ9.ePERO-redwxOr_EFKy-465t45wOYDqiHUdfUJ3gmegqT-ecmmXpe6k7UVvQ_Nu2I1QLuw6j8fPLDkDDuCDzH50Sl15cpbbkyHRBeVCfcSwjEDPqOn7M9wHwvLSLwvRlI3FprIhfd771CtDaAom1o2_1LdWROi6qn7yzEc32e3iRuPq7NC0vp_g7Yp_izgTn4EwD1jMJhBcIutQdMU27B3sM9HwubWlilxWwcnnbctDgNdsj5p_vSnyUym5XkqvADaHmgg5XOwO500FXg-tz3lbehX3u66eOzzn4PmQct_88Mq3eV8l35jx3BLzSZQ5UadBO53sq_UZDeVo6BmZ75SA.yLyBCxwkg2Qbj6zi.WN00hb0B1oFBrmjs_ZmrmaFD_gTnJ66vQ068pw5Rrkft6PD6NmYRmsofOKb42Mxhc30gfryAvquo3AWg4SgG6zkEtiCgRW7i_DhO5WldYByqgj0X5EwSxRlu90Zf0TSTrG8JBre0e0e_uTF6B47AGa2lhabHthERiqZu9BRY8iKqtArvjbAlDWxvn0WvYE9p5EXFbEr4eSS_FKqG23VcGIwGbSFmD9FJBsqkrRStVhOARtAbG-1O-nrDoC28QL6cHrEsDpvYggzSoPLLinefvBv98bpOSnPv0vhyYQm31kkDhfM492U-ZvtmuUEWWPVBuammLLMq6g1spRLT9TL0xAtia4XPbmCGMn734Ul1-9q73Tyepff2zU0nb-6fzMJFxLBLwzR9gq3wjsiyMm5IEiDEw-UYUScI78oAMXuKzC31Gl7N9uwOFZVhxjmGhBXn2PWAv6xEeFgSN4wWMccdGqET8wwDAVrjHCaVvIy0A2mKI5lMPMnfsQXKO8fYgRY9f_I0Xv88Da_jxz-8DPxkVRorfj1gG74miEBXLwM_GQZLZ4FCq8C2FMAH5Mb3TlQ6HlkhpJaMzq6V7Hch43PBjCX8IHlNShmJHCBcV9Bf8dBbA4gxSACKv7Hxfu-bF4-7ZOmfTPUVaweEnZIU_y-LcLL-SSA2b9TOpq3CJ3tKsbf1YeU-k8EwuKMYD76JJGjkHSR5gRvQaJ95hAb4CAF3TLTnnS5R8S_lqLOUYCbVavpwaFnIgJRatovpMmOqPeYB3QSCX6q2wGBzJX_dllhpD8WYjEVlGbMkwYy7GhemvTS6OFnVnK0MNrD-Htr2hZMSQxzh5ZibkNr51-55H335qnlbMy-CPnaByoPFrBBG1QQrVndDNCzYo8P6LslP4je548axqkieZAE9dFvce73lwVW-n5nt6qkg4loD-nr9vHet6ESFEZ7017TCFlATDHgXpvo-517iTsvWbZiAoHc7Tn1OXQBjbUIq5gwObUqB8O6wHQ-OgcYXR0V-7JwLnDYMOv_GXn6fBbm2ao6xOaW5fvEz8IfJFreeVg6CJwxvfaNWXMQdTZvECoM3GlZ4_XqPH9bLflYGeX9wyfM9RJVWMZkTQToGI0QoVpWutOn1b8NeSvVYuPGNTcIyU3rqeqSE3KFvJAXuXUwdk2LN-2LQFLcP9s1RVOjq0LOjMOjpye-K3g6vIDt9rvuCFUQJC5XFanagtRtOKyz1Ei0f56zwpBAOMj_0OntIEkc9agoTu5TWs66yw_AQ7lvg-7dUIPOTyO_XKDri8zW8Kw2rgXkMRsS8-Q2ON5Kp8KSfOgSEQ7M1V6Yvng0RbdY_I73UAqR3FmVZm0AcWcz7PkCR3xwxv2vTRCEUneqyTEyUvagO2-xkCJAU2uz-_6lCJ3uCpHOkmS6kNjTBCfsXynqvvLaC_pLKVY_v-oKd7d_5ypZ4OPYprCtFUjBTfRiC-2YXA0zsoMkTg-mE4wxQnP4lkj94mSQ9HQbbnHukrKEu8aDd3QbhuksRQHaI7PemEVHchNGbukbsQX0mq5odb8tmGOs25EuYfMVc_GRPIbxPAlQSGFjg8RwD6mgoni3HrIIMtmAjaAaZR9OKp-oL02-irKyAQUNomZYPVXLQWByIrn4IQTINLMQkgOWjGcNAqR_rgHK4VyQo4T5_LQb-cbQP66do2wZnI6VaMgOrnqPdwAdDtZeywGRZ7_U6tThTngYL5c4I5yvPEZyQPqJX3RQpH4VLbh_vJqs7ZK9n2iddc7f965ERC92WI5p1RCEib88Zb7XcVIS5tJXGmUFHmJaBFYxDyyn7iO8bYgxCdpUJaO0sgFCX13dzKdOBOfaOGBBCnMGMBbEOtc6PFeX8o3snZBp9axEGerZDsps8DtwE2vBKAt8DCkSLeSRFh5dhbAWzSN4XQHivqj6zbusQ-rV-7sq6me0wniaoTkgGY3UJmxSdkfU6Nia_iNrOLuFonL_lK0TBR8Aujm835udy1Jm8WXljNkKriKFj0abQ2uWSXYrjX_JS0rx9wVKNxKHYw_mEEJFzKudS8pVvigyirUXVT7q3-RfhjjbUxeLz8fxF9JNvqAkb9bJU1U0ooTZTwXKJ2uhoW0Knw9jkUEoOTdnqv_xkvbNMMO7dMt1LbKSWf6Zc6Ws7pFcF-zLbVBstO5kfVKpdipkxQx1zFvoBAQ58IpAY7qo71NnWMhSIUGlzRJ3Vyd_Cly36E3Gb-qjG2IIw5Oxp9OIbXP_UgoBcpxlzZj5qKa4TF9nD4Mk5ZrCHcc2G97eu9T-b7vNAv7laAHxlQs7__gNoHpltHcUsW5sXy8WwG32vzyz_TD56A410UTprFN9y3NVCRFqpgsvyXkA8OWW3HyKnOb0fgPCS4EX8f0F0Ww9WywtjeiURB1dLwLZuVkN8KVt4V2Qp_e4FLCGV8o8y1f3Y7fLaI9YGDE9DGp74KkEztmdkwP4f6sF478mOSzdZwunvoLsfYBbTuj94n24DNTbrVG_MPJRJClooBPRxcwop4etYDv8Oxby2MsCeIGEDwdmAnIMlS-oamooC_dwhF9zMsja1-O6UsnFFUS6uUsDlZX-uBvyHIPe5CPcMmcTsexyP4atX5H5qE-4NIbvFJwUx2Xn0k-qf8a5VWIHvjeYq8ncpSBgWJIT0IRxA6s521HuJpZOvRGQUouDTX7uPJBfysLu26PqkOTEylSy9GA4epyb5Ny3H2JSu-kqMdMDX5bYHFvk7oCnZ_9MpQHnVxj-ky2j6mA5s0Uq96A8IyddvsAqDRY7KjgZNKzkAkajr3eaHbTXX8_wBpnfiRgvL0dDoChzqLWigYkTGyXNfLTfZr9RsGr8o3wQ8YqJ6TzzOUkIcKsJv4pKAMvKnLPOnVYDzetpCRQrVt1EyhLPjKbNMo9Vgt8CXQ5fiJ_czlHLFpZb-iOVE3XaYP-8UwhgUrpNzDJtlHjJzfHr5poVgSauUIVYzXhhYT15c4E-kiGz5PpHdrtCx4KFb_sOgP9vXVcTJ2tS7-Mbc0fufTsHyjZ8_D8De4_Jp8DuiUNNBM9fQQVYFV9lxfci0anEImccX3htc6pSGUsHajYCqnBoafMbMDKrj2qHJs0K00QgKKLUt8VQIzsBu2EEfRMwGRSTwG_PR10g6R7emksRG36dCkNk3-bRV2Zwn5gtstS752pJ1JkFyYV0YuJFMjSwQ4FOrudTSIKg83BDxNX-UDhEk87dL_gaCJlQ587P0U2uNrlQ7tBljUb9gCY2B9pW9qHWFPFEvFee6ImiRfZtjPlE4tpkeYENO2dH_ydjqLHgOAg8UzoxMLxLQyOU1dv1jcbRBeU4a1ZlL-o48SmbCIA_rfnk9zzkTkNtF2yWsMqYSc-KJHor7lzfWxLuoao3Hw7WZKoCYbnua-sXkE49ZQKIUJO4XqqvBhLuC6MM_qkd_KDgryHYLB5_ukSqQshQbtlx1ZuVhuSlOcqc9alBCfbA3_lFOij__5SA3EbhdOHKKFIy89j5Gl-LQKxkf8FJvvtb5Asp-YPKrB5FETk6pUmK1ypNnRNlV9GUjC9DThDbtRqSnaY7g3BO8talml2EoQj5WKDLRqXQVlbNVqSAtGAaY4NADdtF_W-M2eXEXo41FfIlY490BGPwOu4DqTIsGyBBSLAsKhjOD4Kd7_7vqL4QCQBW9U1WDY4gsrnoCKKC5gKNIuAHWII-h8lYjeYpnaOWPaNCeAYBzR682b0yCftGS4FsA_MTOgWat-fz3Edm86iFlkU43mEgdLOVyBqiQxcGxpYuMp6KHVV4XDHHAU7e7GDFZ7NCcjcIQBcNSfGWdNQQY3iGgPo30OCHO2i4DpU80zLDyjo2DIk0zLgrPqY3WHAMigDKDrvv8tMHwvsVBVAqGfLs37pIGGTK53eUiTZUfZtMaCkTDgstnKZxEVZZQ4LMY1d0LoI2_cRAJh7ZC7RIO3Anhrdwk53hd50OOYkZRepmGIqg-hr0wQcgq0VddjGbTQSIh4DNzC4D4FT16s8kZ-2unBkjtx9yE2amzLIgngI8H8DMmzN3q2cxBkg2mSxqU94nElUolRDXYGNt7lUFRdTbNHHiX5C8CbBRs3ypOtpjg4a6RP9eKK-VvJPideAAZL_OjVNQ2FcaUCc1pGMCJHTHZvm4fkbtdLboslQyL0M5tBd_fLiwAq-nDBQFnO57y0qVaJhp1CsxBQh_qZjJ7ureh8kIBaZqvT2AzaxeJMj4H1H1NVJoIy5lvl4oF1YIokgQsFjMkjQeXNXijP0ViwzwwLljxhKz_8R_1LDRQgyVpv8MsVOnzsAUPeLEZxHtHu7GtsgjP0FNYJGjYFRTdB-DROIiDLNdNJUeSnNUUqOacfYcTopLRmD7-tnzSrCGBNhgOsy4zkV-EGZrYUcqXoKWzxCqJYozMTd2RCIJ_kLx-JKFvwgI6UzlWl7cDiclRUcesiKhfSLsirEM9pJTu2wXn6T6QD6GslPdLlPByQEqWxx-8fX1vVlL4mNM_pz5IsnNrvycO8AcyqDrJ2Kp__UOSrHnZuWWFigsW79BEOB6nCr21lArNFOWiOkabrGsXYaBJGrbhN9Wb280lGdNVmlsoR4GFduINxUXmt6QC6v9X2zm8dsgeHjkaGLyFpV10N6vge_Pj_AL0mUvj87BBz6-fV_O_NaFIo37eEHngHoNnhbKRvbpvB1teX8DYG_d2Y6zhxtGExHxmgJlmZ5OUrsEo_kKoXEIRLyxDsOipXr4zjp8az3k2bwhB75Vn2mqLcrR63tRe6NYDOSUh3JMWfmc4MIsiwiM_6fW4aeidQpvWQA6lHCYzEN3pJZAZTe7JVJw53vqsvP987nCVtZZCqI4eXsQuhOellIsun7Y5BnofJvceF_phNtHWs-51yzgM8DkadmFOM12FxiVTlFZXgb5j9SXF2KpwqXptYgplC0v9A7G3SgRQ_yE_Dc5WtawJgKnG6MOqvTdlV06RuBsntQhoyQDPBmCLhmkuy8nRgra5cXiYWjEB1JX-tHLRRXWGUYvpOL0hEXk_fkEuHm3PhShuKbuR2mNV9L6hEvlJxSolgth6TqtmNNRNfwU1xgMgMHXyD9WKvSsemnZqPo8fnEpPkuWLxWjNvOBJ7bI5RbEtEEzIBaDVZATuZjy9-SX0ZB2Xg1sE6sdc4KgFuDJ03gBZgms6yyN8zYLwnzEJb4y0ZfsDOuTS3L9uRkQ8j0_ENVycfos8_TE75WeMVX4phLQfXZjunI7G0LUGeU01GKSdLVU3f4GVWz2BVMNL2AOrmfUKtRJyriTulMXJGmFTm60Iumzkjo2lP-nvBmCvhBeyhcRfOgkXX8Wa9KsnnrZyHDFUTcFotZcmyvqa-NnlJ3vBspbKl1ZqNE581HjXNSF2tQaXWjwnCWTGeGsz6Atbyev6Y324MqepjZ25ROzI1zgEQrRUEqgGC_YXBFcUQJYertu5Pg6bZ9bkgHyYtaVcmD5mRmYkqNuNrDSzrlbQweVbs4vwVkOSFGbAPf4Xcm0Cc8gaH7rGSLVDWP-PlV8wTl6irMDhiMQ88q8rWyLuIn85OIE7Sbl5UeQKSOo1nBfSOqwi5Yvjxz4wcSbSE4qImI1r6f2M41xBmaX1Yhw2ZWL7OvcT_gsKjrE8QkYCPn9FzjKzacqMmh-ppi30sbDKBW09eMO3o7fVq1Lrz2zhiJKos64fbBClfCxyx3Tg4oOIv7s1cc7f3lh4SMbY9FP22JMiQ1tJrrkKbUkLVZeqrT9rX4oztliMEhr2iDAZJ6crhihm0PzEhjf7OSD_xeGBenp2YLg2t0rrm_EbG-0B2ujZi6YVah66Oa-x6wDM84HCeT4rJz7vN9LObyHn4a7PEoEdwkTvWr5SYKnkJjiYjgNhIqFvpFAyB2b7eJKYrf6j0xa-XCoZs8cu04Gd2CJQYiL5pbKKlXfcVrh5N-jya3fuJguGp7jfryR7WSEiEgoP77PgKYHvboGH4EsRpp_-Zf-NPae436okupK9uCK_G7Euid1WZ56ZRQ86X3Ybe6X_jZiPoiZTUVmeRbaGVV1QdrNy6bTfomhzw_z3B0OSMakUFzfgc-Ppji-wpJWdXDJz-AoabnUjJrpk1aNudHs8XvpwL-MAQoevBQmwyJyB_GLs5vtROrzHXtursspqOWoRUjrVITSKh1AxLui5af44W5mczVqUFOllxVC5TZACwAzDegP9C-nk9HZmsOCGZnqyP5a1Ac1P_wsWVvvZQqNoB6xJi9-B_I0UDfY4ReovmU3ameXIx-vMwFceZs2DWqC3SBdACn1dvySVRJ1cDb71A-xN_2ezfX0NBB0Dv0M0SrEtvuC4My2zMM8hgauBLRPAI1HIALaaGKaWJu9WqJUJGg6oKHAzMVxam2cpqdeu8R5flhAUlYoHUvPWk_g5gjwWTg9XoZzrHLF7SuaypAedo-LVGbHqlA.XXtrS-7dGS7nLTQWuCyrWQ");
        hcxIncomingRequest.process(JSONUtils.serialize(new1), Operations.COVERAGE_ELIGIBILITY_ON_CHECK, out1);
        System.out.println("output of incoming request " + JSONUtils.serialize(out1.get("responseObj")));


        ValidationResult covResVal;
        Bundle bundleCovRes = new Bundle();

        //Coverage eligibility response
        CoverageEligibilityResponse covRes1 = HCXCoverageEligibility.coverageEligibilityResponseExample();
        System.out.println("New validation CE response \n");
        bundleCovRes = HCXFHIRUtils.resourceToBundle(covRes1, domList, Bundle.BundleType.COLLECTION, "https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-CoverageEligibilityResponseBundle.html");
        covResVal = validator.validateWithResult(bundleCovRes);
        for (SingleValidationMessage next : covResVal.getMessages()) {
            System.out.println(next.getSeverity() + " -- " + next.getLocationString() + " -- " + next.getMessage());
        }
        System.out.println("Coverage Eligibility Response Example \n" +  p.encodeResourceToString(bundleCovRes) + "\n");


        //Claim

        Claim claim = HCXClaim.claimExample();
        System.out.println("New validation Claim \n");
        bundleCovRes = HCXFHIRUtils.resourceToBundle(claim, domList, Bundle.BundleType.COLLECTION, "https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-ClaimRequestBundle.html");
        covResVal = validator.validateWithResult(bundleCovRes);
        for (SingleValidationMessage next : covResVal.getMessages()) {
            System.out.println(next.getSeverity() + " -- " + next.getLocationString() + " -- " + next.getMessage());
        }
        System.out.println("Claim Request Example \n" +  p.encodeResourceToString(bundleCovRes) + "\n");
        outgoing.generate(p.encodeResourceToString(bundleCovRes), Operations.CLAIM_SUBMIT, "1-29482df3-e875-45ef-a4e9-592b6f565782", outmap);
        System.out.println("generated payload " + outmap);

        ClaimResponse claimRes = HCXClaim.claimResponseExample();
        System.out.println("New validation Claim Response \n");
        bundleCovRes = HCXFHIRUtils.resourceToBundle(claimRes, domList, Bundle.BundleType.COLLECTION, "https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-ClaimResponseBundle.html");
        covResVal = validator.validateWithResult(bundleCovRes);
        for (SingleValidationMessage next : covResVal.getMessages()) {
            System.out.println(next.getSeverity() + " -- " + next.getLocationString() + " -- " + next.getMessage());
        }
        System.out.println("Claim Response Example \n" +  p.encodeResourceToString(bundleCovRes) + "\n");

        Communication comm = HCXCommunicationRequest.communicationExample();
        System.out.println("New validation comm bundle \n");
        bundleCovRes = HCXFHIRUtils.resourceToBundle(comm, domList, Bundle.BundleType.COLLECTION, "https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-CommunicationBundle.html");
        covResVal = validator.validateWithResult(bundleCovRes);
        for (SingleValidationMessage next : covResVal.getMessages()) {
            System.out.println(next.getSeverity() + " -- " + next.getLocationString() + " -- " + next.getMessage());
        }
        outgoing.generate(p.encodeResourceToString(comm), Operations.COMMUNICATION_ON_REQUEST, "1-29482df3-e875-45ef-a4e9-592b6f565782", outmap);
        System.out.println("generated payload " + outmap);

        CommunicationRequest commReq = HCXCommunicationRequest.communicationRequestExample();
        System.out.println("New validation commRequest \n");
        covResVal = validator.validateWithResult(commReq);
        for (SingleValidationMessage next : covResVal.getMessages()) {
            System.out.println(next.getSeverity() + " -- " + next.getLocationString() + " -- " + next.getMessage());
        }


        PaymentNotice pay = HCXPayment.paymentNoticeExample();
        System.out.println("New validation Payment \n");
        covResVal = validator.validateWithResult(pay);
        for (SingleValidationMessage next : covResVal.getMessages()) {
            System.out.println(next.getSeverity() + " -- " + next.getLocationString() + " -- " + next.getMessage());
        }

        PaymentReconciliation payre = HCXPayment.paymentReconciliationExample();
        System.out.println("New validation Payment \n");
        covResVal = validator.validateWithResult(payre);
        for (SingleValidationMessage next : covResVal.getMessages()) {
            System.out.println(next.getSeverity() + " -- " + next.getLocationString() + " -- " + next.getMessage());
        }
        outgoing.generate(p.encodeResourceToString(payre), Operations.PAYMENT_NOTICE_ON_REQUEST, "1-29482df3-e875-45ef-a4e9-592b6f565782", outmap);
        System.out.println("generated payload " + outmap);



    }





}
