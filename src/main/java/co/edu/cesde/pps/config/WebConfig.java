package co.edu.cesde.pps.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración global de CORS (Cross-Origin Resource Sharing).
 *
 * <p>Centraliza las reglas CORS para toda la aplicación usando {@link WebMvcConfigurer}.
 * El origen permitido se configura desde la variable de entorno {@code CORS_ALLOWED_ORIGINS}
 * definida en el archivo {@code .env}, lo que permite ajustarlo sin tocar código Java.</p>
 *
 * <h3>¿Qué es CORS?</h3>
 * <p>Es un mecanismo de seguridad del navegador que bloquea peticiones HTTP que vienen de un
 * origen (protocolo + host + puerto) diferente al del servidor. Por ejemplo, un frontend en
 * {@code http://localhost:5173} haciendo requests a {@code http://localhost:8080} es
 * considerado "cross-origin" y el navegador lo bloquea a menos que el servidor lo autorice
 * explícitamente.</p>
 *
 * <h3>Configuración en .env</h3>
 * <pre>
 * CORS_ALLOWED_ORIGINS=http://localhost:5173
 * # Para multiples origenes (separar con comas):
 * CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
 * </pre>
 *
 * <h3>Nota: allowCredentials + orígenes específicos</h3>
 * <p>{@code allowCredentials(true)} permite que el navegador envíe cookies y headers de
 * autenticación entre orígenes. Esta opción es INCOMPATIBLE con {@code allowedOrigins("*")};
 * siempre debe combinarse con orígenes explícitos como se hace aquí.</p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    /**
     * Orígenes permitidos inyectados desde {@code application.yml} →  variable de entorno
     * {@code CORS_ALLOWED_ORIGINS}. Valor por defecto: {@code http://localhost:5173}
     * (puerto estándar de React + Vite).
     */
    @Value("${app.cors.allowed-origins:http://localhost:5173, http://172.45.1.200:5173}")
    private String allowedOrigins;

    /**
     * Registra las reglas CORS globales de la aplicación.
     *
     * <ul>
     *   <li>{@code /**} — aplica a TODOS los endpoints de la API.</li>
     *   <li>{@code allowedOrigins} — solo los orígenes listados en {@code .env} pueden hacer requests.</li>
     *   <li>{@code allowedMethods} — métodos HTTP permitidos desde el frontend.</li>
     *   <li>{@code allowedHeaders("*")} — acepta cualquier header que el cliente envíe.</li>
     *   <li>{@code allowCredentials(true)} — permite el envío de cookies y tokens de autenticación.</li>
     *   <li>{@code maxAge(3600)} — el navegador cachea la respuesta preflight por 1 hora,
     *       reduciendo el número de peticiones OPTIONS.</li>
     * </ul>
     *
     * @param registry registro de mappings CORS proporcionado por Spring MVC.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Dividir por comas para soportar múltiples orígenes (ej: React dev + staging)
        String[] origins = allowedOrigins.split(",");

        log.info("CORS configurado para los orígenes: {}", allowedOrigins);

        registry.addMapping("/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}