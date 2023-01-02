import org.hl7.fhir.r4.model.*;

import java.util.HashMap;
import java.util.Map;

public class HCXPatient {

    public static Patient PatientExample(){

        //Information needed to create coverage eligibility bundle with mandatory fields
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("PAT_Name","Hina Patel");
        inputMap.put("PAT_Gender",Enumerations.AdministrativeGender.FEMALE);
        inputMap.put("PAT_Identifier1_System","http://gicofIndia.com/beneficiaries");
        inputMap.put("PAT_Identifier1_Value","BEN-101");
        inputMap.put("PAT_Identifier2_System","http://abdm.gov.in/patients");
        inputMap.put("PAT_Identifier2_Value", "hinapatel@abdm");
        //making a Patient resource
        Patient pat = new Patient();
        pat.setId("RVH1003");
        Meta metapat = new Meta();
        metapat.getProfile().add(new CanonicalType("https://nrces.in/ndhm/fhir/r4/StructureDefinition/Patient"));
        pat.setMeta(metapat);
        pat.getIdentifier().add(new Identifier().setType(new CodeableConcept(new Coding().setSystem( "http://terminology.hl7.org/CodeSystem/v2-0203").setCode("SN").setDisplay("Subscriber Number"))).setSystem((String) inputMap.get("PAT_Identifier1_System")).setValue((String) inputMap.get("PAT_Identifier1_Value")));
        pat.setGender((Enumerations.AdministrativeGender) inputMap.get("PAT_Gender"));
        pat.getName().add(new HumanName().setText((String) inputMap.get("PAT_Name")));
        return pat;
    }
}
