package de.muenchen.dave.geodateneai.gen.api;

import de.muenchen.dave.geodateneai.gen.geodaten.ApiClient;
import de.muenchen.dave.geodateneai.gen.model.FeatureCollectionDtoFeatureDtoStadtbezirkDto;
import de.muenchen.dave.geodateneai.gen.model.PointGeometryDto;
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
import reactor.core.publisher.Mono;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen")
public class PointFeatureApi {
    private ApiClient apiClient;

    public PointFeatureApi() {
        this(new ApiClient());
    }

    @Autowired
    public PointFeatureApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Holt die Stadtbezirke die sich mit dem Punkt (im Standard EPSG:4326 (WGS84)) überschneiden.
     *
     * <p>
     * <b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * <p>
     * <b>200</b> - Stadtbezirke erfolgreich abgefragt.
     *
     * @param pointGeometryDto The pointGeometryDto parameter
     * @return FeatureCollectionDtoFeatureDtoStadtbezirkDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getStadtbezirkeRequestCreation(PointGeometryDto pointGeometryDto) throws WebClientResponseException {
        Object postBody = pointGeometryDto;
        // verify the required parameter 'pointGeometryDto' is set
        if (pointGeometryDto == null) {
            throw new WebClientResponseException("Missing the required parameter 'pointGeometryDto' when calling getStadtbezirke",
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

        ParameterizedTypeReference<FeatureCollectionDtoFeatureDtoStadtbezirkDto> localVarReturnType = new ParameterizedTypeReference<FeatureCollectionDtoFeatureDtoStadtbezirkDto>() {
        };
        return apiClient.invokeAPI("/point/stadtbezirke", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams,
                localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Holt die Stadtbezirke die sich mit dem Punkt (im Standard EPSG:4326 (WGS84)) überschneiden.
     *
     * <p>
     * <b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * <p>
     * <b>200</b> - Stadtbezirke erfolgreich abgefragt.
     *
     * @param pointGeometryDto The pointGeometryDto parameter
     * @return FeatureCollectionDtoFeatureDtoStadtbezirkDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<FeatureCollectionDtoFeatureDtoStadtbezirkDto> getStadtbezirke(PointGeometryDto pointGeometryDto) throws WebClientResponseException {
        ParameterizedTypeReference<FeatureCollectionDtoFeatureDtoStadtbezirkDto> localVarReturnType = new ParameterizedTypeReference<FeatureCollectionDtoFeatureDtoStadtbezirkDto>() {
        };
        return getStadtbezirkeRequestCreation(pointGeometryDto).bodyToMono(localVarReturnType);
    }

    /**
     * Holt die Stadtbezirke die sich mit dem Punkt (im Standard EPSG:4326 (WGS84)) überschneiden.
     *
     * <p>
     * <b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * <p>
     * <b>200</b> - Stadtbezirke erfolgreich abgefragt.
     *
     * @param pointGeometryDto The pointGeometryDto parameter
     * @return ResponseEntity&lt;FeatureCollectionDtoFeatureDtoStadtbezirkDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<FeatureCollectionDtoFeatureDtoStadtbezirkDto>> getStadtbezirkeWithHttpInfo(PointGeometryDto pointGeometryDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<FeatureCollectionDtoFeatureDtoStadtbezirkDto> localVarReturnType = new ParameterizedTypeReference<FeatureCollectionDtoFeatureDtoStadtbezirkDto>() {
        };
        return getStadtbezirkeRequestCreation(pointGeometryDto).toEntity(localVarReturnType);
    }

    /**
     * Holt die Stadtbezirke die sich mit dem Punkt (im Standard EPSG:4326 (WGS84)) überschneiden.
     *
     * <p>
     * <b>500</b> - Bei der Erstellung oder Durchführung des Requests ist ein Fehler aufgetreten.
     * <p>
     * <b>200</b> - Stadtbezirke erfolgreich abgefragt.
     *
     * @param pointGeometryDto The pointGeometryDto parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getStadtbezirkeWithResponseSpec(PointGeometryDto pointGeometryDto) throws WebClientResponseException {
        return getStadtbezirkeRequestCreation(pointGeometryDto);
    }
}
