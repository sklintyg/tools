package se.inera.certificate.spec

import se.inera.certificate.spec.util.RestClientFixture

import static groovyx.net.http.ContentType.JSON

public class TaBortTsIntygStub extends RestClientFixture {

    private String url = System.getProperty("certificate.baseUrl");
    
    public void execute(){
        def restClient = createRestClient("${url}")
        def response = restClient.delete(path: 'ts-certificate-stub/certificates')
        assert response.status == 204
    }
}
