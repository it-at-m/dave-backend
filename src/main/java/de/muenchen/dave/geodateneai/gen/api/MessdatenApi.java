package de.muenchen.dave.geodateneai.gen.api;

import de.muenchen.dave.geodateneai.gen.geodaten.ApiClient;
import de.muenchen.dave.geodateneai.gen.model.GetMessdatenRequest;
import de.muenchen.dave.geodateneai.gen.model.MessdatenDto;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen")
public class MessdatenApi {
    private ApiClient apiClient;

    public MessdatenApi() {
        this(new ApiClient());
    }

    @Autowired
    public MessdatenApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Holt die Messdaten zur einer Zählstelle in einem bestimmten Zeitraum.
     *
     * <p>
     * <b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * <p>
     * <b>200</b> - Messdaten erfolgreich abgefragt.
     *
     * @param getMessdatenRequest The getMessdatenRequest parameter
     * @return List&lt;MessdatenDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getMessdatenRequestCreation(GetMessdatenRequest getMessdatenRequest) throws WebClientResponseException {
        Object postBody = getMessdatenRequest;
        // verify the required parameter 'getMessdatenRequest' is set
        if (getMessdatenRequest == null) {
            throw new WebClientResponseException("Missing the required parameter 'getMessdatenRequest' when calling getMessdaten",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
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
        final String[] localVarContentTypes = {
                "application/json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {};

        ParameterizedTypeReference<MessdatenDto> localVarReturnType = new ParameterizedTypeReference<MessdatenDto>() {
        };
        return apiClient.invokeAPI("/messdaten", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Holt die Messdaten zur einer Zählstelle in einem bestimmten Zeitraum.
     *
     * <p>
     * <b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * <p>
     * <b>200</b> - Messdaten erfolgreich abgefragt.
     *
     * @param getMessdatenRequest The getMessdatenRequest parameter
     * @return List&lt;MessdatenDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<MessdatenDto> getMessdaten(GetMessdatenRequest getMessdatenRequest) throws WebClientResponseException {
        ParameterizedTypeReference<MessdatenDto> localVarReturnType = new ParameterizedTypeReference<MessdatenDto>() {
        };
        return getMessdatenRequestCreation(getMessdatenRequest).bodyToFlux(localVarReturnType);
    }

    /**
     * Holt die Messdaten zur einer Zählstelle in einem bestimmten Zeitraum.
     *
     * <p>
     * <b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * <p>
     * <b>200</b> - Messdaten erfolgreich abgefragt.
     *
     * @param getMessdatenRequest The getMessdatenRequest parameter
     * @return ResponseEntity&lt;List&lt;MessdatenDto&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<MessdatenDto>>> getMessdatenWithHttpInfo(GetMessdatenRequest getMessdatenRequest) throws WebClientResponseException {
        ParameterizedTypeReference<MessdatenDto> localVarReturnType = new ParameterizedTypeReference<MessdatenDto>() {
        };
        return getMessdatenRequestCreation(getMessdatenRequest).toEntityList(localVarReturnType);
    }

    /**
     * Holt die Messdaten zur einer Zählstelle in einem bestimmten Zeitraum.
     *
     * <p>
     * <b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * <p>
     * <b>200</b> - Messdaten erfolgreich abgefragt.
     *
     * @param getMessdatenRequest The getMessdatenRequest parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getMessdatenWithResponseSpec(GetMessdatenRequest getMessdatenRequest) throws WebClientResponseException {
        return getMessdatenRequestCreation(getMessdatenRequest);
    }

    /**
     * Holt die Summe der Messdaten zur einer Zählstelle in einem bestimmten Zeitraum.
     *
     * <p>
     * <b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * <p>
     * <b>200</b> - Summe der Messdaten erfolgreich abgefragt.
     *
     * @param getMessdatenRequest The getMessdatenRequest parameter
     * @return List&lt;MessdatenDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getMessdatenSummeRequestCreation(GetMessdatenRequest getMessdatenRequest) throws WebClientResponseException {
        Object postBody = getMessdatenRequest;
        // verify the required parameter 'getMessdatenRequest' is set
        if (getMessdatenRequest == null) {
            throw new WebClientResponseException("Missing the required parameter 'getMessdatenRequest' when calling getMessdatenSumme",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
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
        final String[] localVarContentTypes = {
                "application/json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {};

        ParameterizedTypeReference<MessdatenDto> localVarReturnType = new ParameterizedTypeReference<MessdatenDto>() {
        };
        return apiClient.invokeAPI("/messdaten/summe", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams,
                localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Holt die Summe der Messdaten zur einer Zählstelle in einem bestimmten Zeitraum.
     *
     * <p>
     * <b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * <p>
     * <b>200</b> - Summe der Messdaten erfolgreich abgefragt.
     *
     * @param getMessdatenRequest The getMessdatenRequest parameter
     * @return List&lt;MessdatenDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<MessdatenDto> getMessdatenSumme(GetMessdatenRequest getMessdatenRequest) throws WebClientResponseException {
        ParameterizedTypeReference<MessdatenDto> localVarReturnType = new ParameterizedTypeReference<MessdatenDto>() {
        };
        return getMessdatenSummeRequestCreation(getMessdatenRequest).bodyToFlux(localVarReturnType);
    }

    /**
     * Holt die Summe der Messdaten zur einer Zählstelle in einem bestimmten Zeitraum.
     *
     * <p>
     * <b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * <p>
     * <b>200</b> - Summe der Messdaten erfolgreich abgefragt.
     *
     * @param getMessdatenRequest The getMessdatenRequest parameter
     * @return ResponseEntity&lt;List&lt;MessdatenDto&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<MessdatenDto>>> getMessdatenSummeWithHttpInfo(GetMessdatenRequest getMessdatenRequest) throws WebClientResponseException {
        ParameterizedTypeReference<MessdatenDto> localVarReturnType = new ParameterizedTypeReference<MessdatenDto>() {
        };
        return getMessdatenSummeRequestCreation(getMessdatenRequest).toEntityList(localVarReturnType);
    }

    /**
     * Holt die Summe der Messdaten zur einer Zählstelle in einem bestimmten Zeitraum.
     *
     * <p>
     * <b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * <p>
     * <b>200</b> - Summe der Messdaten erfolgreich abgefragt.
     *
     * @param getMessdatenRequest The getMessdatenRequest parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getMessdatenSummeWithResponseSpec(GetMessdatenRequest getMessdatenRequest) throws WebClientResponseException {
        return getMessdatenSummeRequestCreation(getMessdatenRequest);
    }
}
