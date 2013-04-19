package jp.ddo.chiroru.reset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.ddo.chiroru.rest.Profile;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class RestControllerIntegrationTest {

    private RestTemplate restTemplate;

    public void doIt() {
        HttpHeaders requestHeaders = new HttpHeaders();
        List<MediaType> l = new ArrayList<MediaType>();
        l.add(MediaType.APPLICATION_XML);
        requestHeaders.setAccept(l);
/*        Map<String, String> m = new HashMap<String, String>();
        m.put("content-type", MediaType.APPLICATION_XML_VALUE);
        m.put("Accept", MediaType.APPLICATION_XML_VALUE);
        requestHeaders.setAll(m);*/
        requestHeaders.setContentType(MediaType.APPLICATION_XML);
        HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);
        System.out.println(requestEntity.toString());
        restTemplate = new RestTemplate();
        ResponseEntity<String> result = restTemplate.exchange("http://127.0.0.1:8888/springmvc_showcase/rest/profiles", HttpMethod.GET, requestEntity, String.class);
        System.out.println(result.getBody());
        
        ResponseEntity<String> result2 = restTemplate.exchange("http://127.0.0.1:8888/springmvc_showcase/rest/profile/1", HttpMethod.GET, requestEntity, String.class);
        System.out.println(result2.getBody());
        
        Profile addedProfile = new Profile("add_name", "add_address", "add_tel");
        HttpEntity<Profile> requestEntity2 = new HttpEntity<Profile>(addedProfile, requestHeaders);
        ResponseEntity<String> result3 = restTemplate.exchange("http://127.0.0.1:8888/springmvc_showcase/rest/profile", HttpMethod.POST, requestEntity2, String.class);
        System.out.println(result3.getBody());
        
        ResponseEntity<String> result4 = restTemplate.exchange("http://127.0.0.1:8888/springmvc_showcase/rest/profiles", HttpMethod.GET, requestEntity, String.class);
        System.out.println(result4.getBody());
        
        Profile updatedProfile = new Profile("updated_name", "updated_address", "updated_tel");
        HttpEntity<Profile> requestEntity3 = new HttpEntity<Profile>(updatedProfile, requestHeaders);
        ResponseEntity<String> result5 = restTemplate.exchange("http://127.0.0.1:8888/springmvc_showcase/rest/profile/1", HttpMethod.PUT, requestEntity3, String.class);
        System.out.println(result5.getBody());
        
        ResponseEntity<String> result6 = restTemplate.exchange("http://127.0.0.1:8888/springmvc_showcase/rest/profiles", HttpMethod.GET, requestEntity, String.class);
        System.out.println(result6.getBody());
        
        ResponseEntity<String> result7 = restTemplate.exchange("http://127.0.0.1:8888/springmvc_showcase/rest/profile/1", HttpMethod.DELETE, requestEntity, String.class);
        System.out.println(result7.getBody());
        
        ResponseEntity<String> result8 = restTemplate.exchange("http://127.0.0.1:8888/springmvc_showcase/rest/profiles", HttpMethod.GET, requestEntity, String.class);
        System.out.println(result8.getBody());
    }

    public void sendJsonRequest() {
        HttpHeaders requestHeaders = new HttpHeaders();
        List<MediaType> l = new ArrayList<MediaType>();
        l.add(MediaType.APPLICATION_JSON);
        requestHeaders.setAccept(l);
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);
        ResponseEntity<String> result = restTemplate.exchange("http://127.0.0.1:8888/springmvc_showcase/rest/profiles", HttpMethod.GET, requestEntity, String.class);
        System.out.println(result.getBody());
    }

    public static void main(String[] args) {
        RestControllerIntegrationTest c = new RestControllerIntegrationTest();
        c.doIt();
        c.sendJsonRequest();
    }
}
