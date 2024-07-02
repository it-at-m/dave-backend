package de.muenchen.dave.geodateneai.gen.api;

import de.muenchen.dave.geodateneai.gen.geodaten.ApiClient;

import de.muenchen.dave.geodateneai.gen.model.GetMeasurementValuesAggregateRequest;
import de.muenchen.dave.geodateneai.gen.model.GetMeasurementValuesRequest;
import de.muenchen.dave.geodateneai.gen.model.InformationResponseDto;
import de.muenchen.dave.geodateneai.gen.model.MeasurementValuesAggregateResponse;
import de.muenchen.dave.geodateneai.gen.model.MeasurementValuesResponse;

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
public class MesswerteApi {
    private ApiClient apiClient;

    public MesswerteApi() {
        this(new ApiClient());
    }

    @Autowired
    public MesswerteApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Liefert die durchschnittlichen Messwerte pro Intervall zu einem oder mehreren Messquerschnitt in einem bestimmten Zeitraum oder zu einem Zeitpunkt.
     * 
     * <p><b>200</b> - MesswerteIntervalle erfolgreich abgefragt.
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * @param getMeasurementValuesRequest The getMeasurementValuesRequest parameter
     * @return MeasurementValuesResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getAverageMeasurementValuesPerIntervalRequestCreation(GetMeasurementValuesRequest getMeasurementValuesRequest) throws WebClientResponseException {
        Object postBody = getMeasurementValuesRequest;
        // verify the required parameter 'getMeasurementValuesRequest' is set
        if (getMeasurementValuesRequest == null) {
            throw new WebClientResponseException("Missing the required parameter 'getMeasurementValuesRequest' when calling getAverageMeasurementValuesPerInterval", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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

        ParameterizedTypeReference<MeasurementValuesResponse> localVarReturnType = new ParameterizedTypeReference<MeasurementValuesResponse>() {};
        return apiClient.invokeAPI("/messwerte/average-per-interval", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Liefert die durchschnittlichen Messwerte pro Intervall zu einem oder mehreren Messquerschnitt in einem bestimmten Zeitraum oder zu einem Zeitpunkt.
     * 
     * <p><b>200</b> - MesswerteIntervalle erfolgreich abgefragt.
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * @param getMeasurementValuesRequest The getMeasurementValuesRequest parameter
     * @return MeasurementValuesResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<MeasurementValuesResponse> getAverageMeasurementValuesPerInterval(GetMeasurementValuesRequest getMeasurementValuesRequest) throws WebClientResponseException {
        ParameterizedTypeReference<MeasurementValuesResponse> localVarReturnType = new ParameterizedTypeReference<MeasurementValuesResponse>() {};
        return getAverageMeasurementValuesPerIntervalRequestCreation(getMeasurementValuesRequest).bodyToMono(localVarReturnType);
    }

    /**
     * Liefert die durchschnittlichen Messwerte pro Intervall zu einem oder mehreren Messquerschnitt in einem bestimmten Zeitraum oder zu einem Zeitpunkt.
     * 
     * <p><b>200</b> - MesswerteIntervalle erfolgreich abgefragt.
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * @param getMeasurementValuesRequest The getMeasurementValuesRequest parameter
     * @return ResponseEntity&lt;MeasurementValuesResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<MeasurementValuesResponse>> getAverageMeasurementValuesPerIntervalWithHttpInfo(GetMeasurementValuesRequest getMeasurementValuesRequest) throws WebClientResponseException {
        ParameterizedTypeReference<MeasurementValuesResponse> localVarReturnType = new ParameterizedTypeReference<MeasurementValuesResponse>() {};
        return getAverageMeasurementValuesPerIntervalRequestCreation(getMeasurementValuesRequest).toEntity(localVarReturnType);
    }

    /**
     * Liefert die durchschnittlichen Messwerte pro Intervall zu einem oder mehreren Messquerschnitt in einem bestimmten Zeitraum oder zu einem Zeitpunkt.
     * 
     * <p><b>200</b> - MesswerteIntervalle erfolgreich abgefragt.
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * @param getMeasurementValuesRequest The getMeasurementValuesRequest parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getAverageMeasurementValuesPerIntervalWithResponseSpec(GetMeasurementValuesRequest getMeasurementValuesRequest) throws WebClientResponseException {
        return getAverageMeasurementValuesPerIntervalRequestCreation(getMeasurementValuesRequest);
    }
    /**
     * Holt das Tagesaggregat der Messwerte zu einem Messquerschnitt in einem bestimmten Zeitraum.
     * 
     * <p><b>200</b> - Tagesaggregat der Messwerte erfolgreich abgefragt.
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * @param getMeasurementValuesAggregateRequest The getMeasurementValuesAggregateRequest parameter
     * @return MeasurementValuesAggregateResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getMesswerteTagesaggregatPerMessquerschnittRequestCreation(GetMeasurementValuesAggregateRequest getMeasurementValuesAggregateRequest) throws WebClientResponseException {
        Object postBody = getMeasurementValuesAggregateRequest;
        // verify the required parameter 'getMeasurementValuesAggregateRequest' is set
        if (getMeasurementValuesAggregateRequest == null) {
            throw new WebClientResponseException("Missing the required parameter 'getMeasurementValuesAggregateRequest' when calling getMesswerteTagesaggregatPerMessquerschnitt", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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

        ParameterizedTypeReference<MeasurementValuesAggregateResponse> localVarReturnType = new ParameterizedTypeReference<MeasurementValuesAggregateResponse>() {};
        return apiClient.invokeAPI("/messwerte/tagesaggregat/messquerschnitt", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Holt das Tagesaggregat der Messwerte zu einem Messquerschnitt in einem bestimmten Zeitraum.
     * 
     * <p><b>200</b> - Tagesaggregat der Messwerte erfolgreich abgefragt.
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * @param getMeasurementValuesAggregateRequest The getMeasurementValuesAggregateRequest parameter
     * @return MeasurementValuesAggregateResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<MeasurementValuesAggregateResponse> getMesswerteTagesaggregatPerMessquerschnitt(GetMeasurementValuesAggregateRequest getMeasurementValuesAggregateRequest) throws WebClientResponseException {
        ParameterizedTypeReference<MeasurementValuesAggregateResponse> localVarReturnType = new ParameterizedTypeReference<MeasurementValuesAggregateResponse>() {};
        return getMesswerteTagesaggregatPerMessquerschnittRequestCreation(getMeasurementValuesAggregateRequest).bodyToMono(localVarReturnType);
    }

    /**
     * Holt das Tagesaggregat der Messwerte zu einem Messquerschnitt in einem bestimmten Zeitraum.
     * 
     * <p><b>200</b> - Tagesaggregat der Messwerte erfolgreich abgefragt.
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * @param getMeasurementValuesAggregateRequest The getMeasurementValuesAggregateRequest parameter
     * @return ResponseEntity&lt;MeasurementValuesAggregateResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<MeasurementValuesAggregateResponse>> getMesswerteTagesaggregatPerMessquerschnittWithHttpInfo(GetMeasurementValuesAggregateRequest getMeasurementValuesAggregateRequest) throws WebClientResponseException {
        ParameterizedTypeReference<MeasurementValuesAggregateResponse> localVarReturnType = new ParameterizedTypeReference<MeasurementValuesAggregateResponse>() {};
        return getMesswerteTagesaggregatPerMessquerschnittRequestCreation(getMeasurementValuesAggregateRequest).toEntity(localVarReturnType);
    }

    /**
     * Holt das Tagesaggregat der Messwerte zu einem Messquerschnitt in einem bestimmten Zeitraum.
     * 
     * <p><b>200</b> - Tagesaggregat der Messwerte erfolgreich abgefragt.
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * @param getMeasurementValuesAggregateRequest The getMeasurementValuesAggregateRequest parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getMesswerteTagesaggregatPerMessquerschnittWithResponseSpec(GetMeasurementValuesAggregateRequest getMeasurementValuesAggregateRequest) throws WebClientResponseException {
        return getMesswerteTagesaggregatPerMessquerschnittRequestCreation(getMeasurementValuesAggregateRequest);
    }
    /**
     * Holt das Tagesaggregat der Messwerte zu einem Messquerschnitt in einem bestimmten Zeitraum.
     * 
     * <p><b>200</b> - Tagesaggregat der Messwerte erfolgreich abgefragt.
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * @param getMeasurementValuesAggregateRequest The getMeasurementValuesAggregateRequest parameter
     * @return MeasurementValuesAggregateResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getMesswerteTagesaggregatPerMessstelleRequestCreation(GetMeasurementValuesAggregateRequest getMeasurementValuesAggregateRequest) throws WebClientResponseException {
        Object postBody = getMeasurementValuesAggregateRequest;
        // verify the required parameter 'getMeasurementValuesAggregateRequest' is set
        if (getMeasurementValuesAggregateRequest == null) {
            throw new WebClientResponseException("Missing the required parameter 'getMeasurementValuesAggregateRequest' when calling getMesswerteTagesaggregatPerMessstelle", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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

        ParameterizedTypeReference<MeasurementValuesAggregateResponse> localVarReturnType = new ParameterizedTypeReference<MeasurementValuesAggregateResponse>() {};
        return apiClient.invokeAPI("/messwerte/tagesaggregat/messstelle", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Holt das Tagesaggregat der Messwerte zu einem Messquerschnitt in einem bestimmten Zeitraum.
     * 
     * <p><b>200</b> - Tagesaggregat der Messwerte erfolgreich abgefragt.
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * @param getMeasurementValuesAggregateRequest The getMeasurementValuesAggregateRequest parameter
     * @return MeasurementValuesAggregateResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<MeasurementValuesAggregateResponse> getMesswerteTagesaggregatPerMessstelle(GetMeasurementValuesAggregateRequest getMeasurementValuesAggregateRequest) throws WebClientResponseException {
        ParameterizedTypeReference<MeasurementValuesAggregateResponse> localVarReturnType = new ParameterizedTypeReference<MeasurementValuesAggregateResponse>() {};
        return getMesswerteTagesaggregatPerMessstelleRequestCreation(getMeasurementValuesAggregateRequest).bodyToMono(localVarReturnType);
    }

    /**
     * Holt das Tagesaggregat der Messwerte zu einem Messquerschnitt in einem bestimmten Zeitraum.
     * 
     * <p><b>200</b> - Tagesaggregat der Messwerte erfolgreich abgefragt.
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * @param getMeasurementValuesAggregateRequest The getMeasurementValuesAggregateRequest parameter
     * @return ResponseEntity&lt;MeasurementValuesAggregateResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<MeasurementValuesAggregateResponse>> getMesswerteTagesaggregatPerMessstelleWithHttpInfo(GetMeasurementValuesAggregateRequest getMeasurementValuesAggregateRequest) throws WebClientResponseException {
        ParameterizedTypeReference<MeasurementValuesAggregateResponse> localVarReturnType = new ParameterizedTypeReference<MeasurementValuesAggregateResponse>() {};
        return getMesswerteTagesaggregatPerMessstelleRequestCreation(getMeasurementValuesAggregateRequest).toEntity(localVarReturnType);
    }

    /**
     * Holt das Tagesaggregat der Messwerte zu einem Messquerschnitt in einem bestimmten Zeitraum.
     * 
     * <p><b>200</b> - Tagesaggregat der Messwerte erfolgreich abgefragt.
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * @param getMeasurementValuesAggregateRequest The getMeasurementValuesAggregateRequest parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getMesswerteTagesaggregatPerMessstelleWithResponseSpec(GetMeasurementValuesAggregateRequest getMeasurementValuesAggregateRequest) throws WebClientResponseException {
        return getMesswerteTagesaggregatPerMessstelleRequestCreation(getMeasurementValuesAggregateRequest);
    }
}
