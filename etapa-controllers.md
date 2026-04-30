# Creación de Controllers en Spring Boot

## Tabla de contenido

1. [Prerrequisitos](#1-prerrequisitos)
2. [¿Qué es un Controller?](#2-qué-es-un-controller)
3. [Estructura base de un Controller](#3-estructura-base-de-un-controller)
4. [Anatomía de un método HTTP](#4-anatomía-de-un-método-http)
5. [Manejo de errores con try-catch](#5-manejo-de-errores-con-try-catch)
   - 5.1 [Sin excepciones personalizadas](#51-sin-excepciones-personalizadas)
   - 5.2 [Con excepciones personalizadas](#52-con-excepciones-personalizadas)
6. [Ejemplo completo — AdminUserController](#6-ejemplo-completo--adminusercontroller)
7. [Nota — try-catch vs @ControllerAdvice](#7-nota--try-catch-vs-controlleradvice)

---

## 1. Prerrequisitos

Antes de crear un Controller, el proyecto debe tener listas las siguientes piezas. Cada una cumple un rol distinto dentro de la arquitectura.

| Pieza | Descripción |
|---|---|
| **Entidad / Modelo** | Clase Java que representa la tabla en base de datos (`@Entity`). |
| **Repositorio** | Interfaz que extiende `JpaRepository` para acceder a la base de datos. |
| **Service / ApplicationService** | Clase con `@Service` que contiene la lógica de negocio. El Controller **solo llama al servicio**, nunca accede directamente al repositorio. |
| **DTOs request** | Clases (o `record`) que representan el cuerpo de entrada de la petición HTTP. |
| **DTOs response** | Clases (o `record`) que representan la respuesta que se devuelve al cliente. |
| **Excepciones personalizadas** | Clases que extienden `RuntimeException` (o una excepción base propia) para comunicar errores de negocio de forma clara. |

> En este proyecto los DTOs ya están creados y se asumen disponibles. La guía se enfoca en el Controller y en cómo usa esas piezas.

---

## 2. ¿Qué es un Controller?

Un **Controller** es la clase que recibe las peticiones HTTP del cliente (navegador, app móvil, Postman, frontend) y devuelve una respuesta.

En Spring Boot se usa la anotación `@RestController`, que combina internamente dos anotaciones:

| Anotación | Qué hace |
|---|---|
| `@Controller` | Le dice a Spring que esta clase maneja peticiones web. |
| `@ResponseBody` | Le indica a Spring que el valor que retorna cada método debe serialzarse directamente como JSON en el cuerpo de la respuesta HTTP, en lugar de buscar una vista (HTML). |

```java
// @RestController = @Controller + @ResponseBody
@RestController
public class MiController { }
```

Spring detecta esta clase automáticamente al arrancar la aplicación porque está anotada y se encuentra dentro del paquete escaneado por `@SpringBootApplication`.

---

## 3. Estructura base de un Controller

```java
package co.edu.cesde.pps.web.controller;

import co.edu.cesde.pps.application.AdminUserApplicationService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 1. Marca la clase como un controller REST
@RestController
// 2. Define la ruta base que agrupa todos los endpoints de esta clase
//    Todos los métodos de esta clase responderán bajo "/api/v1/admin/users"
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    // 3. El servicio que contiene la lógica de negocio
    //    El Controller NO tiene lógica de negocio propia, solo delega al servicio
    private final AdminUserApplicationService adminUserApplicationService;

    // 4. Inyección de dependencias por constructor (forma recomendada en Spring)
    //    Spring detecta el constructor y provee automáticamente la instancia del servicio
    public AdminUserController(AdminUserApplicationService adminUserApplicationService) {
        this.adminUserApplicationService = adminUserApplicationService;
    }

    // 5. Aquí van los métodos (endpoints) ...
}
```

### ¿Por qué inyección por constructor?

- Es la forma **más segura**: el objeto no puede existir sin sus dependencias.
- Facilita las **pruebas unitarias**: se puede pasar un mock directo en el constructor.
- Es la práctica recomendada por el equipo de Spring desde Spring Framework 4.3+.

---

## 4. Anatomía de un método HTTP

Cada método público dentro del Controller se convierte en un **endpoint** HTTP. Se usan anotaciones para indicar el verbo HTTP y la ruta.

### Anotaciones de verbo HTTP

| Anotación | Verbo HTTP | Uso típico |
|---|---|---|
| `@GetMapping` | GET | Consultar o listar recursos. |
| `@PostMapping` | POST | Crear un nuevo recurso. |
| `@PutMapping` | PUT | Reemplazar un recurso completo. |
| `@PatchMapping` | PATCH | Modificar parcialmente un recurso. |
| `@DeleteMapping` | DELETE | Eliminar un recurso. |

### Anotaciones de parámetros

| Anotación | Qué captura | Ejemplo |
|---|---|---|
| `@PathVariable` | Segmento de la URL | `/users/{id}` → `@PathVariable Long id` |
| `@RequestBody` | Cuerpo JSON de la petición | `@RequestBody CreateAdminUserRequest request` |
| `@RequestParam` | Parámetro de query string | `/products?search=mouse` → `@RequestParam String search` |
| `@RequestHeader` | Cabecera HTTP | `Authorization: Bearer ...` |
| `@Valid` | Activa la validación del DTO | Se combina con `@RequestBody` |

### ResponseEntity\<T\>

`ResponseEntity<T>` permite controlar **el código de estado HTTP** de la respuesta, además del cuerpo.

```java
// Devuelve 200 OK con el objeto en el cuerpo (forma corta, sin ResponseEntity)
public UserResponse getUser(@PathVariable Long id) {
    return adminUserApplicationService.getUser(id); // Spring asume 200 OK
}

// Devuelve 201 Created con el objeto en el cuerpo (forma explícita)
public ResponseEntity<UserResponse> createUser(@RequestBody CreateAdminUserRequest request) {
    UserResponse created = adminUserApplicationService.createUser(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
}

// Devuelve 204 No Content sin cuerpo
public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    adminUserApplicationService.deleteUser(id);
    return ResponseEntity.noContent().build();
}
```

---

## 5. Manejo de errores con try-catch

Cuando un servicio lanza una excepción, el Controller puede capturarla con un bloque `try-catch` y devolver una respuesta HTTP con el código de error apropiado.

> **Antes de ver los ejemplos**: ¿Qué es una excepción personalizada?
>
> Una excepción personalizada es una clase Java que **extiende `RuntimeException`** (o una clase base propia como `BusinessException`) y comunica un error de negocio específico con un nombre descriptivo.
>
> ```java
> // Excepción personalizada del proyecto
> public class EntityNotFoundException extends BusinessException {
>     public EntityNotFoundException(String entityType, Object searchCriteria) {
>         super(String.format("%s not found with criteria: %s", entityType, searchCriteria));
>     }
> }
> ```
>
> Cuando se lanza `EntityNotFoundException`, cualquier bloque `catch` que espere esta clase específica la capturará. Esto permite dar respuestas HTTP precisas (p. ej. `404 Not Found`) en lugar de responder siempre `500 Internal Server Error`.

---

### 5.1 Sin excepciones personalizadas

Cuando **no** se tienen excepciones personalizadas, solo se puede capturar la clase genérica `Exception`. El problema es que cualquier error (sea porque el recurso no existe o sea un fallo de base de datos) siempre devuelve `500 Internal Server Error`, sin poder diferenciarlos.

```java
@GetMapping("/{id}")
public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
    try {
        // El servicio lanza alguna excepción genérica si no encuentra el usuario
        UserResponse user = adminUserApplicationService.getUser(id);
        return ResponseEntity.ok(user);

    } catch (Exception e) {
        // ❌ No sabemos qué salió mal: ¿el usuario no existe? ¿falló la BD?
        //    Siempre responde 500 aunque el problema sea que el recurso no existe
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
    }
}
```

**Problema real:** un cliente que consulta un usuario con ID inexistente recibirá un `500`, cuando lo correcto sería un `404 Not Found`. Sin excepciones personalizadas no hay forma de distinguir los casos.

---

### 5.2 Con excepciones personalizadas

Con excepciones personalizadas, cada bloque `catch` captura un tipo específico de error y devuelve el código HTTP correcto.

```java
@GetMapping("/{id}")
public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
    try {
        UserResponse user = adminUserApplicationService.getUser(id);
        return ResponseEntity.ok(user);

    } catch (EntityNotFoundException e) {
        // ✅ Captura específicamente "no encontrado" → responde 404
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .build();

    } catch (Exception e) {
        // ✅ Captura cualquier otro error inesperado → responde 500
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
    }
}
```

Se pueden encadenar tantos `catch` como tipos de excepción se quieran manejar de forma diferente:

```java
@PostMapping
public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateAdminUserRequest request) {
    try {
        UserResponse created = adminUserApplicationService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);

    } catch (DuplicateEntityException e) {
        // El email ya existe → 409 Conflict
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .build();

    } catch (EntityNotFoundException e) {
        // Recurso relacionado no encontrado → 404
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .build();

    } catch (Exception e) {
        // Error inesperado → 500
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
    }
}
```

> **Regla de orden en los catch:** siempre se deben poner las excepciones **más específicas primero** y la más genérica (`Exception`) **al final**. Si `Exception` fuera el primer `catch`, capturaría todo y los bloques siguientes nunca se ejecutarían.

---

## 6. Ejemplo completo — AdminUserController

Este es el Controller que más variedad de métodos HTTP maneja en el proyecto: `GET` (listar y por ID), `POST`, `PUT` y `DELETE`.

Se basa en el `AdminUserController` real del proyecto, simplificado para fines educativos (sin guardia de acceso de admin, con try-catch explícito en cada método).

```java
package co.edu.cesde.pps.web.controller;

import co.edu.cesde.pps.application.AdminUserApplicationService;
import co.edu.cesde.pps.exception.DuplicateEntityException;
import co.edu.cesde.pps.exception.EntityNotFoundException;
import co.edu.cesde.pps.web.dto.request.CreateAdminUserRequest;
import co.edu.cesde.pps.web.dto.request.UpdateAdminUserRequest;
import co.edu.cesde.pps.web.dto.response.UserResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// ──────────────────────────────────────────────────────────
// @RestController: marca esta clase como Controller REST.
//   Spring la detecta al arrancar y la registra como bean.
// @RequestMapping: define la ruta base de todos los endpoints.
//   Todos los métodos de esta clase quedan bajo "/api/v1/admin/users".
// ──────────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    // El Controller depende del servicio de aplicación.
    // NO accede directamente a repositorios ni a la base de datos.
    private final AdminUserApplicationService adminUserApplicationService;

    // Inyección por constructor: Spring provee el servicio automáticamente.
    public AdminUserController(AdminUserApplicationService adminUserApplicationService) {
        this.adminUserApplicationService = adminUserApplicationService;
    }

    // ──────────────────────────────────────────────────────────
    // GET /api/v1/admin/users
    // Retorna la lista completa de usuarios.
    // Respuesta exitosa: 200 OK con un arreglo JSON de usuarios.
    // ──────────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<UserResponse>> listUsers() {
        try {
            List<UserResponse> users = adminUserApplicationService.listUsers();
            // ResponseEntity.ok() construye una respuesta 200 OK con el body indicado
            return ResponseEntity.ok(users);

        } catch (Exception e) {
            // Error inesperado → 500 Internal Server Error sin cuerpo
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    // ──────────────────────────────────────────────────────────
    // GET /api/v1/admin/users/{id}
    // Retorna un usuario específico por su ID.
    //   {id} en la URL se captura con @PathVariable.
    // Respuesta exitosa: 200 OK con el usuario en JSON.
    // Error esperado: 404 si el usuario no existe.
    // ──────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        try {
            UserResponse user = adminUserApplicationService.getUser(id);
            return ResponseEntity.ok(user);

        } catch (EntityNotFoundException e) {
            // El servicio lanzó EntityNotFoundException: el usuario con ese ID no existe
            // Se responde 404 Not Found
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();

        } catch (Exception e) {
            // Cualquier otro error inesperado → 500
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    // ──────────────────────────────────────────────────────────
    // POST /api/v1/admin/users
    // Crea un nuevo usuario.
    //   @RequestBody: Spring deserializa el JSON del cuerpo de la petición
    //                 al tipo CreateAdminUserRequest.
    //   @Valid:       activa las validaciones declaradas en el DTO
    //                 (ej: @NotBlank, @Email, @Size).
    // Respuesta exitosa: 201 Created con el usuario creado en JSON.
    // Error esperado: 409 si el email ya existe.
    // ──────────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateAdminUserRequest request) {
        try {
            UserResponse created = adminUserApplicationService.createUser(request);
            // 201 Created: el recurso fue creado exitosamente
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(created);

        } catch (DuplicateEntityException e) {
            // El email ya existe en el sistema → 409 Conflict
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .build();

        } catch (Exception e) {
            // Error inesperado → 500
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    // ──────────────────────────────────────────────────────────
    // PUT /api/v1/admin/users/{id}
    // Actualiza completamente un usuario existente.
    //   PUT reemplaza todos los campos del recurso con los del body.
    //   Se necesita tanto @PathVariable (ID del recurso)
    //   como @RequestBody (datos nuevos).
    // Respuesta exitosa: 200 OK con el usuario actualizado.
    // Errores esperados: 404 si no existe, 409 si email duplicado.
    // ──────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAdminUserRequest request) {
        try {
            UserResponse updated = adminUserApplicationService.updateUser(id, request);
            return ResponseEntity.ok(updated);

        } catch (EntityNotFoundException e) {
            // Usuario con ese ID no existe → 404
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();

        } catch (DuplicateEntityException e) {
            // El nuevo email ya pertenece a otro usuario → 409
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .build();

        } catch (Exception e) {
            // Error inesperado → 500
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    // ──────────────────────────────────────────────────────────
    // DELETE /api/v1/admin/users/{id}
    // Elimina (o desactiva) un usuario por su ID.
    //   En este proyecto el delete es lógico: cambia el status a INACTIVE,
    //   no borra el registro de la base de datos.
    // Respuesta exitosa: 204 No Content (sin cuerpo, el recurso ya no está disponible).
    // Error esperado: 404 si el usuario no existe.
    // ──────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            adminUserApplicationService.deleteUser(id);
            // 204 No Content: operación exitosa, no hay nada que devolver
            return ResponseEntity.noContent().build();

        } catch (EntityNotFoundException e) {
            // Usuario no encontrado → 404
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();

        } catch (Exception e) {
            // Error inesperado → 500
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}
```

### Mapa de endpoints resultante

| Método HTTP | Ruta | Respuesta exitosa | Errores manejados |
|---|---|---|---|
| `GET` | `/api/v1/admin/users` | `200 OK` — lista de usuarios | `500` |
| `GET` | `/api/v1/admin/users/{id}` | `200 OK` — un usuario | `404`, `500` |
| `POST` | `/api/v1/admin/users` | `201 Created` — usuario creado | `409`, `500` |
| `PUT` | `/api/v1/admin/users/{id}` | `200 OK` — usuario actualizado | `404`, `409`, `500` |
| `DELETE` | `/api/v1/admin/users/{id}` | `204 No Content` | `404`, `500` |

---

## 7. Nota — try-catch vs @ControllerAdvice

### El problema del try-catch repetido

El enfoque de try-catch en cada método funciona correctamente, pero genera **código duplicado**. Si hay 10 controllers y todos deben manejar `EntityNotFoundException` con un `404`, ese bloque `catch` se repite en cada método.

```java
// Esto se repite en TODOS los métodos que consultan por ID:
} catch (EntityNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
}
```

### La solución: @RestControllerAdvice

`@RestControllerAdvice` es una clase especial que **intercepta las excepciones** que no fueron capturadas dentro de los controllers y las maneja en un único lugar centralizado.

```java
package co.edu.cesde.pps.web.advice;

import co.edu.cesde.pps.exception.DuplicateEntityException;
import co.edu.cesde.pps.exception.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Esta clase intercepta excepciones lanzadas por CUALQUIER controller del proyecto
@RestControllerAdvice
public class ApiExceptionHandler {

    // Se ejecuta automáticamente cuando cualquier controller lanza EntityNotFoundException
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Void> handleNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // Se ejecuta cuando cualquier controller lanza DuplicateEntityException
    @ExceptionHandler(DuplicateEntityException.class)
    public ResponseEntity<Void> handleDuplicate(DuplicateEntityException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    // Se ejecuta para cualquier otra excepción no capturada
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Void> handleGeneric(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
```

Con este Advice, los controllers pueden escribirse **sin try-catch**, delegando toda la gestión de errores al handler centralizado:

```java
// Con @RestControllerAdvice, el controller queda limpio y sin try-catch
@GetMapping("/{id}")
public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
    // Si el servicio lanza EntityNotFoundException,
    // Spring la captura con el @ExceptionHandler correspondiente del Advice
    return ResponseEntity.ok(adminUserApplicationService.getUser(id));
}
```

### Comparativa

| Aspecto | try-catch en el Controller | @RestControllerAdvice |
|---|---|---|
| **Dónde vive el manejo** | Dentro de cada método | Clase centralizada separada |
| **Repetición de código** | Alta (mismo catch en muchos métodos) | Nula (se define una sola vez) |
| **Legibilidad del Controller** | Más verboso | Más limpio y directo |
| **Facilidad para aprender** | ✅ Más fácil de entender al inicio | Requiere entender cómo Spring intercepta |
| **Uso en producción** | Para casos muy puntuales | ✅ Práctica recomendada |

> En este proyecto el manejo centralizado está implementado en `ApiExceptionHandler.java` dentro del paquete `web/advice/`. Al llegar a esa etapa, los controllers del proyecto no tienen try-catch: Spring se encarga de enrutar las excepciones al Advice automáticamente.

---

*Fin de la guía — Controllers en Spring Boot*

