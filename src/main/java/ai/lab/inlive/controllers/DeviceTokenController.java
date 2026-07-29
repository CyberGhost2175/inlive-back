package ai.lab.inlive.controllers;

import ai.lab.inlive.constants.Utils;
import ai.lab.inlive.dto.request.DeviceTokenDeleteRequest;
import ai.lab.inlive.dto.request.DeviceTokenRegisterRequest;
import ai.lab.inlive.services.DeviceTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/device-tokens")
@Tag(name = "Device Tokens", description = "API для регистрации FCM-токенов устройств")
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    @Operation(summary = "Сохранить или обновить FCM-токен",
            description = "Вызывать при логине и при onTokenRefresh. У пользователя может быть несколько активных токенов (несколько устройств).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Токен успешно сохранён"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса", content = @Content),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> registerToken(@RequestBody @Valid DeviceTokenRegisterRequest request) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);
        deviceTokenService.registerOrUpdateToken(keycloakId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить FCM-токен устройства",
            description = "Вызывать при логауте. Удаляется только токен текущего устройства.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Токен удалён"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса", content = @Content),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content)
    })
    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteToken(@RequestBody @Valid DeviceTokenDeleteRequest request) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);
        deviceTokenService.deleteToken(keycloakId, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
