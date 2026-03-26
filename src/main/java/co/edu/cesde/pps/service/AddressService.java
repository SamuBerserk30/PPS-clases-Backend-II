package co.edu.cesde.pps.service;

import co.edu.cesde.pps.dto.AddressDTO;
import co.edu.cesde.pps.exception.EntityNotFoundException;
import co.edu.cesde.pps.exception.ValidationException;
import co.edu.cesde.pps.mapper.AddressMapper;
import co.edu.cesde.pps.model.Address;
import co.edu.cesde.pps.model.User;
import co.edu.cesde.pps.util.ValidationUtils;
import co.edu.cesde.pps.config.AppConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de direcciones de usuarios.
 *
 * Responsabilidades:
 * - CRUD de direcciones
 * - Gestión bidireccional (User <-> Address)
 * - Validaciones de direcciones
 * - Gestión de dirección por defecto (solo una puede ser default)
 * - Conversión Entity <-> DTO
 *
 * NOTA: En Etapa 06 se agregará:
 * - @Service annotation
 * - @Transactional
 * - Inyección de AddressRepository
 * - Persistencia real
 */
@Service
@Transactional(readOnly = true)
public class AddressService {

    private final AddressMapper addressMapper;
    private final UserService userService;
    // TODO Etapa 06: private final AddressRepository addressRepository;
    // Por ahora trabajamos con lista en memoria
    private final List<Address> addressesInMemory;

    public AddressService(UserService userService) {
        this.addressMapper = new AddressMapper();
        this.userService = userService;
        this.addressesInMemory = new ArrayList<>();
    }

    /**
     * Agrega una dirección a un usuario (gestión bidireccional).
     *
     * @param userId ID del usuario
     * @param addressDTO Datos de la dirección
     * @return AddressDTO de la dirección creada
     * @throws EntityNotFoundException si el usuario no existe
     * @throws ValidationException si excede máximo de direcciones
     */
    public AddressDTO addAddress(Long userId, AddressDTO addressDTO) {
        // Obtener usuario
        User user = userService.findUserEntityOrThrow(userId);

        // Validar máximo de direcciones
        long currentCount = addressesInMemory.stream()
                .filter(a -> a.getUser().getUserId().equals(userId))
                .count();

        if (currentCount >= AppConfig.getMaxAddressesPerUser()) {
            throw new ValidationException("User has reached maximum number of addresses (" +
                AppConfig.getMaxAddressesPerUser() + ")");
        }

        // Validar datos de dirección
        validateAddressData(addressDTO);

        // Convertir DTO a Entity
        Address address = addressMapper.toEntity(addressDTO);
        address.setAddressId(generateNextId());

        // Gestión bidireccional
        user.getAddresses().add(address);    // Agregar a colección del usuario
        address.setUser(user);                // Establecer referencia al usuario

        // Si es la primera dirección, hacerla por defecto
        if (user.getAddresses().size() == 1) {
            address.setIsDefault(true);
        } else if (address.getIsDefault()) {
            // Si se marca como default, desmarcar las otras
            unsetOtherDefaultAddresses(userId);
        }

        // TODO Etapa 06: addressRepository.save(address);
        addressesInMemory.add(address);

        return addressMapper.toDTO(address);
    }

    /**
     * Actualiza una dirección existente.
     *
     * @param addressId ID de la dirección
     * @param addressDTO Nuevos datos
     * @return AddressDTO actualizado
     * @throws EntityNotFoundException si no existe
     */
    public AddressDTO updateAddress(Long addressId, AddressDTO addressDTO) {
        Address address = findAddressEntityOrThrow(addressId);

        // Validar datos
        validateAddressData(addressDTO);

        // Actualizar campos
        address.setType(addressDTO.getType());
        address.setLine1(addressDTO.getLine1());
        address.setLine2(addressDTO.getLine2());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setCountry(addressDTO.getCountry());
        address.setPostalCode(addressDTO.getPostalCode());

        // Si se marca como default, desmarcar las otras
        if (addressDTO.getIsDefault() && !address.getIsDefault()) {
            unsetOtherDefaultAddresses(address.getUser().getUserId());
            address.setIsDefault(true);
        }

        // TODO Etapa 06: addressRepository.save(address);

        return addressMapper.toDTO(address);
    }

    /**
     * Elimina una dirección de un usuario (gestión bidireccional).
     *
     * @param userId ID del usuario
     * @param addressId ID de la dirección
     * @throws EntityNotFoundException si no existe
     * @throws ValidationException si la dirección no pertenece al usuario
     */
    public void deleteAddress(Long userId, Long addressId) {
        User user = userService.findUserEntityOrThrow(userId);
        Address address = findAddressEntityOrThrow(addressId);

        // Validar que la dirección pertenezca al usuario
        if (!address.getUser().getUserId().equals(userId)) {
            throw new ValidationException("Address does not belong to user");
        }

        // Gestión bidireccional
        user.getAddresses().remove(address);  // Remover de colección del usuario
        address.setUser(null);                 // Remover referencia al usuario

        // TODO Etapa 06: addressRepository.delete(address);
        addressesInMemory.remove(address);

        // Si era la default, marcar otra como default
        if (address.getIsDefault() && !user.getAddresses().isEmpty()) {
            user.getAddresses().get(0).setIsDefault(true);
        }
    }

    /**
     * Establece una dirección como por defecto.
     *
     * @param userId ID del usuario
     * @param addressId ID de la dirección
     * @return AddressDTO actualizado
     * @throws EntityNotFoundException si no existe
     * @throws ValidationException si la dirección no pertenece al usuario
     */
    public AddressDTO setDefaultAddress(Long userId, Long addressId) {
        userService.findUserEntityOrThrow(userId); // Validar que usuario existe
        Address address = findAddressEntityOrThrow(addressId);

        // Validar que la dirección pertenezca al usuario
        if (!address.getUser().getUserId().equals(userId)) {
            throw new ValidationException("Address does not belong to user");
        }

        // Desmarcar otras direcciones como default
        unsetOtherDefaultAddresses(userId);

        // Marcar esta como default
        address.setIsDefault(true);

        // TODO Etapa 06: addressRepository.save(address);

        return addressMapper.toDTO(address);
    }

    /**
     * Obtiene todas las direcciones de un usuario.
     *
     * @param userId ID del usuario
     * @return Lista de AddressDTO
     */
    public List<AddressDTO> findUserAddresses(Long userId) {
        userService.findUserEntityOrThrow(userId); // Validar que usuario existe

        // TODO Etapa 06: List<Address> addresses = addressRepository.findByUserId(userId);
        List<Address> addresses = addressesInMemory.stream()
                .filter(a -> a.getUser().getUserId().equals(userId))
                .collect(Collectors.toList());

        return addressMapper.toDTOList(addresses);
    }

    /**
     * Busca una dirección por ID.
     *
     * @param addressId ID de la dirección
     * @return AddressDTO
     * @throws EntityNotFoundException si no existe
     */
    public AddressDTO findById(Long addressId) {
        Address address = findAddressEntityOrThrow(addressId);
        return addressMapper.toDTO(address);
    }

    /**
     * Busca entity Address por ID o lanza excepción.
     * Método interno para uso de otros servicios.
     *
     * @param addressId ID de la dirección
     * @return Address entity
     * @throws EntityNotFoundException si no existe
     */
    public Address findAddressEntityOrThrow(Long addressId) {
        // TODO Etapa 06: return addressRepository.findById(addressId)
        //     .orElseThrow(() -> new EntityNotFoundException("Address", addressId));
        return addressesInMemory.stream()
                .filter(a -> a.getAddressId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Address", addressId));
    }

    // Métodos privados auxiliares

    /**
     * Valida los datos de una dirección.
     */
    private void validateAddressData(AddressDTO dto) {
        ValidationUtils.validateNotNull(dto.getType(), "type");
        ValidationUtils.validateNotBlank(dto.getLine1(), "line1");
        ValidationUtils.validateNotBlank(dto.getCity(), "city");
        ValidationUtils.validateNotBlank(dto.getState(), "state");
        ValidationUtils.validateNotBlank(dto.getCountry(), "country");
        ValidationUtils.validateNotBlank(dto.getPostalCode(), "postalCode");
    }

    /**
     * Desmarca todas las direcciones de un usuario como default.
     */
    private void unsetOtherDefaultAddresses(Long userId) {
        // TODO Etapa 06: addressRepository.unsetDefaultByUserId(userId);
        addressesInMemory.stream()
                .filter(a -> a.getUser().getUserId().equals(userId))
                .forEach(a -> a.setIsDefault(false));
    }

    // Método auxiliar para simular auto-increment en memoria
    private Long generateNextId() {
        return addressesInMemory.stream()
                .mapToLong(Address::getAddressId)
                .max()
                .orElse(0L) + 1;
    }
}
