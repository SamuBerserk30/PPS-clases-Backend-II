package co.edu.cesde.pps.application;

import co.edu.cesde.pps.dto.AddressDTO;
import co.edu.cesde.pps.model.User;
import co.edu.cesde.pps.service.AddressService;
import co.edu.cesde.pps.service.UserSessionService;
import co.edu.cesde.pps.web.dto.request.AddressUpsertRequest;
import co.edu.cesde.pps.web.dto.response.AddressResponse;
import co.edu.cesde.pps.web.mapper.WebRequestMapper;
import co.edu.cesde.pps.web.mapper.WebResponseMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AddressApplicationService {

    private final AddressService addressService;
    private final UserSessionService userSessionService;
    private final WebRequestMapper webRequestMapper;
    private final WebResponseMapper webResponseMapper;

    public AddressApplicationService(AddressService addressService,
                                     UserSessionService userSessionService,
                                     WebRequestMapper webRequestMapper,
                                     WebResponseMapper webResponseMapper) {
        this.addressService = addressService;
        this.userSessionService = userSessionService;
        this.webRequestMapper = webRequestMapper;
        this.webResponseMapper = webResponseMapper;
    }

    public List<AddressResponse> listMyAddresses(String sessionToken) {
        User user = userSessionService.requireAuthenticatedUser(sessionToken);
        return webResponseMapper.toAddressResponseList(addressService.findUserAddresses(user.getUserId()));
    }

    public AddressResponse getMyAddress(String sessionToken, Long addressId) {
        User user = userSessionService.requireAuthenticatedUser(sessionToken);
        return webResponseMapper.toAddressResponse(addressService.findUserAddressById(user.getUserId(), addressId));
    }

    @Transactional
    public AddressResponse addAddress(String sessionToken, AddressUpsertRequest request) {
        User user = userSessionService.requireAuthenticatedUser(sessionToken);
        AddressDTO dto = webRequestMapper.toAddressDTO(request);
        return webResponseMapper.toAddressResponse(addressService.addAddress(user.getUserId(), dto));
    }

    @Transactional
    public AddressResponse updateAddress(String sessionToken, Long addressId, AddressUpsertRequest request) {
        User user = userSessionService.requireAuthenticatedUser(sessionToken);
        AddressDTO dto = webRequestMapper.toAddressDTO(request);
        return webResponseMapper.toAddressResponse(
                addressService.updateUserAddress(user.getUserId(), addressId, dto)
        );
    }

    @Transactional
    public AddressResponse setDefaultAddress(String sessionToken, Long addressId) {
        User user = userSessionService.requireAuthenticatedUser(sessionToken);
        return webResponseMapper.toAddressResponse(addressService.setDefaultAddress(user.getUserId(), addressId));
    }

    @Transactional
    public void deleteAddress(String sessionToken, Long addressId) {
        User user = userSessionService.requireAuthenticatedUser(sessionToken);
        addressService.deleteAddress(user.getUserId(), addressId);
    }
}