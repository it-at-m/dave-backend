package de.muenchen.dave.geodateneai.gen.api;

import de.muenchen.dave.geodateneai.gen.geodaten.ApiClient;

import de.muenchen.dave.geodateneai.gen.model.ChosenTagesTypValidDTO;
import de.muenchen.dave.geodateneai.gen.model.ChosenTagesTypValidRequestDto;
import de.muenchen.dave.geodateneai.gen.model.InformationResponseDto;
import de.muenchen.dave.geodateneai.gen.model.NichtPlausibleTageDto;
import de.muenchen.dave.geodateneai.gen.model.ValidWochentageInPeriodDto;
import de.muenchen.dave.geodateneai.gen.model.ValidWochentageInPeriodRequestDto;

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
public class MessstelleOptionsmenuControllerApi {
    private ApiClient apiClient;

    public MessstelleOptionsmenuControllerApi() {
        this(new ApiClient());
    }

    @Autowired
    public MessstelleOptionsmenuControllerApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Holt alle nicht Plausiblen Tage.
     * 
     * <p><b>200</b> - Nicht Plausible Tage erfolgreich abgefragt.
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * @param messstelleId The messstelleId parameter
     * @return NichtPlausibleTageDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getNichtPlausibleTageRequestCreation(String messstelleId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'messstelleId' is set
        if (messstelleId == null) {
            throw new WebClientResponseException("Missing the required parameter 'messstelleId' when calling getNichtPlausibleTage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "messstelle_id", messstelleId));

        final String[] localVarAccepts = { 
            "*/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<NichtPlausibleTageDto> localVarReturnType = new ParameterizedTypeReference<NichtPlausibleTageDto>() {};
        return apiClient.invokeAPI("/MessstelleOptionsmenu/nichtPlausibleDaten", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Holt alle nicht Plausiblen Tage.
     * 
     * <p><b>200</b> - Nicht Plausible Tage erfolgreich abgefragt.
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * @param messstelleId The messstelleId parameter
     * @return NichtPlausibleTageDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<NichtPlausibleTageDto> getNichtPlausibleTage(String messstelleId) throws WebClientResponseException {
        ParameterizedTypeReference<NichtPlausibleTageDto> localVarReturnType = new ParameterizedTypeReference<NichtPlausibleTageDto>() {};
        return getNichtPlausibleTageRequestCreation(messstelleId).bodyToMono(localVarReturnType);
    }

    /**
     * Holt alle nicht Plausiblen Tage.
     * 
     * <p><b>200</b> - Nicht Plausible Tage erfolgreich abgefragt.
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * @param messstelleId The messstelleId parameter
     * @return ResponseEntity&lt;NichtPlausibleTageDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<NichtPlausibleTageDto>> getNichtPlausibleTageWithHttpInfo(String messstelleId) throws WebClientResponseException {
        ParameterizedTypeReference<NichtPlausibleTageDto> localVarReturnType = new ParameterizedTypeReference<NichtPlausibleTageDto>() {};
        return getNichtPlausibleTageRequestCreation(messstelleId).toEntity(localVarReturnType);
    }

    /**
     * Holt alle nicht Plausiblen Tage.
     * 
     * <p><b>200</b> - Nicht Plausible Tage erfolgreich abgefragt.
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * @param messstelleId The messstelleId parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getNichtPlausibleTageWithResponseSpec(String messstelleId) throws WebClientResponseException {
        return getNichtPlausibleTageRequestCreation(messstelleId);
    }
    /**
     * Gibt zurück, ob der im Optionsmenue ausgewählte TagesTyp valide ist
     * 
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * <p><b>200</b> - Valide Wochentage im Zeitraum konnten ermittelt werden
     * @param validWochentageInPeriodRequestDto The validWochentageInPeriodRequestDto parameter
     * @return ValidWochentageInPeriodDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getValidWochentageInPeriodRequestCreation(ValidWochentageInPeriodRequestDto validWochentageInPeriodRequestDto) throws WebClientResponseException {
        Object postBody = validWochentageInPeriodRequestDto;
        // verify the required parameter 'validWochentageInPeriodRequestDto' is set
        if (validWochentageInPeriodRequestDto == null) {
            throw new WebClientResponseException("Missing the required parameter 'validWochentageInPeriodRequestDto' when calling getValidWochentageInPeriod", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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

        ParameterizedTypeReference<ValidWochentageInPeriodDto> localVarReturnType = new ParameterizedTypeReference<ValidWochentageInPeriodDto>() {};
        return apiClient.invokeAPI("/MessstelleOptionsmenu/validWochentageInPeriod", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gibt zurück, ob der im Optionsmenue ausgewählte TagesTyp valide ist
     * 
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * <p><b>200</b> - Valide Wochentage im Zeitraum konnten ermittelt werden
     * @param validWochentageInPeriodRequestDto The validWochentageInPeriodRequestDto parameter
     * @return ValidWochentageInPeriodDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ValidWochentageInPeriodDto> getValidWochentageInPeriod(ValidWochentageInPeriodRequestDto validWochentageInPeriodRequestDto) throws WebClientResponseException {
        ParameterizedTypeReference<ValidWochentageInPeriodDto> localVarReturnType = new ParameterizedTypeReference<ValidWochentageInPeriodDto>() {};
        return getValidWochentageInPeriodRequestCreation(validWochentageInPeriodRequestDto).bodyToMono(localVarReturnType);
    }

    /**
     * Gibt zurück, ob der im Optionsmenue ausgewählte TagesTyp valide ist
     * 
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * <p><b>200</b> - Valide Wochentage im Zeitraum konnten ermittelt werden
     * @param validWochentageInPeriodRequestDto The validWochentageInPeriodRequestDto parameter
     * @return ResponseEntity&lt;ValidWochentageInPeriodDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<ValidWochentageInPeriodDto>> getValidWochentageInPeriodWithHttpInfo(ValidWochentageInPeriodRequestDto validWochentageInPeriodRequestDto) throws WebClientResponseException {
        ParameterizedTypeReference<ValidWochentageInPeriodDto> localVarReturnType = new ParameterizedTypeReference<ValidWochentageInPeriodDto>() {};
        return getValidWochentageInPeriodRequestCreation(validWochentageInPeriodRequestDto).toEntity(localVarReturnType);
    }

    /**
     * Gibt zurück, ob der im Optionsmenue ausgewählte TagesTyp valide ist
     * 
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * <p><b>200</b> - Valide Wochentage im Zeitraum konnten ermittelt werden
     * @param validWochentageInPeriodRequestDto The validWochentageInPeriodRequestDto parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getValidWochentageInPeriodWithResponseSpec(ValidWochentageInPeriodRequestDto validWochentageInPeriodRequestDto) throws WebClientResponseException {
        return getValidWochentageInPeriodRequestCreation(validWochentageInPeriodRequestDto);
    }
    /**
     * Gibt zurück, ob der im Optionsmenue ausgewählte TagesTyp valide ist
     * 
     * <p><b>200</b> - TagesTyp erfolgreich validiert
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * @param chosenTagesTypValidRequestDto The chosenTagesTypValidRequestDto parameter
     * @return ChosenTagesTypValidDTO
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec isTagesTypDataValidRequestCreation(ChosenTagesTypValidRequestDto chosenTagesTypValidRequestDto) throws WebClientResponseException {
        Object postBody = chosenTagesTypValidRequestDto;
        // verify the required parameter 'chosenTagesTypValidRequestDto' is set
        if (chosenTagesTypValidRequestDto == null) {
            throw new WebClientResponseException("Missing the required parameter 'chosenTagesTypValidRequestDto' when calling isTagesTypDataValid", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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

        ParameterizedTypeReference<ChosenTagesTypValidDTO> localVarReturnType = new ParameterizedTypeReference<ChosenTagesTypValidDTO>() {};
        return apiClient.invokeAPI("/MessstelleOptionsmenu/validateTagesTyp", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gibt zurück, ob der im Optionsmenue ausgewählte TagesTyp valide ist
     * 
     * <p><b>200</b> - TagesTyp erfolgreich validiert
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * @param chosenTagesTypValidRequestDto The chosenTagesTypValidRequestDto parameter
     * @return ChosenTagesTypValidDTO
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ChosenTagesTypValidDTO> isTagesTypDataValid(ChosenTagesTypValidRequestDto chosenTagesTypValidRequestDto) throws WebClientResponseException {
        ParameterizedTypeReference<ChosenTagesTypValidDTO> localVarReturnType = new ParameterizedTypeReference<ChosenTagesTypValidDTO>() {};
        return isTagesTypDataValidRequestCreation(chosenTagesTypValidRequestDto).bodyToMono(localVarReturnType);
    }

    /**
     * Gibt zurück, ob der im Optionsmenue ausgewählte TagesTyp valide ist
     * 
     * <p><b>200</b> - TagesTyp erfolgreich validiert
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * @param chosenTagesTypValidRequestDto The chosenTagesTypValidRequestDto parameter
     * @return ResponseEntity&lt;ChosenTagesTypValidDTO&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<ChosenTagesTypValidDTO>> isTagesTypDataValidWithHttpInfo(ChosenTagesTypValidRequestDto chosenTagesTypValidRequestDto) throws WebClientResponseException {
        ParameterizedTypeReference<ChosenTagesTypValidDTO> localVarReturnType = new ParameterizedTypeReference<ChosenTagesTypValidDTO>() {};
        return isTagesTypDataValidRequestCreation(chosenTagesTypValidRequestDto).toEntity(localVarReturnType);
    }

    /**
     * Gibt zurück, ob der im Optionsmenue ausgewählte TagesTyp valide ist
     * 
     * <p><b>200</b> - TagesTyp erfolgreich validiert
     * <p><b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * @param chosenTagesTypValidRequestDto The chosenTagesTypValidRequestDto parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec isTagesTypDataValidWithResponseSpec(ChosenTagesTypValidRequestDto chosenTagesTypValidRequestDto) throws WebClientResponseException {
        return isTagesTypDataValidRequestCreation(chosenTagesTypValidRequestDto);
    }
}
