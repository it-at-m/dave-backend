package de.muenchen.dave.geodateneai.gen.api;

import de.muenchen.dave.geodateneai.gen.geodaten.ApiClient;

import de.muenchen.dave.geodateneai.gen.model.InformationResponseDto;
import de.muenchen.dave.geodateneai.gen.model.MessstelleDto;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen")
public class MessstelleApi {
    private ApiClient apiClient;

    public MessstelleApi() {
        this(new ApiClient());
    }

    @Autowired
    public MessstelleApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Holt alle relevanten Messstellen.
     * 
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * <p><b>200</b> - Messstellen erfolgreich abgefragt.
     * @return List&lt;MessstelleDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getMessstellenRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "*/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<MessstelleDto> localVarReturnType = new ParameterizedTypeReference<MessstelleDto>() {};
        return apiClient.invokeAPI("/messstelle", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Holt alle relevanten Messstellen.
     * 
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * <p><b>200</b> - Messstellen erfolgreich abgefragt.
     * @return List&lt;MessstelleDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<MessstelleDto> getMessstellen() throws WebClientResponseException {
        ParameterizedTypeReference<MessstelleDto> localVarReturnType = new ParameterizedTypeReference<MessstelleDto>() {};
        return getMessstellenRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Holt alle relevanten Messstellen.
     * 
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * <p><b>200</b> - Messstellen erfolgreich abgefragt.
     * @return ResponseEntity&lt;List&lt;MessstelleDto&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<MessstelleDto>>> getMessstellenWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<MessstelleDto> localVarReturnType = new ParameterizedTypeReference<MessstelleDto>() {};
        return getMessstellenRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Holt alle relevanten Messstellen.
     * 
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * <p><b>200</b> - Messstellen erfolgreich abgefragt.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getMessstellenWithResponseSpec() throws WebClientResponseException {
        return getMessstellenRequestCreation();
    }
}
