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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;

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
    // TODO Etapa 06: private final UserRepository userRepository;
    // Por ahora trabajamos con lista en memoria

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
        // TODO Etapa 06: cargar Role desde BD
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

        // TODO Etapa 06: userRepository.save(user);
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
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new EntityNotFoundException("User with email: " + email));

        return userMapper.toDTO(user);
    }

    /**
     * Lista todos los usuarios.
     *
     * @return Lista de UserDTO
     */
    public List<UserDTO> findAllUsers() {
        // TODO Etapa 06: List<User> users = userRepository.findAll();
        return userMapper.toDTOList(userRepository.findAll());
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

        // TODO Etapa 06: userRepository.save(user);

        return userMapper.toDTO(user);
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
        // TODO Etapa 06: return userRepository.existsByEmail(email);
        // En las primeras etapas usamos una lista en memoria
        return userRepository.existsByEmailIgnoreCase(email);

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
        // TODO Etapa 06: return userRepository.findById(userId)
        //     .orElseThrow(() -> new EntityNotFoundException("User", userId));
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));
    }

    public User findUserEntityByEmailOrThrow(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new EntityNotFoundException("User with email: " + email));
    }

    @Transactional
    public void updatePasswordHash(Long userId, String newPasswordHash) {
        User user = findUserEntityOrThrow(userId);
        user.setPasswordHash(newPasswordHash);
        userRepository.save(user);
    }

    @Transactional
    public UserDTO createAdminUser(String email, String passwordHash, String firstName,
                                   String lastName, String phone, String roleName, UserStatus status) {
        ValidationUtils.validateEmail(email, "email");
        ValidationUtils.validateNotBlank(passwordHash, "passwordHash");
        ValidationUtils.validateNotBlank(firstName, "firstName");
        ValidationUtils.validateNotBlank(lastName, "lastName");

        if (existsByEmail(email)) {
            throw new DuplicateEntityException("User", "email", email);
        }

        Role role = roleRepository.findByNameIgnoreCase(roleName)
                .orElseThrow(() -> new EntityNotFoundException("Role", roleName));

        User user = User.builder()
                .role(role)
                .email(email.toLowerCase().trim())
                .passwordHash(passwordHash)
                .firstName(firstName.trim())
                .lastName(lastName.trim())
                .phone(phone != null ? phone.trim() : null)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);
        return userMapper.toDTO(user);
    }

    @Transactional
    public UserDTO updateAdminUser(Long userId, String email, String firstName,
                                   String lastName, String phone, String roleName, UserStatus status) {
        User user = findUserEntityOrThrow(userId);

        if (email != null && !email.isBlank()) {
            ValidationUtils.validateEmail(email, "email");
            user.setEmail(email.toLowerCase().trim());
        }
        if (firstName != null) user.setFirstName(firstName.trim());
        if (lastName != null) user.setLastName(lastName.trim());
        if (phone != null) user.setPhone(phone.isBlank() ? null : phone.trim());
        if (status != null) user.setStatus(status);
        if (roleName != null) {
            Role role = roleRepository.findByNameIgnoreCase(roleName)
                    .orElseThrow(() -> new EntityNotFoundException("Role", roleName));
            user.setRole(role);
        }

        user = userRepository.save(user);
        return userMapper.toDTO(user);
    }

    // Método auxiliar para simular auto-increment en memoria
    private Long generateNextId() {
        return userRepository.findAll().stream()
                .mapToLong(User::getUserId)
                .max()
                .orElse(0L) + 1;
    }
}
