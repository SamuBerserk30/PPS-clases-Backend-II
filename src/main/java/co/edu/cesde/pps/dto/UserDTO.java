package co.edu.cesde.pps.dto;

import co.edu.cesde.pps.enums.UserStatus;
import co.edu.cesde.pps.model.User;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO para transferencia de datos de Usuario.
 *
 * Se utiliza para:
 * - Respuestas de API (no expone passwordHash)
 * - Registro de usuarios
 * - Actualización de perfil
 * - Listados de usuarios
 *
 * Ventajas vs Entity:
 * - No expone datos sensibles (passwordHash)
 * - Estructura optimizada para cada caso de uso
 * - Desacopla API de modelo de dominio
 * - Previene lazy loading exceptions
 */
public class UserDTO {

    private Long userId;
    private String roleName;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private UserStatus status;
    private LocalDateTime createdAt;
    private String fullName;
    private Integer addressesCount;

    // Constructor vacío
    public UserDTO() {
    }

    // Constructor completo (excepto campos calculados)
    public UserDTO(Long userId, String roleName, String email, String firstName,
                   String lastName, String phone, UserStatus status, LocalDateTime createdAt) {
        this.userId = userId;
        this.roleName = roleName;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.status = status;
        this.createdAt = createdAt;
        this.fullName = firstName + " " + lastName;
    }

    // Getters y Setters

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Integer getAddressesCount() {
        return addressesCount;
    }

    public void setAddressesCount(Integer addressesCount) {
        this.addressesCount = addressesCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDTO userDTO = (UserDTO) o;
        return Objects.equals(userId, userDTO.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", status=" + status +
                '}';
    }
}
