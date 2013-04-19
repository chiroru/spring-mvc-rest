package jp.ddo.chiroru.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping("/rest")
public class RestController {

    private static ProfileList profileList;

    static {
        profileList = new ProfileList();
        List<Profile> l = new ArrayList<Profile>();
        l.add(new Profile("name_0", "address_0", "tel_0"));
        l.add(new Profile("name_1", "address_1", "tel_1"));
        l.add(new Profile("name_2", "address_2", "tel_2"));
        profileList.setProfileList(l);
    }

    @RequestMapping("/profile/index")
    public String index() {
        return "rest/profile/index";
    }
    
    @RequestMapping(value = "/profiles", method = RequestMethod.GET, headers = "Accept=application/xml, application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ProfileList findAllForProfile() {
        return profileList;
    }

    @RequestMapping(value = "/profile/{id}", method = RequestMethod.GET, headers = "Accept=application/xml, application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Profile findByIdForProfile(@PathVariable String id) {
        int selectedId = Integer.parseInt(id);
        return profileList.getProfileList().get(selectedId);
    }

    @RequestMapping(value = "/profile", method = RequestMethod.POST, headers = "Accept=application/xml, application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Profile addForProfile(@RequestBody Profile profile) {
        profileList.getProfileList().add(profile);
        return profile;
    }
    
    @RequestMapping(value = "/profile/{id}", method = RequestMethod.PUT, headers = "Accept=application/xml, application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Profile updateForProfile(@PathVariable String id, @RequestBody Profile profile) {
        int selectedId = Integer.parseInt(id);
        profileList.getProfileList().remove(selectedId);
        profileList.getProfileList().add(selectedId, profile);
        return profile;
    }

    @RequestMapping(value = "/profile/{id}", method = RequestMethod.DELETE, headers = "Accept=application/xml, application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Profile deleteProfile(@PathVariable String id) {
        int selectedId = Integer.parseInt(id);
        Profile p = profileList.getProfileList().get(selectedId);
        profileList.getProfileList().remove(selectedId);
        return p;
    }
}
