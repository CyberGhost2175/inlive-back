package ai.lab.inlive.services;

import ai.lab.inlive.dto.request.DeviceTokenDeleteRequest;
import ai.lab.inlive.dto.request.DeviceTokenRegisterRequest;

public interface DeviceTokenService {

    void registerOrUpdateToken(String keycloakId, DeviceTokenRegisterRequest request);

    void deleteToken(String keycloakId, DeviceTokenDeleteRequest request);
}
