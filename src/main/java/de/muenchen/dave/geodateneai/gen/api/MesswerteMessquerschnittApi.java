package de.muenchen.dave.geodateneai.gen.api;

import de.muenchen.dave.geodateneai.gen.geodaten.ApiClient;

import de.muenchen.dave.geodateneai.gen.model.GetMesswerteMessquerschnittRequest;
import de.muenchen.dave.geodateneai.gen.model.InformationResponseDto;
import de.muenchen.dave.geodateneai.gen.model.MesswerteIntervallMessquerschnittDto;
import de.muenchen.dave.geodateneai.gen.model.MesswerteTagesaggregatMessquerschnittDto;

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
public class MesswerteMessquerschnittApi {
    private ApiClient apiClient;

    public MesswerteMessquerschnittApi() {
        this(new ApiClient());
    }

    @Autowired
    public MesswerteMessquerschnittApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Holt die Messwerte als Intervall zur einer Zählstelle in einem bestimmten Zeitraum.
     * 
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * <p><b>200</b> - Messwerte als Intervall erfolgreich abgefragt.
     * @param getMesswerteMessquerschnittRequest The getMesswerteMessquerschnittRequest parameter
     * @return List&lt;MesswerteIntervallMessquerschnittDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getMesswerteIntervallRequestCreation(GetMesswerteMessquerschnittRequest getMesswerteMessquerschnittRequest) throws WebClientResponseException {
        Object postBody = getMesswerteMessquerschnittRequest;
        // verify the required parameter 'getMesswerteMessquerschnittRequest' is set
        if (getMesswerteMessquerschnittRequest == null) {
            throw new WebClientResponseException("Missing the required parameter 'getMesswerteMessquerschnittRequest' when calling getMesswerteIntervall", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<MesswerteIntervallMessquerschnittDto> localVarReturnType = new ParameterizedTypeReference<MesswerteIntervallMessquerschnittDto>() {};
        return apiClient.invokeAPI("/messwerte/messquerschnitt/intervall", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Holt die Messwerte als Intervall zur einer Zählstelle in einem bestimmten Zeitraum.
     * 
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * <p><b>200</b> - Messwerte als Intervall erfolgreich abgefragt.
     * @param getMesswerteMessquerschnittRequest The getMesswerteMessquerschnittRequest parameter
     * @return List&lt;MesswerteIntervallMessquerschnittDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<MesswerteIntervallMessquerschnittDto> getMesswerteIntervall(GetMesswerteMessquerschnittRequest getMesswerteMessquerschnittRequest) throws WebClientResponseException {
        ParameterizedTypeReference<MesswerteIntervallMessquerschnittDto> localVarReturnType = new ParameterizedTypeReference<MesswerteIntervallMessquerschnittDto>() {};
        return getMesswerteIntervallRequestCreation(getMesswerteMessquerschnittRequest).bodyToFlux(localVarReturnType);
    }

    /**
     * Holt die Messwerte als Intervall zur einer Zählstelle in einem bestimmten Zeitraum.
     * 
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * <p><b>200</b> - Messwerte als Intervall erfolgreich abgefragt.
     * @param getMesswerteMessquerschnittRequest The getMesswerteMessquerschnittRequest parameter
     * @return ResponseEntity&lt;List&lt;MesswerteIntervallMessquerschnittDto&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<MesswerteIntervallMessquerschnittDto>>> getMesswerteIntervallWithHttpInfo(GetMesswerteMessquerschnittRequest getMesswerteMessquerschnittRequest) throws WebClientResponseException {
        ParameterizedTypeReference<MesswerteIntervallMessquerschnittDto> localVarReturnType = new ParameterizedTypeReference<MesswerteIntervallMessquerschnittDto>() {};
        return getMesswerteIntervallRequestCreation(getMesswerteMessquerschnittRequest).toEntityList(localVarReturnType);
    }

    /**
     * Holt die Messwerte als Intervall zur einer Zählstelle in einem bestimmten Zeitraum.
     * 
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * <p><b>200</b> - Messwerte als Intervall erfolgreich abgefragt.
     * @param getMesswerteMessquerschnittRequest The getMesswerteMessquerschnittRequest parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getMesswerteIntervallWithResponseSpec(GetMesswerteMessquerschnittRequest getMesswerteMessquerschnittRequest) throws WebClientResponseException {
        return getMesswerteIntervallRequestCreation(getMesswerteMessquerschnittRequest);
    }
    /**
     * Holt die Messwerte als Tagesaggregat zur einer Zählstelle in einem bestimmten Zeitraum.
     * 
     * <p><b>200</b> - Messwerte als Tagesaggregat erfolgreich abgefragt.
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * @param getMesswerteMessquerschnittRequest The getMesswerteMessquerschnittRequest parameter
     * @return List&lt;MesswerteTagesaggregatMessquerschnittDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getMesswerteTagesaggregatRequestCreation(GetMesswerteMessquerschnittRequest getMesswerteMessquerschnittRequest) throws WebClientResponseException {
        Object postBody = getMesswerteMessquerschnittRequest;
        // verify the required parameter 'getMesswerteMessquerschnittRequest' is set
        if (getMesswerteMessquerschnittRequest == null) {
            throw new WebClientResponseException("Missing the required parameter 'getMesswerteMessquerschnittRequest' when calling getMesswerteTagesaggregat", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<MesswerteTagesaggregatMessquerschnittDto> localVarReturnType = new ParameterizedTypeReference<MesswerteTagesaggregatMessquerschnittDto>() {};
        return apiClient.invokeAPI("/messwerte/messquerschnitt/tagesaggregat", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Holt die Messwerte als Tagesaggregat zur einer Zählstelle in einem bestimmten Zeitraum.
     * 
     * <p><b>200</b> - Messwerte als Tagesaggregat erfolgreich abgefragt.
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * @param getMesswerteMessquerschnittRequest The getMesswerteMessquerschnittRequest parameter
     * @return List&lt;MesswerteTagesaggregatMessquerschnittDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<MesswerteTagesaggregatMessquerschnittDto> getMesswerteTagesaggregat(GetMesswerteMessquerschnittRequest getMesswerteMessquerschnittRequest) throws WebClientResponseException {
        ParameterizedTypeReference<MesswerteTagesaggregatMessquerschnittDto> localVarReturnType = new ParameterizedTypeReference<MesswerteTagesaggregatMessquerschnittDto>() {};
        return getMesswerteTagesaggregatRequestCreation(getMesswerteMessquerschnittRequest).bodyToFlux(localVarReturnType);
    }

    /**
     * Holt die Messwerte als Tagesaggregat zur einer Zählstelle in einem bestimmten Zeitraum.
     * 
     * <p><b>200</b> - Messwerte als Tagesaggregat erfolgreich abgefragt.
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * @param getMesswerteMessquerschnittRequest The getMesswerteMessquerschnittRequest parameter
     * @return ResponseEntity&lt;List&lt;MesswerteTagesaggregatMessquerschnittDto&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<MesswerteTagesaggregatMessquerschnittDto>>> getMesswerteTagesaggregatWithHttpInfo(GetMesswerteMessquerschnittRequest getMesswerteMessquerschnittRequest) throws WebClientResponseException {
        ParameterizedTypeReference<MesswerteTagesaggregatMessquerschnittDto> localVarReturnType = new ParameterizedTypeReference<MesswerteTagesaggregatMessquerschnittDto>() {};
        return getMesswerteTagesaggregatRequestCreation(getMesswerteMessquerschnittRequest).toEntityList(localVarReturnType);
    }

    /**
     * Holt die Messwerte als Tagesaggregat zur einer Zählstelle in einem bestimmten Zeitraum.
     * 
     * <p><b>200</b> - Messwerte als Tagesaggregat erfolgreich abgefragt.
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * @param getMesswerteMessquerschnittRequest The getMesswerteMessquerschnittRequest parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getMesswerteTagesaggregatWithResponseSpec(GetMesswerteMessquerschnittRequest getMesswerteMessquerschnittRequest) throws WebClientResponseException {
        return getMesswerteTagesaggregatRequestCreation(getMesswerteMessquerschnittRequest);
    }
}
