import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class HCXFHIRUtils {

    //Initializing the FHIR parser
    static IParser p = FhirContext.forR4().newJsonParser().setPrettyPrint(true);

    public static Bundle resourceToBundle(DomainResource res, Bundle.BundleType type, String bundleURL){
        DomainResource resource = res.copy();
        Bundle bundle = new Bundle();
        bundle.setId(UUID.randomUUID().toString());
        Meta meta = new Meta();
        meta.getProfile().add(new CanonicalType(bundleURL));
        meta.setLastUpdated(new Date());
        bundle.setMeta(meta);
        bundle.setIdentifier(new Identifier().setSystem( "https://www.tmh.in/bundle").setValue(UUID.randomUUID().toString()));
        bundle.setType((type));
        bundle.setTimestamp(new Date());
        List<Resource> refResources = resource.getContained();
        for (Resource refResource : refResources) {
            String id = refResource.getId().toString().replace("#","");
            refResource.setId(id);
            bundle.getEntry().add(new Bundle.BundleEntryComponent().setFullUrl(refResource.getResourceType() + "/" + id).setResource(refResource));
        }
        //adding the main resource to the bundle entry
        resource.getContained().clear();
        bundle.getEntry().add(new Bundle.BundleEntryComponent().setFullUrl(resource.getResourceType() + "/" + resource.getId().toString().replace("#","")).setResource(resource));
        return bundle;
    }


    public static DomainResource bundleToResource(Bundle resource){
        Bundle newBundle = resource.copy();
        Bundle.BundleEntryComponent par = newBundle.getEntry().get(1);
        DomainResource dm = (DomainResource) par.getResource();
        dm.addContained(newBundle.getEntry().get(0).getResource());
        for(int i=2; i<newBundle.getEntry().size(); i++){
            dm.addContained(newBundle.getEntry().get(i).getResource());
        }
        return dm;
    }

    public static void addContainedToResource(DomainResource mainResource, DomainResource[] args){
        for(DomainResource item:args){
            mainResource.addContained(item);
        }
    }
}
