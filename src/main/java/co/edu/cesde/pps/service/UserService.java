package co.edu.cesde.pps.service;

import co.edu.cesde.pps.dto.UserDTO;
import co.edu.cesde.pps.exception.DuplicateEntityException;
import co.edu.cesde.pps.exception.EntityNotFoundException;
import co.edu.cesde.pps.mapper.UserMapper;
import co.edu.cesde.pps.model.Role;
import co.edu.cesde.pps.model.User;
import co.edu.cesde.pps.repository.RoleRepository;
import co.edu.cesde.pps.repository.UserRepository;
import co.edu.cesde.pps.util.ValidationUtils;
import co.edu.cesde.pps.config.AppConfig;
import co.edu.cesde.pps.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para gestión de usuarios.
 *
 * Responsabilidades:
 * - CRUD de usuarios
 * - Registro con validaciones
 * - Búsqueda por diferentes criterios
 * - Conversión Entity <-> DTO
 *
 * NOTA: En Etapa 06 se agregará:
 * - @Service annotation
 * - @Transactional
 * - Inyección de UserRepository
 * - Persistencia real
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userMapper = new UserMapper();
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * Registra un nuevo usuario.
     *
     * @param email Email del usuario
     * @param passwordHash Hash de la contraseña
     * @param firstName Nombre
     * @param lastName Apellido
     * @param phone Teléfono (opcional)
     * @return UserDTO del usuario creado
     * @throws DuplicateEntityException si el email ya existe
     */
    @Transactional
    public UserDTO registerUser(String email, String passwordHash, String firstName,
                                String lastName, String phone) {
        // Validaciones
        ValidationUtils.validateEmail(email, "email");
        ValidationUtils.validateNotBlank(passwordHash, "passwordHash");
        ValidationUtils.validateMinLength(passwordHash, AppConfig.getMinPasswordLength(), "password");
        ValidationUtils.validateNotBlank(firstName, "firstName");
        ValidationUtils.validateNotBlank(lastName, "lastName");

        if (phone != null && !phone.isBlank()) {
            ValidationUtils.validatePhone(phone, "phone");
        }

        // Verificar email duplicado
        if (existsByEmail(email)) {
            throw new DuplicateEntityException("User", "email", email);
        }

        // Crear usuario
        Role defaultRole = roleRepository.findByNameIgnoreCase("CUSTOMER")
                .orElseThrow(() -> new EntityNotFoundException("Role", "CUSTOMER"));

        User user = User.builder()
                .role(defaultRole)
                .email(email.toLowerCase().trim())
                .passwordHash(passwordHash)
                .firstName(firstName.trim())
                .lastName(lastName.trim())
                .phone(phone != null ? phone.trim() : null)
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        return userMapper.toDTO(user);
    }

    /**
     * Busca usuario por ID.
     *
     * @param userId ID del usuario
     * @return UserDTO
     * @throws EntityNotFoundException si no existe
     */
    public UserDTO findById(Long userId) {
        User user = findUserEntityOrThrow(userId);
        return userMapper.toDTO(user);
    }

    /**
     * Busca usuario por email.
     *
     * @param email Email del usuario
     * @return UserDTO
     * @throws EntityNotFoundException si no existe
     */
    public UserDTO findByEmail(String email) {
        User user = findUserEntityByEmailOrThrow(email);

        return userMapper.toDTO(user);
    }

    /**
     * Lista todos los usuarios.
     *
     * @return Lista de UserDTO
     */
    public List<UserDTO> findAllUsers() {
        return userMapper.toDTOList(userRepository.findAll());
    }

    /**
     * Crea un usuario desde el módulo administrativo.
     */
    @Transactional
    public UserDTO createAdminUser(String email, String passwordHash, String firstName,
                                   String lastName, String phone, String roleName, UserStatus status) {
        ValidationUtils.validateEmail(email, "email");
        ValidationUtils.validateNotBlank(passwordHash, "passwordHash");
        ValidationUtils.validateMinLength(passwordHash, AppConfig.getMinPasswordLength(), "password");
        ValidationUtils.validateNotBlank(firstName, "firstName");
        ValidationUtils.validateNotBlank(lastName, "lastName");
        ValidationUtils.validateNotNull(status, "status");

        if (phone != null && !phone.isBlank()) {
            ValidationUtils.validatePhone(phone, "phone");
        }

        String normalizedEmail = normalizeEmail(email);
        if (existsByEmail(normalizedEmail)) {
            throw new DuplicateEntityException("User", "email", normalizedEmail);
        }

        Role role = resolveRoleOrThrow(roleName);
        User user = User.builder()
                .role(role)
                .email(normalizedEmail)
                .passwordHash(passwordHash)
                .firstName(firstName.trim())
                .lastName(lastName.trim())
                .phone(normalizePhone(phone))
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();

        return userMapper.toDTO(userRepository.save(user));
    }

    /**
     * Actualiza perfil de usuario.
     *
     * @param userId ID del usuario
     * @param firstName Nuevo nombre
     * @param lastName Nuevo apellido
     * @param phone Nuevo teléfono
     * @return UserDTO actualizado
     * @throws EntityNotFoundException si no existe
     */
    @Transactional
    public UserDTO updateProfile(Long userId, String firstName, String lastName, String phone) {
        User user = findUserEntityOrThrow(userId);

        // Validaciones
        if (firstName != null) {
            ValidationUtils.validateNotBlank(firstName, "firstName");
            user.setFirstName(firstName.trim());
        }

        if (lastName != null) {
            ValidationUtils.validateNotBlank(lastName, "lastName");
            user.setLastName(lastName.trim());
        }

        if (phone != null) {
            if (!phone.isBlank()) {
                ValidationUtils.validatePhone(phone, "phone");
                user.setPhone(phone.trim());
            } else {
                user.setPhone(null);
            }
        }

        user = userRepository.save(user);

        return userMapper.toDTO(user);
    }

    /**
     * Actualiza un usuario desde el módulo administrativo.
     */
    @Transactional
    public UserDTO updateAdminUser(Long userId, String email, String firstName,
                                   String lastName, String phone, String roleName, UserStatus status) {
        User user = findUserEntityOrThrow(userId);

        ValidationUtils.validateEmail(email, "email");
        ValidationUtils.validateNotBlank(firstName, "firstName");
        ValidationUtils.validateNotBlank(lastName, "lastName");
        ValidationUtils.validateNotNull(status, "status");
        if (phone != null && !phone.isBlank()) {
            ValidationUtils.validatePhone(phone, "phone");
        }

        String normalizedEmail = normalizeEmail(email);
        userRepository.findByEmailIgnoreCase(normalizedEmail)
                .filter(existing -> !existing.getUserId().equals(userId))
                .ifPresent(existing -> {
                    throw new DuplicateEntityException("User", "email", normalizedEmail);
                });

        Role role = resolveRoleOrThrow(roleName);
        user.setEmail(normalizedEmail);
        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setPhone(normalizePhone(phone));
        user.setRole(role);
        user.setStatus(status);

        return userMapper.toDTO(userRepository.save(user));
    }

    /**
     * Actualiza el hash de contraseña de un usuario.
     *
     * @param userId ID del usuario
     * @param passwordHash Nuevo hash de contraseña
     * @throws EntityNotFoundException si no existe
     */
    @Transactional
    public void updatePasswordHash(Long userId, String passwordHash) {
        ValidationUtils.validateNotBlank(passwordHash, "passwordHash");

        User user = findUserEntityOrThrow(userId);
        user.setPasswordHash(passwordHash);
        userRepository.save(user);
    }

    /**
     * Elimina un usuario (soft delete cambiando estado).
     *
     * @param userId ID del usuario
     * @throws EntityNotFoundException si no existe
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = findUserEntityOrThrow(userId);
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    /**
     * Verifica si existe un usuario con el email dado.
     *
     * @param email Email a verificar
     * @return true si existe
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    /**
     * Busca entity User por email o lanza excepción.
     * Método interno para auth y otras capas de aplicación.
     *
     * @param email Email del usuario
     * @return User entity
     * @throws EntityNotFoundException si no existe
     */
    public User findUserEntityByEmailOrThrow(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new EntityNotFoundException("User with email: " + email));
    }

    /**
     * Busca entity User por ID o lanza excepción.
     * Método interno para uso de otros servicios.
     *
     * @param userId ID del usuario
     * @return User entity
     * @throws EntityNotFoundException si no existe
     */
    public User findUserEntityOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));
    }

    private Role resolveRoleOrThrow(String roleName) {
        ValidationUtils.validateNotBlank(roleName, "role");
        String normalizedRole = roleName.trim().toUpperCase(Locale.ROOT);
        return roleRepository.findByNameIgnoreCase(normalizedRole)
                .orElseThrow(() -> new EntityNotFoundException("Role", normalizedRole));
    }

    private String normalizeEmail(String email) {
        return email.toLowerCase(Locale.ROOT).trim();
    }

    private String normalizePhone(String phone) {
        return phone == null || phone.isBlank() ? null : phone.trim();
    }
}
