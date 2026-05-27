package co.edu.cesde.pps.web.controller;

import co.edu.cesde.pps.application.AddressApplicationService;
import co.edu.cesde.pps.web.dto.request.AddressUpsertRequest;
import co.edu.cesde.pps.web.dto.response.AddressResponse;
import co.edu.cesde.pps.web.security.CurrentSessionResolver;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiRoutes.USER_ADDRESSES)
public class AddressController {

    private final AddressApplicationService addressApplicationService;
    private final CurrentSessionResolver currentSessionResolver;

    public AddressController(AddressApplicationService addressApplicationService,
                             CurrentSessionResolver currentSessionResolver) {
        this.addressApplicationService = addressApplicationService;
        this.currentSessionResolver = currentSessionResolver;
    }

    @GetMapping
    public List<AddressResponse> listMyAddresses(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
                                                 String authorizationHeader) {
        return addressApplicationService.listMyAddresses(currentSessionResolver.resolveCurrentToken(authorizationHeader));
    }

    @GetMapping("/{id}")
    public AddressResponse getMyAddress(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
                                        String authorizationHeader,
                                        @PathVariable Long id) {
        return addressApplicationService.getMyAddress(currentSessionResolver.resolveCurrentToken(authorizationHeader), id);
    }

    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
                                                      String authorizationHeader,
                                                      @Valid @RequestBody AddressUpsertRequest request) {
        AddressResponse response = addressApplicationService.addAddress(
                currentSessionResolver.resolveCurrentToken(authorizationHeader), request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public AddressResponse updateAddress(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
                                         String authorizationHeader,
                                         @PathVariable Long id,
                                         @Valid @RequestBody AddressUpsertRequest request) {
        return addressApplicationService.updateAddress(currentSessionResolver.resolveCurrentToken(authorizationHeader), id, request);
    }

    @PatchMapping("/{id}/default")
    public AddressResponse setDefaultAddress(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
                                             String authorizationHeader,
                                             @PathVariable Long id) {
        return addressApplicationService.setDefaultAddress(currentSessionResolver.resolveCurrentToken(authorizationHeader), id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
                                              String authorizationHeader,
                                              @PathVariable Long id) {
        addressApplicationService.deleteAddress(currentSessionResolver.resolveCurrentToken(authorizationHeader), id);
        return ResponseEntity.noContent().build();
    }
}

